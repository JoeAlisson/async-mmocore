package io.github.joealisson.mmocore;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

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
            client.close(new PacketStatic());
            client.close();
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClosedConnection() throws IOException {
        try(AsynchronousSocketChannel channel = AsynchronousSocketChannel.open()) {
            Connection<AsyncClient> connection = new Connection<>(channel, null, null);
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
    public void testWriteWithException() throws InterruptedException, ExecutionException, IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionHandler<AsyncClient> handler = ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).shutdownWaitTime(100).build();
        try {
            handler.start();
            AsyncClient client = Connector.create(AsyncClient::new, null, null).connect(socketAddress);
            client.writePacket(new WritablePacket<>() {
                @Override
                protected boolean write(AsyncClient client) {
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
                protected boolean write(EncriptationFailClient client) {
                    writeLong(90);
                    writeLong(80);
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
            BigEncripterClient client = Connector.create(BigEncripterClient::new, null, null).connect(socketAddress);
            client.writePacket(new WritablePacket<>() {
                @Override
                protected boolean write(BigEncripterClient client) {
                    writeLong(90);
                    writeLong(80);
                    return true;
                }
            });
        } finally {
            handler.shutdown();
            handler.join();
        }
    }

    @Test
    public void testStaticPacket() throws IOException, ExecutionException, InterruptedException {
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

    @StaticPacket
    static class PacketStatic extends SendablePacket {

        @Override
        protected boolean write(AsyncClient client) {
            writeInt(20);
            writeLong(50);
            return true;
        }
    }

    static class BigEncripterClient extends Client<Connection<BigEncripterClient>> {


        public BigEncripterClient(Connection<BigEncripterClient> connection) {
            super(connection);
        }

        @Override
        public int encryptedSize(int dataSize) {
            return getResourcePool().getSmallSize() * 2;
        }

        @Override
        public byte[] encrypt(byte[] data, int offset, int size) {
            return data;
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

        }
    }

    static class EncriptationFailClient extends Client<Connection<EncriptationFailClient>> {

        EncriptationFailClient(Connection<EncriptationFailClient> connection) {
            super(connection);
        }

        @Override
        public int encryptedSize(int dataSize) {
            return -1;
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

        }
    }
}
