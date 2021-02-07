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
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * @author JoeAlisson
 */
public class ClientTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullInitialize() {
        new AsyncClient(null);
    }

    @Test
    public void testRemoteAddress() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, (buffer, client) -> null, incomingPacket -> { }).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, ((buffer, client1) -> null), incomingPacket -> {
            }).connect(socketAddress);
            Assert.assertEquals("127.0.0.1", client.getHostAddress());
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testCloseAlreadyClosed() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, (buffer, client) -> null, incomingPacket -> { }).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, ((buffer, client1) -> null), incomingPacket -> {
            }).connect(socketAddress);
            client.close(new AsyncClientClosePacket());
            client.close();
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClosedConnection() throws IOException {
        try(AsynchronousSocketChannel channel = AsynchronousSocketChannel.open()) {
            Connection<AsyncClient> connection = new Connection<>(channel, null, null, new ConnectionConfig(null));
            channel.close();
            new AsyncClient(connection);
        }
    }

    @Test
    public void testCloseWithNullPacket() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, (buffer, client) -> null, incomingPacket -> { }).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, ((buffer, client1) -> null), incomingPacket -> {
            }).connect(socketAddress);
            client.close();
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testWriteNullPacket() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            client.sendPacket(null);
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testWriteEmptyPacket() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            client.sendPacket(new EmptyPacket());
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testWriteMultipleEmptyPacket() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            client.sendPackets(new EmptyPacket(), new EmptyPacket());
        } finally {
            handler.shutdown();
            handler.join();
        }
    }


    @Test
    public void testWriteEmptyEncryptedPacket() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            EmptyEncrypterClient client = Connector.create(EmptyEncrypterClient::new, null, null).connect(socketAddress);
            client.writePacket(new EmptyEncrypterPacket());
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testMultipleNullPacket() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            EmptyEncrypterClient client = Connector.create(EmptyEncrypterClient::new, null, null).connect(socketAddress);
            client.writePackets(null);
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testMultipleEmptyPacket() throws IOException, ExecutionException, InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            EmptyEncrypterClient client = Connector.create(EmptyEncrypterClient::new, null, null).connect(socketAddress);
            client.writePackets(null);
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testWriteWithException() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            client.writePacket(new WritablePacket<>() {
                @Override
                protected boolean write(AsyncClient client, WritableBuffer buffer) {
                    throw new IllegalStateException();
                }
            });
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testEncriptationFailed() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<EncriptationFailClient> handler = ConnectionBuilder.create(socketAddress, EncriptationFailClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            EncriptationFailClient client = Connector.create(EncriptationFailClient::new, null, null).connect(socketAddress);
            client.writePacket(new WritablePacket<>() {
                @Override
                protected boolean write(EncriptationFailClient client, WritableBuffer buffer) {
                    buffer.writeLong(90);
                    buffer.writeLong(80);
                    return true;
                }
            });
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testEncryptedDataOverflow() throws InterruptedException, IOException, ExecutionException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<BigEncripterClient> handler = ConnectionBuilder.create(socketAddress, BigEncripterClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            BigEncripterClient client = Connector.create(BigEncripterClient::new, null, null)
                    .addBufferPool(10, 4).addBufferPool(10, 16).connect(socketAddress);
            client.writePacket(new WritablePacket<>() {
                @Override
                protected boolean write(BigEncripterClient client, WritableBuffer buffer) {
                    buffer.writeLong(90);
                    buffer.writeLong(80);
                    return true;
                }
            });
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testMultipleEncryptedDataOverflow() throws InterruptedException, IOException, ExecutionException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<BigEncripterClient> handler = ConnectionBuilder.create(socketAddress, BigEncripterClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            BigEncripterClient client = Connector.create(BigEncripterClient::new, null, null)
                    .addBufferPool(10, 4).addBufferPool(10, 16).connect(socketAddress);
            client.writePacket(new WritablePacket<BigEncripterClient>() {
                @Override
                protected boolean write(BigEncripterClient client, WritableBuffer buffer) {
                    buffer.writeLong(90);
                    buffer.writeLong(80);
                    return true;
                }
            });
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testNewBuffer() throws InterruptedException, IOException, ExecutionException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9090);
        ConnectionHandler<BigEncripterClient> handler = ConnectionBuilder.create(socketAddress, BigEncripterClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            BigEncripterClient client = Connector.create(BigEncripterClient::new, null, null)
                    .addBufferPool(10, 4).addBufferPool(10, 16).connect(socketAddress);
            client.writePacket(new WritablePacket<>() {
                @Override
                protected boolean write(BigEncripterClient client, WritableBuffer buffer) {
                    buffer.writeByte(10);
                    buffer.writeShort(20);
                    buffer.writeInt(30);
                    buffer.writeFloat(40);
                    buffer.writeDouble(50);
                    buffer.writeBytes((byte)60, (byte)70, (byte) 80);
                    buffer.writeLong(90);
                    buffer.writeLong(80);
                    return true;
                }
            });
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    static class BigEncripterClient extends Client<Connection<BigEncripterClient>> {

        public BigEncripterClient(Connection<BigEncripterClient> connection) {
            super(connection);
        }

        public int encryptedSize(int dataSize) {
            return dataSize * 2;
        }

        @Override
        public boolean encrypt(Buffer data, int offset, int size) {
            var encryptedSize = encryptedSize(size);
            data.limit(encryptedSize);
            return true;
        }

        @Override
        public boolean decrypt(Buffer data, int offset, int size) {
            return true;
        }

        @Override
        protected void onDisconnection() {

        }

        @Override
        public void onConnected() {

        }
    }

    static class EncriptationFailClient extends Client<Connection<EncriptationFailClient>> {

        EncriptationFailClient(Connection<EncriptationFailClient> connection) {
            super(connection);
        }

        @Override
        public boolean encrypt(Buffer data, int offset, int size) {
            return false;
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

        }
    }

    static class EmptyEncrypterClient extends Client<Connection<EmptyEncrypterClient>> {

        public EmptyEncrypterClient(Connection<EmptyEncrypterClient> connection) {
            super(connection);
        }

        @Override
        public boolean encrypt(Buffer data, int offset, int size) {
            data.limit(0);
            return true;
        }

        @Override
        public boolean decrypt(Buffer data, int offset, int size) {
            return true;
        }

        @Override
        protected void onDisconnection() {

        }

        @Override
        public void onConnected() {

        }
    }

    static class EmptyEncrypterPacket extends WritablePacket<EmptyEncrypterClient> {

        @Override
        protected boolean write(EmptyEncrypterClient client, WritableBuffer buffer) {
            buffer.writeDouble(42);
            return true;
        }
    }

    static class EmptyPacket extends WritablePacket<AsyncClient> {

        @Override
        protected boolean write(AsyncClient client, WritableBuffer buffer) {
            return true;
        }
    }
}
