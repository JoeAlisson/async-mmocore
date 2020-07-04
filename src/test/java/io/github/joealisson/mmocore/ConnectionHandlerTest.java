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
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ConnectionHandlerTest {

    @Test
    public void testCachedThreadPoolHandler() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress listenAddress = new InetSocketAddress(9090);
        GenericClientHandler handler = new GenericClientHandler();
        ConnectionBuilder<AsyncClient> builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).threadPoolSize(-1).shutdownWaitTime(100);
        ConnectionHandler<AsyncClient> connectionHandler = builder.build();
        Connector<AsyncClient> connector = Connector.create(AsyncClient::new, handler, handler);
        try {
            connectionHandler.start();
            AsyncClient client = connector.connect(null, 9090);

             client.sendPacket(new AsyncClientClosePacket());
            Awaitility.waitAtMost(30, TimeUnit.SECONDS).until(() -> !client.isConnected());
            Assert.assertFalse(client.isConnected());
        }finally {
            connectionHandler.shutdown();
            connectionHandler.join();
        }
    }

    @Test
    public void testRefuseConnection() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress listenAddress = new InetSocketAddress(9090);
        GenericClientHandler handler = new GenericClientHandler();
        ConnectionBuilder<AsyncClient> builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).filter(channel -> false).shutdownWaitTime(100);
        ConnectionHandler<AsyncClient> connectionHandler = builder.build();
        Connector<AsyncClient> connector = Connector.create(AsyncClient::new, handler, handler);
        try {
            connectionHandler.start();
            AsyncClient client = connector.connect("", 9090);

            Awaitility.waitAtMost(30, TimeUnit.SECONDS).until(() -> !client.isConnected());
            Assert.assertFalse(client.isConnected());
        }finally {
            connectionHandler.shutdown();
            connectionHandler.join();
        }
    }

    @Test
    public void testNonAcceptedConnection() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress listenAddress = new InetSocketAddress(9090);
        GenericClientHandler handler = new GenericClientHandler();
        ThrowableFactory factory = new ThrowableFactory(new NullPointerException());
        ConnectionBuilder<ThrowableClient> builder = ConnectionBuilder.create(listenAddress, factory, null, null).shutdownWaitTime(100);
        ConnectionHandler<ThrowableClient> connectionHandler = builder.build();
        Connector<AsyncClient> connector = Connector.create(AsyncClient::new, handler, handler);
        try {
            connectionHandler.start();
            AsyncClient client = connector.connect("127.0.0.1", 9090);
            Awaitility.waitAtMost(30, TimeUnit.SECONDS).until(() -> !client.isConnected());
            Assert.assertFalse(client.isConnected());
        }finally {
            connectionHandler.shutdown();
            connectionHandler.join();
        }
    }

    static class ThrowableFactory implements ClientFactory<ThrowableClient> {

        private final RuntimeException exeception;

        ThrowableFactory(RuntimeException e) {
            this.exeception = e;
        }

        @Override
        public ThrowableClient create(Connection<ThrowableClient> connection) {
            return new ThrowableClient(connection, exeception);
        }
    }

    static class ThrowableClient extends Client<Connection<ThrowableClient>> {

        private final RuntimeException exception;

        public ThrowableClient(Connection<ThrowableClient> connection, RuntimeException e) {
            super(connection);
            this.exception=e;
        }

        @Override
        public int encryptedSize(int dataSize) {
            return 0;
        }

        @Override
        public byte[] encrypt(byte[] data, int offset, int size) {
            return new byte[0];
        }

        @Override
        public boolean decrypt(byte[] data, int offset, int size) {
            return false;
        }

        @Override
        protected void onDisconnection() {

        }

        @Override
        public void onConnected() {
            throw exception;
        }
    }
}
