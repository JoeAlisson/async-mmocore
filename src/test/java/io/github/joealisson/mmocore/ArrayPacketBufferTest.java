/*
 * Copyright © 2019-2021 Async-mmocore
 *
 * This file is part of the Async-mmocore project.
 *
 * Async-mmocore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Async-mmocore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.joealisson.mmocore;

import io.github.joealisson.mmocore.internal.ArrayPacketBuffer;
import io.github.joealisson.mmocore.internal.InternalWritableBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author JoeAlisson
 */
public class ArrayPacketBufferTest {

    @Test
    public void testIntegrity() {
        ConnectionConfig config = new ConnectionConfig(null);
        config.complete();
        ArrayPacketBuffer buffer = new ArrayPacketBuffer(10, config.resourcePool);

        buffer.writeByte((byte) 1);
        buffer.writeBytes(new byte[] {2, 3, 4, 5});
        buffer.writeShort((short) 6);
        buffer.writeChar('A');
        buffer.writeInt(7);
        buffer.writeFloat(8.5f);
        buffer.writeLong(9);
        buffer.writeDouble(10.5);
        buffer.writeBytes(null);

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
        ConnectionConfig config = new ConnectionConfig(null);
        InternalWritableBuffer buffer = InternalWritableBuffer.of(config.resourcePool);

        buffer.writeShort((short) 10);
        buffer.mark();
        Assert.assertEquals(buffer.position(), buffer.limit());

        buffer.releaseResources();
        Assert.assertEquals(0, buffer.position());
        Assert.assertEquals(config.resourcePool.getSegmentSize(), buffer.limit());
    }

}