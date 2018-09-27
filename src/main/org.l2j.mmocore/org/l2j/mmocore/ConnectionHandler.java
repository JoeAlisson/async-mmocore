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

public final class ConnectionHandler<T extends Client<Connection<T>>> extends Thread {

    private final AsynchronousChannelGroup group;
    private final AsynchronousServerSocketChannel listener;
    private final WriteHandler<T> writeHandler;
    private final ClientFactory<T> clientFactory;
    private final ReadHandler<T> readHandler;
    private final boolean useNagle;
    private final ConnectionFilter acceptFilter;
    private boolean shutdown;
    private boolean cached = false;

    public ConnectionHandler(InetSocketAddress address, boolean useNagle, int threadPoolSize, ClientFactory<T> clientFactory, PacketHandler<T> packetHandler, PacketExecutor<T> executor, ConnectionFilter filter)
            throws IOException {
        this.clientFactory = clientFactory;
        this.useNagle = useNagle;
        this.acceptFilter = filter;
        this.readHandler = new ReadHandler<>(packetHandler, executor);
        this.writeHandler = new WriteHandler<>();
        group = createChannelGroup(threadPoolSize);
        listener = group.provider().openAsynchronousServerSocketChannel(group);
        listener.bind(address);
    }

    private AsynchronousChannelGroup createChannelGroup(int threadPoolSize) throws IOException {
        if(threadPoolSize <= 0 || threadPoolSize >= Short.MAX_VALUE) {
            cached = true;
            return AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 5);
        }
        return AsynchronousChannelGroup.withFixedThreadPool(threadPoolSize, Executors.defaultThreadFactory());
    }

    @Override
    public void run() {
        listener.accept(null, new AcceptConnectionHandler());
        if(cached) {
            while(!shutdown) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void closeConnection() {
        try {
            listener.close();
            group.awaitTermination(10, TimeUnit.SECONDS);
            group.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void acceptConnection(AsynchronousSocketChannel channel) {
        if(nonNull(channel) && channel.isOpen()) {
            try {
                if(nonNull(acceptFilter) && !acceptFilter.accept(channel)) {
                    channel.close();
                    return;
                }

                channel.setOption(StandardSocketOptions.TCP_NODELAY, !useNagle);
                Connection<T> connection = new Connection<>(channel, readHandler, writeHandler);
                T client = clientFactory.create(connection);
                connection.setClient(client);
                connection.read();
                client.onConnected();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        System.out.println("Shuting Server Down");
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