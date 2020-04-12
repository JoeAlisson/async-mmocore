package io.github.joealisson.mmocore;

import static java.util.Objects.nonNull;

public class AsyncClient extends Client<Connection<AsyncClient>> {

    public PacketBuffer receivedPacket;

    public AsyncClient(Connection<AsyncClient> connection) {
        super(connection);
    }

    @Override
    public int encryptedSize(int dataSize) {
        return dataSize;
    }

    @Override
    public boolean decrypt(byte[] data, int offset, int size) {
        return true;
    }

    @Override
    public byte[] encrypt(byte[] data, int offset, int size) {
        return data;
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
