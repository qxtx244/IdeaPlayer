package org.qxtx.idea.player.exception;

/**
 * Created by QXTX-GOSPELL on 2018/12/10 0010.
 */
public class BindViewException extends Exception {
    private final String TAG = "BindViewException: ";

    public BindViewException(String message) {
        super(message);
    }

    public BindViewException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindViewException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return TAG + super.getLocalizedMessage() + ", code=";
    }
}
