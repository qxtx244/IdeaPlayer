package org.qxtx.idea.player.exception;

/**
 * Created by QXTX-GOSPELL on 2018/12/10 0010.
 */

public class UnknownException extends Exception {
    private final String TAG = "UnknownException: ";

    public UnknownException() {}

    public UnknownException(String message) {
        super(message);
    }

    public UnknownException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return TAG + super.getLocalizedMessage() + ", code=";
    }
}
