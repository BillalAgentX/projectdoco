package com.projectdocupro.mobile.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.utility.Utils;

import java.lang.reflect.Type;
import java.util.List;

@Entity
public class DefectsModel implements Parcelable {


    protected DefectsModel(Parcel in) {
        defectLocalId = in.readLong();
        defectId = in.readString();
        runId = in.readString();
        runidInt = in.readInt();
        projectId = in.readString();
        userId = in.readString();
        defectType = in.readString();
        defectName = in.readString();
        defectDate = in.readString();
        lastupdate = in.readString();
        status = in.readString();
        creator = in.readString();
        discipline = in.readString();
        discipline_id = in.readString();
        isSynced = in.readByte() != 0;
        isUserSelectedStatus = in.readByte() != 0;
        uploadStatus = in.readString();
        isPhotoAttach = in.readByte() != 0;
        description = in.readString();
        created = in.readString();
        fristDate = in.readString();
        fristdate_df = in.readLong();
        deleted = in.readString();
        pdflawflagList = in.createTypedArrayList(Pdflawflag.CREATOR);
        noticeDate = in.readString();
        noticeDate_df = in.readLong();
        notifieDate = in.readString();
        notifiedate_df = in.readLong();
        secondFristDate = in.readString();
        secondFristDate_df = in.readLong();
        doneDate = in.readString();
        donedate_df = in.readLong();
        createDate = in.readString();
        creator_id = in.readString();
        createDate_df = in.readLong();
        responsibleUser = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(defectLocalId);
        dest.writeString(defectId);
        dest.writeString(runId);
        dest.writeInt(runidInt);
        dest.writeString(projectId);
        dest.writeString(userId);
        dest.writeString(defectType);
        dest.writeString(defectName);
        dest.writeString(defectDate);
        dest.writeString(lastupdate);
        dest.writeString(status);
        dest.writeString(creator);
        dest.writeString(discipline);
        dest.writeString(discipline_id);
        dest.writeByte((byte) (isSynced ? 1 : 0));
        dest.writeByte((byte) (isUserSelectedStatus ? 1 : 0));
        dest.writeString(uploadStatus);
        dest.writeByte((byte) (isPhotoAttach ? 1 : 0));
        dest.writeString(description);
        dest.writeString(created);
        dest.writeString(fristDate);
        dest.writeLong(fristdate_df);
        dest.writeString(deleted);
        dest.writeTypedList(pdflawflagList);
        dest.writeString(noticeDate);
        dest.writeLong(noticeDate_df);
        dest.writeString(notifieDate);
        dest.writeLong(notifiedate_df);
        dest.writeString(secondFristDate);
        dest.writeLong(secondFristDate_df);
        dest.writeString(doneDate);
        dest.writeLong(donedate_df);
        dest.writeString(createDate);
        dest.writeString(creator_id);
        dest.writeLong(createDate_df);
        dest.writeString(responsibleUser);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DefectsModel> CREATOR = new Creator<DefectsModel>() {
        @Override
        public DefectsModel createFromParcel(Parcel in) {
            return new DefectsModel(in);
        }

        @Override
        public DefectsModel[] newArray(int size) {
            return new DefectsModel[size];
        }
    };

    @Override
    public String toString() {
        return "DefectsModel{" +
                "defectLocalId=" + defectLocalId +
                ", defectId='" + defectId + '\'' +
                ", runId='" + runId + '\'' +
                ", projectId='" + projectId + '\'' +
                ", userId='" + userId + '\'' +
                ", defectType='" + defectType + '\'' +
                ", defectName='" + defectName + '\'' +
                ", defectDate='" + defectDate + '\'' +
                ", status='" + status + '\'' +
                ", creator='" + creator + '\'' +
                ", discipline='" + discipline + '\'' +
                ", discipline_id='" + discipline_id + '\'' +
                ", isSynced=" + isSynced +
                ", isUserSelectedStatus=" + isUserSelectedStatus +
                ", uploadStatus='" + uploadStatus + '\'' +
                ", isPhotoAttach=" + isPhotoAttach +
                ", description='" + description + '\'' +
                ", created='" + created + '\'' +
                ", fristDate='" + fristDate + '\'' +
                ", fristdate_df=" + fristdate_df +
                ", deleted='" + deleted + '\'' +
                ", planItems=" + planItems +
                ", defectPhotoModelList=" + defectPhotoModelList +
                ", defectTradeModelList=" + defectTradeModelList +
                ", pdflawflagList=" + pdflawflagList +
                ", noticeDate='" + noticeDate + '\'' +
                ", noticeDate_df=" + noticeDate_df +
                ", notifieDate='" + notifieDate + '\'' +
                ", notifiedate_df=" + notifiedate_df +
                ", secondFristDate='" + secondFristDate + '\'' +
                ", secondFristDate_df=" + secondFristDate_df +
                ", doneDate='" + doneDate + '\'' +
                ", donedate_df=" + donedate_df +
                ", createDate='" + createDate + '\'' +
                ", creator_id='" + creator_id + '\'' +
                ", createDate_df=" + createDate_df +
                ", responsibleUser='" + responsibleUser + '\'' +
                '}';
    }

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("defectLocalId")
    @ColumnInfo(name = "defectLocalId")
    public long defectLocalId;

    @NonNull
    @SerializedName("pdflawid")
    @ColumnInfo(name = "pdflawid")
    String defectId;

    @SerializedName("runid")
    String runId;

    @SerializedName("runidInt")
    int runidInt;

    public int getRunidInt() {
        return runidInt;
    }

    public void setRunidInt(int runidInt) {
        this.runidInt = runidInt;
    }

    @SerializedName("projectid")
    String projectId;

    @SerializedName("pduserid")
    @ColumnInfo(name = "pduserid")
    String userId;

    @SerializedName("flawtype")
    @ColumnInfo(name = "flawtype")
    String defectType;

    @SerializedName("flawname")
    @ColumnInfo(name = "flawname")
    String defectName;

    @SerializedName("flawdate")
    @ColumnInfo(name = "flawdate")
    String defectDate;

    @SerializedName("lastupdate")
    @ColumnInfo(name = "lastupdate")
    String lastupdate;

    @SerializedName("status")
    String status;

    @SerializedName("creator")
    String creator;

    @SerializedName("discipline")
    String discipline;
    @SerializedName("discipline_id")
    String discipline_id;

    @NonNull
    @ColumnInfo(name = "isSynced")
    boolean isSynced = false;

    public boolean isUserSelectedStatus() {
        return isUserSelectedStatus;
    }

    public void setUserSelectedStatus(boolean userSelectedStatus) {
        isUserSelectedStatus = userSelectedStatus;
    }

    @NonNull
    boolean isUserSelectedStatus = false;

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
       // Utils.showLogger("DefectModelSynch"+synced);
        isSynced = synced;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    @SerializedName("uploadStatus")
    private String uploadStatus = "0";

    public boolean isPhotoAttach() {
        return isPhotoAttach;
    }

    public void setPhotoAttach(boolean photoAttach) {
        isPhotoAttach = photoAttach;
    }

    @Ignore
    boolean isPhotoAttach;

    public String getDiscipline_id() {
        return discipline_id;
    }

    public void setDiscipline_id(String discipline_id) {
        this.discipline_id = discipline_id;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    @SerializedName("description")
    String description;

    @SerializedName("created")
    String created;

//    @Ignore
//    @SerializedName("flawitems")
//    List<FlawItem> flawItems;

    @SerializedName("fristdate")
    @ColumnInfo(name = "fristdate")
    public String fristDate;
    @SerializedName("fristdate_df")
    public long fristdate_df;

    @SerializedName("deleted")
    String deleted;

    @Ignore
    @SerializedName("planitems")
    List<PlanItem> planItems;

    @Ignore
    @SerializedName("flawitems")
    public List<PhotoModel> defectPhotoModelList;

    @Ignore
    @SerializedName("pdflawtrades")
    public List<DefectTradeModel> defectTradeModelList;

    @Ignore
    @SerializedName("pdflawflags")
    public List<Pdflawflag> pdflawflagList;

    @SerializedName("noticedate")
    @ColumnInfo(name = "noticedate")
    String noticeDate;

    @SerializedName("noticedate_df")
    @ColumnInfo(name = "noticedate_df")
    public long noticeDate_df;

    @SerializedName("notifiedate")
    @ColumnInfo(name = "notifiedate")
    String notifieDate;

    @SerializedName("notifiedate_df")
    public long notifiedate_df;

    @SerializedName("secondfristdate")
    @ColumnInfo(name = "secondfristdate")
    String secondFristDate;

    @SerializedName("secondFristDate_df")
    public long secondFristDate_df;
    ;

    @SerializedName("donedate")
    @ColumnInfo(name = "donedate")
    String doneDate;

    public long getDefectLocalId() {
        return defectLocalId;
    }

    public void setDefectLocalId(long defectLocalId) {
        this.defectLocalId = defectLocalId;
    }

    public void setDefectId(@NonNull String defectId) {
        this.defectId = defectId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDefectType(String defectType) {
        this.defectType = defectType;
    }

    public void setDefectName(String defectName) {
        this.defectName = defectName;
    }

    public void setDefectDate(String defectDate) {
        this.defectDate = defectDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setFristDate(String fristDate) {
        this.fristDate = fristDate;
    }

    public long getFristdate_df() {
        return fristdate_df;
    }

    public void setFristdate_df(long fristdate_df) {
        this.fristdate_df = fristdate_df;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public void setPlanItems(List<PlanItem> planItems) {
        this.planItems = planItems;
    }

    public List<PhotoModel> getDefectPhotoModelList() {
        return defectPhotoModelList;
    }

    public void setDefectPhotoModelList(List<PhotoModel> defectPhotoModelList) {
        this.defectPhotoModelList = defectPhotoModelList;
    }

    public List<DefectTradeModel> getDefectTradeModelList() {
        return defectTradeModelList;
    }

    public void setDefectTradeModelList(List<DefectTradeModel> defectTradeModelList) {
        this.defectTradeModelList = defectTradeModelList;
    }

    public List<Pdflawflag> getPdflawflagList() {
        return pdflawflagList;
    }

    public void setPdflawflagList(List<Pdflawflag> pdflawflagList) {
        this.pdflawflagList = pdflawflagList;
    }

    public void setNoticeDate(String noticeDate) {
        this.noticeDate = noticeDate;
    }

    public long getNoticeDate_df() {
        return noticeDate_df;
    }

    public void setNoticeDate_df(long noticeDate_df) {
        this.noticeDate_df = noticeDate_df;
    }

    public void setNotifieDate(String notifieDate) {
        this.notifieDate = notifieDate;
    }

    public long getNotifiedate_df() {
        return notifiedate_df;
    }

    public void setNotifiedate_df(long notifiedate_df) {
        this.notifiedate_df = notifiedate_df;
    }

    public void setSecondFristDate(String secondFristDate) {
        this.secondFristDate = secondFristDate;
    }

    public long getSecondFristDate_df() {
        return secondFristDate_df;
    }

    public void setSecondFristDate_df(long secondFristDate_df) {
        this.secondFristDate_df = secondFristDate_df;
    }

    public void setDoneDate(String doneDate) {
        Utils.showLogger("settingDate>>"+doneDate);
        this.doneDate = doneDate;
    }

    public long getDonedate_df() {
        return donedate_df;
    }

    public void setDonedate_df(long donedate_df) {
        this.donedate_df = donedate_df;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public long getCreateDate_df() {
        return createDate_df;
    }

    public void setCreateDate_df(long createDate_df) {
        this.createDate_df = createDate_df;
    }

    public void setResponsibleUser(String responsibleUser) {
        this.responsibleUser = responsibleUser;
    }

    @SerializedName("donedate_df")
    public long donedate_df;
    ;

    @SerializedName("createDate")
    public String createDate;

    @SerializedName("creator_id")
    public String creator_id;

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    @SerializedName("createDate_df")
    public long createDate_df;

    @SerializedName("responsibleuser")
    @ColumnInfo(name = "responsibleuser")
    String responsibleUser;


//    public DefectsModel(@NonNull String defectId, String runId, String projectId, String userId, String defectType, String defectName, String defectDate, String status, String creator, String description, String created, List<FlawItem> flawItems, String fristDate, String deleted, String planItems, String noticeDate, String notifieDate, String secondFristDate, String doneDate, String responsibleUser) {
//        this.defectId = defectId;
//        this.runId = runId;
//        this.projectId = projectId;
//        this.userId = userId;
//        this.defectType = defectType;
//        this.defectName = defectName;
//        this.defectDate = defectDate;
//        this.status = status;
//        this.creator = creator;
//        this.description = description;
//        this.created = created;
//        this.flawItems = flawItems;
//        this.fristDate = fristDate;
//        this.deleted = deleted;
//        this.planItems = planItems;
//        this.noticeDate = noticeDate;
//        this.notifieDate = notifieDate;
//        this.secondFristDate = secondFristDate;
//        this.doneDate = doneDate;
//        this.responsibleUser = responsibleUser;
//    }dateString1


    public DefectsModel(@NonNull String defectId, String runId, String projectId, String userId, String defectType, String defectName, String defectDate, String status, String creator, String description, String created, String fristDate, String deleted, String noticeDate, String notifieDate, String secondFristDate, String doneDate, String responsibleUser,String lastupdate) {
        this.defectId = defectId;
        this.runId = runId;
        this.projectId = projectId;
        this.userId = userId;
        this.defectType = defectType;
        this.defectName = defectName;
        this.defectDate = defectDate;
        this.status = status;
        this.creator = creator;
        this.description = description;
        this.created = created;
        this.fristDate = fristDate;
        this.deleted = deleted;
        this.noticeDate = noticeDate;
        this.notifieDate = notifieDate;
        this.secondFristDate = secondFristDate;
        this.doneDate = doneDate;
        this.responsibleUser = responsibleUser;
        this.lastupdate = lastupdate;
    }

    @NonNull
    public String getDefectId() {
        return defectId;
    }

    public String getRunId() {
        return runId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDefectType() {
        return defectType;
    }

    public String getDefectName() {
        return defectName;
    }

    public String getDefectDate() {
        return defectDate;
    }

    public String getStatus() {
        return status;
    }

    public String getCreator() {
        return creator;
    }

    public String getDescription() {
        return description;
    }

    public String getCreated() {
        return created;
    }

//    @TypeConverter
//    public String getFlawItems() {
//        Gson gson = new Gson();
//        return gson.toJson(flawItems);
//    }
//
//    @TypeConverter
//    public void setFlawItems(String flawItemsString) {
//        Type listType = new TypeToken<List<FlawItem>>() {}.getType();
//        flawItems   =    new Gson().fromJson(flawItemsString, listType);
//    }

    @TypeConverter
    public String getPlanItems() {
        Gson gson = new Gson();
        return gson.toJson(planItems);
    }

    @TypeConverter
    public void setPlanItems(String planItemsString) {
        Type listType = new TypeToken<List<PlanItem>>() {
        }.getType();
        planItems = new Gson().fromJson(planItemsString, listType);
    }

    @TypeConverter
    public String getDefectPhotoModel() {
        Gson gson = new Gson();
        return gson.toJson(defectPhotoModelList);
    }

    @TypeConverter
    public void setDefectPhotoModel(String defectPhotoString) {
        Type listType = new TypeToken<List<PhotoModel>>() {
        }.getType();
        defectPhotoModelList = new Gson().fromJson(defectPhotoString, listType);
        if (defectPhotoModelList != null && defectPhotoModelList.size() > 0) {
            defectPhotoModelList.get(defectPhotoModelList.size() - 1).setFlaw_id(getDefectId());
            defectPhotoModelList.get(defectPhotoModelList.size() - 1).setProjectId(getProjectId());
        }
    }

    @TypeConverter
    public String getPdFlawFlagList() {
        Gson gson = new Gson();
        return gson.toJson(pdflawflagList);
    }

    @TypeConverter
    public void setpdFlawFlagModel(String defectPhotoString) {
        Type listType = new TypeToken<List<Pdflawflag>>() {
        }.getType();
        pdflawflagList = new Gson().fromJson(defectPhotoString, listType);
        if (pdflawflagList != null && pdflawflagList.size() > 0) {
            pdflawflagList.get(pdflawflagList.size() - 1).setFlaw_Id(getDefectId());
            pdflawflagList.get(defectPhotoModelList.size() - 1).setPdProjectid(getProjectId());
        }
    }

    @TypeConverter
    public String getDefectTradeModel() {
        Gson gson = new Gson();
        return gson.toJson(defectTradeModelList);
    }

    @TypeConverter
    public void setDefectTradeModel(String defectPhotoString) {
        Type listType = new TypeToken<List<DefectTradeModel>>() {
        }.getType();
        defectTradeModelList = new Gson().fromJson(defectPhotoString, listType);
        if (defectTradeModelList != null && defectTradeModelList.size() > 0) {
            defectTradeModelList.get(defectTradeModelList.size() - 1).setPdflawid(getDefectId());
        }
    }

    public String getFristDate() {
        return fristDate;
    }

    public String getDeleted() {
        return deleted;
    }

    public String getNoticeDate() {
        return noticeDate;
    }

    public String getNotifieDate() {
        return notifieDate;
    }

    public String getSecondFristDate() {
        return secondFristDate;
    }

    public String getDoneDate() {
        return doneDate;
    }

    public String getResponsibleUser() {
        return responsibleUser;
    }

    public String getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(String lastupdate) {
        this.lastupdate = lastupdate;
    }

    private String extra1;
    private String extra2;
    private String extra3;
    private String extra4;
    private String extra5;



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
