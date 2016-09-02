package wings.my2b.exception;

/**
 * Created by InThEnd on 2016/9/2.
 * 不支持协议版本。
 */
public class UnsupportedProtocolVersionException extends My2bException {

    private static final String message = "不支持的协议版本号：";

    public UnsupportedProtocolVersionException() {
        super(message);
    }

    public UnsupportedProtocolVersionException(int protocolVersion) {
        super(message + protocolVersion);
    }

    public UnsupportedProtocolVersionException(int protocolVersion, Throwable cause) {
        super(message + protocolVersion, cause);
    }

    public UnsupportedProtocolVersionException(Throwable cause) {
        super(cause);
    }

}
