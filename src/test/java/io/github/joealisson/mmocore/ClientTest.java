package io.github.joealisson.mmocore;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class ClientTest {

    @Test
    public void testNullInitialize() {
        try {
            new AsyncClient(null);
            Assert.fail("Should Throw an IllegalArgument Exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testClosedConnection() throws IOException {
        try(AsynchronousSocketChannel channel = AsynchronousSocketChannel.open()) {
            Connection<AsyncClient> connection = new Connection<>(channel, null, null);
            new AsyncClient(connection);
            Assert.fail("Should Throw an IllegalArgument Exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWriteNullPacket() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress(9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).build();
        handler.start();
        AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
        client.sendPacket(null);
        handler.shutdown();
        handler.join();
    }
}
