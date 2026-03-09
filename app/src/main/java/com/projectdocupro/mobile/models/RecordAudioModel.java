package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class RecordAudioModel {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("recordid")
    private int  recordId;

    @SerializedName("recordServerId")
    private String  recordServerId;

    public String getRecordServerId() {
        return recordServerId;
    }

    public void setRecordServerId(String recordServerId) {
        this.recordServerId = recordServerId;
    }

    @SerializedName("projectid")
    private String  projectId;

    @SerializedName("photoid")
    private long photoId;

    @SerializedName("name")
    private String  name;

    @SerializedName("path")
    private String  path;

    @SerializedName("date")
    private String  date;

    @SerializedName("duration")
    private String  duration;

    @SerializedName("timeStamp")
    private long  timeStamp;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Ignore
    @SerializedName("isPlaying")
    public boolean  isPlaying;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

//


    public RecordAudioModel( String projectId, long photoId, String name, String  path, String date, String duration,long timeStamp) {
        this.projectId = projectId;
        this.photoId = photoId;
        this.path   =   path;
        this.name = name;
        this.date = date;
        this.duration = duration;
        this.timeStamp = timeStamp;
        setRecordServerId("");
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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
