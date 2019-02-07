package io.github.joealisson.mmocore;

import java.net.SocketAddress;
import java.nio.ByteOrder;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;

class ConnectionConfig<T extends Client<Connection<T>>> {

    int bufferDefaultSize = 8 * 1024;
    int bufferSmallSize = 1024;
    int bufferMediumSize = 2 * 1024;
    int bufferLargeSize = 4 * 1024;
    int bufferPoolSize = 100;
    int bufferSmallPoolSize = 100;
    int bufferMediumPoolSize = 50;
    int bufferLargePoolSize = 25;

    long shutdownWaitTime = 5000;
    ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    boolean useNagle;

    ClientFactory<T> clientFactory;
    ConnectionFilter acceptFilter;
    ReadHandler<T> readHandler;
    WriteHandler<T> writeHandler;
    int threadPoolSize;
    SocketAddress address;

    ConnectionConfig(SocketAddress address, ClientFactory<T> factory, ReadHandler<T> readHandler) {
        this.address = address;
        this.clientFactory = factory;
        this.readHandler = readHandler;
        this.writeHandler = new WriteHandler<>();
        threadPoolSize = max(1, getRuntime().availableProcessors() - 2);
    }
}