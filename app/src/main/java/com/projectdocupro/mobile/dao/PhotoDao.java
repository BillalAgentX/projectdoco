package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;

import java.util.List;

@Dao
public interface PhotoDao {

    int photoMaxFailedCount = LocalPhotosRepository.MAX_PHOTO_FAILED_COUNT;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PhotoModel photoModel);

    @Query("DELETE FROM photomodel")
    void deleteAll();

    @Query("DELETE FROM photomodel WHERE projectId = :projectId")
    void deleteUsingProjectId(String projectId);

    @Query("DELETE FROM photomodel WHERE pdphotolocalId = :photoId")
    void deleteUsingPhotoId(long photoId);

    @Query("DELETE FROM photomodel WHERE projectId = :projectId AND pdphotolocalId=:photoId")
    void deleteUsingPhotoId(String projectId, String photoId);

    @Query("SELECT * from photomodel WHERE projectId = :projectId AND photoModel.photo_type='local_photo'  ORDER BY photomodel.created_df DESC ")
    LiveData<List<PhotoModel>> getPhotoModel(String projectId);

    @Query("SELECT * from photomodel WHERE projectId = :projectId AND photoModel.photo_type='local_photo'  ORDER BY photomodel.created_df DESC ")
    List<PhotoModel> getPhotoLocalList(String projectId);


    @Query("SELECT * from photomodel WHERE projectId = :projectId")
    List<PhotoModel> allPhotosOfProject(String projectId);

    @Query("SELECT * from photomodel WHERE pdphotolocalId = :photoId")
    PhotoModel getPhotoModel(long photoId);

    @Query("SELECT * from photomodel WHERE pdphotoid = :photoId")
    PhotoModel getPhotoModelGlobal(long photoId);


    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(PhotoModel photoModel);

    @Query("SELECT * from photomodel WHERE pdphotolocalId = :photoId")
    LiveData<PhotoModel> getUpdatedPhotoModel(long photoId);

    @Query("SELECT * from photomodel WHERE projectId = :projectId   ORDER BY photomodel.created_df DESC ")
    List<PhotoModel> getPhotosList(String projectId);

    @Query("SELECT * from photomodel WHERE pdphotolocalId = :photoId")
    PhotoModel getPhotosOBJ(String photoId);


    //    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId AND (photo_type='mangel_photo' OR photomodel.defectAdded=1) ORDER BY photomodel.created_df DESC ")
    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId  AND photomodel.flaw_id=:flawId ORDER BY photomodel.created_df DESC ")
    List<PhotoModel> getDefectPhotosList(String projectId, String flawId);

    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId ORDER BY photomodel.created_df DESC ")
    List<PhotoModel> getDefectPhotosAndLocalPhotosList(String projectId, String flawId);

    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId ORDER BY photomodel.created_df DESC ")
    LiveData<List<PhotoModel>> getDefectPhotosAndLocalPhotosListLiveData(String projectId, String flawId);


    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId AND photomodel.photoUploadStatus !=2 AND failedCount<=10 ORDER BY photomodel.created_df DESC ")
    List<PhotoModel> getDefectPhotosAndUnSyncLocalPhotosList(String projectId, String flawId);

    @Query("SELECT Count(*)  from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId AND photomodel.photoUploadStatus!=2 ORDER BY photomodel.created_df DESC ")
    long getDefectPhotosAndUnSyncLocalPhotosCount(String projectId, String flawId);


    @Query("SELECT Count(*)  from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId AND photomodel.photoUploadStatus=1 ORDER BY photomodel.created_df DESC ")
    long getDefectPhotosAndUploadingLocalPhotosCount(String projectId, String flawId);


    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId ORDER BY photomodel.created_df DESC LIMIT 1")
    PhotoModel getDefectPhotosAndLocalPhotosOBj(String projectId, String flawId);

    @Query("SELECT * from photomodel WHERE projectId = :projectId  AND photomodel.local_flaw_id=:flawId AND photo_type='mangel_photo' ORDER BY photomodel.created_df DESC ")
    LiveData<List<PhotoModel>> getDefectPhotosObserveList(String projectId, String flawId);

    @Query("SELECT  DISTINCT photomodel.pdUserId from photomodel WHERE projectId = :projectId AND photo_type=:photo_type")
    List<String> getPhotosUserIdList(String projectId, String photo_type);

    @Query("SELECT  DISTINCT photomodel.pdUserId from photomodel WHERE projectId = :projectId")
    List<String> getPhotosAllUserIdList(String projectId);

    @Query("SELECT  pdphotolocalId,created ,failedCount,isUserSelectedStatus,isPhotoSynced,created_df,isPhotoCached,wordAdded,planAdded,defectAdded,isFromGallery,recordingAdded,brushImageAdded, MIN(photomodel.created_df) from photomodel WHERE projectId = :projectId AND photo_type=:photo_type LIMIT 1")
    List<PhotoModel> getMinCreatedPhoto(String projectId, String photo_type);

    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId  AND pdphotolocalId = :photo_Id ")
    List<PhotoModel> getDefectPhotosListUsingLoalID(String projectId, String photo_Id);

    @Query("SELECT photomodel.local_flaw_id from photomodel WHERE projectId = :projectId AND local_flaw_id=:flawId  AND pdphotolocalId=:photoID AND photo_type='local_photo'")
    List<String> getDefectIdsAttachedWithPhotos(String projectId, String flawId, String photoID);

    @Query("SELECT * from photomodel WHERE projectId = :projectId AND local_flaw_id=:local_flaw_id  AND photoUploadStatus=2")
    List<PhotoModel> getPhotosForSyncing(String projectId, String local_flaw_id);
    @Query("SELECT * from photomodel WHERE projectId = :projectId AND local_flaw_id=:local_flaw_id  AND pdphotoid!=''  ")
    List<PhotoModel> getPhotosForSyncing2ServerId(String projectId, String local_flaw_id);


    //    @Query("SELECT * from photomodel WHERE projectId = :projectId AND  photo_type=:photoType    AND failedCount<=:maxCount")
//    @Query("SELECT * from photomodel WHERE projectId = :projectId AND photoUploadStatus=:photoUploadStatus AND photo_type=:photoType   AND isPhotoSynced=0  AND failedCount<=:maxCount ORDER BY photomodel.created_df DESC")
    @Query("SELECT * from photomodel WHERE projectId = :projectId AND photoUploadStatus=:photoUploadStatus AND photo_type=:photoType   AND isPhotoSynced=0 AND isUserSelectedStatus=1  AND failedCount<=10  ORDER BY photomodel.created_df ASC")
//    List<PhotoModel>  localUploadingPhotosCount(String projectId,String photoType,int maxCount);
    List<PhotoModel> userActionUploadinglocalPhotosCount(String projectId, String photoUploadStatus, String photoType/*,int maxCount*/);

    @Query("SELECT * from photomodel WHERE projectId = :projectId AND photoUploadStatus=:photoUploadStatus AND photo_type=:photoType   AND isPhotoSynced=0 AND isUserSelectedStatus=1  AND failedCount<=10  ORDER BY photomodel.created_df DESC")
//    List<PhotoModel>  localUploadingPhotosCount(String projectId,String photoType,int maxCount);
    List<PhotoModel> userActionUploadinglocalPhotosCountDESC(String projectId, String photoUploadStatus, String photoType/*,int maxCount*/);

    @Query("SELECT * from photomodel WHERE projectId = :projectId AND photoUploadStatus=:photoUploadStatus AND photo_type=:photoType   AND isPhotoSynced=0 AND isUserSelectedStatus=0 AND failedCount<=10  ORDER BY photomodel.created_df ASC")
//    List<PhotoModel>  localUploadingPhotosCount(String projectId,String photoType,int maxCount);
    List<PhotoModel> autoUploadinglocalPhotosCount(String projectId, String photoUploadStatus, String photoType/*,int maxCount*/);


    @Query("SELECT * from photomodel WHERE projectId = :projectId AND photoUploadStatus=:photoUploadStatus AND photo_type=:photoType   AND isPhotoSynced=0 AND isUserSelectedStatus=0 AND failedCount<=10  ORDER BY photomodel.created_df DESC")
//    List<PhotoModel>  localUploadingPhotosCount(String projectId,String photoType,int maxCount);
    List<PhotoModel> autoUploadinglocalPhotosCountDESC(String projectId, String photoUploadStatus, String photoType/*,int maxCount*/);


    @Query("SELECT * from photomodel WHERE  photoUploadStatus=:photoUploadStatus AND photo_type=:photoType   AND isPhotoSynced=0 AND isUserSelectedStatus=0  AND failedCount<=10  ORDER BY photomodel.failedCount ASC")
    List<PhotoModel> autoUploadingLocalPhotosCountAllProjects(String photoUploadStatus, String photoType/*,int maxCount*/);

    @Query("SELECT * from photomodel WHERE  photoUploadStatus!=2 AND photo_type=:photoType   AND isPhotoSynced=0 AND isUserSelectedStatus=0   AND failedCount<=10  ORDER BY photomodel.failedCount ASC")
    List<PhotoModel> autoUploadinglocalPhotosAllProjects(/*String photoUploadStatus,*/String photoType/*,int maxCount*/);


    @Query("SELECT * from photomodel WHERE  photoUploadStatus!=2 AND photo_type=:photoType  ")
    List<PhotoModel> markUnSyncUploadingPhotos(/*String photoUploadStatus,*/String photoType/*,int maxCount*/);


    @Query("SELECT * from photomodel")
    List<PhotoModel> getAllPhotos();

    @RawQuery
    List<PhotoModel> getFilterListViaQuery(SupportSQLiteQuery query);

    @Query("SELECT Count(*) from photomodel WHERE projectId = :projectId AND photoModel.photo_type='local_photo' AND isPhotoSynced=1 ")
    long getSyncedPhotoCount(String projectId);

    @Query("SELECT Count(*) from photomodel WHERE projectId = :projectId AND photoModel.photo_type='local_photo' AND photoUploadStatus=3 ")
    long getShortlySyncedPhotoCount(String projectId);

    @Query("SELECT Count(*) from photomodel WHERE projectId = :projectId AND photoModel.photo_type='local_photo' AND isPhotoSynced=0 ")
    long getUnSyncedPhotoCount(String projectId);

    @Query("SELECT * from photomodel WHERE photoModel.photo_type='local_photo' AND isPhotoSynced=0 ")
    List<PhotoModel> getAllUnSyncedPhotoCount();

    @Query("SELECT Count(*) from photomodel WHERE photoModel.photo_type='local_photo' AND isPhotoSynced=1 ")
    long getSyncedPhotoCountAllProject();

    @Query("SELECT Count(*) from photomodel WHERE photoModel.photo_type='local_photo' AND photoUploadStatus=3 ")
    long getShortlySyncedPhotoCountAllProject();

    @Query("SELECT Count(*) from photomodel WHERE  photoModel.photo_type='local_photo' AND isPhotoSynced=0 ")
    long getUnSyncedPhotoCountAllProject();


    @Query("SELECT Count(*) from photomodel WHERE projectId = :projectId AND photoModel.photo_type='local_photo'")
    long getAllPhotoCountProject(String projectId);

    @Query("SELECT * from photomodel WHERE  photoModel.photo_type='local_photo' AND isPhotoSynced=0 ORDER BY photomodel.created_df ASC")
    List<PhotoModel> getUnSyncedPhotoListASC();

    @Query("SELECT * from photomodel WHERE  photoModel.photo_type='local_photo' AND isPhotoSynced=0 ORDER BY photomodel.created_df DESC")
    List<PhotoModel> getUnSyncedPhotoListDESC();


    @Query("SELECT  DISTINCT * from photomodel WHERE projectId = :projectId   AND photomodel.photoUploadStatus !=2 ORDER BY photomodel.created_df DESC ")
    List<PhotoModel> getUnSyncLocalPhotosList(String projectId);


    @Query("SELECT Count(*) from photomodel WHERE projectId = :projectId AND photoModel.photo_type='local_photo' AND photomodel.photoUploadStatus =1 ")
    long getUploadingPhotoCount(String projectId);

    @Query("SELECT Count(*) from photomodel WHERE projectId = :projectId AND photoModel.photo_type='local_photo' AND photomodel.photoUploadStatus =0 ")
    long getSpecificUnSyncPhotoCount(String projectId);

    @Query("UPDATE photomodel SET photoUploadStatus = :status WHERE pdphotolocalId = :tid")
    int updateStatus(long tid, String status);

    @Query("SELECT * from photomodel WHERE failedCount >= 6")
    List<PhotoModel> getPhotosWithFailedCountGreaterThanSix();


}
