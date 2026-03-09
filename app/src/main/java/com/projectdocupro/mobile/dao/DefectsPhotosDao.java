package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.DefectPhotoModel;


import java.util.List;

@Dao
public interface DefectsPhotosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DefectPhotoModel defectPhotoModel);

    @Query("DELETE FROM defectPhotoModel")
    void deleteAll();

    //    @Query("SELECT * from defectPhotoModel WHERE projectid = :projectId")
    @Query("SELECT * from defectPhotoModel")
    LiveData<List<DefectPhotoModel>> getDefectPhotoModel(/*String projectId*/);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(DefectPhotoModel photoModel);


    @Query("SELECT * from defectPhotoModel WHERE defectPhotoModel.projectid = :projectId AND  defectPhotoModel.flawId = :falwId")
    List<DefectPhotoModel> getDefectPhotoObject(String projectId, String falwId);

    @Query("SELECT * from defectPhotoModel ")
    List<DefectPhotoModel> getAllDefectPhotoModel();

    @Query("DELETE FROM defectphotomodel WHERE projectid = :projectId")
    void deleteByProjectId(String projectId);
}
