package org.l2j.mmocore;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
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

        builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler);
        connector = Connector.create(AsyncClient::new, handler, handler);

    }

    @Test
    public void testIntegration() throws IOException, ExecutionException, InterruptedException {
        ConnectionHandler<AsyncClient> connectionHandler = builder.build();
        connectionHandler.start();

        AsyncClient client = connector.connect(listenAddress);
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
