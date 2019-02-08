package io.github.joealisson.mmocore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;

/**
 * Builds the {@link ConnectionHandler} responsible to manage all incoming connections.
 *
 */
public class ConnectionBuilder<T extends Client<Connection<T>>> {

    private ConnectionConfig<T> config;

    /**
     * Creates a ConnectionBuilder holding the minimum requirements to create a ConnectionHandler.
     *
     * @param address - The socket address to listen the incoming connections.
     * @param clientFactory - The factory responsible to create a new Client when a new connection is accepted.
     * @param packetHandler  - The handle responsible to convert the data received into a {@link ReadablePacket}
     * @param executor - The responsible to execute the incoming packets.
     * @param <T> - The Type of client that ConnectionBuilder will handle.
     *
     * @return A ConnectionBuilder with default configuration.
     *
     */
    public static <T extends Client<Connection<T>>> ConnectionBuilder<T> create(InetSocketAddress address, ClientFactory<T> clientFactory, PacketHandler<T> packetHandler, PacketExecutor<T> executor) {
        ConnectionBuilder<T> builder = new ConnectionBuilder<>();
        builder.config = new ConnectionConfig<>(address, clientFactory, new ReadHandler<>(packetHandler, executor));
        return builder;
    }

    /**
     * Sets a filter to be used on incoming connections.
     * The filter must decide if a connection is acceptable or not.
     *
     * @param filter - the {@link ConnectionFilter} to be set.
     *
     * @return  this.
     */
    public ConnectionBuilder<T> filter(ConnectionFilter filter) {
        this.config.acceptFilter = filter;
        return this;
    }


    /**
     * Set the size of the threadPool used to manage the connections and data sending.
     *
     * If the size is less than or equal to zero or greater than {@link Short#MAX_VALUE} then a cachedThreadPool is used.
     * Otherwise a FixedThreadPool with the size set is used.
     *
     * The default value is the quantity of available processors minus 2.
     *
     * @param size - the size to be Set
     *
     * @return this
     */
    public ConnectionBuilder<T> threadPoolSize(int size) {
        this.config.threadPoolSize = size;
        return this;
    }


    /**
     * Defines if small outgoing packets must be combined to be sent all at once. This improves the network performance,
     * but can cause lags on clients waiting for the packet.
     *
     * The default value is false.
     *
     * @param useNagle - true if the Nagle's algorithm must be used.
     *
     * @return this.
     */
    public ConnectionBuilder<T> useNagle(boolean useNagle) {
        this.config.useNagle = useNagle;
        return this;
    }

    /**
     * Sets the shutdown wait time in milliseconds.
     *
     * The default value is 5 seconds.
     *
     * @param waitTime - the wait time to close all connections resources after a {@link ConnectionHandler#shutdown()} is called.
     *
     * @return this
     */
    public ConnectionBuilder<T> shutdownWaitTime(long waitTime) {
        config.shutdownWaitTime = waitTime;
        return this;
    }

    /**
     * Sets the size limit of the data buffer sent/received. The size must be as bigger as the biggest packet received.
     *
     * The default value is 8KB.
     *
     * @param bufferSize - the buffer size to be set
     *
     * @return this.
     */
    public ConnectionBuilder<T> bufferDefaultSize(int bufferSize) {
        config.bufferDefaultSize = bufferSize;
        return this;
    }

    /**
     * Sets the small size of the data buffer sent/received.
     *
     * The default value is 1KB.
     *
     * @param bufferSize - the buffer size to be set
     *
     * @return this.
     */
    public ConnectionBuilder<T> bufferSmallSize(int bufferSize) {
        config.bufferSmallSize = bufferSize;
        return this;
    }

    /**
     * Sets the large size of the data buffer sent/received.
     *
     * The default value is 4KB.
     *
     * @param bufferSize - the buffer size to be set
     *
     * @return this.
     */
    public ConnectionBuilder<T> bufferLargeSize(int bufferSize) {
        config.bufferLargeSize = bufferSize;
        return this;
    }

    /**
     * Sets the medium size of the data buffer sent/received.
     *
     * The default value is 2KB.
     *
     * @param bufferSize - the buffer size to be set
     *
     * @return this.
     */
    public ConnectionBuilder<T> bufferMediumSize(int bufferSize) {
        config.bufferMediumSize = bufferSize;
        return this;
    }

    /**
     * Sets the maximum amount of buffer with default size that can be holder on the BufferPool.
     * A small value can be lead to buffer overhead creation.
     * Otherwise a too big size can be lead to unwanted memory usage.
     *
     * The size must be restricted related to the number of expected clients and taking considerations of system resources.
     *
     * The default value is 100.
     *
     * @param bufferPoolSize - the size of the buffer pool size.
     *
     * @return this
     */
        public ConnectionBuilder<T> bufferPoolSize(int bufferPoolSize) {
        config.bufferPoolSize = bufferPoolSize;
        return this;
    }

    /**
     * Sets the maximum amount of buffer with small size that can be holder on the BufferPool.
     * A small value can be lead to buffer overhead creation.
     * Otherwise a too big size can be lead to unwanted memory usage.
     *
     * The size must be restricted related to the number of expected clients and taking considerations of system resources.
     *
     * The default value is 100.
     *
     * @param bufferPoolSize - the size of the buffer pool size.
     *
     * @return this
     */
    public ConnectionBuilder<T> bufferSmallPoolSize(int bufferPoolSize) {
        config.bufferSmallPoolSize = bufferPoolSize;
        return this;
    }

    /**
     * Sets the maximum amount of buffer with medium size that can be holder on the BufferPool.
     * A small value can be lead to buffer overhead creation.
     * Otherwise a too big size can be lead to unwanted memory usage.
     *
     * The size must be restricted related to the number of expected clients and taking considerations of system resources.
     *
     * The default value is 50.
     *
     * @param bufferPoolSize - the size of the buffer pool size.
     *
     * @return this
     */
    public ConnectionBuilder<T> bufferMediumPoolSize(int bufferPoolSize) {
        config.bufferMediumPoolSize = bufferPoolSize;
        return  this;
    }

    /**
     * Sets the maximum amount of buffer with large size that can be holder on the BufferPool.
     * A small value can be lead to buffer overhead creation.
     * Otherwise a too big size can be lead to unwanted memory usage.
     *
     * The size must be restricted related to the number of expected clients and taking considerations of system resources.
     *
     * The default value is 25.
     *
     * @param bufferPoolSize - the size of the buffer pool size.
     *
     * @return this
     */
    public ConnectionBuilder<T> bufferLargePoolSize(int bufferPoolSize) {
        config.bufferLargePoolSize = bufferPoolSize;
        return  this;
    }

    /**
     * Sets the byte order used to send and receive packets.
     *
     * The default value is {@link ByteOrder#LITTLE_ENDIAN}
     *
     * @param order - the order to be used.
     *
     * @return this.
     */
    public ConnectionBuilder<T> byteOrder(ByteOrder order) {
        config.byteOrder = order;
        return  this;
    }

    /**
     * Builds a new ConnectionHandler based on the options configured.
     *
     * @return a ConnectionHandler
     *
     * @throws IOException - If the Socket Address configured can't be used.
     */
    public ConnectionHandler<T> build() throws IOException {
        return new ConnectionHandler<>(config);
    }
}