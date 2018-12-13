package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public abstract class Client<T extends Connection<?>> {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final T connection;
    private Queue<WritablePacket<? extends Client<T>>> packetsToWrite = new ConcurrentLinkedQueue<>();
    private int dataSentSize;
    private AtomicBoolean writing = new AtomicBoolean(false);
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
    protected void writePacket(WritablePacket<? extends Client<T>> packet) {
        if(!isConnected() || isNull(packet)) {
            return;
        }
        putClientOnPacket(packet);
        packetsToWrite.add(packet);
        tryWriteNextPacket();
    }

    @SuppressWarnings("unchecked")
    private void putClientOnPacket(WritablePacket packet) {
        packet.client = this;
    }

    private void tryWriteNextPacket() {
        logger.debug("Trying to send next packet");
        if(writing.compareAndSet(false, true)) {
            if(packetsToWrite.isEmpty()) {
                writing.getAndSet(false);
                logger.debug("no packet found");
            } else {
                WritablePacket<? extends Client<T>> packet = packetsToWrite.poll();
                write(packet);
            }
        }
    }

    void resumeSend(int result) {
        dataSentSize-= result;
        connection.write();
    }

    void finishWriting() {
        connection.releaseWritingBuffer();
        writing.getAndSet(false);
        tryWriteNextPacket();
    }

    private void write(WritablePacket packet, boolean sync) {
        if(isNull(packet)) {
            return;
        }

        int dataSize = packet.writeData();

        if(dataSize <= 0) {
            return;
        }

        dataSentSize  = encrypt(packet.data, ReadHandler.HEADER_SIZE, dataSize - ReadHandler.HEADER_SIZE) + ReadHandler.HEADER_SIZE;
        if(dataSentSize > 0) {
            packet.writeHeader(dataSentSize);
            connection.write(packet.data, 0, dataSentSize, sync);
            logger.debug("Sending packet {} to {}", packet, this);
        }
    }

    private void write(WritablePacket<? extends Client<T>> packet) {
        try {
            write(packet, false);
        } catch (Exception e) {
            tryWriteNextPacket();
        }
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
        isClosing = true;
        logger.debug("Closing client connection {} with packet {}", this, packet);
        packetsToWrite.clear();
        if(nonNull(packet)) {
            putClientOnPacket(packet);
            ensureCanWrite();
            write(packet, true);
        }
        disconnect();
    }

    private synchronized void ensureCanWrite() {
        while (!writing.compareAndSet(false, true)) {
            try {
                wait(500);
            } catch (InterruptedException e) {
                logger.warn(e.getLocalizedMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
    }

    protected final void disconnect() {
        logger.debug("Client {} disconnecting", this);
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

    protected boolean isConnected() {
        return connection.isOpen() && !isClosing;
    }

    void setResourcePool(ResourcePool resourcePool) {
        this.resourcePool = resourcePool;
    }

    ResourcePool getResourcePool() {
        return resourcePool;
    }


    /**
     * Encrypt the data in-place.
     *
     * @param data - the data to be encrypted
     * @param offset - the initial index to be encrypted
     * @param size - the length of data to be encrypted
     *
     * @return The data size after encrypted
     */
    public abstract int encrypt(byte[] data, int offset, int size);

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
     *
     * The Packets can be sent only after this method is called.
     */
    public abstract void onConnected();
}
