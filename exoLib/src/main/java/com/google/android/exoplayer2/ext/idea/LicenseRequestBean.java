package com.google.android.exoplayer2.ext.idea;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by QXTX-GOSPELL on 2018/10/17 0017.
 */
public class LicenseRequestBean {
    private String type;
    private String drmRequestMsg;
    private List<String> drmContentIDs;
    private String drmDeviceID;
    private List<String> ottContentIDs;
    private String ottUserID;

    public LicenseRequestBean() {
        List<String> contentId = new ArrayList<>();
        this.type = "packedLicenseRequest";
        this.drmContentIDs = contentId;
        this.drmDeviceID = "drmDeviceID?";
        this.ottContentIDs = contentId;
        this.ottUserID = "ottUserID?";
    }

    public LicenseRequestBean(String drmRequestMsg) {
        List<String> contentId = new ArrayList<>();
        contentId.add("U4cSEvpCUyA=");
        this.type = "packedLicenseRequest";
        this.drmRequestMsg = drmRequestMsg;
        this.drmContentIDs = contentId;
        this.drmDeviceID = "drmDeviceID?";
        this.ottContentIDs = contentId;
        this.ottUserID = "ottUserID?";
    }

    public LicenseRequestBean(String type, String drmRequestMsg, List<String> drmContentIDs, String drmDeviceID, List<String> ottContentIDs, String ottUserID) {
        this.type = type;
        this.drmRequestMsg = drmRequestMsg;
        this.drmContentIDs = drmContentIDs;
        this.drmDeviceID = drmDeviceID;
        this.ottContentIDs = ottContentIDs;
        this.ottUserID = ottUserID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDrmRequestMsg() {
        return drmRequestMsg;
    }

    public void setDrmRequestMsg(String drmRequestMsg) {
        this.drmRequestMsg = drmRequestMsg;
    }

    public List<String> getDrmContentIDs() {
        return drmContentIDs;
    }

    public void setDrmContentIDs(List<String> drmContentIDs) {
        this.drmContentIDs = drmContentIDs;
    }

    public String getDrmDeviceID() {
        return drmDeviceID;
    }

    public void setDrmDeviceID(String drmDeviceID) {
        this.drmDeviceID = drmDeviceID;
    }

    public List<String> getOttContentIDs() {
        return ottContentIDs;
    }

    public void setOttContentIDs(List<String> ottContentIDs) {
        this.ottContentIDs = ottContentIDs;
    }

    public String getOttUserID() {
        return ottUserID;
    }

    public void setOttUserID(String ottUserID) {
        this.ottUserID = ottUserID;
    }
}
