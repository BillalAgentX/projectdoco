package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class ReferPointJSONPlanModel {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @SerializedName("referPointlocalId")

    long referPointlocalId;

    public long getReferPointlocalId() {
        return referPointlocalId;
    }

    @SerializedName("pdplanid")
    String pdplanid;

    @SerializedName("pdProjectId")
    String pdProjectId;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

    public String getPdProjectId() {
        return pdProjectId;
    }

    public void setPdProjectId(String pdProjectId) {
        this.pdProjectId = pdProjectId;
    }

    public String getPdplanid() {
        return pdplanid;
    }

    public void setPdplanid(String pdplanid) {
        this.pdplanid = pdplanid;
    }

    public ReferPointJSONPlanModel() {
    }

    public void setReferPointlocalId(long referPointlocalId) {
        this.referPointlocalId = referPointlocalId;
    }

    @SerializedName("x")
    public float xCoord=0.0f;


    @SerializedName("y")
    public float yCoord=0.0f;

    @SerializedName("lat")
    private float lat;

    public float getxCoord() {
        return xCoord;
    }

    public void setxCoord(float xCoord) {
        this.xCoord = xCoord;
    }

    public float getyCoord() {
        return yCoord;
    }

    public void setyCoord(float yCoord) {
        this.yCoord = yCoord;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public String get_explicitType() {
        return _explicitType;
    }

    public void set_explicitType(String _explicitType) {
        this._explicitType = _explicitType;
    }

    @SerializedName("lon")
    private float lon;

    @SerializedName("_explicitType")
    private String _explicitType;

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

}
