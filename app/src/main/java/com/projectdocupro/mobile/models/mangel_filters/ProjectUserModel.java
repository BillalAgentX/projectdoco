package com.projectdocupro.mobile.models.mangel_filters;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class ProjectUserModel implements Serializable {


    @NonNull
    @PrimaryKey(autoGenerate = true)
    @SerializedName("localAutoId")
    public long localAutoId;

    public long getLocalAutoId() {
        return localAutoId;
    }

    public void setLocalAutoId(long localAutoId) {
        this.localAutoId = localAutoId;
    }

    @NonNull
    @SerializedName("pduserid")
    String pduserid;

    @SerializedName("projectId")
    String projectId;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @SerializedName("firstname")
    String firstname;

    @SerializedName("lastname")
    String lastname;

    @SerializedName("email")
    String email;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;


    @NonNull
    public String getPduserid() {
        return pduserid;
    }

    public void setPduserid(@NonNull String pduserid) {
        this.pduserid = pduserid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @SerializedName("status")
    String status;

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