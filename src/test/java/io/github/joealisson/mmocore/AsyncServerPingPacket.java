/*
 * Copyright © 2019-2020 Async-mmocore
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
    private boolean trueByteBoolean;
    private boolean falseByteBoolean;
    private boolean trueShortBoolean;
    private boolean falseShortBoolean;
    private boolean trueIntBoolean;
    private boolean falseIntBoolean;

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
            trueByteBoolean = readBoolean();
            falseByteBoolean = readBoolean();
            trueShortBoolean = readShortAsBoolean();
            falseShortBoolean = readShortAsBoolean();
            trueIntBoolean = readIntAsBoolean();
            falseIntBoolean = readIntAsBoolean();
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
            Assert.assertTrue(trueByteBoolean);
            Assert.assertFalse(falseByteBoolean);
            Assert.assertTrue(trueShortBoolean);
            Assert.assertFalse(falseShortBoolean);
            Assert.assertTrue(trueIntBoolean);
            Assert.assertFalse(falseIntBoolean);
        } catch (Exception e) {
            CommunicationTest.shutdown(false);
        }

        client.sendPacket(new AsyncServerPongPacket());
    }
}