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

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.*;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Runtime.getRuntime;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public final class ConnectionHandler<T extends Client<Connection<T>>> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);
    private static final int CACHED_THREAD_POLL_THRESHOLD = 1000;

    private final AsynchronousChannelGroup group;
    private final AsynchronousServerSocketChannel listener;
    private final ConnectionConfig<T> config;
    private volatile boolean shutdown;
    private final ResourcePool resourcePool;

    ConnectionHandler(ConnectionConfig<T> config) throws IOException {
        this.config = config;
        resourcePool = ResourcePool.initialize(config);
        group = createChannelGroup(config.threadPoolSize);
        listener = group.provider().openAsynchronousServerSocketChannel(group);
        listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        listener.bind(config.address);
    }

    private AsynchronousChannelGroup createChannelGroup(int threadPoolSize) throws IOException {
        if(threadPoolSize <= 0 || threadPoolSize >= CACHED_THREAD_POLL_THRESHOLD) {
            LOGGER.debug("Channel group is using CachedThreadPool");
            return AsynchronousChannelGroup.withCachedThreadPool(new ThreadPoolExecutor(0, Short.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>()),
                    getRuntime().availableProcessors());
        }
        LOGGER.debug("Channel group is using FixedThreadPool");
        return AsynchronousChannelGroup.withFixedThreadPool(threadPoolSize, Executors.defaultThreadFactory());
    }

    /**
     * Start to listen connections.
     */
    @Override
    public void run() {
        listener.accept(null, new AcceptConnectionHandler());
    }

    private void closeConnection() {
        try {
            listener.close();
            group.awaitTermination(config.shutdownWaitTime, TimeUnit.MILLISECONDS);
            group.shutdownNow();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    /**
     * Shutdown the connection listener, the thread pool and all associated resources.
     *
     * This method closes all established connections.
     */
    public void shutdown() {
        LOGGER.debug("Shutting ConnectionHandler down");
        shutdown = true;
        closeConnection();
    }

    private class AcceptConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
            tryAcceptNewConnection();
            acceptConnection(clientChannel);
        }

        private void tryAcceptNewConnection() {
            if(!shutdown && listener.isOpen()) {
                listener.accept(null, this);
            }
        }

        @Override
        public void failed(Throwable t, Void attachment) {
            if(t instanceof ClosedChannelException) {
                LOGGER.debug(t.getMessage(), t);
            } else {
                tryAcceptNewConnection();
                LOGGER.warn(t.getMessage(), t);
            }
        }

        private void acceptConnection(AsynchronousSocketChannel channel) {
            if(nonNull(channel) && channel.isOpen()) {
                try {
                    LOGGER.debug("Accepting connection from {}", channel);
                    if(nonNull(config.acceptFilter) && !config.acceptFilter.accept(channel)) {
                        channel.close();
                        LOGGER.debug("Rejected connection");
                        return;
                    }

                    channel.setOption(StandardSocketOptions.TCP_NODELAY, !config.useNagle);
                    Connection<T> connection = new Connection<>(channel, config.readHandler, config.writeHandler);
                    T client = config.clientFactory.create(connection);
                    client.setResourcePool(resourcePool);
                    connection.setClient(client);
                    client.onConnected();
                    client.read();
                } catch (ClosedChannelException e) {
                    LOGGER.debug(e.getMessage(), e);
                } catch (Exception  e) {
                    LOGGER.error(e.getMessage(), e);
                    try {
                        channel.close();
                    } catch (IOException ie) {
                        LOGGER.warn(ie.getMessage(), ie);
                    }
                }
            }
        }
    }
}