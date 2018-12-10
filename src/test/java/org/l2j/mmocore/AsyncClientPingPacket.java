package org.l2j.mmocore;

public class AsyncClientPingPacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write() {
        writeByte(0x01);
        writeLong(Long.MAX_VALUE);
        writeDouble(Double.MAX_VALUE);
        writeInt(Integer.MAX_VALUE);
        writeFloat(Float.MAX_VALUE);
        writeShort(Short.MAX_VALUE);
        writeByte(Byte.MAX_VALUE);
        writeString("Ping");
        writeSizedString("Packet");
        return true;
    }
}
