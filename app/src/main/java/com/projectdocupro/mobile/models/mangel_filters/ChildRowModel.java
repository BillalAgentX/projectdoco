package com.projectdocupro.mobile.models.mangel_filters;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class  ChildRowModel implements Serializable {


    @SerializedName("title")
    private String  title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SerializedName("type")
    private String  type;

    @SerializedName("id")
    private String  id;

    @SerializedName("id_sorting")
    private Integer  id_sorting;

    public Integer getId_sorting() {
        return id_sorting;
    }

    public void setId_sorting(Integer id_sorting) {
        this.id_sorting = id_sorting;
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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @SerializedName("isChecked")
    private boolean  isChecked;

    public ChildRowModel() {
    }

    public ChildRowModel(String title, String id, Integer id_sorting) {
        this.title = title;
        this.id = id;
        this.id_sorting = id_sorting;
    }

    public ChildRowModel(String title, String type, String id, Integer id_sorting, boolean isChecked) {
        this.title = title;
        this.type = type;
        this.id = id;
        this.id_sorting = id_sorting;
        this.isChecked = isChecked;
    }
}
