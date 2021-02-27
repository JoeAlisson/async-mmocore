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
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public class ConnectionHandlerTest {

    @Test
    public void testCachedThreadPoolHandler() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress listenAddress = new InetSocketAddress(9090);
        GenericClientHandler handler = new GenericClientHandler();
        ConnectionBuilder<AsyncClient> builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).useCachedThreadPool(true).shutdownWaitTime(100);
        ConnectionHandler<AsyncClient> connectionHandler = builder.build();
        Connector<AsyncClient> connector = Connector.create(AsyncClient::new, handler, handler);
        try {
            connectionHandler.start();
            AsyncClient client = connector.connect("localhost", 9090);

            client.sendPacket(new AsyncClientClosePacket());
            Awaitility.waitAtMost(30, TimeUnit.SECONDS).until(() -> !client.isConnected());
            Assert.assertFalse(client.isConnected());
        }finally {
            connectionHandler.shutdown();
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
        }
    }

    @Test
    public void testResourceStats() throws IOException {
        var template = "Pool {maxSize=%d, bufferSize=%d, estimateUse=%d}";
        var listenAddress = new InetSocketAddress(9090);
        var handler = new GenericClientHandler();
        ConnectionHandler<AsyncClient> connectionHandler = null;

        try {
            connectionHandler = createConnectionHandler(listenAddress, handler, 0);
            checkStats(template, connectionHandler, 0, 0, 0);
        } finally {
            if(nonNull(connectionHandler))
                connectionHandler.shutdown();
        }

        try {
            connectionHandler = createConnectionHandler(listenAddress, handler, 0.5f);
            checkStats(template, connectionHandler, 2, 4, 6);
        } finally {
            if(nonNull(connectionHandler))
                connectionHandler.shutdown();
        }


    }

    private ConnectionHandler<AsyncClient> createConnectionHandler(InetSocketAddress listenAddress, GenericClientHandler handler, float initFactor) throws IOException {
        ConnectionHandler<AsyncClient> connectionHandler;
        connectionHandler = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).shutdownWaitTime(100).initBufferPoolFactor(initFactor)
                .addBufferPool(4, 5)
                .addBufferPool(8, 6)
                .addBufferPool(12, 7).build();
        return connectionHandler;
    }

    private void checkStats(String template, ConnectionHandler<AsyncClient> connectionHandler, int estimate1, int estimate2, int estimate3) {
        String stats = connectionHandler.resourceStats();
        Assert.assertTrue(stats.contains(String.format(template, 4, 5, estimate1)));
        Assert.assertTrue(stats.contains(String.format(template, 8, 6, estimate2)));
        Assert.assertTrue(stats.contains(String.format(template, 12, 7, estimate3)));
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
        public boolean encrypt(Buffer data, int offset, int size) {
            return  false;
        }

        @Override
        public boolean decrypt(Buffer data, int offset, int size) {
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
