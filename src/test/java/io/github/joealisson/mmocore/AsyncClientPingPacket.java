package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;

public class AsyncClientPingPacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write(AsyncClient client, ByteBuffer buffer) {
        buffer.put((byte) 0x01);
        buffer.putLong(Long.MAX_VALUE);
        buffer.putDouble(Double.MAX_VALUE);
        buffer.putInt(Integer.MAX_VALUE);
        buffer.putFloat(Float.MAX_VALUE);
        buffer.putShort((short)Short.MAX_VALUE);
        buffer.put((byte)Byte.MAX_VALUE);
        writeString("Ping", buffer);
        writeString(null, buffer);
        writeSizedString("Packet", buffer);
        writeSizedString(null, buffer);
        return true;
    }
}
