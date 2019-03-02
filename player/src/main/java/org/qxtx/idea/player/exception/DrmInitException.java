package org.qxtx.idea.player.exception;

/**
 * Created by QXTX-GOSPELL on 2018/12/11 0011.
 */

public class DrmInitException extends Exception {
    private final String TAG = "DrmInitException: ";

    public DrmInitException(String message) {
        super(message);
    }

    public DrmInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public DrmInitException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return TAG + super.getLocalizedMessage() + ", code=";
    }
}
