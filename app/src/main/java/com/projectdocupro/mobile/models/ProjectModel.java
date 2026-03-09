package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class ProjectModel {
    @Override
    public String toString() {
        return "ProjectModel{" +
                "projectid='" + projectid + '\'' +
                ", project_name='" + project_name + '\'' +
                ", shortname='" + shortname + '\'' +
                ", project_number='" + project_number + '\'' +
                ", zipcode='" + zipcode + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", constructor='" + constructor + '\'' +
                ", lastupdated='" + lastupdated + '\'' +
                ", logo1='" + logo1 + '\'' +
                ", logo2='" + logo2 + '\'' +
                ", decoimage='" + decoimage + '\'' +
                ", customer_id='" + customer_id + '\'' +
                ", invitationstatus='" + invitationstatus + '\'' +
                ", active='" + active + '\'' +
                ", company_name='" + company_name + '\'' +
                ", company_name_department='" + company_name_department + '\'' +
                ", photocount='" + photocount + '\'' +
                ", flawcount='" + flawcount + '\'' +
                ", cacheImagePath='" + cacheImagePath + '\'' +
                ", favorite=" + favorite +
                ", isImageCache=" + isImageCache +
                ", lastOpen=" + lastOpen +
                '}';
    }

    @PrimaryKey
    @NonNull
    @SerializedName("projectid")
    private String projectid;

    @SerializedName("project_name")
    private String project_name;

    @SerializedName("shortname")
    private String shortname;

    @SerializedName("project_number")
    private String project_number;

    @SerializedName("zipcode")
    private String zipcode;

    @SerializedName("city")
    private String city;

    @SerializedName("country")
    private String country;

    @SerializedName("constructor")
    private String constructor;

    @SerializedName("lastupdated")
    private String lastupdated;

    @SerializedName("logo1")
    private String logo1;

    @SerializedName("logo2")
    private String logo2;

    @SerializedName("decoimage")
    private String decoimage;

    @SerializedName("customer_id")
    private String customer_id;

    @SerializedName("invitationstatus")
    private String invitationstatus;

    @SerializedName("active")
    private String active;

    @SerializedName("company_name")
    private String company_name;

    @SerializedName("company_name_department")
    private String company_name_department;

    @SerializedName("photocount")
    private String photocount;

    public void setPhotocount(String photocount) {
        this.photocount = photocount;
    }

    @SerializedName("flawcount")
    private String flawcount;

    @SerializedName("cacheImagePath")
    private String cacheImagePath;

    @SerializedName("favorite")
    private boolean favorite;
    private boolean isImageCache;

    @SerializedName("isPhotoSynced")
    private String isPhotoSynced = "N";

    @SerializedName("isDefectSynced")
    private String isDefectSynced = "N";

    @SerializedName("syncStatus")
    private String syncStatus = "0";

    @SerializedName("lastUpdatedProjectStatus")
    private String lastUpdatedProjectStatus="0";


    @SerializedName("status")
    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

    public String getLastUpdatedProjectStatus() {
        return lastUpdatedProjectStatus;
    }

    public void setLastUpdatedProjectStatus(String lastUpdatedProjectStatus) {
        this.lastUpdatedProjectStatus = lastUpdatedProjectStatus;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getIsPhotoSynced() {
        return isPhotoSynced;
    }

    public void setIsPhotoSynced(String isPhotoSynced) {
        this.isPhotoSynced = isPhotoSynced;
    }

    public String getIsDefectSynced() {
        return isDefectSynced;
    }

    public void setIsDefectSynced(String isDefectSynced) {
        this.isDefectSynced = isDefectSynced;
    }

    private Long lastOpen;


    public String getCacheImagePath() {
        return cacheImagePath;
    }

    public void setCacheImagePath(String cacheImagePath) {
        this.cacheImagePath = cacheImagePath;
    }

    public boolean isImageCache() {
        return isImageCache;
    }

    public void setImageCache(boolean imageCache) {
        isImageCache = imageCache;
    }

    public ProjectModel(String projectid, String project_name, String shortname, String project_number, String zipcode, String city, String country, String constructor, String lastupdated, String logo1, String logo2, String decoimage, String customer_id, String invitationstatus, String active, String company_name, String company_name_department, String photocount, String flawcount, boolean favorite, Long lastOpen) {
        this.projectid = projectid;
        this.project_name = project_name;
        this.shortname = shortname;
        this.project_number = project_number;
        this.zipcode = zipcode;
        this.city = city;
        this.country = country;
        this.constructor = constructor;
        this.lastupdated = lastupdated;
        this.logo1 = logo1;
        this.logo2 = logo2;
        this.decoimage = decoimage;
        this.customer_id = customer_id;
        this.invitationstatus = invitationstatus;
        this.active = active;
        this.company_name = company_name;
        this.company_name_department = company_name_department;
        this.photocount = photocount;
        this.flawcount = flawcount;
        this.favorite = favorite;
        this.lastOpen = lastOpen;
    }

    public String getProjectid() {
        return projectid;
    }

    public String getProject_name() {
        return project_name;
    }

    public String getShortname() {
        return shortname;
    }

    public String getProject_number() {
        return project_number;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getConstructor() {
        return constructor;
    }

    public String getLastupdated() {
        return lastupdated;
    }

    public String getLogo1() {
        return logo1;
    }

    public String getLogo2() {
        return logo2;
    }

    public String getDecoimage() {
        return decoimage;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public String getInvitationstatus() {
        return invitationstatus;
    }

    public String getActive() {
        return active;
    }

    public String getCompany_name() {
        return company_name;
    }

    public String getCompany_name_department() {
        return company_name_department;
    }

    public String getPhotocount() {
        return photocount;
    }

    public String getFlawcount() {
        return flawcount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setLastOpen(Long lastOpen) {
        this.lastOpen = lastOpen;
    }

    public Long getLastOpen() {
        return lastOpen;
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
