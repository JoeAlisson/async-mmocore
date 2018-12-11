package io.github.joealisson.mmocore;

public class SendablePacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write() {
        return true;
    }
}
