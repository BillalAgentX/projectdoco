package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.projectdocupro.mobile.models.OnlinePhotoModel;

import java.util.List;

@Dao
public interface OnlinePhotoDao {

    public static final String TYPE_LOCAL_PHOTO = "local_photo";
    public static final String TYPE_MANGEL_PHOTO = "mangel_photo";
    public static final String TYPE_ONLINE_PHOTO = "online_photo";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OnlinePhotoModel onlinePhotoModel);

    @Query("DELETE FROM OnlinePhotoModel")
    void deleteAll();

    @Query("SELECT * from onlinePhotoModel WHERE projectId = :projectId AND onlinePhotoModel.photo_type='local_photo'")
    LiveData<List<OnlinePhotoModel>> getPhotoModel(String projectId);

    @Query("SELECT * from onlinePhotoModel WHERE projectId = :projectId AND onlinePhotoModel.photo_type='online_photo'")
    List<OnlinePhotoModel> getAllOnlinePhotos(String projectId);

    @Query("SELECT * from OnlinePhotoModel WHERE pdphotolocalId = :photoId")
    OnlinePhotoModel getPhotoModel(long photoId);


    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(OnlinePhotoModel onlinePhotoModel);

    @Query("SELECT * from photomodel WHERE pdphotolocalId = :photoId")
    LiveData<OnlinePhotoModel> getUpdatedPhotoModel(long photoId);

    @Query("SELECT * from onlinePhotoModel WHERE projectId = :projectId   ORDER BY onlinePhotoModel.created_df DESC ")
    List<OnlinePhotoModel>  getPhotosList(String projectId);


    @Query("SELECT * from onlinePhotoModel WHERE projectId = :projectId AND onlinePhotoModel.pdphotoid in (:stringList)  ORDER BY onlinePhotoModel.created_df DESC ")
    List<OnlinePhotoModel> getPhotosListUsingPhotoIds(String projectId, List<String> stringList);

    //    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId AND (photo_type='mangel_photo' OR photomodel.defectAdded=1) ORDER BY photomodel.created_df DESC ")
    @Query("SELECT  DISTINCT * from onlinePhotoModel WHERE projectId = :projectId  AND onlinePhotoModel.flaw_id=:flawId ORDER BY onlinePhotoModel.created_df DESC ")
    List<OnlinePhotoModel> getDefectPhotosList(String projectId, String flawId);

    @Query("SELECT  DISTINCT * from onlinePhotoModel WHERE projectId = :projectId  AND onlinePhotoModel.local_flaw_id=:flawId ORDER BY onlinePhotoModel.created_df DESC ")
    List<OnlinePhotoModel> getDefectPhotosAndLocalPhotosList(String projectId, String flawId);

    @Query("SELECT * from onlinePhotoModel WHERE projectId = :projectId  AND onlinePhotoModel.local_flaw_id=:flawId AND photo_type='mangel_photo' ORDER BY onlinePhotoModel.created_df DESC ")
    LiveData<List<OnlinePhotoModel>> getDefectPhotosObserveList(String projectId, String flawId);

    @Query("SELECT DISTINCT onlinePhotoModel.pdUserId from onlinePhotoModel WHERE projectId = :projectId AND photo_type=:photo_type")
    List<String> getPhotosUserIdList(String projectId, String photo_type);

    @Query("SELECT  DISTINCT * from onlinePhotoModel WHERE projectId = :projectId  AND pdphotolocalId = :photo_Id ")
    List<OnlinePhotoModel> getDefectPhotosListUsingLoalID(String projectId, String photo_Id);

    @Query("SELECT onlinePhotoModel.local_flaw_id from onlinePhotoModel WHERE projectId = :projectId AND local_flaw_id=:flawId  AND pdphotolocalId=:photoID AND photo_type='local_photo'")
    List<String> getDefectIdsAttachedWithPhotos(String projectId, String flawId, String photoID);

    @RawQuery
    List<OnlinePhotoModel> getFilterListViaQuery(SupportSQLiteQuery query);

    @Query("SELECT  DISTINCT * from onlinePhotoModel WHERE projectId = :projectId    LIMIT :pageSize OFFSET :offset")
    List<OnlinePhotoModel> getOnlinePhotoUsingLimitList(String projectId, String offset, String pageSize);

    @Query("SELECT  DISTINCT * from onlinePhotoModel WHERE projectId = :projectId ORDER BY created_df DESC")
    List<OnlinePhotoModel> getOnlinePhotoUsingLimitList2(String projectId);

    @Query("SELECT  DISTINCT COUNT(*) from onlinePhotoModel WHERE projectId = :projectId ")
    int  getOnlinePhotoDBCount(String projectId);

    @Query("SELECT * from OnlinePhotoModel WHERE  OnlinePhotoModel.projectId = :projectId AND OnlinePhotoModel.pdphotoid = :pdphotoid LIMIT 1")
    OnlinePhotoModel getOnlinePhotoModelOBJ(String projectId, String pdphotoid);

    @Query("SELECT * from OnlinePhotoModel WHERE projectId = :projectId   ORDER BY onlinePhotoModel.created_df DESC ")
    LiveData<List<OnlinePhotoModel>> getPhotosOnlineObsList(String projectId);

    @Query("SELECT  pdphotolocalId,created ,created_df,isPhotoCached,wordAdded,planAdded,defectAdded,recordingAdded,brushImageAdded, MIN(OnlinePhotoModel.created_df) from OnlinePhotoModel WHERE projectId = :projectId AND photo_type=:photo_type LIMIT 1")
    List<OnlinePhotoModel> getMinCreatedPhoto(String projectId, String photo_type);

    @Query("DELETE FROM OnlinePhotoModel WHERE projectId = :projectId")
    void deleteByProjectId(String projectId);
}
