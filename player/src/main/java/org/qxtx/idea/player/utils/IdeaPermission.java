package org.qxtx.idea.player.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * @CreateDate 2018/12/17 9:16.
 * @Author QXTX-GOSPELL
 */
public final class IdeaPermission {
    public static final int PERMISSION_CODE_RW = 0;

    public static boolean checkPermission(@NonNull Context context) {
        String PERMISSION_RW = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            return true;
        }

        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("Error context that not a Activity object.");
        }

        Activity activity = (Activity)context;
        try {
            int permission = checkSelfPermission(activity, PERMISSION_RW);
            switch (permission) {
                case PermissionChecker.PERMISSION_DENIED:
                    activity.requestPermissions(new String[] {PERMISSION_RW}, PERMISSION_CODE_RW);
                    IdeaLog.e("Need to request permission.");
                    return false;
                case PermissionChecker.PERMISSION_DENIED_APP_OP:
                    IdeaLog.e("Check permission: " + "Result of permission check is PERMISSION_DENIED_APP_OP, what's it?");
                    return false;
                default:
                    return true;
            }

        } catch (IllegalArgumentException i) {
            IdeaLog.e("Permission check error. reset application?");
            return false;
        }
    }
}
