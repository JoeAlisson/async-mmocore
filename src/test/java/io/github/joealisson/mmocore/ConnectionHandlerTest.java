package io.github.joealisson.mmocore;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ConnectionHandlerTest {

    @Test
    public void testCachedThreadPoolHandler() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress listenAddress = new InetSocketAddress(9090);
        GenericClientHandler handler = new GenericClientHandler();
        ConnectionBuilder<AsyncClient> builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).threadPoolSize(-1).shutdownWaitTime(100);
        ConnectionHandler<AsyncClient> connectionHandler = builder.build();
        Connector<AsyncClient> connector = Connector.create(AsyncClient::new, handler, handler);
        try {
            connectionHandler.start();
            AsyncClient client = connector.connect("127.0.0.1", 9090);

             client.sendPacket(new AsyncClientClosePacket());
            Awaitility.waitAtMost(30, TimeUnit.SECONDS).until(() -> !client.isConnected());
            Assert.assertFalse(client.isConnected());
        }finally {
            connectionHandler.shutdown();
            connectionHandler.join();
        }
    }

    @Test
    public void testRefuseConnection() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress listenAddress = new InetSocketAddress(9090);
        GenericClientHandler handler = new GenericClientHandler();
        ConnectionBuilder<AsyncClient> builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).filter(channel -> false).shutdownWaitTime(100);
        ConnectionHandler<AsyncClient> connectionHandler = builder.build();
        Connector<AsyncClient> connector = Connector.create(AsyncClient::new, handler, handler);
        try {
            connectionHandler.start();
            AsyncClient client = connector.connect("127.0.0.1", 9090);

            Awaitility.waitAtMost(30, TimeUnit.SECONDS).until(() -> !client.isConnected());
            Assert.assertFalse(client.isConnected());
        }finally {
            connectionHandler.shutdown();
            connectionHandler.join();
        }
    }
}
