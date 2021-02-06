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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public final class ConnectionHandler<T extends Client<Connection<T>>> extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);

    private final AsynchronousChannelGroup group;
    private final AsynchronousServerSocketChannel listener;
    private final ConnectionConfig config;
    private final WriteHandler<T> writeHandler;
    private final ReadHandler<T> readHandler;
    private final ClientFactory<T> clientFactory;
    private volatile boolean shutdown;

    ConnectionHandler(ConnectionConfig config, ClientFactory<T> clientFactory, ReadHandler<T> readHandler) throws IOException {
        setName("MMO-Networking");
        this.config = config;
        this.readHandler = readHandler;
        this.clientFactory = clientFactory;
        group = createChannelGroup();
        listener = group.provider().openAsynchronousServerSocketChannel(group);
        listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        listener.bind(config.address);
        writeHandler = new WriteHandler<>();
    }

    private AsynchronousChannelGroup createChannelGroup() throws IOException {
        if(config.useCachedThreadPool) {
            LOGGER.debug("Channel group is using CachedThreadPool");
            ExecutorService threadPool = new ThreadPoolExecutor(config.threadPoolSize, config.maxCachedThreads, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new MMOThreadFactory("Server"));
            return AsynchronousChannelGroup.withCachedThreadPool(threadPool, 0);
        }
        LOGGER.debug("Channel group is using FixedThreadPool");
        return AsynchronousChannelGroup.withFixedThreadPool(config.threadPoolSize, new MMOThreadFactory("Server"));
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
                    Connection<T> connection = new Connection<>(channel, readHandler, writeHandler, config);
                    T client = clientFactory.create(connection);
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