package com.projectdocupro.mobile.models.mangel_filters;

import com.google.gson.annotations.SerializedName;
import com.projectdocupro.mobile.models.WordModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GroupheadingModel implements Serializable {


    @SerializedName("title")
    private String title;
    private boolean isVisible=true;


    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public GroupheadingModel(String title, String type, boolean isMultiSelect, HashMap<String, List<ChildRowModel>> listChildData) {
        this.title = title;
        this.type = type;
        this.isMultiSelect = isMultiSelect;
        this.listChildData = listChildData;
    }

    public GroupheadingModel(String projectId,String title, String type, boolean isMultiSelect, HashMap<String, List<ChildRowModel>> listChildData) {
        this.title = title;
        this.type = type;
        this.isMultiSelect = isMultiSelect;
        this.listChildData = listChildData;
        project_id=projectId;
    }

    public GroupheadingModel(String title, String type, long start_date, boolean isMultiSelect, HashMap<String, List<ChildRowModel>> listChildData) {
        this.title = title;
        this.type = type;
        this.start_date = start_date;
        this.isMultiSelect = isMultiSelect;
        this.listChildData = listChildData;
    }

    @SerializedName("type")
    private String type;

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    @SerializedName("project_id")
    private String project_id;

    @SerializedName("start_date")
    private long start_date;

    public long getStart_date() {
        return start_date;
    }

    public void setStart_date(long start_date) {
        this.start_date = start_date;
    }

    public long getEnd_date() {
        return end_date;
    }

    public void setEnd_date(long end_date) {
        this.end_date = end_date;
    }

    @SerializedName("end_date")
    private long end_date;

    public HashMap<String, List<ChildRowModel>> getListChildData() {
        return listChildData;
    }

    public void setListChildData(HashMap<String, List<ChildRowModel>> listChildData) {
        this.listChildData = listChildData;
    }

    @SerializedName("isGroupOpen")
    private boolean isGroupOpen;

    @SerializedName("isMultiSelect")
    private boolean isMultiSelect;

    @SerializedName("isSwitchOn")
    private boolean isSwitchOn;

    public boolean isSwitchOn() {
        return isSwitchOn;
    }

    public void setSwitchOn(boolean switchOn) {
        isSwitchOn = switchOn;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @SerializedName("keyword")
    private String keyword;

    HashMap<String, List<ChildRowModel>> listChildData = new HashMap<>();
    List<String> listChildDataSelected = new ArrayList<>();

    List<WordModel> wordModelArrayList = new ArrayList<>();

    public List<WordModel> getWordModelArrayList() {
        return wordModelArrayList;
    }

    public void setWordModelArrayList(List<WordModel> wordModelArrayList) {
        this.wordModelArrayList = wordModelArrayList;
    }

    public List<String> getListChildDataSelected() {
        return listChildDataSelected;
    }

    public void setListChildDataSelected(List<String> listChildDataSelected) {
        this.listChildDataSelected = listChildDataSelected;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isGroupOpen() {
        return isGroupOpen;
    }

    public void setGroupOpen(boolean groupOpen) {
        isGroupOpen = groupOpen;
    }

    public boolean isMultiSelect() {
        return isMultiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        isMultiSelect = multiSelect;
    }

}
