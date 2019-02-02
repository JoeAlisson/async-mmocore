package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;

public class AsyncServerClosedConnection extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write(AsyncClient client, ByteBuffer buffer) {
        buffer.put((byte)0x04);
        buffer.putShort((short)0x01);
        buffer.putInt(0x02);
        writeString(client.getHostAddress(), buffer);
        buffer.put(new byte[20]);
        return true;
    }
}
