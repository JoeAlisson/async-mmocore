package io.github.joealisson.mmocore;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

public class ConnectionTest {

    private AsyncClient connectionClient;

    @Test
    public void testWriteWithClosedChannel() throws IOException {
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        Connection<AsyncClient> connection = new Connection<>(channel,null, null);
        channel.close();
        connection.write(new byte[10], 10);
    }

    @Test
    public void testIsOpenWithNoConnectedChannel() throws IOException {
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        Connection<AsyncClient> connection = new Connection<>(channel,null, null);
        Assert.assertFalse(connection.isOpen());
    }

    @Test
    public void testRemoteAddressWithClosedChannel() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            Connection<AsyncClient> connection = client.getConnection();
            connection.close();

            Assert.assertFalse(connection.isOpen());
            Assert.assertEquals("", connection.getRemoteAddress());
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testWriteBeforeClose() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, this::buildClient, (buffer, client1) ->  { client1.receivedPacket = buffer ; return null;}, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new,  null, null).connect(socketAddress);

            client.close(new AsyncClientClosePacket());
            Awaitility.waitAtMost(10, TimeUnit.SECONDS).until(() -> nonNull(connectionClient.receivedPacket));
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
