package com.projectdocupro.mobile.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.projectdocupro.mobile.models.DefectTradeModel;

import java.util.List;

@Dao
public interface DefectsTradesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DefectTradeModel defectPhotoModel);

    @Query("DELETE FROM DefectTradeModel")
    void deleteAll();

    @Query("DELETE FROM DefectTradeModel Where DefectTradeModel.pdprojectid=:projectId")
    void deleteUsingProjectId(String projectId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(DefectTradeModel photoModel);


    @Query("SELECT * from DefectTradeModel ")
    List<DefectTradeModel> getAllDefectTradeModel();

    @Query("SELECT DISTINCT *  from DefectTradeModel Where DefectTradeModel.pdprojectid=:projectId AND DefectTradeModel.localpdflawid=:flaw_id  AND DefectTradeModel. selected=1")
    List<DefectTradeModel> getAllDefectTradeWithStatusONModel(String projectId,String flaw_id);

    @Query("SELECT DISTINCT *  from DefectTradeModel Where DefectTradeModel.pdprojectid=:projectId AND DefectTradeModel.localpdflawid IN(:flaw_id ) AND DefectTradeModel. selected=1")
    List<DefectTradeModel> getAllDefectTradeWithStatusONModelUsingDefectList(String projectId,List<String> flaw_id);

    @Query("SELECT *  from DefectTradeModel Where DefectTradeModel.pdprojectid=:projectId AND DefectTradeModel.selectvalue IN (:gewerkList) AND DefectTradeModel. selected=1")
    List<DefectTradeModel> getAllDefectTradeWithStatusONModelWithProject(String projectId,List<String> gewerkList);

    @Query("SELECT DISTINCT * from DefectTradeModel Where DefectTradeModel.pdprojectid=:projectId")
    List<DefectTradeModel> getUniqueDefect(String projectId);

    @Query("SELECT DISTINCT * from DefectTradeModel  Where DefectTradeModel.pdprojectid=:projectId ")
    List<DefectTradeModel> getUniqueDefectTrade(String projectId );

    @Query("SELECT DISTINCT * from DefectTradeModel  Where DefectTradeModel.pdprojectid=:projectId AND DefectTradeModel.pdflawid!='' LIMIT 1")
    DefectTradeModel getFirstDefectTradeWithServerID(String projectId );

    @Query("SELECT DISTINCT *  from DefectTradeModel Where DefectTradeModel.pdprojectid=:projectId AND DefectTradeModel.pdflawid=:flaw_id ")
    List<DefectTradeModel> getAllDefectTradeList(String projectId,String flaw_id);

    @Query("SELECT DISTINCT *  from DefectTradeModel Where DefectTradeModel.pdprojectid=:projectId AND DefectTradeModel.localpdflawid=:local_flaw_id ")
    List<DefectTradeModel> getAllDefectTradeWithLocalDefectIdList(String projectId,String local_flaw_id);

    @Query("DELETE FROM DefectTradeModel WHERE pdprojectid = :projectId")
    void deleteByProjectId(String projectId);
}
