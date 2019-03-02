package org.qxtx.idea.player.exception;

/**
 * Created by QXTX-GOSPELL on 2018/12/10 0010.
 */

public class IllegalUuidException extends Exception {
    private final String TAG = "BindViewException: ";

    public IllegalUuidException(String message) {
        super(message);
    }

    public IllegalUuidException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalUuidException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return TAG + super.getLocalizedMessage() + ", code=";
    }
}
