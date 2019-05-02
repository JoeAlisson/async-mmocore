package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;

public class AsyncClientClosedConnection extends ReadablePacket<AsyncClient> {
    private final byte[] bytes;

    public AsyncClientClosedConnection(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    protected boolean read(ByteBuffer buffer) {
        return true;
    }

    @Override
    public void run() {
        client.close(new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write(AsyncClient client, ByteBuffer buffer) {
                buffer.put(bytes);
                return false;
            }
        });
        client.close();
        CommunicationTest.shutdown(true);
    }
}
