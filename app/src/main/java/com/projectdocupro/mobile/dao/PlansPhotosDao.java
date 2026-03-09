package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.PlansPhotoModel;

import java.util.List;

@Dao
public interface PlansPhotosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PlansPhotoModel plansPhotoModel);

    @Query("DELETE FROM plansPhotoModel")
    void deleteAll();


    @Query("DELETE FROM plansPhotoModel WHERE projectId = :projectId ")
    void deleteRowUsingId(String projectId);

//    @Query("SELECT * from defectPhotoModel WHERE projectid = :projectId")
    @Query("SELECT * from plansPhotoModel")
   LiveData< List<PlansPhotoModel>> getPlanPhotoModel(/*String projectId*/);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(PlansPhotoModel photoModel);


    @Query("SELECT * from plansPhotoModel WHERE projectId = :projectId")
    List<PlansPhotoModel> getPlansListPhotoObject(String projectId);

    @Query("SELECT * from plansPhotoModel WHERE planId = :planID LIMIT 1")
    PlansPhotoModel getPlansPhotoObject(String planID);

    @Query("SELECT * from plansPhotoModel ")
    List<PlansPhotoModel> getAllPlansPhotoModel();
}
