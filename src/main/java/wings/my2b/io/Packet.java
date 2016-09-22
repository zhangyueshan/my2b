package wings.my2b.io;

import wings.my2b.StringUtils;
import wings.my2b.exception.My2bException;

import java.io.UnsupportedEncodingException;

/**
 * Created by InThEnd on 2016/8/29.
 * Data packet.
 */
public class Packet {

    //data
    private byte[] byteBuffer;

    //packet length
    private int bufLength = 0;

    //read position
    private int position = 0;

    //constructor
    public Packet(byte[] buf) {
        this.byteBuffer = buf;
        this.bufLength = buf.length;
    }

    public Packet(int size) {
        this.byteBuffer = new byte[size];
        setBufLength(this.byteBuffer.length);
        this.position = 4;
    }

    public byte[] getByteBuffer() {
        return byteBuffer;
    }

    public int getBufLength() {
        return bufLength;
    }

    public void setBufLength(int bufLength) {
        this.bufLength = bufLength;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    final byte readByte() {
        return this.byteBuffer[this.position++];
    }

    final byte readByte(int readAt) {
        return this.byteBuffer[readAt];
    }


    /**
     * Read string[NUL]
     */
    String readNullEndString(String encoding) {
        int i = this.position;
        int len = 0;
        int maxLen = getBufLength();

        while ((i < maxLen) && (this.byteBuffer[i] != 0)) {
            len++;
            i++;
        }
        try {
            return StringUtils.toString(this.byteBuffer, this.position, len, encoding);
        } catch (UnsupportedEncodingException uEE) {
            throw new My2bException(uEE);
        } finally {
            this.position += (len + 1); // update cursor
        }
    }

    /**
     * Read string[$len]
     */
    String readLengthExpectedString(String encoding, int expectedLength) {
        if (this.position + expectedLength > getBufLength()) {
            throw new My2bException("长度不够。");
        }
        try {
            return StringUtils.toString(this.byteBuffer, this.position, expectedLength, encoding);
        } catch (UnsupportedEncodingException uEE) {
            throw new My2bException(uEE);
        } finally {
            this.position += expectedLength; // update cursor
        }
    }

    int read2Int() {
        byte[] b = this.byteBuffer; // a little bit optimization
        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8);
    }

    int read3Int() {
        byte[] b = this.byteBuffer;
        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8) | ((b[this.position++] & 0xff) << 16);
    }

    long read4Int() {
        byte[] b = this.byteBuffer;
        return ((long) b[this.position++] & 0xff) | (((long) b[this.position++] & 0xff) << 8) | ((long) (b[this.position++] & 0xff) << 16)
                | ((long) (b[this.position++] & 0xff) << 24);
    }

    long read8Int() {
        byte[] b = this.byteBuffer;
        return (b[this.position++] & 0xff) | ((long) (b[this.position++] & 0xff) << 8) | ((long) (b[this.position++] & 0xff) << 16)
                | ((long) (b[this.position++] & 0xff) << 24) | ((long) (b[this.position++] & 0xff) << 32) | ((long) (b[this.position++] & 0xff) << 40)
                | ((long) (b[this.position++] & 0xff) << 48) | ((long) (b[this.position++] & 0xff) << 56);
    }

    void jump(int len) {
        position += len;
    }


    byte[] readBytes(int len) {
        byte[] dest = new byte[len];
        System.arraycopy(this.byteBuffer, position, dest, 0, len);
        position += len;
        return dest;
    }


    final void write4Int(long i) {
        ensureCapacity(4);

        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
        b[this.position++] = (byte) (i >>> 24);
    }

    final void write3Int(int i) {
        ensureCapacity(3);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
    }

    final void write8Int(long i) {
        ensureCapacity(8);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
        b[this.position++] = (byte) (i >>> 24);
        b[this.position++] = (byte) (i >>> 32);
        b[this.position++] = (byte) (i >>> 40);
        b[this.position++] = (byte) (i >>> 48);
        b[this.position++] = (byte) (i >>> 56);
    }

    final void writeByte(byte b) {
        ensureCapacity(1);
        this.byteBuffer[this.position++] = b;
    }

    //	 Write null-terminated string in the given encoding
    final void writeString(String s, String encoding) {
        ensureCapacity((s.length() * 3) + 1);
        try {
            writeStringNoNull(s, encoding);
        } catch (UnsupportedEncodingException ue) {
            throw new My2bException("E");
        }

        this.byteBuffer[this.position++] = 0;
    }

    // Write a String using the specified character encoding
    final void writeStringNoNull(String s, String encoding)
            throws UnsupportedEncodingException {
        byte[] b = s.getBytes(encoding);
        int len = b.length;
        ensureCapacity(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    // Write a byte array
    public final void writeBytes(byte[] bytes) {
        int len = bytes.length;
        ensureCapacity(len);
        System.arraycopy(bytes, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }


    private void ensureCapacity(int additionalData) {
        if ((this.position + additionalData) > getBufLength()) {
            if ((this.position + additionalData) < this.byteBuffer.length) {
                // byteBuffer.length is != getBufLength() all of the time due to re-using of packets (we don't shrink them)
                //
                // If we can, don't re-alloc, just set buffer length to size of current buffer
                setBufLength(this.byteBuffer.length);
            } else {
                //
                // Otherwise, re-size, and pad so we can avoid allocing again in the near future
                //
                int newLength = (int) (this.byteBuffer.length * 1.25);

                if (newLength < (this.byteBuffer.length + additionalData)) {
                    newLength = this.byteBuffer.length + (int) (additionalData * 1.25);
                }

                if (newLength < this.byteBuffer.length) {
                    newLength = this.byteBuffer.length + additionalData;
                }

                byte[] newBytes = new byte[newLength];

                System.arraycopy(this.byteBuffer, 0, newBytes, 0, this.byteBuffer.length);
                this.byteBuffer = newBytes;
                setBufLength(this.byteBuffer.length);
            }
        }
    }

    public String toHexString() {
        byte[] bytes1 = new byte[position];
        System.arraycopy(this.byteBuffer, 0, bytes1, 0, position);
        return StringUtils.hexString(bytes1);
    }


}
