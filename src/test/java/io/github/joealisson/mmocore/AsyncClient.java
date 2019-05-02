package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;

import static java.util.Objects.nonNull;

public class AsyncClient extends Client<Connection<AsyncClient>> {

    public ByteBuffer receivedPacket;

    public AsyncClient(Connection<AsyncClient> connection) {
        super(connection);
    }

    @Override
    public boolean decrypt(byte[] data, int offset, int size) {
        return true;
    }

    @Override
    public int encrypt(byte[] data, int offset, int size) {
        return size;
    }

    @Override
    protected void onDisconnection() {

    }

    @Override
    public void onConnected() {

    }


    public void sendPacket(WritablePacket<AsyncClient> packet) {
        if(nonNull(packet)) {
            System.out.println("Sending " + packet.toString());
        }
        writePacket(packet);
    }
}
