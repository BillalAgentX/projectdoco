package com.projectdocupro.mobile.interfaces;

public interface ISyncTaskComplete {

    public void onSuccess(String projectId,boolean isSync);
    public void onFailure(String projectId,boolean isSync);
}
