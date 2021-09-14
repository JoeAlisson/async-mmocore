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

import io.github.joealisson.mmocore.util.Comparator;

/**
 * @author JoeAlisson
 */
public class AsyncClientFairnessReceivePacket extends ReadablePacket<AsyncClient> {

    private long varLong;
    private double varDouble;
    private float varFloat;
    private int varInt;
    private short varShort;
    private byte varByte;
    private String varString;
    private String varSizedString;

    @Override
    protected boolean read() {
        varLong = readLong();
        varDouble = readDouble();
        varInt = readInt();
        varFloat = readFloat();
        varShort = readShort();
        varByte = readByte();
        varString = readString();
        String empty = readString();
        varSizedString = readSizedString();
        return true;
    }

    @Override
    public void run() {
        try {
            Comparator.assertEquals(Long.MAX_VALUE, varLong);
            Comparator.assertEquals(Double.MAX_VALUE, varDouble);
            Comparator.assertEquals(Integer.MAX_VALUE, varInt);
            Comparator.assertEquals(Float.MAX_VALUE, varFloat);
            Comparator.assertEquals(Short.MAX_VALUE, varShort);
            Comparator.assertEquals(Byte.MAX_VALUE, varByte);
            Comparator.assertEquals("Ping", varString);
            Comparator.assertEquals("Packet", varSizedString);
            client.sendPacket(new AsyncClientFairnessRespondPacket());

        } catch (Exception e) {
            CommunicationTest.shutdown(false);
        }

    }
}
