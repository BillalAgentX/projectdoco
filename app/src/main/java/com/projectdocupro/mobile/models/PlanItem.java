package com.projectdocupro.mobile.models;

import com.google.gson.annotations.SerializedName;

public class PlanItem {

    @SerializedName("pdplanid")
    String  planId;

    @SerializedName("pdplanname")
    String  planName;

    @SerializedName("projectid")
    String  projectId;

    @SerializedName("pduserid")
    String  userId;

    @SerializedName("hash")
    String  hash;

    @SerializedName("origname")
    String  origName;

    @SerializedName("plannumber")
    String  planNumber;

    @SerializedName("description")
    String  Description;

    @SerializedName("plandate")
    String  planDate;

    @SerializedName("expirationdate")
    String  expirationDate;

    @SerializedName("predecessorid")
    String  predecessorId;

    @SerializedName("revision")
    String  revision;

    @SerializedName("mapscale")
    String  mapScale;

    @SerializedName("extrainfo")
    String  extraInfo;

    @SerializedName("state")
    String  state;

    @SerializedName("deleted")
    String  deleted;

    @SerializedName("created")
    String  created;

    @SerializedName("color")
    String  color;

    @SerializedName("runid")
    String  runId;

    @SerializedName("planparamsjson")
    String  planParamsJson;

    @SerializedName("refpointsjson")
    String  refPointsJson;

    @SerializedName("north_deviation_angle")
    String  northDeviationAngle;

    @SerializedName("is_georef")
    String  isGeoref;

    @SerializedName("lastupdated")
    String  lastUpdated;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

    public PlanItem(String planId, String planName, String projectId, String userId, String hash, String origName, String planNumber, String description, String planDate, String expirationDate, String predecessorId, String revision, String mapScale, String extraInfo, String state, String deleted, String created, String color, String runId, String planParamsJson, String refPointsJson, String northDeviationAngle, String isGeoref, String lastUpdated) {
        this.planId = planId;
        this.planName = planName;
        this.projectId = projectId;
        this.userId = userId;
        this.hash = hash;
        this.origName = origName;
        this.planNumber = planNumber;
        Description = description;
        this.planDate = planDate;
        this.expirationDate = expirationDate;
        this.predecessorId = predecessorId;
        this.revision = revision;
        this.mapScale = mapScale;
        this.extraInfo = extraInfo;
        this.state = state;
        this.deleted = deleted;
        this.created = created;
        this.color = color;
        this.runId = runId;
        this.planParamsJson = planParamsJson;
        this.refPointsJson = refPointsJson;
        this.northDeviationAngle = northDeviationAngle;
        this.isGeoref = isGeoref;
        this.lastUpdated = lastUpdated;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getOrigName() {
        return origName;
    }

    public void setOrigName(String origName) {
        this.origName = origName;
    }

    public String getPlanNumber() {
        return planNumber;
    }

    public void setPlanNumber(String planNumber) {
        this.planNumber = planNumber;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getPredecessorId() {
        return predecessorId;
    }

    public void setPredecessorId(String predecessorId) {
        this.predecessorId = predecessorId;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getMapScale() {
        return mapScale;
    }

    public void setMapScale(String mapScale) {
        this.mapScale = mapScale;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getPlanParamsJson() {
        return planParamsJson;
    }

    public void setPlanParamsJson(String planParamsJson) {
        this.planParamsJson = planParamsJson;
    }

    public String getRefPointsJson() {
        return refPointsJson;
    }

    public void setRefPointsJson(String refPointsJson) {
        this.refPointsJson = refPointsJson;
    }

    public String getNorthDeviationAngle() {
        return northDeviationAngle;
    }

    public void setNorthDeviationAngle(String northDeviationAngle) {
        this.northDeviationAngle = northDeviationAngle;
    }

    public String getIsGeoref() {
        return isGeoref;
    }

    public void setIsGeoref(String isGeoref) {
        this.isGeoref = isGeoref;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
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
