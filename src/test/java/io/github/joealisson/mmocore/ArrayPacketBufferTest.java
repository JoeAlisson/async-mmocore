package io.github.joealisson.mmocore;

import io.github.joealisson.mmocore.internal.ArrayPacketBuffer;
import io.github.joealisson.mmocore.internal.WritableBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class ArrayPacketBufferTest {

    @Test
    public void testIntegrity() {
        ConnectionConfig<?> config = new ConnectionConfig<>(null, null, null);
        ResourcePool resourcePool = ResourcePool.initialize(config);
        ArrayPacketBuffer buffer = new ArrayPacketBuffer(10, resourcePool);

        buffer.writeByte((byte) 1);
        buffer.writeBytes(new byte[] {2, 3, 4, 5});
        buffer.writeShort((short) 6);
        buffer.writeChar('A');
        buffer.writeInt(7);
        buffer.writeFloat(8.5f);
        buffer.writeLong(9);
        buffer.writeDouble(10.5);

        buffer.mark();
        int position = buffer.position();
        Assert.assertEquals(33, position);
        Assert.assertEquals(position, buffer.limit());

        buffer.position(0);
        Assert.assertEquals(position, buffer.remaining());

        Assert.assertEquals(1, buffer.readByte());
        byte[] data = new byte[4];
        buffer.readBytes(data);
        Assert.assertEquals(2, data[0]);
        Assert.assertEquals(3, data[1]);
        Assert.assertEquals(4, data[2]);
        Assert.assertEquals(5, data[3]);
        Assert.assertEquals(6, buffer.readShort());
        Assert.assertEquals('A', buffer.readChar());
        Assert.assertEquals(7, buffer.readInt());
        Assert.assertEquals(8.5f, buffer.readFloat(), 0);
        Assert.assertEquals(9, buffer.readLong());
        Assert.assertEquals(10.5, buffer.readDouble(), 0);

        Assert.assertEquals(1, buffer.readByte(0));
        Assert.assertEquals(6, buffer.readShort(5));
        Assert.assertEquals(7, buffer.readInt(9));

        int limit = buffer.limit();
        buffer.limit(limit * 2);

        ByteBuffer[] byteBuffers = buffer.toByteBuffers();

        Assert.assertEquals(1,byteBuffers.length);
        ByteBuffer byteBuffer = byteBuffers[0];

        Assert.assertEquals(limit*2, byteBuffer.remaining());

    }

    @Test
    public void testReleaseResources() {
        ConnectionConfig<?> config = new ConnectionConfig<>(null, null, null);
        ResourcePool resourcePool = ResourcePool.initialize(config);
        WritableBuffer buffer = WritableBuffer.of(10, resourcePool);

        buffer.writeShort((short) 10);
        buffer.mark();
        Assert.assertEquals(buffer.position(), buffer.limit());

        buffer.releaseResources();
        Assert.assertEquals(0, buffer.position());
        Assert.assertEquals(10, buffer.limit());
    }

}