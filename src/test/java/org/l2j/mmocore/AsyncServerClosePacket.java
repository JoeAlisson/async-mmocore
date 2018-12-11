package org.l2j.mmocore;

public class AsyncServerClosePacket extends ReadablePacket<AsyncClient> {
    @Override
    protected boolean read() {
        return true;
    }

    @Override
    public void run() {
        client.close(new AsyncServerClosedConnection());
        CommunicationTest.shutdown(true);
    }
}
