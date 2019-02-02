package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;

public class SendablePacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write(AsyncClient client, ByteBuffer buffer) {
        return true;
    }
}
