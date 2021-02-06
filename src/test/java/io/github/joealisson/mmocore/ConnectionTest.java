/*
 * Copyright © 2019-2020 Async-mmocore
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
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

public class ConnectionTest {

    private AsyncClient connectionClient;

    @Test
    public void testWriteWithClosedChannel() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            Connection<AsyncClient> connection = client.getConnection();
            connection.close();

            ByteBuffer buffer = ByteBuffer.allocateDirect(10);
            buffer.putLong(80);
            buffer.flip();
            connection.write();
            connection.write(new ByteBuffer[] {buffer});

            Assert.assertFalse(connection.isOpen());
            Assert.assertEquals("", connection.getRemoteAddress());
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testIsOpenWithNoConnectedChannel() throws IOException {
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
        Connection<AsyncClient> connection = new Connection<>(channel,null, null, new ConnectionConfig(null));
        channel.close();
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
