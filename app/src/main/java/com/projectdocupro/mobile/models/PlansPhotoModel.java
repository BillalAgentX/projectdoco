package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

@Entity
public class PlansPhotoModel implements Serializable {

    @PrimaryKey
    @NonNull
    @SerializedName("pdplanid")
    String planId;

    @SerializedName("projectid")
    String projectId;

    @SerializedName("pdplanname")
    String projectName;

    @SerializedName("pohotPath")
    private String pohotPath;

    boolean isPhotoCached;

    @SerializedName("arrow_direction_center_x")
    private String arrow_direction_center_x;

    @SerializedName("arrow_direction_center_y")
    private String arrow_direction_center_y;

    @SerializedName("arrow_direction_x")
    private String arrow_direction_x;

    @SerializedName("arrow_direction_y")
    private String arrow_direction_y;

    @SerializedName("arrow_angel")
    private String arrow_angel;

    @SerializedName("focus_point_x")
    private String focus_point_x;

    @SerializedName("focus_point_y")
    private String focus_point_y;

    @SerializedName("scale_factor")
    private String scale_factor;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;

    public String getScale_factor() {
        return scale_factor;
    }

    public void setScale_factor(String scale_factor) {
        this.scale_factor = scale_factor;
    }

    @NonNull
    public String getPlanId() {
        return planId;
    }

    public void setPlanId(@NonNull String planId) {
        this.planId = planId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getPohotPath() {
        return pohotPath;
    }

    public void setPohotPath(String pohotPath) {
        this.pohotPath = pohotPath;
    }

    public boolean isPhotoCached() {
        return isPhotoCached;
    }

    public void setPhotoCached(boolean photoCached) {
        isPhotoCached = photoCached;
    }

    public String getArrow_direction_center_x() {
        return arrow_direction_center_x;
    }

    public void setArrow_direction_center_x(String arrow_direction_center_x) {
        this.arrow_direction_center_x = arrow_direction_center_x;
    }

    public String getArrow_direction_center_y() {
        return arrow_direction_center_y;
    }

    public void setArrow_direction_center_y(String arrow_direction_center_y) {
        this.arrow_direction_center_y = arrow_direction_center_y;
    }

    public String getArrow_direction_x() {
        return arrow_direction_x;
    }

    public void setArrow_direction_x(String arrow_direction_x) {
        this.arrow_direction_x = arrow_direction_x;
    }

    public String getArrow_direction_y() {
        return arrow_direction_y;
    }

    public void setArrow_direction_y(String arrow_direction_y) {
        this.arrow_direction_y = arrow_direction_y;
    }

    public String getArrow_angel() {
        return arrow_angel;
    }

    public void setArrow_angel(String arrow_angel) {
        this.arrow_angel = arrow_angel;
    }

    public String getFocus_point_x() {
        return focus_point_x;
    }

    public void setFocus_point_x(String focus_point_x) {
        this.focus_point_x = focus_point_x;
    }

    public String getFocus_point_y() {
        return focus_point_y;
    }

    public void setFocus_point_y(String focus_point_y) {
        this.focus_point_y = focus_point_y;
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
