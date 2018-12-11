package org.l2j.mmocore;

public class AsyncClientClosedConnection extends ReadablePacket<AsyncClient> {
    private final byte[] bytes;

    public AsyncClientClosedConnection(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    protected boolean read() {
        return true;
    }

    @Override
    public void run() {
        client.close(null);
        client.close(new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeBytes(bytes);
                return false;
            }
        });
    }
}
