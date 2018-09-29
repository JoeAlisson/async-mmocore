package org.l2j.mmocore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;

public class ConnectionBuilder<T extends Client<Connection<T>>> {

    private ConnectionConfig<T> config;

    public static <T extends Client<Connection<T>>> ConnectionBuilder<T> create(InetSocketAddress address, ClientFactory<T> clientFactory, PacketHandler<T> packetHandler, PacketExecutor<T> executor) {
        ConnectionBuilder<T> builder = new ConnectionBuilder<>();
        builder.config = new ConnectionConfig<>(address, clientFactory, new ReadHandler<>(packetHandler, executor));
        return builder;
    }

    public ConnectionBuilder<T> filter(ConnectionFilter filter) {
        this.config.acceptFilter = filter;
        return this;
    }

    public ConnectionBuilder<T> threadPoolSize(int size) {
        this.config.threadPoolSize = size;
        return this;
    }

    public ConnectionBuilder<T> useNagle(boolean useNagle) {
        this.config.useNagle = useNagle;
        return this;
    }
    public ConnectionBuilder<T> shutdownWaitTime(long waitTime) {
        config.shutdownWaitTime = waitTime;
        return this;
    }

    public ConnectionBuilder<T> bufferSize(int bufferSize) {
        config.bufferSize = bufferSize;
        return this;
    }

    public ConnectionBuilder<T> bufferPoolSize(int bufferPoolSize) {
        config.bufferPoolSize = bufferPoolSize;
        return this;
    }
    public ConnectionBuilder<T> byteOrder(ByteOrder order) {
        config.byteOrder = order;
        return  this;
    }

    public ConnectionHandler<T> build() throws IOException {
        return new ConnectionHandler<>(config);
    }
}