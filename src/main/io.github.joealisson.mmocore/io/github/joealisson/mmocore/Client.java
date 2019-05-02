package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public abstract class Client<T extends Connection<?>> {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    static final int HEADER_SIZE = 2;

    private final T connection;
    private Queue<WritablePacket<? extends Client<T>>> packetsToWrite = new ConcurrentLinkedQueue<>();
    private int dataSentSize;
    private AtomicBoolean writing = new AtomicBoolean(false);
    private volatile boolean isClosing;
    private ResourcePool resourcePool;
    private ByteBuffer buffer;

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
        packetsToWrite.add(packet);
        tryWriteNextPacket();
    }

    private void tryWriteNextPacket() {
        logger.debug("Trying to send next packet");
        if(writing.compareAndSet(false, true)) {
            if(packetsToWrite.isEmpty()) {
                writing.getAndSet(false);
                logger.debug("no packet found");
            } else {
                write(packetsToWrite.poll());
            }
        }
    }

    private void write(WritablePacket<? extends Client<T>> packet) {
        try {
            write(packet, false);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            finishWriting();
        }
    }

    @SuppressWarnings("unchecked")
    private void write(WritablePacket packet, boolean sync) throws ExecutionException, InterruptedException {
        getBuffer(packet.size(this)).position(HEADER_SIZE);
        int dataSize = packet.writeData(this, buffer);

        if(dataSize <= 0) {
            finishWriting();
            return;
        }

        dataSentSize  = encrypt(buffer.array(), HEADER_SIZE, dataSize - HEADER_SIZE) + HEADER_SIZE;
        if(dataSentSize > HEADER_SIZE) {
            buffer.putShort(0, (short) dataSentSize).position(dataSentSize);
            buffer.flip();
            connection.write(buffer, sync);
            logger.debug("Sending packet {} to {}", packet.toString(), this);
        } else {
            finishWriting();
        }
    }

    private ByteBuffer getBuffer(int size) {
        if(isNull(buffer)) {
            buffer = size > HEADER_SIZE ? resourcePool.getPooledBuffer(size) : resourcePool.getPooledBuffer();
        }
        return buffer;
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
        isClosing = true;
        logger.debug("Closing client connection {} with packet {}", this, packet);
        packetsToWrite.clear();
        if(nonNull(packet)) {
            try {
                ensureCanWrite();
                write(packet, true);
            } catch (ExecutionException | InterruptedException e) {
                logger.warn(e.getLocalizedMessage(), e);
                disconnect();
                Thread.currentThread().interrupt();
            }
        }
        disconnect();
    }

    void resumeSend(int result) {
        dataSentSize-= result;
        connection.write();
    }

    void finishWriting() {
        connection.releaseWritingBuffer();
        resourcePool.recycleBuffer(buffer);
        buffer = null;
        writing.getAndSet(false);
        tryWriteNextPacket();
    }

    private synchronized void ensureCanWrite() throws InterruptedException {
        while (!writing.compareAndSet(false, true)) {
            wait(500);
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
