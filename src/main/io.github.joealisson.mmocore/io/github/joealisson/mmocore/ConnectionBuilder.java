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

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Builds the {@link ConnectionHandler} responsible to manage all incoming connections.
 *
 * @author JoeAlisson
 */
public class ConnectionBuilder<T extends Client<Connection<T>>> {

    private ConnectionConfig config;
    private ReadHandler<T> readerHandler;
    private ClientFactory<T> clientFactory;

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
        builder.config = new ConnectionConfig(address);
        builder.readerHandler = new ReadHandler<>(packetHandler, executor);
        builder.clientFactory = clientFactory;
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
     * Configures the connection to use CachedThreadPool as defined in {@link java.nio.channels.AsynchronousChannelGroup#withCachedThreadPool(java.util.concurrent.ExecutorService, int)}.
     *
     * The default behaviour is to use a fixed thread poll as defined in {@link java.nio.channels.AsynchronousChannelGroup#withFixedThreadPool(int, java.util.concurrent.ThreadFactory)}.
     *
     * @param cached use a cached thread pool if true, otherwise use fixed thread pool
     * @return this
     */
    public ConnectionBuilder<T> useCachedThreadPool(boolean cached) {
        this.config.useCachedThreadPool = cached;
        return this;
    }

    /**
     * Set the size of the threadPool used to manage the connections and data sending.
     *
     * If the thread pool is cached this method defines the corePoolSize of  ({@link java.util.concurrent.ThreadPoolExecutor})
     * If the thread pool is fixed this method defines the amount of threads
     *
     * The min accepted value is 1.
     * The default value is the quantity of available processors minus 2.
     *
     * @param size - the size of thread pool to be Set
     *
     * @return this
     */
    public ConnectionBuilder<T> threadPoolSize(int size) {
        this.config.threadPoolSize = size;
        return this;
    }

    /**
     * Set the size of max threads allowed in the cached thread pool.
     * The default is the value defined in {@link #threadPoolSize(int)} plus 2.
     *
     * This config is ignored when a fixed thread pool is used.
     *
     * @param size the max cached threads in the cached thread pool.
     * @return this
     */
    public ConnectionBuilder<T> maxCachedThreads(int size) {
        this.config.maxCachedThreads(size);
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
     * Add a new {@link java.nio.ByteBuffer} grouping pool
     *
     * @param size the max amount of {@link java.nio.ByteBuffer} supported
     * @param bufferSize the {@link java.nio.ByteBuffer}'s size inside the pool.
     *
     * @return this
     */
    public ConnectionBuilder<T> addBufferPool(int size, int bufferSize) {
        config.newBufferGroup(size, bufferSize);
        return this;
    }

    /**
     * Define the factor of pre-initialized {@link java.nio.ByteBuffer} inside a pool.
     *
     * @param factor the factor of initialized buffers
     * @return this
     */
    public ConnectionBuilder<T> initBufferPoolFactor(float factor) {
        config.initBufferPoolFactor = factor;
        return this;
    }

    /**
     * Define the size of dynamic buffer's segment. A segment is used to increase the Buffer when needed.
     *
     * @param size of dynamic buffer segment
     * @return this
     */
    public ConnectionBuilder<T> bufferSegmentSize(int size) {
        config.resourcePool.setBufferSegmentSize(size);
        return this;
    }

    /**
     * Define the threshold to allow the client to drop disposable packets.
     *
     * When the client has queued more than {@code threshold} disposable packets will be disposed.
     *
     * @param threshold the minimum value to drop packets. The default value is 250
     * @return this
     */
    public ConnectionBuilder<T> dropPacketThreshold(int threshold) {
        config.dropPacketThreshold = threshold;
        return this;
    }

    /**
     * Builds a new ConnectionHandler based on the options configured.
     *
     * @return a ConnectionHandler
     *
     * @throws IOException - If the Socket Address configured can't be used.
     */
    public ConnectionHandler<T> build() throws IOException {
        return new ConnectionHandler<>(config.complete(), clientFactory, readerHandler);
    }
}