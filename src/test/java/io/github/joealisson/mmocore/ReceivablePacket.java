package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;

public class ReceivablePacket extends ReadablePacket<AsyncClient> {

    @Override
    protected boolean read(ByteBuffer buffer) {
        return true;
    }

    @Override
    public void run() {

    }
}
