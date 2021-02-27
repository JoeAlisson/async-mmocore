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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import static io.github.joealisson.mmocore.ConnectionConfig.HEADER_SIZE;

/**
 * @author JoeAlisson
 */
public class ReadHandlerTest {

    private ConnectionBuilder<AsyncClient>  connectionBuilder;
    private InetSocketAddress address;
    private Connector<AsyncClient> connector;

    @Before
    public void setUp() {
        address = new InetSocketAddress("127.0.0.1", 9090);
        connectionBuilder = ConnectionBuilder.create(address, AsyncClient::new, (data, client) -> null, (packet) -> { });
        connector = Connector.create(AsyncClient::new, null, null);
    }

    @Test
    public void testFailed() throws InterruptedException, ExecutionException, IOException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        try {
            connectionHandler.start();
            AsyncClient client = connector.connect(address);
            ReadHandler<AsyncClient> handler = new ReadHandler<>(null, null);
            handler.failed(new IllegalAccessException(), client);
            boolean connected = client.isConnected();
            Assert.assertFalse(connected);
        } finally {
            connectionHandler.shutdown();
        }
    }

    @Test
    public void testCompletedDisconnected() throws InterruptedException, ExecutionException, IOException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        try {
            connectionHandler.start();
            AsyncClient client = connector.connect(address);
            Assert.assertTrue(client.isConnected());
            client.disconnect();
            Assert.assertFalse(client.isConnected());
            ReadHandler<AsyncClient> handler = new ReadHandler<>(null, null);
            handler.completed(2, client);
            Assert.assertEquals(HEADER_SIZE, client.getExpectedReadSize());
        } finally {
            connectionHandler.shutdown();
        }
    }

    @Test
    public void testCompletedWithoutData() throws InterruptedException, ExecutionException, IOException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        connectionHandler.start();
        AsyncClient client = connector.connect(address);
        try {
            ReadHandler<AsyncClient> handler = new ReadHandler<>(null, null);
            handler.completed(2, client);
            Assert.fail("Exception is Expected");
        } catch (Exception e) {
            boolean connected = client.isConnected();
            Assert.assertTrue(connected);
        } finally {
            connectionHandler.shutdown();
        }
    }

    @Test
    public void testCompletedWithoutEnoughData() throws InterruptedException, ExecutionException, IOException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        connectionHandler.start();
        AsyncClient client = connector.connect(address);
        try {
            ReadHandler<AsyncClient> handler = new ReadHandler<>(null, null);
            ByteBuffer buffer = client.getConnection().getReadingBuffer();
            buffer.putShort((short) 10);
            handler.completed(2, client);
        } catch (Exception e) {
            boolean connected = client.isConnected();
            Assert.assertTrue(connected);
        } finally {
            connectionHandler.shutdown();
        }
    }

    @Test
    public void testTwoPacketOnce() throws IOException, ExecutionException, InterruptedException {
        ConnectionHandler<AsyncClient> connectionHandler = connectionBuilder.shutdownWaitTime(100).build();
        connectionHandler.start();
        AsyncClient client = connector.connect(address);
        try {
            ReadHandler<AsyncClient> handler = new ReadHandler<>( (data, client1) ->  null, (packet) -> { });
            ByteBuffer buffer = client.getConnection().getReadingBuffer();
            buffer.putShort((short) 4);
            buffer.put((byte) 10);
            buffer.putShort((short) 20);
            buffer.putShort((short) 10);

            handler.completed(7, client);
        } catch (Exception e) {
            boolean connected = client.isConnected();
            Assert.assertTrue(connected);
        } finally {
            connectionHandler.shutdown();
        }
    }
}
