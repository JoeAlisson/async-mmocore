package org.l2j.mmocore.async;

import org.l2j.mmocore.Client;
import org.l2j.mmocore.Connection;
import org.l2j.mmocore.WritablePacket;

public class AsyncClient extends Client<Connection<AsyncClient>> {

    AsyncClient(Connection<AsyncClient> connection) {
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
        writePacket(packet);
    }
}
