package org.qxtx.idea.player.utils;

import android.util.Log;

/**
 * Created by QXTX-GOSPELL on 2018/10/23 0023.
 */

public final class IdeaLog {
    private static boolean allowDebug = false;

    public static void e(String msg) {
        if (IdeaLog.allowDebug) {
            Log.e("visioncrypt_modular", msg);
        }
    }

    public static void e(String tag, String msg) {
        if (IdeaLog.allowDebug) {
            Log.e(tag, msg);
        }
    }


    public static boolean isAllowDebug() {
        return allowDebug;
    }
    public static void setFlag(boolean allowDebug) {
        IdeaLog.allowDebug = allowDebug;
    }
}
