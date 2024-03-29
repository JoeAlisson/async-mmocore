/*
 * Copyright © 2019-2021 Async-mmocore
 *
 * This file is part of the Async-mmocore project.
 *
 * Async-mmocore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Async-mmocore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.joealisson.mmocore;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

/**
 * @author JoeAlisson
 */
public class CommunicationTest {

    private static final AtomicBoolean shutdown = new AtomicBoolean(false);
    private static boolean success = true;
    private ConnectionBuilder<AsyncClient> builder;
    private Connector<AsyncClient> connector;
    private final InetSocketAddress listenAddress = new InetSocketAddress(9090);
    private ConnectionHandler<AsyncClient> connectionHandler;

    private static AtomicInteger packetsSent;
    private static int PACKET_SENT_TO_SUCCESS = 10;

    static void shutdown(boolean success) {
        shutdown.getAndSet(true);
        CommunicationTest.success = success;
    }

    static void incrementPacketSent() {
        if(packetsSent.incrementAndGet() == PACKET_SENT_TO_SUCCESS) {
            shutdown(true);
        }
    }

    @Before
    public void SetUp() {
        packetsSent = new AtomicInteger();
        GenericClientHandler handler = new GenericClientHandler();

        builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).filter(channel -> true).threadPoolSize(2).useNagle(false)
                .shutdownWaitTime(500).addBufferPool(10,300).initBufferPoolFactor(0.2f).bufferSegmentSize(256).threadPriority(10);
        connector = Connector.create(AsyncClient::new, handler, handler).addBufferPool(10, 300).initBufferPoolFactor(0.2f)
                .bufferSegmentSize(128).useCachedThreadPool(true).threadPoolSize(2).maxCachedThreads(4).threadPriority(10);
        packetsSent.set(0);
        shutdown.set(false);
    }

    @Test
    public void testIntegration() throws IOException, ExecutionException, InterruptedException {
        connectionHandler = builder.build();
        connectionHandler.start();

        AsyncClient client = connector.connect("localhost", 9090);
        client.sendPacket(new AsyncClientPingPacket());

        Awaitility.waitAtMost(10, TimeUnit.SECONDS).untilTrue(shutdown);

        connectionHandler.shutdown();
        if(!success) {
            fail();
        }
    }

    @Test
    public void testIntegrationWithCachedThreadPool() throws IOException, ExecutionException, InterruptedException {
        connectionHandler = builder.useCachedThreadPool(true).maxCachedThreads(4).build();
        connectionHandler.start();

        AsyncClient client = connector.connect("localhost", 9090);
        client.sendPacket(new AsyncClientPingPacket());

        Awaitility.waitAtMost(10, TimeUnit.SECONDS).untilTrue(shutdown);

        connectionHandler.shutdown();
        if(!success) {
            fail();
        }
    }

    @Test
    public void testBroadcast() throws IOException, ExecutionException, InterruptedException {
        connectionHandler = builder.build();
        connectionHandler.start();
        packetsSent.set(0);
        AsyncClient[] clients = new AsyncClient[PACKET_SENT_TO_SUCCESS];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = connector.connect("localhost", 9090);
        }

        AsyncClientBroadcastPacket packet = new AsyncClientBroadcastPacket();
        packet.sendInBroadcast(true);

        for (AsyncClient client : clients) {
            client.sendPacket(packet);
        }

        Awaitility.waitAtMost(10, TimeUnit.SECONDS).untilTrue(shutdown);

        for (AsyncClient client : clients) {
            client.close();
        }

        connectionHandler.shutdown();
        if(packetsSent.get() != clients.length) {
            fail();
        }
    }

    @Test
    public void testDisposable() throws IOException, ExecutionException, InterruptedException {
        int disposeThreshold = 5;
        PACKET_SENT_TO_SUCCESS = 100;
        packetsSent.set(0);
        connectionHandler = builder.dropPacketThreshold(disposeThreshold).build();
        connectionHandler.start();

        AsyncClient client = connector.dropPacketThreshold(disposeThreshold).connect("localhost", 9090);
        AsyncClientBroadcastPacket[] packets = new AsyncClientBroadcastPacket[100];
        for (int i = 0; i < 100; i++) {
            packets[i] = new AsyncClientBroadcastPacket();
        }
        AsyncDisposablePacket packet = new AsyncDisposablePacket();
        client.sendPackets(packets);

        Assert.assertTrue(client.getEstimateQueueSize() > disposeThreshold);
        client.sendPacket(packet);
        client.sendPacket(new AsyncClientBroadcastPacket());

        Awaitility.waitAtMost(10, TimeUnit.SECONDS).untilTrue(shutdown);

        connectionHandler.shutdown();
        if(packetsSent.get() != packets.length + 1) {
            fail();
        }
    }

    @Test
    public void testReadingThrottling() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<ReadingThrottlingHelper.RTClient> handler = ConnectionBuilder.create(socketAddress, ReadingThrottlingHelper::create, ReadingThrottlingHelper::handlePacket, ReadingThrottlingHelper::execute).disableAutoReading(true).build();
        try {
            handler.start();
            ReadingThrottlingHelper.RTClient client = Connector.create(ReadingThrottlingHelper.RTClient::new, ReadingThrottlingHelper::handlePacket, ReadingThrottlingHelper::execute).disableAutoReading(true).connect(socketAddress);

            client.writePacket(ReadingThrottlingHelper.ping());
            client.writePacket(ReadingThrottlingHelper.ping2nd());
            ReadingThrottlingHelper.RTClient receivingClient = ReadingThrottlingHelper.lastClient;

            Awaitility.waitAtMost(3, TimeUnit.SECONDS).untilTrue(receivingClient.readableAgain);
            Assert.assertTrue(receivingClient.hasMinimumTimeBetweenPackets());


            receivingClient.readableAgain.set(false);
            client.writePackets(List.of(ReadingThrottlingHelper.ping(), ReadingThrottlingHelper.ping2nd()));
            receivingClient.readNextPacket();

            Awaitility.waitAtMost(3, TimeUnit.SECONDS).untilTrue(receivingClient.readableAgain);
            Assert.assertTrue(receivingClient.hasMinimumTimeBetweenPackets());

            client.close();
            receivingClient.close();

        } catch (ExecutionException | InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            handler.shutdown();
        }
    }

    @Test
    public void testFairnessController() throws IOException, ExecutionException, InterruptedException {
        GenericClientHandler handler = new GenericClientHandler();
        connectionHandler = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).fairnessBuckets(4).build();
        connectionHandler.start();

        PACKET_SENT_TO_SUCCESS = 10;
        packetsSent.set(0);

        AsyncClient[] clients = new AsyncClient[PACKET_SENT_TO_SUCCESS];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = connector.connect("localhost", 9090);
        }

        AsyncClientFairnessPacket packet = new AsyncClientFairnessPacket();

        for (AsyncClient client : clients) {
            CompletableFuture.runAsync(() -> client.sendPacket(packet));
        }

        Awaitility.waitAtMost(10, TimeUnit.SECONDS).untilTrue(shutdown);

        for (AsyncClient client : clients) {
            client.close();
        }

        connectionHandler.shutdown();
        if(packetsSent.get() != clients.length) {
            fail();
        }
    }

    static class AsyncDisposablePacket extends WritablePacket<AsyncClient> {

        @Override
        protected boolean write(AsyncClient client, WritableBuffer buffer) {
            shutdown(false);
            throw new IllegalStateException();
        }

        @Override
        public boolean canBeDropped(AsyncClient client) {
            return true;
        }
    }

    @After
    public void tearDown() {
        if(!shutdown.get() && connectionHandler != null) {
            connectionHandler.shutdown();
        }
    }
}
