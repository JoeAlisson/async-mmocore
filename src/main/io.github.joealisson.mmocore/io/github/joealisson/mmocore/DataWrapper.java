package io.github.joealisson.mmocore;

/**
 * This class is a data wrapper that supply a basic interface that facilitates the reading of the incoming data.
 */
public final class DataWrapper extends ReadablePacket<Object> {

    private DataWrapper(byte[] data) {
        this.data = data;
    }

    @Override
    protected boolean read() {
        return false;
    }

    /**
     * @throws UnsupportedOperationException - this method does nothing just throw
     */
    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    static DataWrapper wrap(byte[] data) {
        return new DataWrapper(data);
    }

    /**
     * get the next byte from the underlying data.
     *
     * @return the next byte
     */
    public  byte get() {
        return readByte();
    }

    /**
     * get the next short from the underlying data
     *
     * @return the next short
     */
    public short getShort() {
        return readShort();
    }

    /**
     * get the next integer from the underlying data.
     *
     * @return the next integer
     */
    public int getInt() {
        return readInt();
    }

    /**
     *
     * @return the length in bytes of the remaining data.
     */
    public int available() {
        return availableData();
    }

    /**
     *
     * @return the underlying data
     */
    public byte[] expose() {
        return data;
    }
}
