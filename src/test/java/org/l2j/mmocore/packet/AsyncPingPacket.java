package org.l2j.mmocore.packet;

import org.l2j.mmocore.ReadablePacket;
import org.l2j.mmocore.async.AsyncClient;

public class AsyncPingPacket extends ReadablePacket<AsyncClient> {
    private long packetSize;


    @Override
    protected boolean read() {
        packetSize = readShort();

        while (availableData() > 8) {
            readLong();
        }

        while (availableData() > 4) {
            readInt();
        }

        while (availableData() > 2) {
            readShort();
        }

        while (availableData() > 1) {
            read();
        }
        return true;
    }

    @Override
    public void run() {
        client.sendPacket(new AsyncPongPacket(packetSize));
    }
}