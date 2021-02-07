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

import java.nio.charset.StandardCharsets;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public abstract class WritableBuffer implements Buffer {

    /**
     * Write a <B>byte</B> to the buffer. <BR>
     * 8bit integer (00)
     *
     * If the underlying data array can't hold a new byte its size is increased 20%
     *
     * @param value to be written
     */
    public abstract void writeByte(final byte value);

    /**
     * Write a int to the buffer, the int is casted to a byte;
     *
     * @param value to be written
     */
     public void writeByte(final int value) {
        writeByte((byte) value);
    }

    /**
     * Write <B>boolean</B> to the buffer. <BR>
     *  If the value is true so write a <B>byte</B> with value 1, otherwise 0
     *  8bit integer (00)
     * @param value to be written
     */
    public void writeByte(final boolean value) {
        writeByte((byte) (value ? 0x01 : 0x00));
    }

    /**
     * Write <B>byte[]</B> to the buffer. <BR>
     * 8bit integer array (00 ...)
     * @param value to be written
     */
    public abstract void writeBytes(final byte... value);

    /**
     * Write <B>short</B> to the buffer. <BR>
     * 16bit integer (00 00)
     * @param value to be written
     */
    public abstract void writeShort(final short value);

    /**
     * Write <B>short</B> to the buffer. <BR>
     * 16bit integer (00 00)
     * @param value to be written
     */
    public void writeShort(final int value) {
        writeShort((short) value);
    }

    /**
     * Write <B>boolean</B> to the buffer. <BR>
     * If the value is true so write a <B>byte</B> with value 1, otherwise 0
     *  16bit integer (00 00)
     * @param value to be written
     */
    public void writeShort(final boolean value) {
        writeShort((short) (value ? 0x01 : 0x00));
    }

    /**
     * Write <B>char</B> to the buffer.<BR>
     * 16 bit char
     *
     * @param value the char to be put on data.
     */
    public abstract void writeChar(final char value);

    /**
     * Write <B>int</B> to the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @param value to be written
     */
    public abstract void writeInt(final int value);

    /**
     * Write <B>boolean</B> to the buffer. <BR>
     * If the value is true so write a <B>byte</B> with value 1, otherwise 0
     *  32bit integer (00 00 00 00)
     * @param value to be written
     */
    public void writeInt(final boolean value) {
        writeInt(value ? 0x01 : 0x00);
    }

    /**
     * Write <B>float</B> to the buffer. <BR>
     *  32bit float point number (00 00 00 00)
     * @param value to be written
     */
    public abstract void writeFloat(final float value);

    /**
     * Write <B>long</B> to the buffer. <BR>
     * 64bit integer (00 00 00 00 00 00 00 00)
     * @param value to be written
     */
    public abstract void writeLong(final long value);

    /**
     * Write <B>double</B> to the buffer. <BR>
     * 64bit double precision float (00 00 00 00 00 00 00 00)
     * @param value to be written
     */
    public abstract void writeDouble(final double value);

    /**
     * Write a <B>String</B> to the buffer with a null termination (\000).
     * Each character is a 16bit char
     *
     * @param text to be written
     */
    public void writeString(final CharSequence text) {
        if(isNull(text)) {
            writeChar('\000');
            return;
        }
        writeStringWithCharset(text);
        writeChar('\000');
    }

    private void writeStringWithCharset(CharSequence text) {
        writeBytes(text.toString().getBytes(StandardCharsets.UTF_16LE));
    }

    /**
     * Write <B>String</B> to the buffer preceded by a <B>short</B> 16 bit with String length and no null termination.
     * Each character is a 16bit char.
     *
     * @param text to be written
     */
    public void writeSizedString(final CharSequence text) {
        if(nonNull(text) && text.length() > 0) {
            writeShort(text.length());
            writeStringWithCharset(text);
        } else {
            writeShort(0);
        }
    }

}
