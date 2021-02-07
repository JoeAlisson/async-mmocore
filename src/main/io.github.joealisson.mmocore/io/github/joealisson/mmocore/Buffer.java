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

/**
 * @author JoeAlisson
 */
public interface Buffer {

    /**
     * Read <B>byte</B> from the buffer at index. <BR>
     * 8bit integer (00)
     *
     * @param index index to read from
     * @return the byte value at the index.
     */
    byte readByte(int index);

    /**
     * Write <B>byte</B> to the buffer at index. <BR>
     * 8bit integer (00)
     *
     * @param index index to write to
     * @param value to be written
     */
    void writeByte(int index, byte value);

    /**
     * Read <B>short</B> from the buffer at index. <BR>
     * 16bit integer (00 00)
     *
     * @param index index to read from
     * @return the short value at index.
     */
    short readShort(int index);

    /**
     * Write <B>int</B> to the buffer at index. <BR>
     * 16bit integer (00 00)
     *
     * @param index index to write to
     * @param value to be written
     */
    void writeShort(int index, short value);

    /**
     * Read <B>int</B> from the buffer at index. <BR>
     * 32bit integer (00 00 00 00)
     *
     * @param index index to read from
     * @return the int value at index.
     */
    int readInt(int index);

    /**
     * Write <B>int</B> to the buffer at index. <BR>
     * 32bit integer (00 00 00 00)
     *
     * @param index index to write to
     * @param value to be written
     */
    void writeInt(int index, int value);

    /**
     * @return the buffer's limit
     */
    int limit();

    /**
     * Change the buffer's limit
     *
     * @param newLimit the new limit
     */
    void limit(int newLimit);

}
