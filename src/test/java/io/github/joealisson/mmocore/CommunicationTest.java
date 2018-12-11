package io.github.joealisson.mmocore;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.fail;

public class CommunicationTest {

    private static boolean shutdown = false;
    private static boolean success = true;

    static void shutdown(boolean success) {
        shutdown = true;
        CommunicationTest.success = success;
    }

    ConnectionBuilder<AsyncClient> builder;
    Connector<AsyncClient> connector;
    InetSocketAddress listenAddress;

    @Before
    public void SetUp() {
        listenAddress = new InetSocketAddress(9090);
        GenericClientHandler handler = new GenericClientHandler();

        builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).filter(channel -> true).threadPoolSize(2).useNagle(false).shutdownWaitTime(500)
                .bufferDefaultSize(100).bufferMinSize(40).bufferLargeSize(200).bufferMediumSize(75).bufferPoolSize(10).bufferMinPoolSize(10).bufferMediumPoolSize(8)
                .bufferLargePoolSize(3).byteOrder(ByteOrder.LITTLE_ENDIAN);
        connector = Connector.create(AsyncClient::new, handler, handler).bufferDefaultSize(100).bufferLargePoolSize(3).bufferLargeSize(200).bufferMediumPoolSize(8)
                .bufferMediumSize(75).bufferMinPoolSize(10).bufferPoolSize(10).bufferMinSize(50).byteOrder(ByteOrder.LITTLE_ENDIAN);

    }

    @Test
    public void testIntegration() throws IOException, ExecutionException, InterruptedException {
        ConnectionHandler<AsyncClient> connectionHandler = builder.build();
        connectionHandler.start();

        AsyncClient client = connector.connect("localhost", 9090);
        client.sendPacket(new AsyncClientPingPacket());

        while (!shutdown) {
            Thread.sleep(5000);
        }

        connectionHandler.shutdown();
        connectionHandler.join();
        if(!success) {
            fail();
        }
    }
}
