package io.github.joealisson.mmocore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static java.util.Objects.isNull;

/**
 * @author JoeAlisson
 */
public class Connector<T extends Client<Connection<T>>>  {

    private ConnectionConfig<T> config;
    private static final int THREAD_POOL_SIZE = 2;

    /**
     * Creates a Connector holding the minimum requirements to create a Client.
     *
     * @param clientFactory - The factory responsible to create a new Client when a new connection is accepted.
     * @param packetHandler  - The handle responsible to convert the data received into a {@link ReadablePacket}
     * @param executor - The responsible to execute the incoming packets.
     * @param <T> - The Type of client that ConnectionBuilder will handle.
     *
     * @return A ConnectionBuilder with default configuration.
     *
     */
    public static <T extends Client<Connection<T>>> Connector<T> create(ClientFactory<T> clientFactory, PacketHandler<T> packetHandler, PacketExecutor<T> executor)  {
        Connector<T> builder = new Connector<>();
        builder.config = new ConnectionConfig<>(null, clientFactory, new ReadHandler<>(packetHandler, executor));
        return builder;
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
    public Connector<T> bufferDefaultSize(int bufferSize) {
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
    public Connector<T> bufferSmallSize(int bufferSize) {
        config.bufferSmallSize = bufferSize;
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
    public Connector<T> bufferMediumSize(int bufferSize) {
        config.bufferMediumSize = bufferSize;
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
    public Connector<T> bufferLargeSize(int bufferSize) {
        config.bufferLargeSize = bufferSize;
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
    public Connector<T> bufferPoolSize(int bufferPoolSize) {
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
    public Connector<T> bufferSmallPoolSize(int bufferPoolSize) {
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
    public Connector<T> bufferMediumPoolSize(int bufferPoolSize) {
        config.bufferMediumPoolSize = bufferPoolSize;
        return this;
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
    public Connector<T> bufferLargePoolSize(int bufferPoolSize) {
        config.bufferLargePoolSize = bufferPoolSize;
        return this;
    }

    /**
     * Connects to a host using the address and port.
     *
     * @param host the address to be connected to
     * @param port the port of the host
     *
     * @return A client connected to the host and port
     *
     * @throws IOException if a IO error happens during the connection.
     * @throws ExecutionException  if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public T connect(String host, int port) throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress;
        if(isNull(host) || host.isEmpty()) {
            socketAddress = new InetSocketAddress(port);
        } else {
            socketAddress = new InetSocketAddress(host, port);
        }
        return connect(socketAddress);
    }

    /**
     * Connects to a host with socketAddress
     *
     * @param socketAddress the address which will be connected
     *
     * @return a client that represents the connection with the socketAddress
     *
     * @throws IOException if a IO error happens during the connection.
     * @throws ExecutionException  if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public T connect(InetSocketAddress socketAddress) throws IOException, ExecutionException, InterruptedException {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(THREAD_POOL_SIZE, Executors.defaultThreadFactory());
        AsynchronousSocketChannel channel = group.provider().openAsynchronousSocketChannel(group);
        channel.connect(socketAddress).get();
        Connection<T> connection = new Connection<>(channel, config.readHandler, new WriteHandler<>());
        T client = config.clientFactory.create(connection);
        client.setResourcePool(ResourcePool.initialize(config));
        connection.setClient(client);
        client.read();
        client.onConnected();
        return client;
    }
}
