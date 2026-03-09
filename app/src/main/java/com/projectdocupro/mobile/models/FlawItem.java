package com.projectdocupro.mobile.models;

import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

@Entity
public class FlawItem {
    @SerializedName("_explicitType")
    private String  explicitType;

    @SerializedName("pdflawitemid")
    private String  flawItemId;

    @SerializedName("pdflawid")
    private String  flawId;

    @SerializedName("pdphototext")
    private String   photoText;

    @SerializedName("pdphotoid")
    private String photoId;


    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;



    public FlawItem(String explicitType, String flawItemId, String flawId, String photoText, String photoId) {
        this.explicitType = explicitType;
        this.flawItemId = flawItemId;
        this.flawId = flawId;
        this.photoText = photoText;
        this.photoId = photoId;
    }

    public String getExplicitType() {
        return explicitType;
    }

    public String getFlawItemId() {
        return flawItemId;
    }

    public String getFlawId() {
        return flawId;
    }

    public String getPhotoText() {
        return photoText;
    }

    public String getPhotoId() {
        return photoId;
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


}
