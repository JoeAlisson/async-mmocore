package org.l2j.mmocore;

import org.l2j.mmocore.packet.PingPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.time.ZoneId;

public class TestClient {

    public static void main(String[] args) throws IOException {
        TestClient client = new TestClient();
        client.connect(8585);
        client.sendPing();
    }

    ByteBuffer buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer rBuffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
    private SocketChannel socket;

    public void connect(int port) throws IOException {
        socket = SocketChannel.open(new InetSocketAddress(port));
    }

    public void sendPing() throws IOException {

        PingPacket packet = new PingPacket();
        packet.write(buffer);
        buffer.flip();

        int i = socket.write(buffer);
        while (i < 3) {
            i += socket.write(buffer);
        }

        buffer.clear();
        buffer.putLong(System.currentTimeMillis());
        buffer.putShort((short)11);
        buffer.put((byte)0x01);
        buffer.putLong(System.currentTimeMillis());

        buffer.flip();

        i = socket.write(buffer);
        while (i < 19) {
            i += socket.write(buffer);
        }


        i = socket.read(rBuffer);
        while(i < 19) {
            i += socket.read(rBuffer);
        }
        long current = System.currentTimeMillis();
        rBuffer.flip();
        rBuffer.getShort();
        rBuffer.get();

        long send = rBuffer.getLong();
        long received = rBuffer.getLong();
        System.out.println("Initial Time: " + Instant.ofEpochMilli(send).atZone(ZoneId.systemDefault()));
        System.out.println("Server Received Time: " + Instant.ofEpochMilli(received).atZone(ZoneId.systemDefault()));
        System.out.println("Round Trip Time: " + (current - send) );
        System.out.println("Send Time: " + (received - send));

    }

}
