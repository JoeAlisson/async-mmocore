/*
 * Copyright © 2019-2020 Async-mmocore
 *
 * This file is part of the Async-mmocore project.
 *
 * Async-mmocore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Async-mmocore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.joealisson.mmocore.ConnectionConfig.HEADER_SIZE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public abstract class Client<T extends Connection<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final T connection;
    private final Queue<WritablePacket<? extends Client<T>>> packetsToWrite = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean writing = new AtomicBoolean(false);
    private int dataSentSize;
    private volatile boolean isClosing;
    private ResourcePool resourcePool;
    private boolean readingPayload;
    private int expectedReadSize;

    /**
     * Construct a new Client
     *
     * @param connection - The Connection to the client.
     * @throws IllegalArgumentException if the connection is null or closed.
     */
    public Client(T connection) {
        if(isNull(connection) || !connection.isOpen()) {
            throw new IllegalArgumentException("The Connection is null or closed");
        }
        this.connection = connection;
    }

    T getConnection() {
        return connection;
    }

    /**
     * Sends a packet to this client.
     *
     * If another packet is been sent to this client, the actual packet is put on a queue to be sent after all previous packets.
     * Otherwise the packet is sent immediately.
     *
     * @param packet to be sent.
     */
    protected final void writePacket(WritablePacket<? extends Client<T>> packet) {
        if(!isConnected() || isNull(packet)) {
            return;
        }
        packetsToWrite.add(packet);
        tryWriteNextPacket();
    }

    private void tryWriteNextPacket() {
        LOGGER.debug("Trying to send next packet");
        if(writing.compareAndSet(false, true)) {
            if(packetsToWrite.isEmpty()) {
                connection.releaseWritingBuffer();
                writing.set(false);
                LOGGER.debug("There is no packet to send");
                if(isClosing) {
                    disconnect();
                }
            } else {
                write(packetsToWrite.poll());
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void write(WritablePacket packet) {
        try {
            int dataSize = packet.writeData(this);

            if(dataSize <= 0) {
                finishWriting();
                return;
            }

            var payloadSize = dataSize - HEADER_SIZE;
            dataSentSize = encryptedSize(payloadSize) + HEADER_SIZE;

            if(dataSentSize <= HEADER_SIZE) {
                finishWriting();
                return;
            }

            encryptAndWrite(packet, payloadSize);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            finishWriting();
        } finally {
            packet.releaseData();
        }
    }

    private void encryptAndWrite(WritablePacket<? extends Client<T>> packet, int payloadSize) {
        var buffer = packet.buffer();
        if(dataSentSize > buffer.data.length) {
            buffer.data = Arrays.copyOf(buffer.data, dataSentSize);
        }
        buffer.data = encrypt(buffer.data, HEADER_SIZE, payloadSize);
        packet.writeHeaderAndRecord(dataSentSize);
        connection.write(buffer.data, dataSentSize);
        LOGGER.debug("Sending packet {} to {}", packet, this);
    }

    void read() {
        expectedReadSize = HEADER_SIZE;
        readingPayload = false;
        connection.readHeader();
    }

    void readPayload(int dataSize) {
        expectedReadSize = dataSize;
        readingPayload = true;
        connection.read(dataSize);
    }

    /**
     * close the underlying Connection to the client.
     *
     * All pending packets are cancelled.
     *
     */
    public void close() {
        close(null);
    }

    /**
     * Sends the packet and close the underlying Connection to the client.
     *
     * All others pending packets are cancelled.
     *
     * @param packet to be sent before the connection is closed.
     */
    public void close(WritablePacket<? extends Client<T>> packet) {
        if(!isConnected()) {
            return;
        }
        packetsToWrite.clear();
        if(nonNull(packet)) {
            packetsToWrite.add(packet);
        }
        isClosing = true;
        LOGGER.debug("Closing client connection {} with packet {}", this, packet);
        tryWriteNextPacket();
    }

    void resumeSend(int result) {
        dataSentSize-= result;
        connection.write();
    }

    void finishWriting() {
        writing.set(false);
        tryWriteNextPacket();
    }

    final void disconnect() {
        LOGGER.debug("Client {} disconnecting", this);
        onDisconnection();
        try {
            // Give a time to send last packet
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            // ignore
        }
        connection.close();
    }

    int getDataSentSize() {
        return dataSentSize;
    }

    /**
     * @return The client's IP address.
     */
    public String getHostAddress() {
        return connection.getRemoteAddress();
    }

    /**
     * @return if client still connected
     */
    public boolean isConnected() {
        return connection.isOpen() && !isClosing;
    }

    void setResourcePool(ResourcePool resourcePool) {
        this.resourcePool = resourcePool;
    }

    ResourcePool getResourcePool() {
        return resourcePool;
    }

    boolean isReadingPayload() {
        return readingPayload;
    }

    void resumeRead(int bytesRead) {
        expectedReadSize -=  bytesRead;
        connection.read();
    }

    int getExpectedReadSize() {
        return expectedReadSize;
    }

    /**
     * @param dataSize the data size to be encrypted
     *
     * @return the size of the data after encrypted
     */
    public abstract int encryptedSize(int dataSize);

    /**
     * Encrypt the data in-place.
     *
     * @param data - the data to be encrypted
     * @param offset - the initial index to be encrypted
     * @param size - the length of data to be encrypted
     *
     * @return The data after encrypted
     */
    public abstract byte[] encrypt(byte[] data, int offset, int size);

    /**
     * Decrypt the data in-place
     *
     * @param data - data to be decrypted
     * @param offset - the initial index to be encrypted.
     * @param size - the length of data to be encrypted.
     *
     * @return if the data was decrypted.
     */
    public abstract boolean decrypt(byte[] data, int offset, int size);

    /**
     * Handles the client's disconnection.
     *
     * This method must save all data and release all resources related to the client.
     *
     * No more packet can be sent after this method is called.
     */
    protected abstract void  onDisconnection();

    /**
     * Handles the client's connection.
     * This method should not use blocking operations.
     *
     * The Packets can be sent only after this method is called.
     */
    public abstract void onConnected();
}
