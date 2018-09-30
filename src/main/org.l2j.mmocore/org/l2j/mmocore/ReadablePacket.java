/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package org.l2j.mmocore;

import java.nio.charset.Charset;

import static java.lang.Byte.toUnsignedInt;
import static java.lang.Byte.toUnsignedLong;
import static java.lang.Double.longBitsToDouble;

/**
 * This class represents a Packet received from the client.
 *
 * All data received must have a header with 2 bytes and an optional payload.
 *
 * The first and second bytes is a 16 bits integer holding the size of the packet.
 *
 */
public abstract class ReadablePacket<T> extends AbstractPacket<T> implements Runnable {

	protected ReadablePacket() { }

	protected final int availableData() {
	    return data.length - dataIndex;
    }

	/**
	 *
	 * Reads as many bytes as the length of the array.
	 * @param dst : the byte array which will be filled with the data.
	 */
	protected final void readBytes(final byte[] dst) {
	    readBytes(dst,0, dst.length);
	}
	
	/**
	 *
	 * Reads as many bytes as the given length (len). Starts to fill the
	 * byte array from the given offset to <B>offset</B> + <B>len</B>.
	 * @param dst : the byte array which will be filled with the data.
	 * @param offset : starts to fill the byte array from the given offset.
	 * @param length : the given length of bytes to be read.
	 */
	protected final void readBytes(final byte[] dst, final int offset, final int length) {
		System.arraycopy(data, dataIndex, dst, offset, length);
	    dataIndex += length;
	}

    /**
     * Reads raw <B>byte</B> from the buffer
     * @return byte read
     */
	protected final byte readByte() {
	    return data[dataIndex++];
    }


    /**
     *  Reads <B>char</B> from the buffer
     * @return char read
     */
	protected final char readChar() {
	    return convertEndian((char) readShort());
    }

    /**
     * Reads <B>byte</B> from the buffer. <BR>
     * 8bit integer (00)
     * @return unsigned int read
     */
	protected final int readUnsignedByte() {
		return toUnsignedInt(data[dataIndex++]);
	}
	
	/**
	 * Reads <B>short</B> from the buffer. <BR>
	 * 16bit integer (00 00)
	 * @return short read
	 */
	protected final short readShort()  {
		return convertEndian((short) (readUnsignedByte() << pickShift(8, 0) |
                                      readUnsignedByte() << pickShift(8, 8)));
	}
	
	/**
	 * Reads <B>int</B> from the buffer. <BR>
	 * 32bit integer (00 00 00 00)
	 * @return int read
	 */
	protected final int readInt() {
        return convertEndian(readUnsignedByte() << pickShift(24, 0)  |
                                readUnsignedByte() << pickShift(24, 8)  |
                                readUnsignedByte() << pickShift(24, 16) |
                                readUnsignedByte() << pickShift(24, 24) );

	}
	
	/**
	 * Reads <B>long</B> from the buffer. <BR>
	 * 64bit integer (00 00 00 00 00 00 00 00)
	 * @return long read
	 */
	protected final long readLong() {
		return convertEndian(toUnsignedLong(readByte()) << pickShift(56, 0)  |
                                toUnsignedLong(readByte()) << pickShift(56, 8)  |
                                toUnsignedLong(readByte()) << pickShift(56, 16) |
                                toUnsignedLong(readByte()) << pickShift(56, 24) |
                                toUnsignedLong(readByte()) << pickShift(56, 32) |
                                toUnsignedLong(readByte()) << pickShift(56,40)  |
                                toUnsignedLong(readByte()) << pickShift(56, 48) |
                                toUnsignedLong(readByte()) << pickShift(56, 56) );
	}
	
	/**
	 * Reads <B>double</B> from the buffer. <BR>
	 * 64bit double precision float (00 00 00 00 00 00 00 00)
	 * @return double read
	 */
	protected final double readDouble() {
	    return longBitsToDouble(readLong());
	}
	
	/**
	 * Reads <B>String</B> from the buffer.
	 * @return String read
	 */
	protected final String readString()  {
	    int start = dataIndex;
	    int size = 0;

	    while (dataIndex < data.length &&  readChar() != '\000'){
			size += 2;
        }
	    return new String(data, start, size, Charset.forName("UTF-16LE"));
	}

    private static int pickShift(int top, int pos) { return isBigEndian ? top - pos : pos; }

    protected abstract boolean read();

    @Override
    public abstract void run();
}