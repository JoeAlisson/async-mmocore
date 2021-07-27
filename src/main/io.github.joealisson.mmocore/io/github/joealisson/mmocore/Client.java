/*
 * Copyright © 2019-2021 Async-mmocore
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

import io.github.joealisson.mmocore.internal.InternalWritableBuffer;
import io.github.joealisson.mmocore.internal.NotWrittenBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
    private final AtomicBoolean disconnecting = new AtomicBoolean(false);
    private int estimateQueueSize = 0;
    private int dataSentSize;
    private volatile boolean isClosing;
    private boolean readingPayload;
    private int expectedReadSize;
    private final AtomicBoolean readNext = new AtomicBoolean(false);

    volatile boolean isReading;

    /**
     * Construct a new Client
     *
     * @param connection - The Connection to the client.
     * @throws IllegalArgumentException if the connection is null or closed.
     */
    protected Client(T connection) {
        if(isNull(connection) || !connection.isOpen()) {
            throw new IllegalArgumentException("The Connection is null or closed");
        }
        this.connection = connection;
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
        if (!isConnected() || isNull(packet) || packetCanBeDropped(packet)) {
            return;
        }

        estimateQueueSize++;
        packetsToWrite.add(packet);
        writeFairPacket();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean packetCanBeDropped(WritablePacket packet) {
        return estimateQueueSize > connection.dropPacketThreshold() && packet.canBeDropped(this);
    }

    protected final void writePackets(Collection<WritablePacket<? extends Client<T>>> packets) {
        if(!isConnected() || isNull(packets) || packets.isEmpty()) {
            return;
        }
        estimateQueueSize += packets.size();
        packetsToWrite.addAll(packets);
        writeFairPacket();
    }

    private void writeFairPacket() {
        if(writing.compareAndSet(false, true)) {
            FairnessController.sendFairPacket(this);
        }
    }

    private void writeNextPacket() {
        WritablePacket<? extends Client<T>> packet = packetsToWrite.poll();
        if(isNull(packet)) {
            releaseWritingResource();
            LOGGER.debug("There is no packet to send");
            if(isClosing) {
                disconnect();
            }
        } else {
            estimateQueueSize--;
            write(packet);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void write(WritablePacket packet) {
        boolean written = false;
        InternalWritableBuffer buffer = null;
        try {
             buffer = packet.writeData(this);

            var payloadSize = buffer.limit() - HEADER_SIZE;
            if(payloadSize <= 0) {
                return;
            }

            if(encrypt(buffer, HEADER_SIZE, payloadSize)) {
                dataSentSize = buffer.limit();

                if (dataSentSize <= HEADER_SIZE) {
                    return;
                }

                packet.writeHeader(buffer, dataSentSize);
                written = connection.write(buffer.toByteBuffers());
                LOGGER.debug("Sending packet {}[{}] to {}", packet, dataSentSize, this);
            }
        } catch (NotWrittenBufferException ignored) {
            LOGGER.debug("packet was not written {} to {}", packet, this);
        } catch (Exception e) {
            LOGGER.error("Error while {} writing {}", this, packet, e);
        } finally {
            if(!written) {
                handleNotWritten(buffer);
            }
        }
    }

    private void handleNotWritten(InternalWritableBuffer buffer) {
        if(!releaseWritingResource() && nonNull(buffer)) {
            buffer.releaseResources();
        }
        if(isConnected()) {
            writeFairPacket();
        }
    }

    void read() {
        isReading = true;
        expectedReadSize = HEADER_SIZE;
        readingPayload = false;
        connection.readHeader();
    }

    void readPayload(int dataSize) {
        expectedReadSize = dataSize;
        readingPayload = true;
        connection.read(dataSize);
    }

    public void readNextPacket() {
        if(isReading) {
            readNext.set(true);
        } else {
            read();
        }
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
        writeFairPacket();
    }

    void resumeSend(long result) {
        dataSentSize-= result;
        connection.write();
    }

    void finishWriting() {
        connection.releaseWritingBuffer();
        FairnessController.sendFairPacket(this);
    }

    private boolean releaseWritingResource() {
        boolean released = connection.releaseWritingBuffer();
        writing.set(false);
        return released;
    }

    final void disconnect() {
        if(disconnecting.compareAndSet(false, true)) {
            try {
                LOGGER.debug("Client {} disconnecting", this);
                onDisconnection();
            } finally {
                packetsToWrite.clear();
                connection.close();
            }
        }
    }

    T getConnection() {
        return connection;
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

    /**
     * @return the estimate amount of packet queued to send
     */
    public int getEstimateQueueSize() {
        return estimateQueueSize;
    }

    ResourcePool getResourcePool() {
        return connection.getResourcePool();
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

    boolean canReadNextPacket() {
        return connection.isAutoReadingEnabled() || readNext.getAndSet(false);
    }

    /**
     * Encrypt the data in-place.
     * @param data - the data to be encrypted
     * @param offset - the initial index to be encrypted
     * @param size - the length of data to be encrypted
     *
     * @return if data was encrypted
     */
    public abstract boolean encrypt(Buffer data, int offset, int size);

    /**
     * Decrypt the data in-place
     *
     * @param data - data to be decrypted
     * @param offset - the initial index to be encrypted.
     * @param size - the length of data to be encrypted.
     *
     * @return if the data was decrypted.
     */
    public abstract boolean decrypt(Buffer data, int offset, int size);

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

    private static class FairnessController {

        private static final ConcurrentLinkedQueue<Client<?>> readyClients = new ConcurrentLinkedQueue<>();

        private static void sendFairPacket(Client<?> client) {
            readyClients.offer(client);
            writeToNextClient();
        }

        private static void writeToNextClient() {
            Client<?> client = readyClients.poll();
            if(nonNull(client)) {
                client.writeNextPacket();
            }
        }
    }
}
