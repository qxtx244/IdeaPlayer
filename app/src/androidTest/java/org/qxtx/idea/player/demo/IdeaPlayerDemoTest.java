package org.qxtx.idea.player.demo;

import org.junit.runner.RunWith;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;
import android.widget.Toast;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class IdeaPlayerDemoTest extends TestCase {
    private final String TAG = getClass().getSimpleName();

    @Test
    public void testPlay_cycle() {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        uiDevice.pressHome();
        try {
            String result = uiDevice.executeShellCommand("am start -n org.qxtx.idea.player.demo/.ChooseActivity");
            Log.e(TAG + "$" + getName(), "commmand result== " + result);
            UiObject2 uiList = uiDevice.findObject(By.res("org.qxtx.idea.player.demo:id/playlist"));
            if (uiList == null) {
                Log.e(TAG, "list is null!");
                return ;
            }

            List<UiObject2> uiItems = uiList.getChildren();
            if (uiItems == null) {
                Log.e(TAG, "item is empty!");
                return ;
            }

            int i = 0;
            do {
                uiItems.get(i).click();
                SystemClock.sleep(5000);
                uiDevice.pressBack();
                SystemClock.sleep(1000);
                i++;
                if (i == uiItems.size()) {
                    i = 0;
                }
            } while (i < uiItems.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFindUDisk() {
        char pathFlag = 'a';
        File file;
        Context context = InstrumentationRegistry.getTargetContext();
        Looper.prepare();
        for (int i = 0; i < 26; i++) {
            for (int j = -1; j < 10; j++) {
                file = (j == -1) ? new File("storage/sd" + pathFlag + "/") : new File("storage/sd" + pathFlag + j + "/");
                if (file.exists()) {
                    Toast.makeText(context, "find UDisk: " + file.getPath(), Toast.LENGTH_SHORT).show();
                    Log.e("findUDisk", file.getPath());
                }
            }
            pathFlag = (char)((int)pathFlag + 1);
        }
        Looper.loop();
        Assert.fail("Not UDisk");

    }
}
