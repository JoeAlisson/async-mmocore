package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;

public class AsyncClientClosePacket extends WritablePacket<AsyncClient> {
    @Override
    protected boolean write(AsyncClient client, ByteBuffer buffer) {
        buffer.put((byte)0x03);
        return true;
    }
}
