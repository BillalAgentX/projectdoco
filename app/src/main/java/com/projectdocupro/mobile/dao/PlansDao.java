package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.PlansModel;

import java.util.List;

@Dao
public interface PlansDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlansModel plansModel);

    @Query("DELETE FROM plansmodel")
    void deleteAll();

    @Query("DELETE FROM plansmodel  WHERE plansmodel.projectId=:projectId ")
    void deleteUsingProjectId(String projectId);

    @Query("SELECT * from plansmodel ORDER BY planId ASC")
    LiveData<List<PlansModel>> getPlansList();

    @Query("SELECT * from plansmodel WHERE plansmodel.projectId=:projectId ORDER BY planId ASC")
    LiveData<List<PlansModel>> getPlansList(String projectId);

    @Query("SELECT * from plansmodel ORDER BY planId ASC")
    List<PlansModel> getAllPlansList();

    @Query("SELECT * from plansmodel WHERE plansmodel.projectId=:projectId ORDER BY planId ASC")
    List<PlansModel> getAllPlansList(String projectId);

    @Query("SELECT Count(*)  from plansmodel WHERE plansmodel.projectId=:projectId ORDER BY planId ASC")
    long getAllPlansAgaintProject(String projectId);

    @Query("SELECT * from plansmodel WHERE favorite = 1 ORDER BY projectid ASC")
    LiveData<List<PlansModel>> getFavoritePlans();

    @Query("SELECT * from plansmodel WHERE   plansmodel.projectId=:projectId AND favorite = 1 ORDER BY projectid ASC")
    LiveData<List<PlansModel>> getFavoritePlans(String projectId);

    @Query("SELECT * from plansmodel WHERE   plansmodel.projectId=:projectId AND favorite = 1 ORDER BY projectid ASC")
    List<PlansModel> getFavoritePlansBG(String projectId);

    @Query("SELECT DISTINCT * from plansmodel WHERE plansmodel.projectId=:projectId AND plansmodel.planId IN (:plansList)")
    List<PlansModel> getPlansUsingDefect(String projectId, List<String> plansList);

    @Query("SELECT DISTINCT * from plansmodel WHERE plansmodel.projectId=:projectId AND plansmodel.planId IN (:plansList)")
    PlansModel getPlansUsingDefectOBJ(String projectId, String plansList);

    @Query("SELECT * from plansmodel")
    int getPlansCount();

    @Query("SELECT * from plansmodel WHERE plansmodel.projectId =:projectId AND  plansmodel.planId=:planId")
    PlansModel getPlansUsingPlanID(String projectId, String planId);


    @Query("SELECT * from plansmodel WHERE plansmodel.planId=:planId")
    PlansModel getPlansUsingPlanIDOnly( String planId);

    @Query("SELECT * from plansmodel WHERE favorite = 1")
    int getFavouritePlansCount();

    @Update
    void update(PlansModel plansModel);
}
