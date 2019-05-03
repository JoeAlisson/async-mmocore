package io.github.joealisson.mmocore;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

public class WriteHandlerTest {

    @Test
    public void testCompletedWithNegative() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
            writeHandler.completed(-1, client);
            boolean connected = client.isConnected();
            Assert.assertFalse(connected);
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testCompleted() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            client.sendPacket(new AsyncClientPingPacket());
            WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
            client.getConnection().releaseWritingBuffer();
            writeHandler.completed(2, client);
            client.disconnect();
            Assert.assertTrue(client.getDataSentSize() > 0);
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testFailed() throws InterruptedException, IOException, ExecutionException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
            writeHandler.failed(new IllegalArgumentException(), client);
            boolean connected = client.isConnected();
            Assert.assertFalse(connected);
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

}
