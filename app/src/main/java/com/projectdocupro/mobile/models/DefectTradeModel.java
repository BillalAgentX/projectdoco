package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class DefectTradeModel implements Serializable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @SerializedName("localAutoId")
    public long localAutoId;

    public String getPdflawid() {
        return pdflawid;
    }

    public void setPdflawid(String pdflawid) {
        this.pdflawid = pdflawid;
    }
    @NonNull
    @SerializedName("pdflawid")
    private String pdflawid;
    @SerializedName("localpdflawid")
    private String localpdflawid;

    public String getLocalpdflawid() {
        return localpdflawid;
    }

    public void setLocalpdflawid(String localpdflawid) {
        this.localpdflawid = localpdflawid;
    }

    @SerializedName("projectuserserviceid")
    private String projectuserserviceid;

    @SerializedName("pdprojectid")
    private String pdprojectid;

    @SerializedName("pduserid")
    private String pduserid;

    @SerializedName("pdserviceid")
    private String pdserviceid;

    @SerializedName("created")
    private String created;

    @SerializedName("servicenumber")
    private String servicenumber;


    @SerializedName("pdservicetitle")
    private String pdservicetitle;

    @SerializedName("pdservicedescription")
    private String pdservicedescription;
    @SerializedName("pdservicearea_id")
    private String pdservicearea_id;

    @SerializedName("pdorder")
    private String pdorder;

    @SerializedName("company")
    private String company;
    @NonNull
    @SerializedName("selected")
    private Integer selected;

    @SerializedName("selectvalue")
    private String selectvalue;


    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;



    public long getLocalAutoId() {
        return localAutoId;
    }

    public void setLocalAutoId(long localAutoId) {
        this.localAutoId = localAutoId;
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

    public String getPdservicearea_id() {
        return pdservicearea_id;
    }

    public void setPdservicearea_id(String pdservicearea_id) {
        this.pdservicearea_id = pdservicearea_id;
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

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
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
