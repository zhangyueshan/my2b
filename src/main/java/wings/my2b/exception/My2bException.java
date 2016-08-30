package wings.my2b.exception;

/**
 * Created by InThEnd on 2016/8/30.
 * My2bException
 */
public class My2bException extends RuntimeException {

    private static final String message = "记录访问错误。";

    public My2bException() {
        super(message);
    }

    public My2bException(String message) {
        super(message);
    }

    public My2bException(String message, Throwable cause) {
        super(message, cause);
    }

    public My2bException(Throwable cause) {
        super(cause);
    }

}
