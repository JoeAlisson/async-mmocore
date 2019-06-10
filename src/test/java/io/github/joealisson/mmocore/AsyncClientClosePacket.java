package io.github.joealisson.mmocore;

public class AsyncClientClosePacket extends WritablePacket<AsyncClient> {
    @Override
    protected boolean write(AsyncClient client) {
        writeByte(0x03);
        return true;
    }
}
