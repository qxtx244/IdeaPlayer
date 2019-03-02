package org.qxtx.idea.player.player;

import junit.framework.TestCase;
import java.io.File;

/**
 * @CreateDate 2018/12/24 9:34.
 * @Author QXTX-GOSPELL
 */
public class IdeaPlayerTest extends TestCase {
    public void testShowMsg() {
        String callerName = "start";
        String msg = "Player start.";
        System.out.println(callerName + "() execute: " + msg);
    }

    public void testFindUDisk() {
        File file;
        for (int i = -1; i < 9; i++) {
            file = (i == -1) ? new File("storage/sda/") : new File("storage/sda" + i + "/");
            System.out.println(file.getPath());
        }
    }
}