package org.qxtx.idea.player.exception;

/**
 * Created by QXTX-GOSPELL on 2018/12/11 0011.
 */

public class MediaSourceException extends Exception {
    private final String TAG = "MediaSourceException: ";

    public MediaSourceException(String message) {
        super(message);
    }

    public MediaSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MediaSourceException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getLocalizedMessage() {
        return TAG + super.getLocalizedMessage() + ", code=";
    }
}
