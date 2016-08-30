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
}
