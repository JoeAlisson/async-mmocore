package org.l2j.mmocore;

public class AsyncServerClosedConnection extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write() {
        writeByte(0x04);
        writeShort(0x01);
        writeInt(0x02);
        writeString(client.getHostAddress());
        return false;
    }
}
