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
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * @author JoeAlisson
 */
public class WriteHandlerTest {

    @Test
    public void testCompletedWithNegative() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
            writeHandler.completed(-1L, client);
            boolean connected = client.isConnected();
            Assert.assertFalse(connected);
        } finally {
            handler.shutdown();
        }
    }

    @Test
    public void testCompleted() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            client.sendPacket(new AsyncClientPingPacket());
            WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
            client.getConnection().releaseWritingBuffer();
            writeHandler.completed(2L, client);
            client.disconnect();
            Assert.assertTrue(client.getDataSentSize() > 0);
        } finally {
            handler.shutdown();
        }
    }

    @Test
    public void testFailed() throws InterruptedException, IOException, ExecutionException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            WriteHandler<AsyncClient> writeHandler = new WriteHandler<>();
            writeHandler.failed(new IllegalArgumentException(), client);
            boolean connected = client.isConnected();
            Assert.assertFalse(connected);
        } finally {
            handler.shutdown();
        }
    }

}
