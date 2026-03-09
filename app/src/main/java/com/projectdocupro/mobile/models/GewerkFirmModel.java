package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
@Entity
public class GewerkFirmModel {
    @PrimaryKey (autoGenerate = true)
    @NonNull
    @SerializedName("autoID")
    long autoIDGewerk;

    @SerializedName("projectparamid")
    private String  projectParamId;

    @SerializedName("projectuserserviceid")
    @Expose
    private String projectuserserviceid;
    @SerializedName("pdprojectid")
    @Expose
    private String pdprojectid;
    @SerializedName("pduserid")
    @Expose
    private String pduserid;
    @SerializedName("pdserviceid")
    @Expose
    private String pdserviceid;
    @SerializedName("created")
    @Expose
    private String created;
    @SerializedName("servicenumber")
    @Expose
    private String servicenumber;
    @SerializedName("pdservicetitle")
    @Expose
    private String pdservicetitle;
    @SerializedName("pdservicedescription")
    @Expose
    private String pdservicedescription;
    @SerializedName("pdservicearea_id")
    @Expose
    private String pdserviceareaId;
    @SerializedName("pdorder")
    @Expose
    private String pdorder;
    @SerializedName("company")
    @Expose
    private String company;
    @SerializedName("selectvalue")
    @Expose
    private String selectvalue;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

    public long getAutoIDGewerk() {
        return autoIDGewerk;
    }

    public void setAutoIDGewerk(long autoIDGewerk) {
        this.autoIDGewerk = autoIDGewerk;
    }

    public String getProjectParamId() {
        return projectParamId;
    }

    public void setProjectParamId(String projectParamId) {
        this.projectParamId = projectParamId;
    }

    public String getProjectuserserviceid() {
        return projectuserserviceid;
    }

    public void setProjectuserserviceid(String projectuserserviceid) {
        this.projectuserserviceid = projectuserserviceid;
    }

    public String getPdprojectid() {
        return pdprojectid;
    }

    public void setPdprojectid(String pdprojectid) {
        this.pdprojectid = pdprojectid;
    }

    public String getPduserid() {
        return pduserid;
    }

    public void setPduserid(String pduserid) {
        this.pduserid = pduserid;
    }

    public String getPdserviceid() {
        return pdserviceid;
    }

    public void setPdserviceid(String pdserviceid) {
        this.pdserviceid = pdserviceid;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getServicenumber() {
        return servicenumber;
    }

    public void setServicenumber(String servicenumber) {
        this.servicenumber = servicenumber;
    }

    public String getPdservicetitle() {
        return pdservicetitle;
    }

    public void setPdservicetitle(String pdservicetitle) {
        this.pdservicetitle = pdservicetitle;
    }

    public String getPdservicedescription() {
        return pdservicedescription;
    }

    public void setPdservicedescription(String pdservicedescription) {
        this.pdservicedescription = pdservicedescription;
    }

    public String getPdserviceareaId() {
        return pdserviceareaId;
    }

    public void setPdserviceareaId(String pdserviceareaId) {
        this.pdserviceareaId = pdserviceareaId;
    }

    public String getPdorder() {
        return pdorder;
    }

    public void setPdorder(String pdorder) {
        this.pdorder = pdorder;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSelectvalue() {
        return selectvalue;
    }

    public void setSelectvalue(String selectvalue) {
        this.selectvalue = selectvalue;
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
