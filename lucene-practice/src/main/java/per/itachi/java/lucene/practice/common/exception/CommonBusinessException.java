package per.itachi.java.lucene.practice.common.exception;

public class CommonBusinessException extends RuntimeException {

    public CommonBusinessException() {
        super();
    }

    public CommonBusinessException(String message) {
        super(message);
    }

    public CommonBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommonBusinessException(Throwable cause) {
        super(cause);
    }

}
