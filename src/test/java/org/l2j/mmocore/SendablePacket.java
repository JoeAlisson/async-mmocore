package org.l2j.mmocore;

public class SendablePacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write() {
        return true;
    }
}
