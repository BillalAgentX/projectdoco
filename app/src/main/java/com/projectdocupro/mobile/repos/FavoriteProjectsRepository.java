package com.projectdocupro.mobile.repos;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.models.ProjectModel;

import java.util.List;

public class FavoriteProjectsRepository {

    private ProjectDao mProjectDao;
    private LiveData<List<ProjectModel>> mFavoriteProjects;

    public FavoriteProjectsRepository(Context context) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mProjectDao = db.projectDao();
        mFavoriteProjects = mProjectDao.getAllProjects();
    }

    public LiveData<List<ProjectModel>> getFavoriteProjects() {
        return mFavoriteProjects;
    }

    public LiveData<List<ProjectModel>> getSearchResults(String querry) {
        return mProjectDao.getSearchFavoriteProjects(querry);
    }

}
