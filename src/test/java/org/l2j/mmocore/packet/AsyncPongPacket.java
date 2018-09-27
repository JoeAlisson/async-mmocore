package org.l2j.mmocore.packet;

import org.l2j.mmocore.WritablePacket;
import org.l2j.mmocore.async.AsyncClient;

public class AsyncPongPacket extends WritablePacket<AsyncClient> {

    private long packetSize;

    public AsyncPongPacket(long packetSize) {
        this.packetSize = packetSize;
    }

    @Override
    protected void write() {
        while(packetSize >= 8) {
            writeLong(Long.MAX_VALUE);
            packetSize -=  8;
        }

        while (packetSize >= 4) {
            writeInt(Integer.MAX_VALUE);
            packetSize -= 4;
        }

        while (packetSize >= 2) {
            writeShort(Integer.MAX_VALUE);
            packetSize -= 2;
        }

        while (packetSize >= 1) {
            writeByte(Byte.MAX_VALUE);
            packetSize--;
        }

    }
}
