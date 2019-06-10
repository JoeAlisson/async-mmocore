package io.github.joealisson.mmocore;

public class SendablePacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write(AsyncClient client) {
        return true;
    }
}
