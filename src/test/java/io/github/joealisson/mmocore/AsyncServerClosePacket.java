package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;

public class AsyncServerClosePacket extends ReadablePacket<AsyncClient> {

    @Override
    protected boolean read(ByteBuffer buffer) {
        return true;
    }

    @Override
    public void run() {
        client.sendPacket(new AsyncServerClosedConnection());
    }
}
