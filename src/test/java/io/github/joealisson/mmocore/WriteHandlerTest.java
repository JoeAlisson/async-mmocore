package io.github.joealisson.mmocore;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

public class WriteHandlerTest {

    @Test
    public void testCompletedWithNegative() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        handler.start();
        AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
        WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
        writeHandler.completed(-1, client);
        boolean connected = client.isConnected();
        handler.shutdown();
        handler.join();
        Assert.assertFalse(connected);
    }

    @Test
    public void testCompleted() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        handler.start();
        AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
        client.sendPacket(new AsyncClientPingPacket());
        WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
        client.getConnection().releaseWritingBuffer();
        writeHandler.completed(2, client);
        client.disconnect();
        handler.shutdown();
        handler.join();
        Assert.assertTrue(client.getDataSentSize() > 0);
    }

    @Test
    public void testFailed() throws InterruptedException, IOException, ExecutionException {
        InetSocketAddress socketAddress = new InetSocketAddress(9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        handler.start();
        AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
        WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
        writeHandler.failed(new IllegalArgumentException(), client);
        boolean connected = client.isConnected();
        handler.shutdown();
        handler.join();
        Assert.assertFalse(connected);
    }

}
