package io.github.joealisson.mmocore;

public class AsyncServerClosePacket extends ReadablePacket<AsyncClient> {

    @Override
    protected boolean read() {
        return true;
    }

    @Override
    public void run() {
        client.sendPacket(new AsyncServerClosedConnection());
    }
}
