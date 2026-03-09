package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.projectdocupro.mobile.utility.Utils;

import java.util.List;

@Entity
public class PlansModel {

    @Override
    public String toString() {
        return "PlansModel{" +
                "planId='" + planId + '\'' +
                ", projectId='" + projectId + '\'' +
                ", projectName='" + projectName + '\'' +
                ", origName='" + origName + '\'' +
                ", planNumber='" + planNumber + '\'' +
                ", description='" + description + '\'' +
                ", planDate='" + planDate + '\'' +
                ", pdFlawFlagServerId='" + pdFlawFlagServerId + '\'' +
                ", revision='" + revision + '\'' +
                ", color='" + color + '\'' +
                ", refPointsJson=" + refPointsJson +
                ", northDeviationAngle='" + northDeviationAngle + '\'' +
                ", isGeoref='" + isGeoref + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", planPhotoPath='" + planPhotoPath + '\'' +
                ", planPhotoPathLargeSize='" + planPhotoPathLargeSize + '\'' +
                ", xcoord=" + xcoord +
                ", ycoord=" + ycoord +
                ", degree=" + degree +
                ", viewx=" + viewx +
                ", viewy=" + viewy +
                ", scale_factor=" + scale_factor +
                ", is_arrow_located=" + is_arrow_located +
                ", favorite=" + favorite +
                '}';
    }

    @PrimaryKey
    @NonNull
    @SerializedName("pdplanid")
    String planId;

    @SerializedName("projectid")
    String projectId;

    @SerializedName("pdplanname")
    String projectName;

    @SerializedName("origname")
    String origName;

    @SerializedName("plannumber")
    String planNumber;

    @SerializedName("description")
    String description;

    @SerializedName("plandate")
    String planDate;

    @SerializedName("pdFlawFlagServerId")
    String pdFlawFlagServerId;

    public String getPdFlawFlagServerId() {
        return pdFlawFlagServerId;
    }

    public void setPdFlawFlagServerId(String pdFlawFlagServerId) {
        this.pdFlawFlagServerId = pdFlawFlagServerId;
    }

    @SerializedName("revision")
    String revision;

    @SerializedName("color")
    String color;

    @Ignore
    @SerializedName("refpointsjson")
    List<ReferPointJSONPlanModel> refPointsJson;

    @SerializedName("north_deviation_angle")
    String northDeviationAngle;

    @SerializedName("is_georef")
    String isGeoref;

    @SerializedName("lastupdated")
    String lastUpdated;
    @SerializedName("planPhotoPath")
    String planPhotoPath;

    @SerializedName("planPhotoPathLargeSize")
    String planPhotoPathLargeSize;

    @SerializedName("xcoord")
    @Expose
    private Integer xcoord;

    @SerializedName("ycoord")
    @Expose
    private Integer ycoord;

    @SerializedName("degree")
    @Expose
    public Integer degree;
    @SerializedName("viewx")
    @Expose
    public Integer viewx;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

    public Integer getDegree() {
        return degree;
    }

    public void setPlanId(@NonNull String planId) {
        this.planId = planId;
    }


    public Integer getXcoord() {
        return xcoord;
    }

    public void setXcoord(Integer xcoord) {
        this.xcoord = xcoord;
    }

    public Integer getYcoord() {
        //Utils.showLogger("gettingLastCoordinates>>>"+ycoord);
        return ycoord;
    }

    public void setYcoord(Integer ycoord) {
       // Utils.showLogger("savingLastCoordinates>>>"+ycoord);
        this.ycoord = ycoord;
    }

    public float getScale_factor() {
        return scale_factor;
    }

    public void setScale_factor(float scale_factor) {
        this.scale_factor = scale_factor;
    }

    public Integer getIs_arrow_located() {
        return is_arrow_located;
    }

    public void setIs_arrow_located(Integer is_arrow_located) {
        this.is_arrow_located = is_arrow_located;
    }

    public void setDegree(Integer degree) {
        this.degree = degree;
    }

    public Integer getViewx() {
        return viewx;
    }

    public void setViewx(Integer viewx) {
        this.viewx = viewx;
    }

    public Integer getViewy() {
        return viewy;
    }

    public void setViewy(Integer viewy) {
        this.viewy = viewy;
    }

    @SerializedName("viewy")
    @Expose
    public Integer viewy;

    public float scale_factor = 0;
    public Integer is_arrow_located = 0;

    boolean favorite;

    public String getPlanPhotoPath() {
        return planPhotoPath;
    }

    public void setPlanPhotoPath(String planPhotoPath) {
        this.planPhotoPath = planPhotoPath;
    }

    public String getPlanPhotoPathLargeSize() {
        return planPhotoPathLargeSize;
    }

    public void setPlanPhotoPathLargeSize(String planPhotoPathLargeSize) {
        this.planPhotoPathLargeSize = planPhotoPathLargeSize;
    }

    public PlansModel(@NonNull String planId, String projectId, String projectName, String origName, String planNumber, String description, String planDate, String revision, String color, List<ReferPointJSONPlanModel> refPointsJson, String northDeviationAngle, String isGeoref, String lastUpdated, boolean favorite) {
        this.planId = planId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.origName = origName;
        this.planNumber = planNumber;
        this.description = description;
        this.planDate = planDate;
        this.revision = revision;
        this.color = color;
        this.refPointsJson = refPointsJson;
        this.northDeviationAngle = northDeviationAngle;
        this.isGeoref = isGeoref;
        this.lastUpdated = lastUpdated;
        this.favorite = favorite;
    }

    public PlansModel(@NonNull String planId, String projectId, String projectName, String origName, String planNumber, String description, String planDate, String revision, String color, String northDeviationAngle, String isGeoref, String lastUpdated, boolean favorite) {
        this.planId = planId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.origName = origName;
        this.planNumber = planNumber;
        this.description = description;
        this.planDate = planDate;
        this.revision = revision;
        this.color = color;
        this.northDeviationAngle = northDeviationAngle;
        this.isGeoref = isGeoref;
        this.lastUpdated = lastUpdated;
        this.favorite = favorite;
    }

    @NonNull
    public String getPlanId() {
        return planId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getOrigName() {
        return origName;
    }

    public String getPlanNumber() {
        return planNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getPlanDate() {
        return planDate;
    }

    public String getRevision() {
        return revision;
    }

    public String getColor() {
        return color;
    }

    public List<ReferPointJSONPlanModel> getRefPointsJson() {
        return refPointsJson;
    }

    public String getNorthDeviationAngle() {
        return northDeviationAngle;
    }

    public String getIsGeoref() {
        return isGeoref;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
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
