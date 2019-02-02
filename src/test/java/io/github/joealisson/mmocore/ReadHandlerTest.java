package io.github.joealisson.mmocore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class ReadHandlerTest {

    private ConnectionBuilder<AsyncClient>  connectionBuilder;
    private InetSocketAddress address;
    private Connector<AsyncClient> connector;

    @Before
    public void setUp() {
        address = new InetSocketAddress(9090);
        connectionBuilder = ConnectionBuilder.create(address, AsyncClient::new, (data, client) -> null, (packet) -> { });
        connector = Connector.create(AsyncClient::new, null, null);
    }


    @Test
    public void testFailed() throws InterruptedException, ExecutionException, IOException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        connectionHandler.start();
        AsyncClient client = connector.connect(address);
        ReadHandler<AsyncClient> handler = new ReadHandler<>(null, null);
        handler.failed(new IllegalAccessException(), client);
        boolean connected = client.isConnected();
        connectionHandler.shutdown();
        connectionHandler.join();
        Assert.assertFalse(connected);
    }

    @Test
    public void testCompletedDiconnected() throws InterruptedException, ExecutionException, IOException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        connectionHandler.start();
        AsyncClient client = connector.connect(address);
        client.disconnect();
        ReadHandler<AsyncClient> handler = new ReadHandler<>(null, null);
        handler.completed(2, client);
        connectionHandler.shutdown();
        connectionHandler.join();
    }

    @Test
    public void testCompletedWithoutData() throws InterruptedException, ExecutionException, IOException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        connectionHandler.start();
        AsyncClient client = connector.connect(address);
        ReadHandler<AsyncClient> handler = new ReadHandler<>(null, null);
        try {
            handler.completed(2, client);
        } catch (Exception e) {
            boolean connected = client.isConnected();
            Assert.assertTrue(connected);
        } finally {
            connectionHandler.shutdown();
            connectionHandler.join();
        }
    }

    @Test
    public void testCompletedWithoutEnoughData() throws InterruptedException, ExecutionException, IOException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        connectionHandler.start();
        AsyncClient client = connector.connect(address);
        ReadHandler<AsyncClient> handler = new ReadHandler<>(null, null);
        ByteBuffer buffer = client.getConnection().getReadingBuffer();
        buffer.putShort((short) 10);
        try {
            handler.completed(2, client);
        } catch (Exception e) {
            boolean connected = client.isConnected();
            Assert.assertTrue(connected);
        } finally {
            connectionHandler.shutdown();
            connectionHandler.join();
        }
    }

    @Test
    public void testTwoPacketOnce() throws IOException, ExecutionException, InterruptedException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        connectionHandler.start();
        AsyncClient client = connector.connect(address);
        ReadHandler<AsyncClient> handler = new ReadHandler<>( (data, client1) ->  null, (packet) -> { });
        ByteBuffer buffer = client.getConnection().getReadingBuffer();
        buffer.putShort((short) 4);
        buffer.put((byte) 10);
        buffer.putShort((short) 20);
        buffer.putShort((short) 10);
        try {
            handler.completed(7, client);
        } catch (Exception e) {
            boolean connected = client.isConnected();
            Assert.assertTrue(connected);
        } finally {
            connectionHandler.shutdown();
            connectionHandler.join();
        }
    }
}
