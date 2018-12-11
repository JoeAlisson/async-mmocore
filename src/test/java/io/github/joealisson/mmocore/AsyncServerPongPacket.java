package io.github.joealisson.mmocore;

public class AsyncServerPongPacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write() {
        writeByte(0x02);
        writeLong(Long.MAX_VALUE);
        writeDouble(Double.MAX_VALUE);
        writeInt(Integer.MAX_VALUE);
        writeFloat(Float.MAX_VALUE);
        writeShort(Short.MAX_VALUE);
        writeByte(Byte.MAX_VALUE);
        writeString("Pong");
        writeSizedString("Packet");
        return true;
    }
}
