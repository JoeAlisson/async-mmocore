package org.l2j.mmocore;

import java.net.SocketAddress;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import java.nio.ByteOrder;

class ConnectionConfig<T extends Client<Connection<T>>> {

    int bufferPoolSize = 100;
    int bufferSize = 9 * 1024;
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