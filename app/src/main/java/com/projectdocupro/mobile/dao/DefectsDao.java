package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.projectdocupro.mobile.models.DefectsModel;

import java.util.List;

@Dao
public interface DefectsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DefectsModel defectsModel);

    @Query("DELETE FROM defectsmodel")
    void deleteAll();

    @Query("DELETE FROM defectsmodel  WHERE projectId = :projectId")
    void deleteUsingProjectId(String projectId);

    @Query("DELETE FROM defectsmodel  WHERE defectLocalId = :localDefectId")
    void deleteUsingLocalDefectId(long localDefectId);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId ORDER BY defectLocalId DESC")
    LiveData<List<DefectsModel>> getDefectsList(String projectId);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId ORDER BY defectLocalId DESC")
    List<DefectsModel> getDefectsSimpleList(String projectId);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId AND runidInt !=0 AND deleted='0' ORDER BY runidInt DESC")
    List<DefectsModel> getDefectsListByRunId(String projectId);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId AND runidInt =0 ORDER BY runidInt DESC")
    List<DefectsModel> getDefectsListEmptyRunId(String projectId);

    @Query("SELECT * from defectsmodel WHERE defectLocalId = :defectLocalId  LIMIT 1")
    LiveData<DefectsModel> getDefectsObject(String defectLocalId);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId AND defectLocalId = :defectLocalId  LIMIT 1")
    DefectsModel getDefectsObjectt(String projectId, String defectLocalId);

    @Query("SELECT DISTINCT defectsmodel.status from defectsmodel WHERE projectId = :projectId ")
    List<String> getDefectsUniqueStatusObject(String projectId);

    @Query("SELECT DISTINCT defectsmodel.flawtype from defectsmodel WHERE projectId = :projectId ")
    List<String> getDefectsUniqueDefectObject(String projectId);

    @Query("SELECT DISTINCT defectsmodel.responsibleUser from defectsmodel WHERE projectId = :projectId ")
    List<String> getDefectsUniqueResponsibleUserObject(String projectId);

    @Query("SELECT DISTINCT defectsmodel.creator from defectsmodel WHERE projectId = :projectId ")
    List<String> getDefectsUniqueUserCreatorObject(String projectId);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId AND defectLocalId = :pdFlawId ORDER BY pdflawid ASC LIMIT 1")
    DefectsModel getDefectsObjectWithDecipline(String projectId, String pdFlawId);

    @Query("SELECT * from defectsmodel")
    int getDefectsCount();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(DefectsModel defectsModel);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId ORDER BY defectLocalId ASC")
    List<DefectsModel> getDefectsListtt(String projectId);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId AND status IN(:status_Id)  AND flawtype =:art_Id  AND fristdate_df <=:deadline_date ORDER BY defectLocalId ASC")
    List<DefectsModel> getFilteredDefectList(String projectId, List<String> status_Id, String art_Id, long deadline_date);

    @Query("SELECT DISTINCT defectsmodel.defectLocalId from defectsmodel WHERE projectId = :projectId ")
    List<Long> getDefectsUniqueDefectIds(String projectId);

    @Query("SELECT * from defectsmodel WHERE projectId = :projectId AND defectLocalId=:local_flaw_id")
    DefectsModel getDefectsOBJ(String projectId, String local_flaw_id);

    @RawQuery
    List<DefectsModel> getFilterListViaQuery(SupportSQLiteQuery query);

    @Query("SELECT Count(*) from defectsmodel WHERE projectId = :projectId AND defectsmodel.uploadStatus=2 ")
    long getSyncedPhotoCount(String projectId);

    @Query("SELECT Count(*) from defectsmodel WHERE projectId = :projectId AND defectsmodel.uploadStatus!=2 ")
    long getUnSyncedPhotoCount(String projectId);

    @Query("SELECT Count(*) from defectsmodel WHERE  defectsmodel.uploadStatus=2 ")
    long getSyncedPhotoCountAllProject();

    @Query("SELECT Count(*) from defectsmodel WHERE  defectsmodel.uploadStatus!=2 ")
    long getUnSyncedPhotoCountAllProject();


    @Query("SELECT Count(*) from defectsmodel WHERE  projectId = :projectId ")
    long getdefectCountofProject(String projectId);


    @Query("SELECT Count(*) from defectsmodel WHERE projectId = :projectId AND defectsmodel.uploadStatus=1 ")
    long getUploadingPhotoCount(String projectId);

    @Query("SELECT Count(*) from defectsmodel WHERE projectId = :projectId AND defectsmodel.uploadStatus=0 ")
    long getSpecificUnSyncedPhotoCount(String projectId);


    @Query("SELECT * from defectsmodel ")
    List<DefectsModel> getAllDefects();

    @Query("SELECT * from defectsmodel WHERE  projectId = :projectId AND defectsmodel.uploadStatus!=2 ")
    List<DefectsModel> getUnSyncedDefectList(String projectId);

    @Query("SELECT EXISTS(SELECT * FROM defectsmodel WHERE pdflawid = :pdflawid)")
    boolean getRecordExistOrNot(String pdflawid);

    @Query("SELECT * from defectsmodel WHERE pdflawid = :pdflawid")
    DefectsModel getDefectsExitOrNot(String pdflawid);


}
