package io.github.joealisson.mmocore;

import io.github.joealisson.mmocore.internal.DynamicPacketBuffer;
import io.github.joealisson.mmocore.internal.WritableBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class DynamicPacketBufferTest {

    @Test
    public void testIncrease() {
        ConnectionConfig<?> config = new ConnectionConfig<>(null, null, null);
        config.newBufferGroup(4, 32);
        ResourcePool resourcePool = ResourcePool.initialize(config);
        DynamicPacketBuffer packetBuffer = new DynamicPacketBuffer(ByteBuffer.allocate(32), resourcePool);
        for (int i = 0; i < 16; i++) {
            packetBuffer.writeBytes(new byte[34 + i * 64]);
        }
    }

    @Test
    public void testIntegrity() {
        ConnectionConfig<?> config = new ConnectionConfig<>(null, null, null);
        config.newBufferGroup(4, 32);
        ResourcePool resourcePool = ResourcePool.initialize(config);
        WritableBuffer packetBuffer = WritableBuffer.dynamicOf(ByteBuffer.allocate(32), resourcePool);

        packetBuffer.writeByte((byte) 1);
        packetBuffer.writeShort((short) 2);
        packetBuffer.writeChar('A');
        packetBuffer.writeInt(3);
        packetBuffer.writeFloat(4);
        packetBuffer.writeLong(5);
        packetBuffer.writeDouble(6);

        Assert.assertEquals(1, packetBuffer.readByte(0));
        Assert.assertEquals(2, packetBuffer.readShort(1));
        Assert.assertEquals(3, packetBuffer.readInt(5));

        packetBuffer.writeByte(4, (byte) 5);
        packetBuffer.writeShort(10, (short) 6);
        packetBuffer.writeInt(0,  40);

        Assert.assertEquals(5, packetBuffer.readByte(4));
        Assert.assertEquals(6, packetBuffer.readShort(10));
        Assert.assertEquals(40, packetBuffer.readInt(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testNegativeIndex() {
        ConnectionConfig<?> config = new ConnectionConfig<>(null, null, null);
        ResourcePool resourcePool = ResourcePool.initialize(config);
        DynamicPacketBuffer packetBuffer = new DynamicPacketBuffer(ByteBuffer.allocate(32), resourcePool);
        packetBuffer.writeInt(-1, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOutOfBoundIndex() {
        ConnectionConfig<?> config = new ConnectionConfig<>(null, null, null);
        ResourcePool resourcePool = ResourcePool.initialize(config);
        DynamicPacketBuffer packetBuffer = new DynamicPacketBuffer(ByteBuffer.allocate(2), resourcePool);
        packetBuffer.writeInt(100, 10);
    }

    @Test
    public void testBufferLimitWriting() {
        ConnectionConfig<?> config = new ConnectionConfig<>(null, null, null);
        ResourcePool resourcePool = ResourcePool.initialize(config);
        DynamicPacketBuffer packetBuffer = new DynamicPacketBuffer(ByteBuffer.allocate(2), resourcePool);

        packetBuffer.writeInt(1000000);

        int shortLimit = packetBuffer.limit() - 1;
        packetBuffer.position(shortLimit);
        packetBuffer.writeShort((short) 20000);

        int longLimit = packetBuffer.limit() - 1;
        packetBuffer.position(longLimit);
        packetBuffer.writeLong(10000);

        Assert.assertEquals(1000000, packetBuffer.readInt(0));
        Assert.assertEquals(20000, packetBuffer.readShort(shortLimit));
        Assert.assertEquals(10000, packetBuffer.readInt(longLimit));
    }

    @Test
    public void testBufferLimit() {
        ConnectionConfig<?> config = new ConnectionConfig<>(null, null, null);
        ResourcePool resourcePool = ResourcePool.initialize(config);
        DynamicPacketBuffer packetBuffer = new DynamicPacketBuffer(ByteBuffer.allocate(32), resourcePool);

        packetBuffer.writeBytes(new byte[10]);
        int pos = packetBuffer.position();
        packetBuffer.mark();
        Assert.assertEquals(pos, packetBuffer.limit());

        packetBuffer.limit(64);
        Assert.assertTrue(packetBuffer.capacity() >= 64);
    }

}