package io.github.joealisson.mmocore;

import org.junit.Assert;

import java.nio.ByteBuffer;

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
    protected boolean read(ByteBuffer buffer) {
        varLong = buffer.getLong();
        varDouble = buffer.getDouble();
        varInt = buffer.getInt();
        varFloat = buffer.getFloat();
        varShort = buffer.getShort();
        varByte = buffer.get();
        varString = readString(buffer);
        emptyString = readString(buffer);
        varSizedString = readSizedString(buffer);
        emptySizedString = readSizedString(buffer);
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