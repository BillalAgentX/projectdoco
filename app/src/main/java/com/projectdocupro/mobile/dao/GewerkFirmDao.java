package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.GewerkFirmModel;

import java.util.List;

@Dao
public interface GewerkFirmDao {

    @Insert
    void insert(GewerkFirmModel wordModel);

    @Query("DELETE FROM wordmodel")
    void deleteAll();

    @Query("SELECT * from GewerkFirmModel" )
    LiveData<List<GewerkFirmModel>> getGewerkFirmLDataList();

    @Query("SELECT * from GewerkFirmModel s")
    List<GewerkFirmModel> getGewerkFirmList();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(GewerkFirmModel wordModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAll(List<GewerkFirmModel> wordModels);

    @Query("DELETE FROM GewerkFirmModel WHERE pdprojectid = :projectId")
    void deleteByProjectId(String projectId);
}
