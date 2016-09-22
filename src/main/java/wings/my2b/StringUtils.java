package wings.my2b;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    // Right from Monty's code
    public static String newCrypt(String password, String seed, String encoding) {
        byte b;
        double d;

        if ((password == null) || (password.length() == 0)) {
            return password;
        }

        long[] pw = newHash(seed.getBytes());
        long[] msg = hashPre41Password(password, encoding);
        long max = 0x3fffffffL;
        long seed1 = (pw[0] ^ msg[0]) % max;
        long seed2 = (pw[1] ^ msg[1]) % max;
        char[] chars = new char[seed.length()];

        for (int i = 0; i < seed.length(); i++) {
            seed1 = ((seed1 * 3) + seed2) % max;
            seed2 = (seed1 + seed2 + 33) % max;
            d = (double) seed1 / (double) max;
            b = (byte) java.lang.Math.floor((d * 31) + 64);
            chars[i] = (char) b;
        }

        seed1 = ((seed1 * 3) + seed2) % max;
        seed2 = (seed1 + seed2 + 33) % max;
        d = (double) seed1 / (double) max;
        b = (byte) java.lang.Math.floor(d * 31);

        for (int i = 0; i < seed.length(); i++) {
            chars[i] ^= (char) b;
        }

        return new String(chars);
    }

    public static long[] hashPre41Password(String password, String encoding) {
        // remove white spaces and convert to bytes
        try {
            return newHash(password.replaceAll("\\s", "").getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            return new long[0];
        }
    }

    static long[] newHash(byte[] password) {
        long nr = 1345345333L;
        long add = 7;
        long nr2 = 0x12345671L;
        long tmp;

        for (byte b : password) {
            tmp = 0xff & b;
            nr ^= ((((nr & 63) + add) * tmp) + (nr << 8));
            nr2 += ((nr2 << 8) ^ nr);
            add += tmp;
        }

        long[] result = new long[2];
        result[0] = nr & 0x7fffffffL;
        result[1] = nr2 & 0x7fffffffL;

        return result;
    }

    public static byte[] scramble411(String password, String seed) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        byte[] passwordHashStage1 = md.digest(password.getBytes("ASCII"));
        md.reset();

        byte[] passwordHashStage2 = md.digest(passwordHashStage1);
        md.reset();

        byte[] seedAsBytes = seed.getBytes("utf-8"); // for debugging
        md.update(seedAsBytes);
        md.update(passwordHashStage2);

        byte[] toBeXord = md.digest();

        int numToXor = toBeXord.length;

        for (int i = 0; i < numToXor; i++) {
            toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
        }

        return toBeXord;
    }
}
