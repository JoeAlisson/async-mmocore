package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.isNull;

/**
 * @author JoeAlisson
 */
public abstract class Client<T extends Connection<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    static final int HEADER_SIZE = 2;

    private final T connection;
    private final Queue<WritablePacket<? extends Client<T>>> packetsToWrite = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean writing = new AtomicBoolean(false);
    private int dataSentSize;
    private volatile boolean isClosing;
    private ResourcePool resourcePool;

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
                LOGGER.debug("no packet found");
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
        packetsToWrite.add(packet);
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

    protected final void disconnect() {
        LOGGER.debug("Client {} disconnecting", this);
        onDisconnection();
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
