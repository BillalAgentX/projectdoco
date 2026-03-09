package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.ProjectModel;

@Dao
public interface ProjectDetailDao {

    @Insert
    void insert(ProjectModel projectModel);

    @Query("DELETE FROM projectmodel")
    void deleteAll();

    @Query("SELECT * from projectmodel WHERE projectid = :projectId")
    LiveData<ProjectModel> getProject(String    projectId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ProjectModel    projectModel);
}
