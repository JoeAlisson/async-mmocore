package io.github.joealisson.mmocore;

public class AsyncClientPingPacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write(AsyncClient client) {
        writeByte(0x01);
        writeLong(Long.MAX_VALUE);
        writeDouble(Double.MAX_VALUE);
        writeInt(Integer.MAX_VALUE);
        writeFloat(Float.MAX_VALUE);
        writeShort(Short.MAX_VALUE);
        writeByte(Byte.MAX_VALUE);
        writeString("Ping");
        writeString(null);
        writeSizedString("Packet");
        writeSizedString(null);
        return true;
    }
}
