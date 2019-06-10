package io.github.joealisson.mmocore;

public class AsyncServerClosedConnection extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write(AsyncClient client) {
        writeByte(0x04);
        writeShort(0x01);
        writeInt(0x02);
        writeString(client.getHostAddress());
        writeBytes(new byte[20]);
        return true;
    }
}
