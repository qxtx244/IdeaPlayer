package org.qxtx.idea.player.exception;

/**
 * Created by QXTX-GOSPELL on 2018/12/10 0010.
 */

public class ParseUrlException extends Exception {
    private final String TAG = "ParseUrlException: ";

    public ParseUrlException(String message) {
        super(message);
    }

    public ParseUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseUrlException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return TAG + super.getLocalizedMessage() + ", code=";
    }
}
