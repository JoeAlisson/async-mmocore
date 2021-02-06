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

import io.github.joealisson.mmocore.internal.MMOThreadFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

import static java.util.Objects.isNull;

/**
 * @author JoeAlisson
 */
public class Connector<T extends Client<Connection<T>>> {

    private static final Object groupLock = new Object();

    private static AsynchronousChannelGroup group;
    private ConnectionConfig config;
    private ReadHandler<T> readHandler;
    private ClientFactory<T> clientFactory;

    /**
     * Creates a Connector holding the minimum requirements to create a Client.
     *
     * @param clientFactory - The factory responsible to create a new Client when a new connection is accepted.
     * @param packetHandler - The handle responsible to convert the data received into a {@link ReadablePacket}
     * @param executor      - The responsible to execute the incoming packets.
     * @param <T>           - The Type of client that ConnectionBuilder will handle.
     * @return A ConnectionBuilder with default configuration.
     */
    public static <T extends Client<Connection<T>>> Connector<T> create(ClientFactory<T> clientFactory, PacketHandler<T> packetHandler, PacketExecutor<T> executor) {
        Connector<T> builder = new Connector<>();
        builder.config = new ConnectionConfig(null);
        builder.readHandler = new ReadHandler<>(packetHandler, executor);
        builder.clientFactory = clientFactory;
        return builder;
    }

    /**
     * Add a new {@link java.nio.ByteBuffer} grouping pool
     *
     * @param size       the max amount of {@link java.nio.ByteBuffer} supported
     * @param bufferSize the {@link java.nio.ByteBuffer}'s size inside the pool.
     * @return this
     */
    public Connector<T> addBufferPool(int size, int bufferSize) {
        config.newBufferGroup(size, bufferSize);
        return this;
    }

    /**
     * define the factor of pre-initialized {@link java.nio.ByteBuffer} inside a pool.
     *
     * @param factor the factor of initialized buffers
     * @return this
     */
    public Connector<T> initBufferPoolFactor(float factor) {
        config.initBufferPoolFactor = factor;
        return this;
    }

    /**
     * Define the size of dynamic buffer's segment. A segment is used to increase the Buffer when needed.
     *
     * @param size of dynamic buffer segment
     * @return this
     */
    public Connector<T> bufferSegmentSize(int size) {
        config.resourcePool.setBufferSegmentSize(size);
        return this;
    }

    /**
     * Define the threshold to allow the client to drop disposable packets.
     * <p>
     * When the client has queued more than {@code threshold} disposable packets will can be disposed.
     *
     * @param threshold the minimum value to drop packets. The default value is 250
     * @return this
     */
    public Connector<T> dropPacketThreshold(int threshold) {
        config.dropPacketThreshold = threshold;
        return this;
    }

    /**
     * Configures the connection to use CachedThreadPool as defined in {@link java.nio.channels.AsynchronousChannelGroup#withCachedThreadPool(java.util.concurrent.ExecutorService, int)}.
     * <p>
     * The default behaviour is to use a fixed thread poll as defined in {@link java.nio.channels.AsynchronousChannelGroup#withFixedThreadPool(int, java.util.concurrent.ThreadFactory)}.
     *
     * @param cached use a cached thread pool if true, otherwise use fixed thread pool
     * @return this
     */
    public Connector<T> useCachedThreadPool(boolean cached) {
        this.config.useCachedThreadPool = cached;
        return this;
    }

    /**
     * Set the size of the threadPool used to manage the connections and data sending.
     * <p>
     * If the thread pool is cached this method defines the corePoolSize of  ({@link java.util.concurrent.ThreadPoolExecutor})
     * If the thread pool is fixed this method defines the amount of threads
     * <p>
     * The min accepted value is 1.
     * The default value is the quantity of available processors minus 2.
     *
     * @param size - the size of thread pool to be Set
     * @return this
     */
    public Connector<T> threadPoolSize(int size) {
        this.config.threadPoolSize = size;
        return this;
    }

    /**
     * Set the size of max threads allowed in the cached thread pool.
     *
     * This config is ignored when a fixed thread pool is used.
     *
     * @param size the max cached threads in the cached thread pool.
     * @return this
     */
    public Connector<T> maxCachedThreads(int size) {
        this.config.maxCachedThreads = size;
        return this;
    }


    /**
     * Connects to a host using the address and port.
     *
     * @param host the address to be connected to
     * @param port the port of the host
     * @return A client connected to the host and port
     * @throws IOException          if a IO error happens during the connection.
     * @throws ExecutionException   if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public T connect(String host, int port) throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress;
        if (isNull(host) || host.isEmpty()) {
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
     * @return a client that represents the connection with the socketAddress
     * @throws IOException          if a IO error happens during the connection.
     * @throws ExecutionException   if the   computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public T connect(InetSocketAddress socketAddress) throws IOException, ExecutionException, InterruptedException {
        createChannelGroup();

        AsynchronousSocketChannel channel = group.provider().openAsynchronousSocketChannel(group);
        channel.connect(socketAddress).get();
        Connection<T> connection = new Connection<>(channel, readHandler, new WriteHandler<>(), config.complete());
        T client = clientFactory.create(connection);
        connection.setClient(client);
        client.onConnected();
        client.read();
        return client;
    }

    private void createChannelGroup() throws IOException {
        if (isNull(group)) {
            synchronized (groupLock) {
                if (isNull(group)) {
                    if(config.useCachedThreadPool) {
                        ExecutorService threadPool = new ThreadPoolExecutor(config.threadPoolSize, config.maxCachedThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new MMOThreadFactory("Client"));
                        group = AsynchronousChannelGroup.withCachedThreadPool(threadPool, config.threadPoolSize);
                    } else {
                        group = AsynchronousChannelGroup.withFixedThreadPool(config.threadPoolSize, new MMOThreadFactory("Client"));
                    }
                }
            }
        }
    }
}
