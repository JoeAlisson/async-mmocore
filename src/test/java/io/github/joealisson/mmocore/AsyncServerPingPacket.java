package io.github.joealisson.mmocore;

import org.junit.Assert;

public class AsyncServerPingPacket extends ReadablePacket<AsyncClient> {

    private long varLong;
    private double varDouble;
    private float varFloat;
    private int varInt;
    private short varShort;
    private byte varByte;
    private String varString;
    private String varSizedString;
    private String emptyString;
    private String emptySizedString;

    @Override
    protected boolean read() {
        if(available() > 0) {
            varLong = readLong();
            varDouble = readDouble();
            varInt = readInt();
            varFloat = readFloat();
            varShort = readShort();
            varByte = readByte();
            varString = readString();
            emptyString = readString();
            varSizedString = readSizedString();
            emptySizedString = readSizedString();
            readBoolean();
            readShortAsBoolean();
            readIntAsBoolean();
        }
        return true;
    }

    @Override
    public void run() {
        try {
            Assert.assertEquals(Long.MAX_VALUE, varLong);
            Assert.assertEquals(Double.MAX_VALUE, varDouble, 0);
            Assert.assertEquals(Integer.MAX_VALUE, varInt);
            Assert.assertEquals(Float.MAX_VALUE, varFloat, 0);
            Assert.assertEquals(Short.MAX_VALUE, varShort);
            Assert.assertEquals(Byte.MAX_VALUE, varByte);
            Assert.assertEquals("Ping", varString);
            Assert.assertEquals("", emptyString);
            Assert.assertEquals("Packet", varSizedString);
            Assert.assertEquals("", emptySizedString);
        } catch (Exception e) {
            CommunicationTest.shutdown(false);
        }

        client.sendPacket(new AsyncServerPongPacket());
    }
}