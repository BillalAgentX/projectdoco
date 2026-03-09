package com.projectdocupro.mobile.models;

public class SyncModel {

    private String  projectID;
    private boolean  isSync;

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean sync) {
        isSync = sync;
    }
}
