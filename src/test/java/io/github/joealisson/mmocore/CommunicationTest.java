package io.github.joealisson.mmocore;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;

public class CommunicationTest {

    private static AtomicBoolean shutdown = new AtomicBoolean(false);
    private static boolean success = true;
    private ConnectionBuilder<AsyncClient> builder;
    private Connector<AsyncClient> connector;
    private InetSocketAddress listenAddress = new InetSocketAddress(9090);
    ConnectionHandler<AsyncClient> connectionHandler;

    static void shutdown(boolean success) {
        shutdown.getAndSet(true);
        CommunicationTest.success = success;
    }

    @Before
    public void SetUp() {
        GenericClientHandler handler = new GenericClientHandler();

        builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).filter(channel -> true).threadPoolSize(2).useNagle(false).shutdownWaitTime(500)
                .bufferDefaultSize(300).bufferSmallSize(40).bufferLargeSize(100).bufferMediumSize(50).bufferPoolSize(10).bufferSmallPoolSize(10).bufferMediumPoolSize(8)
                .bufferLargePoolSize(3).byteOrder(ByteOrder.LITTLE_ENDIAN);
        connector = Connector.create(AsyncClient::new, handler, handler).bufferDefaultSize(300).bufferLargePoolSize(3).bufferLargeSize(100).bufferMediumPoolSize(8)
                .bufferMediumSize(50).bufferSmallPoolSize(10).bufferPoolSize(10).bufferSmallSize(40).byteOrder(ByteOrder.LITTLE_ENDIAN);

    }

    @Test
    public void testIntegration() throws IOException, ExecutionException, InterruptedException {
        connectionHandler = builder.build();
        connectionHandler.start();

        AsyncClient client = connector.connect("localhost", 9090);
        client.sendPacket(new AsyncClientPingPacket());

        Awaitility.waitAtMost(1000, TimeUnit.SECONDS).untilTrue(shutdown);

        connectionHandler.shutdown();
        connectionHandler.join();
        if(!success) {
            fail();
        }
    }

    @After
    public void tearDown() {
        if(!shutdown.get()) {
            connectionHandler.shutdown();
        }
    }
}
