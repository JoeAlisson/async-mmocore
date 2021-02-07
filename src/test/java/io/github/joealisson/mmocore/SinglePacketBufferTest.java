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

import io.github.joealisson.mmocore.internal.SinglePacketBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author JoeAlisson
 */
public class SinglePacketBufferTest {

    @Test
    public void testIntegrity() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put((byte) 1);
        buffer.put(new byte[] {2, 3, 4, 5});
        buffer.putShort((short) 6);
        buffer.putChar('A');
        buffer.putInt(7);
        buffer.putFloat(8.5f);
        buffer.putLong(9);
        buffer.putDouble(10.5);

        SinglePacketBuffer packetBuffer = new SinglePacketBuffer(buffer.flip());

        Assert.assertEquals(1, packetBuffer.readByte());
        byte[] data = new byte[4];
        packetBuffer.readBytes(data);
        Assert.assertEquals(2, data[0]);
        Assert.assertEquals(3, data[1]);
        Assert.assertEquals(4, data[2]);
        Assert.assertEquals(5, data[3]);
        Assert.assertEquals(6, packetBuffer.readShort());
        Assert.assertEquals('A', packetBuffer.readChar());
        Assert.assertEquals(7, packetBuffer.readInt());
        Assert.assertEquals(8.5f, packetBuffer.readFloat(), 0);
        Assert.assertEquals(9, packetBuffer.readLong());
        Assert.assertEquals(10.5, packetBuffer.readDouble(), 0);


        packetBuffer.writeByte(0, (byte) 2);
        packetBuffer.writeShort(5, (short) 8);
        packetBuffer.writeInt(9, 9);

        Assert.assertEquals(2, packetBuffer.readByte(0));
        Assert.assertEquals(8, packetBuffer.readShort(5));
        Assert.assertEquals(9, packetBuffer.readInt(9));
    }

    @Test
    public void testLimits() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        SinglePacketBuffer packetBuffer = new SinglePacketBuffer(buffer);

        Assert.assertEquals(100, packetBuffer.limit());

        packetBuffer.limit(50);
        Assert.assertEquals(50, packetBuffer.limit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutOfLimits(){
        ByteBuffer buffer = ByteBuffer.allocate(100);
        SinglePacketBuffer packetBuffer = new SinglePacketBuffer(buffer);
        packetBuffer.limit(150);
    }

}