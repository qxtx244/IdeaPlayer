package org.qxtx.idea.player.utils;

import android.content.Context;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by QXTX-GOSPELL on 2018/10/10 0010.
 */

public final class IdeaToast {
    private static IdeaToast ideaToast;
    private Toast toast;
    private WeakReference<Context> context;
    private boolean canShow;

    private IdeaToast(Context context) {
        this.context = new WeakReference<Context>(context);
        canShow = true;
    }

    public static IdeaToast getInstance(Context context) {
        if (ideaToast == null) {
            synchronized (IdeaToast.class) {
                if (ideaToast == null) {
                    ideaToast = new IdeaToast(context);
                }
            }
        }

        return ideaToast;
    }

    public void showToast(CharSequence msg, int duration) {
        if (context == null || context.get() == null) {
            return ;
        }

        if (toast != null && canShow) {
            toast.cancel();
            toast = new Toast(context.get());
        }

        toast = Toast.makeText(context.get(), msg, duration);
        toast.show();
    }

    public void cancel() {
        if (toast != null) {
            toast.cancel();
            toast = null;
            context = null;
        }
    }

    void setCanShow(boolean canShow) {
        this.canShow = canShow;
    }
}
