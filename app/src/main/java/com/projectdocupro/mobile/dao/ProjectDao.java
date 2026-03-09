package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.ProjectModel;

import java.util.List;

@Dao
public interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProjectModel projectModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ProjectModel    projectModel);

    @Query("DELETE FROM projectmodel")
    void deleteAll();

    @Query("SELECT * from projectmodel Where projectid = :project_id ")
    ProjectModel getProjectOBJ(String project_id);


    @Query("SELECT * from projectmodel ORDER BY projectid ASC")
    LiveData<List<ProjectModel>> getAllProjects();

    @Query("SELECT * from projectmodel ORDER BY projectid ASC")
    List<ProjectModel> getAllProjectsList();

    @Query("SELECT * from projectmodel WHERE extra1 =1 ORDER BY lastupdated DESC")
    List<ProjectModel> getAllActive();

    @Query("SELECT * from projectmodel WHERE extra1 =0 ORDER BY lastupdated DESC")
    List<ProjectModel> getAllInActive();


    @Query("SELECT * from projectmodel ORDER BY lastupdated DESC")
    List<ProjectModel> getAllItems();

    @Query("SELECT * from projectmodel WHERE project_name LIKE :projectName")
    LiveData<List<ProjectModel>> getSearchedProjects(String projectName);

    @Query("SELECT * from projectmodel WHERE favorite = 1 AND syncStatus =2  ORDER BY projectid ASC")
    LiveData<List<ProjectModel>> getFavoriteProjects();

    @Query("SELECT * from projectmodel WHERE favorite = 1 AND project_name LIKE :search")
    LiveData<List<ProjectModel>> getSearchFavoriteProjects(String search);

    @Query("SELECT * from projectmodel WHERE lastOpen > 0 ORDER BY lastOpen DESC")
    LiveData<List<ProjectModel>> getLastUsedProjects();

    @Query("SELECT * from projectmodel WHERE lastOpen > 0  AND project_name LIKE :search ORDER BY lastOpen DESC")
    LiveData<List<ProjectModel>> getSearchLastUsedProjects(String search);

    @Query("SELECT * from projectmodel WHERE projectmodel.favorite = 1")
    List<ProjectModel> getFavouriteProject();

    @Query("SELECT * from projectmodel WHERE projectmodel.projectid = :projectId  LIMIT 1")
    ProjectModel getSpecificProject(String projectId);

    @Query("DELETE FROM projectmodel WHERE projectId = :projectId")
    void deleteByProjectId(String projectId);
}
