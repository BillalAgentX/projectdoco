package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;

import java.util.List;

@Dao
public interface ProjectUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ProjectUserModel wordModel);

    @Query("DELETE FROM ProjectUserModel WHERE ProjectUserModel.projectId = :projectID")
    void deleteUsingProjectId(String projectID);

    @Query("DELETE FROM ProjectUserModel")
    void deleteAll();

    @Query("SELECT * from ProjectUserModel WHERE ProjectUserModel.projectId = :projectID")
    LiveData<List<ProjectUserModel>> getUserProjectLDataList(String projectID);

    @Query("SELECT * from ProjectUserModel WHERE  ProjectUserModel.projectId = :projectID ")
    List<ProjectUserModel> getUserProjectList(String projectID);

    @Query("SELECT * from ProjectUserModel WHERE  ProjectUserModel.projectId = :projectID AND ProjectUserModel.pduserid = :pdUserId LIMIT 1")
    ProjectUserModel getProjectUserInfo(String projectID,String pdUserId);

    @Query("SELECT * from ProjectUserModel ")
    List<ProjectUserModel> getUserProjectAllList();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ProjectUserModel ProjectUserModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAll(List<ProjectUserModel> wordModels);


    @Query("SELECT DISTINCT * from ProjectUserModel WHERE  ProjectUserModel.projectId = :projectID AND ProjectUserModel.pduserid IN (:pdUserId)")
    List<ProjectUserModel> getProjectUserListInfo(String projectID, List<String> pdUserId);

    @Query("SELECT DISTINCT * from ProjectUserModel")
    List<ProjectUserModel> getDistinctProjectUserListInfo();

    @Query("DELETE FROM ProjectUserModel WHERE projectId = :projectId")
    void deleteByProjectId(String projectId);

}
