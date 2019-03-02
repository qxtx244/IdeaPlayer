package org.qxtx.idea.player.demo;

import java.io.Serializable;

/**
 * Created by QXTX-GOSPELL on 2018/10/10 0010.
 */

public class ItemBean implements Serializable {
    private String name;
    private String url;
    private String serverLicenseUrl;

    public ItemBean() {}

    public ItemBean(String name, String url, String serverLicenseUrl) {
        this.name = name;
        this.url = url;
        this.serverLicenseUrl = serverLicenseUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServerLicenseUrl() {
        return serverLicenseUrl;
    }

    public void setServerLicenseUrl(String serverLicenseUrl) {
        this.serverLicenseUrl = serverLicenseUrl;
    }
}
