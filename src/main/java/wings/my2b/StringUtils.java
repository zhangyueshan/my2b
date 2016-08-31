package wings.my2b;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by InThEnd on 2016/8/30.
 * String utils
 */
public class StringUtils {

    public static String toString(byte[] value, int offset, int length, String encoding) throws UnsupportedEncodingException {
        Charset cs = Charset.forName(encoding);
        return cs.decode(ByteBuffer.wrap(value, offset, length)).toString();
    }

    public static String toString(byte[] value, String encoding) throws UnsupportedEncodingException {
        Charset cs = Charset.forName(encoding);
        return cs.decode(ByteBuffer.wrap(value)).toString();
    }

    public static String hexString(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return hex;
    }

    public static String hexString(byte[] b) {
        String hex = "";
        for (byte a : b) {
            String hex1 = Integer.toHexString(a & 0xFF);
            if (hex1.length() == 1) {
                hex1 = '0' + hex1;
            }
            hex = hex + hex1;
        }
        return hex;
    }

    public static byte[] intToByteArray(final int integer) {
        int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
        byte[] byteArray = new byte[4];

        for (int n = 0; n < byteNum; n++)
            byteArray[3 - n] = (byte) (integer >>> (n * 8));

        return (byteArray);
    }
}
