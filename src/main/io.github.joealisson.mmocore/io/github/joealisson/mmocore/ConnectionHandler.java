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

import io.github.joealisson.mmocore.internal.MMOThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public final class ConnectionHandler<T extends Client<Connection<T>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);

    private final AsynchronousChannelGroup group;
    private final AsynchronousServerSocketChannel listener;
    private final ConnectionConfig config;
    private final WriteHandler<T> writeHandler;
    private final ReadHandler<T> readHandler;
    private final ClientFactory<T> clientFactory;

    ConnectionHandler(ConnectionConfig config, ClientFactory<T> clientFactory, ReadHandler<T> readHandler) throws IOException {
        this.config = config;
        this.readHandler = readHandler;
        this.clientFactory = clientFactory;
        writeHandler = new WriteHandler<>();
        group = createChannelGroup();
        listener = openServerSocket(config);
    }

    private AsynchronousChannelGroup createChannelGroup() throws IOException {
        if(config.useCachedThreadPool) {
            LOGGER.debug("Channel group is using CachedThreadPool");
            ExecutorService threadPool = new ThreadPoolExecutor(config.threadPoolSize, config.maxCachedThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new MMOThreadFactory("Server", config.threadPriority));
            return AsynchronousChannelGroup.withCachedThreadPool(threadPool, 0);
        }
        LOGGER.debug("Channel group is using FixedThreadPool");
        return AsynchronousChannelGroup.withFixedThreadPool(config.threadPoolSize, new MMOThreadFactory("Server", config.threadPriority));
    }

    private AsynchronousServerSocketChannel openServerSocket(ConnectionConfig config) throws IOException {
        var socketChannel = group.provider().openAsynchronousServerSocketChannel(group);
        socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        socketChannel.bind(config.address);
        return socketChannel;
    }

    /**
     * Start to listen connections.
     */
    public void start() {
        listener.accept(null, new AcceptConnectionHandler());
    }

    /**
     * Shutdown the connection listener, the thread pool and all associated resources.
     *
     * This method closes all established connections.
     */
    public void shutdown() {
        LOGGER.debug("Shutting ConnectionHandler down");
        boolean terminated = false;
        try {
            listener.close();
            group.shutdown();
            terminated = group.awaitTermination(config.shutdownWaitTime, TimeUnit.MILLISECONDS);
            group.shutdownNow();
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        LOGGER.debug("ConnectionHandler was shutdown with success status {}", terminated);
    }

    /**
     * Return the current use of Resource Buffers Pools
     *
     * API Note: This method exists mainly to support debugging, where you want to see the use of Buffers resource.
     *
     * @return the resource buffers pool stats
     */
    public String resourceStats() {
        return config.resourcePool.stats();
    }

    private class AcceptConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
            listenConnections();
            processNewConnection(clientChannel);
        }

        private void listenConnections() {
            if(listener.isOpen())
                listener.accept(null, this);
        }

        @Override
        public void failed(Throwable t, Void attachment) {
            LOGGER.warn(t.getMessage(), t);
            listenConnections();
        }

        private void processNewConnection(AsynchronousSocketChannel channel) {
            if(nonNull(channel) && channel.isOpen()) {
                try {
                    connectToChannel(channel);
                } catch (ClosedChannelException e) {
                    LOGGER.debug(e.getMessage(), e);
                } catch (Exception  e) {
                    LOGGER.error(e.getMessage(), e);
                    closeChannel(channel);
                }
            }
        }

        private void closeChannel(AsynchronousSocketChannel channel) {
            try {
                channel.close();
            } catch (IOException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        private void connectToChannel(AsynchronousSocketChannel channel) throws IOException {
            LOGGER.debug("Connecting to {}", channel);
            if(acceptConnection(channel)) {
                T client = createClient(channel);
                client.onConnected();
                client.read();
            } else {
                LOGGER.debug("Rejected connection");
                closeChannel(channel);
            }
        }

        private boolean acceptConnection(AsynchronousSocketChannel channel) {
            return isNull(config.acceptFilter) || config.acceptFilter.accept(channel);
        }

        private T createClient(AsynchronousSocketChannel channel) throws IOException {
            channel.setOption(StandardSocketOptions.TCP_NODELAY, !config.useNagle);
            Connection<T> connection = new Connection<>(channel, readHandler, writeHandler, config);
            T client = clientFactory.create(connection);
            connection.setClient(client);
            return client;
        }
    }
}