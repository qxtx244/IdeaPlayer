package org.qxtx.idea.player.demo;

import android.util.Log;

/**
 * Created by QXTX-GOSPELL on 2018/10/23 0023.
 */
class MyLog {
    static void e(String msg) {
        if (IdeaApplication.LOG_ENABLE) {
            Log.e("app_module", msg);
        }
    }
}
