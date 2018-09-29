package org.l2j.mmocore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;
import static java.lang.Runtime.getRuntime;

public final class ConnectionHandler<T extends Client<Connection<T>>> extends Thread {

    private final AsynchronousChannelGroup group;
    private final AsynchronousServerSocketChannel listener;
    private final ConnectionConfig<T> config;
    private boolean shutdown;
    private boolean cached = false;

    ConnectionHandler(ConnectionConfig<T> config) throws IOException {
        this.config = config;
        initalizeResourcePool();
        group = createChannelGroup(config.threadPoolSize);
        listener = group.provider().openAsynchronousServerSocketChannel(group);
        listener.bind(config.address);
    }

    private void initalizeResourcePool() {
        ResourcePool.setBufferPoolSize(config.bufferPoolSize);
        ResourcePool.setBufferSize(config.bufferSize);
        ResourcePool.setByteOrder(config.byteOrder);
    }

    private AsynchronousChannelGroup createChannelGroup(int threadPoolSize) throws IOException {
        if(threadPoolSize <= 0 || threadPoolSize >= Short.MAX_VALUE) {
            cached = true;
            return AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), getRuntime().availableProcessors());
        }
        return AsynchronousChannelGroup.withFixedThreadPool(threadPoolSize, Executors.defaultThreadFactory());
    }

    @Override
    public void run() {
        listener.accept(null, new AcceptConnectionHandler());
        if(cached) {
            while(!shutdown) {
                try {
                    Thread.sleep(config.shutdownWaitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private void acceptConnection(AsynchronousSocketChannel channel) {
        if(nonNull(channel) && channel.isOpen()) {
            try {
                if(nonNull(config.acceptFilter) && !config.acceptFilter.accept(channel)) {
                    channel.close();
                    return;
                }

                channel.setOption(StandardSocketOptions.TCP_NODELAY, !config.useNagle);
                Connection<T> connection = new Connection<>(channel, config.readHandler, config.writeHandler);
                T client = config.clientFactory.create(connection);
                connection.setClient(client);
                connection.read();
                client.onConnected();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
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
            t.printStackTrace();
        }
    }
}