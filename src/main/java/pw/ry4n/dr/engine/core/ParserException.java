package pw.ry4n.dr.engine.core;

public class ParserException extends RuntimeException {
	private static final long serialVersionUID = 7664783043495462632L;

	public ParserException() {
    }

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParserException(Throwable cause) {
        super(cause);
    }

    public ParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
