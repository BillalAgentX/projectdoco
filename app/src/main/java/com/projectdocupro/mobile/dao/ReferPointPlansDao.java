package com.projectdocupro.mobile.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.ReferPointJSONPlanModel;

import java.util.List;

@Dao
public interface ReferPointPlansDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReferPointJSONPlanModel referPointJSONPlanModel);

    @Query("DELETE FROM referPointJSONPlanModel")
    void deleteAll();

    @Query("DELETE FROM referPointJSONPlanModel WHERE pdProjectId = :projectId")
    void deleteUsingProjectId(String projectId);


    @Query("SELECT * from referPointJSONPlanModel WHERE pdProjectId = :projectId AND pdplanid=:planId")
    List<ReferPointJSONPlanModel> getReferPointList(String projectId,String planId);

    @Query("SELECT * from referPointJSONPlanModel ")
    List<ReferPointJSONPlanModel> getReferPointList();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ReferPointJSONPlanModel recordAudioModel);

    @Query("DELETE FROM referPointJSONPlanModel WHERE pdProjectId = :projectId")
    void deleteByProjectId(String projectId);

}
