package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class OnlinePhotoModel implements Serializable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @SerializedName("pdphotolocalId")

    long pdphotolocalId;

    @SerializedName("pdphotoname")
    String pdPhotoName;

    @SerializedName("projectid")
    String projectId;

    @SerializedName("pduserid")
    String pdUserId;

    @SerializedName("hash")
    String hash;

    @SerializedName("origname")
    String origName;

    @SerializedName("params")
    String params;

    @SerializedName("description")
    String description;

    @SerializedName("photodate")
    String photoDate;

    @SerializedName("exif")
    String exif;

    @SerializedName("dailydocu")
    String dailyDocu;

    @SerializedName("md5_hash")
    String md5_hash;

    public String getMd5_hash() {
        return md5_hash;
    }

    public void setMd5_hash(String md5_hash) {
        this.md5_hash = md5_hash;
    }

    @SerializedName("created")
    String created;

    @SerializedName("deleted")
    String deleted;

    @SerializedName("exifwidth")
    String exifWidth;

    @SerializedName("exifheight")
    String exifHeight;

    @SerializedName("exifgpslat")
    String exifGpsLat;

    @SerializedName("exifgpslon")
    String exifGpsLon;

    @SerializedName("exifgpsdirection")
    String exifGpsDirection;

    @SerializedName("exifhasgpsdirection")
    String exifHasGpsDirection;

    @SerializedName("exifdate")
    String exifDate;

    @SerializedName("savedate")
    String saveDate;

    @SerializedName("runid")
    String runId;

    @SerializedName("orighash")
    String origHash;

    @SerializedName("quality")
    String quality;

    @SerializedName("exifgpsx")
    String exifGpsX;

    @SerializedName("exifgpsy")
    String exifGpsY;

    @SerializedName("exifhasgps")
    String exifHasGps;

    @SerializedName("lastupdated")
    String lastUpdated;

    @SerializedName("exiforientation")
    String exifOrientation;

    @SerializedName("gpsaccuracy")
    String gpsAccuracy;

    @SerializedName("inplan")
    String inPlan;

    String path;

    @SerializedName("photoTime")
    String photoTime;

    public String getPhotoTime() {
        return photoTime;
    }

    public void setPhotoTime(String photoTime) {
        this.photoTime = photoTime;
    }

    public OnlinePhotoModel() {
    }

    @SerializedName("created_df")
    public long created_df;

    @SerializedName("photo_type")
    String photo_type;

    @SerializedName("local_flaw_id")
    String local_flaw_id;

    public String getFlaw_id() {
        return flaw_id;
    }

    public void setFlaw_id(String flaw_id) {
        this.flaw_id = flaw_id;
    }

    @SerializedName("flaw_id")
    String flaw_id;
    boolean isPhotoCached = false;
    @Ignore
    boolean isPhotoSelected = false;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

    public boolean isPhotoSelected() {

        return isPhotoSelected;
    }

    public void setPhotoSelected(boolean photoSelected) {
        isPhotoSelected = photoSelected;
    }

    public boolean isCameraOpen() {
        return isCameraOpen;
    }

    public void setCameraOpen(boolean cameraOpen) {
        isCameraOpen = cameraOpen;
    }

    @Ignore
    boolean isCameraOpen = false;
    public String getLocal_flaw_id() {
        return local_flaw_id;
    }

    public void setLocal_flaw_id(String local_flaw_id) {
        this.local_flaw_id = local_flaw_id;
    }

    @SerializedName("pohotPath")
    private String pohotPath;

    public boolean isPhotoCached() {
        return isPhotoCached;
    }

    public void setPhotoCached(boolean photoCached) {
        isPhotoCached = photoCached;
    }

    public String getPohotPath() {
        return pohotPath;
    }

    public void setPohotPath(String pohotPath) {
        this.pohotPath = pohotPath;
    }

    public String getPhoto_type() {
        return photo_type;
    }

    public void setPhoto_type(String photo_type) {
        this.photo_type = photo_type;
    }

    @SerializedName("pdphototext")
    private String pdphototext;

    @SerializedName("pdphotoid")
    private String pdphotoid;


    boolean wordAdded;

    boolean planAdded;

    boolean defectAdded;

    boolean recordingAdded;

    boolean brushImageAdded;

    public OnlinePhotoModel(String pdPhotoName, String projectId, String pdUserId, String hash, String origName, String params, String description, String photoDate, String exif, String dailyDocu, String created, String deleted, String exifWidth, String exifHeight, String exifGpsLat, String exifGpsLon, String exifGpsDirection, String exifHasGpsDirection, String exifDate, String saveDate, String runId, String origHash, String quality, String exifGpsX, String exifGpsY, String exifHasGps, String lastUpdated, String exifOrientation, String gpsAccuracy, String inPlan) {
        this.pdPhotoName = pdPhotoName;
        this.projectId = projectId;
        this.pdUserId = pdUserId;
        this.hash = hash;
        this.origName = origName;
        this.params = params;
        this.description = description;
        this.photoDate = photoDate;
        this.exif = exif;
        this.dailyDocu = dailyDocu;
        this.created = created;
        this.deleted = deleted;
        this.exifWidth = exifWidth;
        this.exifHeight = exifHeight;
        this.exifGpsLat = exifGpsLat;
        this.exifGpsLon = exifGpsLon;
        this.exifGpsDirection = exifGpsDirection;
        this.exifHasGpsDirection = exifHasGpsDirection;
        this.exifDate = exifDate;
        this.saveDate = saveDate;
        this.runId = runId;
        this.origHash = origHash;
        this.quality = quality;
        this.exifGpsX = exifGpsX;
        this.exifGpsY = exifGpsY;
        this.exifHasGps = exifHasGps;
        this.lastUpdated = lastUpdated;
        this.exifOrientation = exifOrientation;
        this.gpsAccuracy = gpsAccuracy;
        this.inPlan = inPlan;
    }

    public OnlinePhotoModel(String pdPhotoName, String projectId, String pdUserId, String hash, String origName, String params, String description, String photoDate, String exif, String dailyDocu, String created,
                            String deleted, String exifWidth, String exifHeight, String exifGpsLat,
                            String exifGpsLon, String exifGpsDirection, String exifHasGpsDirection, String exifDate,
                            String saveDate, String runId, String origHash, String quality, String exifGpsX,
                            String exifGpsY, String exifHasGps, String lastUpdated, String exifOrientation,
                            String gpsAccuracy, String inPlan, String photo_type) {
        this.pdPhotoName = pdPhotoName;
        this.projectId = projectId;
        this.pdUserId = pdUserId;
        this.hash = hash;
        this.origName = origName;
        this.params = params;
        this.description = description;
        this.photoDate = photoDate;
        this.exif = exif;
        this.dailyDocu = dailyDocu;
        this.created = created;
        this.deleted = deleted;
        this.exifWidth = exifWidth;
        this.exifHeight = exifHeight;
        this.exifGpsLat = exifGpsLat;
        this.exifGpsLon = exifGpsLon;
        this.exifGpsDirection = exifGpsDirection;
        this.exifHasGpsDirection = exifHasGpsDirection;
        this.exifDate = exifDate;
        this.saveDate = saveDate;
        this.runId = runId;
        this.origHash = origHash;
        this.quality = quality;
        this.exifGpsX = exifGpsX;
        this.exifGpsY = exifGpsY;
        this.exifHasGps = exifHasGps;
        this.lastUpdated = lastUpdated;
        this.exifOrientation = exifOrientation;
        this.gpsAccuracy = gpsAccuracy;
        this.inPlan = inPlan;
        this.photo_type = photo_type;
    }

    public OnlinePhotoModel(String pdPhotoName, String projectId, String pdUserId,
                            String hash, String origName, String params,
                            String description, String photoDate, String exif,
                            String dailyDocu, String created, String deleted
            , String exifWidth, String exifHeight, String exifGpsLat,
                            String exifGpsLon, String exifGpsDirection, String exifHasGpsDirection,
                            String exifDate, String saveDate, String runId, String origHash, String quality,
                            String exifGpsX, String exifGpsY, String exifHasGps, String lastUpdated, String
                              exifOrientation, String gpsAccuracy, String inPlan, long created_DF, String photo_time, String photo_type) {
        this.pdPhotoName = pdPhotoName;
        this.projectId = projectId;
        this.pdUserId = pdUserId;
        this.hash = hash;
        this.origName = origName;
        this.params = params;
        this.description = description;
        this.photoDate = photoDate;
        this.exif = exif;
        this.dailyDocu = dailyDocu;
        this.created = created;
        this.deleted = deleted;
        this.exifWidth = exifWidth;
        this.exifHeight = exifHeight;
        this.exifGpsLat = exifGpsLat;
        this.exifGpsLon = exifGpsLon;
        this.exifGpsDirection = exifGpsDirection;
        this.exifHasGpsDirection = exifHasGpsDirection;
        this.exifDate = exifDate;
        this.saveDate = saveDate;
        this.runId = runId;
        this.origHash = origHash;
        this.quality = quality;
        this.exifGpsX = exifGpsX;
        this.exifGpsY = exifGpsY;
        this.exifHasGps = exifHasGps;
        this.lastUpdated = lastUpdated;
        this.exifOrientation = exifOrientation;
        this.gpsAccuracy = gpsAccuracy;
        this.inPlan = inPlan;
        this.created_df = created_DF;
        this.photoTime = photo_time;
        this.photo_type = photo_type;
    }

    public long getCreated_df() {
        return created_df;
    }

    public void setCreated_df(long created_df) {
        this.created_df = created_df;
    }


    public String getPdPhotoName() {
        return pdPhotoName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getPdUserId() {
        return pdUserId;
    }

    public String getHash() {
        return hash;
    }

    public String getOrigName() {
        return origName;
    }

    public String getParams() {
        return params;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoDate() {
        return photoDate;
    }

    public String getExif() {
        return exif;
    }

    public String getDailyDocu() {
        return dailyDocu;
    }

    public String getCreated() {
        return created;
    }

    public String getDeleted() {
        return deleted;
    }

    public String getExifWidth() {
        return exifWidth;
    }

    public String getExifHeight() {
        return exifHeight;
    }

    public String getExifGpsLat() {
        return exifGpsLat;
    }

    public String getExifGpsLon() {
        return exifGpsLon;
    }

    public String getExifGpsDirection() {
        return exifGpsDirection;
    }

    public String getExifHasGpsDirection() {
        return exifHasGpsDirection;
    }

    public String getExifDate() {
        return exifDate;
    }

    public String getSaveDate() {
        return saveDate;
    }

    public String getRunId() {
        return runId;
    }

    public String getOrigHash() {
        return origHash;
    }

    public String getQuality() {
        return quality;
    }

    public String getExifGpsX() {
        return exifGpsX;
    }

    public String getExifGpsY() {
        return exifGpsY;
    }

    public String getExifHasGps() {
        return exifHasGps;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getExifOrientation() {
        return exifOrientation;
    }

    public String getGpsAccuracy() {
        return gpsAccuracy;
    }

    public String getInPlan() {
        return inPlan;
    }


    public void setParams(String params) {
        this.params = params;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPdphotolocalId() {
        return pdphotolocalId;
    }

    public void setPdphotolocalId(long pdphotolocalId) {
        this.pdphotolocalId = pdphotolocalId;
    }

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

    public void setPdPhotoName(String pdPhotoName) {
        this.pdPhotoName = pdPhotoName;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setPdUserId(String pdUserId) {
        this.pdUserId = pdUserId;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setOrigName(String origName) {
        this.origName = origName;
    }

    public void setPhotoDate(String photoDate) {
        this.photoDate = photoDate;
    }

    public void setExif(String exif) {
        this.exif = exif;
    }

    public void setDailyDocu(String dailyDocu) {
        this.dailyDocu = dailyDocu;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public void setExifWidth(String exifWidth) {
        this.exifWidth = exifWidth;
    }

    public void setExifHeight(String exifHeight) {
        this.exifHeight = exifHeight;
    }

    public void setExifGpsLat(String exifGpsLat) {
        this.exifGpsLat = exifGpsLat;
    }

    public void setExifGpsLon(String exifGpsLon) {
        this.exifGpsLon = exifGpsLon;
    }

    public void setExifGpsDirection(String exifGpsDirection) {
        this.exifGpsDirection = exifGpsDirection;
    }

    public void setExifHasGpsDirection(String exifHasGpsDirection) {
        this.exifHasGpsDirection = exifHasGpsDirection;
    }

    public void setExifDate(String exifDate) {
        this.exifDate = exifDate;
    }

    public void setSaveDate(String saveDate) {
        this.saveDate = saveDate;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setOrigHash(String origHash) {
        this.origHash = origHash;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public void setExifGpsX(String exifGpsX) {
        this.exifGpsX = exifGpsX;
    }

    public void setExifGpsY(String exifGpsY) {
        this.exifGpsY = exifGpsY;
    }

    public void setExifHasGps(String exifHasGps) {
        this.exifHasGps = exifHasGps;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setExifOrientation(String exifOrientation) {
        this.exifOrientation = exifOrientation;
    }

    public void setGpsAccuracy(String gpsAccuracy) {
        this.gpsAccuracy = gpsAccuracy;
    }

    public void setInPlan(String inPlan) {
        this.inPlan = inPlan;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isWordAdded() {
        return wordAdded;
    }

    public void setWordAdded(boolean wordAdded) {
        this.wordAdded = wordAdded;
    }

    public boolean isPlanAdded() {
        return planAdded;
    }

    public void setPlanAdded(boolean planAdded) {
        this.planAdded = planAdded;
    }

    public boolean isDefectAdded() {
        return defectAdded;
    }

    public void setDefectAdded(boolean defectAdded) {
        this.defectAdded = defectAdded;
    }

    public boolean isRecordingAdded() {
        return recordingAdded;
    }

    public void setRecordingAdded(boolean recordingAdded) {
        this.recordingAdded = recordingAdded;
    }

    public boolean isBrushImageAdded() {
        return brushImageAdded;
    }

    public void setBrushImageAdded(boolean brushImageAdded) {
        this.brushImageAdded = brushImageAdded;
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
