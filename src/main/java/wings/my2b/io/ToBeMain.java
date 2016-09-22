package wings.my2b.io;

import wings.my2b.exception.My2bException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

/**
 * Created by InThEnd on 2016/8/29.
 * ToBeMain
 */
public class ToBeMain {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Socket socket = new Socket("192.168.2.9", 3306);
        My2bIO io = new My2bIO(socket);
        io.doHandShake();
        System.out.println();
    }

    private static int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }

        int n = 0;

        while (n < len) {
            int count = in.read(b, off + n, len - n);

            if (count < 0) {
                throw new My2bException("...");
            }

            n += count;
        }

        return n;
    }

    private static long skipFully(InputStream in, long len) throws IOException {
        if (len < 0) {
            throw new IOException("Negative skip length not allowed");
        }

        long n = 0;

        while (n < len) {
            long count = in.skip(len - n);

            if (count < 0) {
                throw new My2bException("...");
            }

            n += count;
        }

        return n;
    }

}
