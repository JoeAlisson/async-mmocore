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
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static java.util.Objects.isNull;

/**
 * @author JoeAlisson
 */
public class Connector<T extends Client<Connection<T>>>  {

    private static final Object groupLock = new Object();

    private static AsynchronousChannelGroup group;
    private ConnectionConfig<T> config;
    private ReadHandler<T> readHandler;
    private ClientFactory<T> clientFactory;

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
        builder.config = new ConnectionConfig<>(null);
        builder.readHandler = new ReadHandler<>(packetHandler, executor);
        builder.clientFactory = clientFactory;
        return builder;
    }

    /**
     * Add a new {@link java.nio.ByteBuffer} grouping pool
     *
     * @param size the max amount of {@link java.nio.ByteBuffer} supported
     * @param bufferSize the {@link java.nio.ByteBuffer}'s size inside the pool.
     *
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
        config.bufferSegmentSize = size;
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
     * @throws ExecutionException  if the   computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public T connect(InetSocketAddress socketAddress) throws IOException, ExecutionException, InterruptedException {
        synchronized (groupLock) {
            if(isNull(group)) {
                group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 2);
            }
        }

        AsynchronousSocketChannel channel = group.provider().openAsynchronousSocketChannel(group);
        channel.connect(socketAddress).get();
        Connection<T> connection = new Connection<>(channel, readHandler, new WriteHandler<>());
        config.complete();
        T client = clientFactory.create(connection);
        client.setResourcePool(ResourcePool.initialize(config));
        connection.setClient(client);
        client.onConnected();
        client.read();
        return client;
    }
}
