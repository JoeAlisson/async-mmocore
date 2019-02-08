package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Runtime.getRuntime;
import static java.util.Objects.nonNull;

public final class ConnectionHandler<T extends Client<Connection<T>>> extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    private final AsynchronousChannelGroup group;
    private final AsynchronousServerSocketChannel listener;
    private final ConnectionConfig<T> config;
    private volatile boolean shutdown;
    private boolean cached;
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
        if(threadPoolSize <= 0 || threadPoolSize >= Short.MAX_VALUE) {
            cached = true;
            logger.debug("Channel group is using CachedThreadPool");
            return AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), getRuntime().availableProcessors());
        }
        logger.debug("Channel group is using FixedThreadPool");
        return AsynchronousChannelGroup.withFixedThreadPool(threadPoolSize, Executors.defaultThreadFactory());
    }

    /**
     * Start to listen connections.
     */
    @Override
    public void run() {
        listener.accept(null, new AcceptConnectionHandler());
        if(cached) {
            while(!shutdown) {
                try {
                    Thread.sleep(config.shutdownWaitTime);
                } catch (InterruptedException e) {
                    logger.warn(e.getLocalizedMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void closeConnection() {
        try {
            listener.close();
            group.awaitTermination(config.shutdownWaitTime, TimeUnit.MILLISECONDS);
            group.shutdownNow();
        } catch (Exception e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Shutdown the connection listener, the thread pool and all associated resources.
     *
     * This method closes all established connections.
     */
    public void shutdown() {
        logger.debug("Shutting ConnectionHandler down");
        shutdown = true;
        closeConnection();
    }

    private class AcceptConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
            if(!shutdown && listener.isOpen()) {
                listener.accept(null, this);
            }
            acceptConnection(clientChannel);
        }

        @Override
        public void failed(Throwable t, Void attachment) {
            logger.error(t.getLocalizedMessage(), t);
        }

        private void acceptConnection(AsynchronousSocketChannel channel) {
            if(nonNull(channel) && channel.isOpen()) {
                try {
                    logger.debug("Accepting connection from {}", channel);
                    if(nonNull(config.acceptFilter) && !config.acceptFilter.accept(channel)) {
                        channel.close();
                        logger.debug("Rejected connection");
                        return;
                    }

                    channel.setOption(StandardSocketOptions.TCP_NODELAY, !config.useNagle);
                    Connection<T> connection = new Connection<>(channel, config.readHandler, config.writeHandler);
                    T client = config.clientFactory.create(connection);
                    client.setResourcePool(resourcePool);
                    connection.setClient(client);
                    client.onConnected();
                    connection.read();
                } catch (Exception  e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }
}