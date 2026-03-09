package com.projectdocupro.mobile.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.projectdocupro.mobile.models.localFilters.ImageId_VS_Input;
import com.projectdocupro.mobile.models.localFilters.WordContentModel;
import com.projectdocupro.mobile.utility.Utils;

@Entity
public class WordModel {
    @PrimaryKey
    @NonNull
    @SerializedName("projectparamid")
    private String  projectParamId;

    @SerializedName("projectid")
    private String  projectId;

    @SerializedName("group")
    private String group;

    @SerializedName("name")
    private String  name;

    @SerializedName("type")
    private String  type;

    @SerializedName("order")
    private String  order;

    @SerializedName("visible")
    private String  visible;

    @SerializedName("value")
    private String  value;

    @SerializedName("isUsed")
    private String  isUsed;

    @SerializedName("paramType")
    private String  paramType;

    private String  photoIds;

    private String  onlinePhotoIds;
    private String  open_field_content;

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;


    public String getOpen_field_content() {
        return open_field_content;
    }

    public void setOpen_field_content(String open_field_content) {

        this.open_field_content = open_field_content;
    }

    private String  photoType;
    private boolean clocked;

    public String getOnlinePhotoIds() {
        return onlinePhotoIds;
    }

    public void setOnlinePhotoIds(String onlinePhotoIds) {
        this.onlinePhotoIds = onlinePhotoIds;
    }

    public String getPhotoType() {
        return photoType;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    private int useCount;

    private long    lastUsed;

    private boolean isFavorite;

    public WordModel(@NonNull String projectParamId, String projectId, String group, String name, String type, String order, String visible, String value, String isUsed, String paramType) {
        this.projectParamId = projectParamId;
        this.projectId = projectId;
        this.group = group;
        this.name = name;
        this.type = type;
        this.order = order;
        this.visible = visible;
        this.value = value;
        this.isUsed = isUsed;
        this.paramType = paramType;
    }


    @NonNull
    public String getProjectParamId() {
        return projectParamId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getOrder() {
        return order;
    }

    public String getVisible() {
        return visible;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIsUsed() {
        return isUsed;
    }

    public String getParamType() {
        return paramType;
    }

    public String getPhotoIds() {
        return photoIds;
    }

    public void setPhotoIds(String photoIds) {
        this.photoIds = photoIds;
    }

    public boolean isClocked() {
        return clocked;
    }

    public void setClocked(boolean clocked) {
        this.clocked = clocked;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
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

    public void addOrUpdateInputField(String photoID,String currentText) {

        Utils.showLogger("addOrUpdateInputField>>>"+photoID);

        Gson gson = new Gson();


        String oldContent = getOpen_field_content();
        WordContentModel wordModel=null;
        try {
            wordModel = gson.fromJson(oldContent, WordContentModel.class);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if(wordModel==null)
            wordModel = new WordContentModel();

        ImageId_VS_Input oldValue = wordModel.findByImageId(photoID, name);

        if(oldValue!=null)
            oldValue.setInputFields(currentText);
        else {
            Utils.showLogger("values not found");
            oldValue = new ImageId_VS_Input(photoID, currentText, getName());
            wordModel.getInputsList().add(oldValue);

            if(getPhotoIds()==null)
                setPhotoIds(","+photoID);

            if(getPhotoIds().contains(getPhotoIds()+","+photoID))
            setPhotoIds(getPhotoIds()+","+photoID);
        }

       setOpen_field_content(gson.toJson(wordModel));
    }
}
