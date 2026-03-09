package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class DefectPhotoModel implements Serializable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @SerializedName("localPhotoId")
    public long  localPhotoId=0;
    @SerializedName("pdphototext")
    private String  pdphototext;

    @SerializedName("pdphotoid")
    private String  pdphotoid;

    @SerializedName("pdflawid")
    private String  flawId;

    @SerializedName("projectid")
    private String   projectid;

    @SerializedName("pohotPath")
    private String   pohotPath;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;


    public String getPohotPath() {
        return pohotPath;
    }

    public void setPohotPath(String pohotPath) {
        this.pohotPath = pohotPath;
    }

    public boolean isPhotoCached() {
        return isPhotoCached;
    }

    public void setPhotoCached(boolean photoCached) {
        isPhotoCached = photoCached;
    }

    boolean isPhotoCached=false;

    public String getPdphototext() {
        return pdphototext;
    }

    public void setPdphototext(String pdphototext) {
        this.pdphototext = pdphototext;
    }

    public String getPdphotoid() {
        return pdphotoid;
    }

    public void setPdphotoid(String pdphotoid) {
        this.pdphotoid = pdphotoid;
    }

    public String getFlawId() {
        return flawId;
    }

    public void setFlawId(String flawId) {
        this.flawId = flawId;
    }

    public String getProjectid() {
        return projectid;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public String getExtra3() {
        return extra3;
    }

    public void setExtra3(String extra3) {
        this.extra3 = extra3;
    }

    public String getExtra4() {
        return extra4;
    }

    public void setExtra4(String extra4) {
        this.extra4 = extra4;
    }

    public String getExtra5() {
        return extra5;
    }

    public void setExtra5(String extra5) {
        this.extra5 = extra5;
    }

    public DefectPhotoModel(String pdphototext, String pdphotoid, String flawId, @NonNull String projectid) {
        this.pdphototext = pdphototext;
        this.pdphotoid = pdphotoid;
        this.flawId = flawId;
        this.projectid = projectid;
    }
}
