package io.github.joealisson.mmocore;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class ConnectionTest {

    private AsyncClient connectionClient;

    @Test
    public void testWriteWithClosedChannel() throws IOException, ExecutionException, InterruptedException {
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        Connection<AsyncClient> connection = new Connection<>(channel,null, null);
        channel.close();
        connection.write(ByteBuffer.wrap(new byte[10]), false);
    }

    @Test
    public void testIsOpenWithNoConnectedChannel() throws IOException {
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        Connection<AsyncClient> connection = new Connection<>(channel,null, null);
        Assert.assertFalse(connection.isOpen());
    }

    @Test
    public void testRemoteAddressWithClosedChannel() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress(9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        handler.start();
        AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
        Connection<AsyncClient> connection = client.getConnection();
        connection.close();
        handler.shutdown();
        handler.join();
        Assert.assertFalse(connection.isOpen());
        Assert.assertEquals("", connection.getRemoteAddress());
    }

    @Test
    public void testWriteSyncBeforeClose() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress(9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, this::buildClient, (buffer, client1) ->  { client1.receivedPacket = buffer ; return null;}, null).shutdownWaitTime(100).build();
        handler.start();
        AsyncClient client = Connector.create(AsyncClient::new,  null, null).connect(socketAddress);
        try {
            client.close(new AsyncClientClosePacket());
            Assert.assertNotNull(connectionClient.receivedPacket);
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    private AsyncClient buildClient(Connection<AsyncClient> tConnection) {
        connectionClient = new AsyncClient(tConnection);
        return connectionClient;
    }

    @After
    public void tearDown() {
        connectionClient = null;
    }
}
