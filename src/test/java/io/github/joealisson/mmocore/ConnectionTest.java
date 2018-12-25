package io.github.joealisson.mmocore;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class ConnectionTest {

    @Test
    public void testWriteWithClosedChannel() throws IOException {
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        Connection<AsyncClient> connection = new Connection<>(channel,null, null);
        channel.close();
        connection.write(new byte[10], 2, 8, false);
    }

    @Test
    public void testWriteSync() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress(9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).build();
        handler.start();
        AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
        try {
            client.getConnection().write(new byte[10], 2, 6, true);
        } finally {
            handler.shutdown();
            handler.join();
        }
    }
}
