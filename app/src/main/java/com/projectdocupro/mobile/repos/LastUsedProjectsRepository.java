package com.projectdocupro.mobile.repos;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.models.ProjectModel;

import java.util.List;

public class LastUsedProjectsRepository {

    private ProjectDao mProjectDao;
    private LiveData<List<ProjectModel>> mLastUsedProjects;

    public LastUsedProjectsRepository(Context context) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mProjectDao = db.projectDao();
        mLastUsedProjects = mProjectDao.getLastUsedProjects();
    }

    public LiveData<List<ProjectModel>> getLastUsedProjects() {
        return mLastUsedProjects;
    }

    public LiveData<List<ProjectModel>> getSearchResults(String    search) {
        return mProjectDao.getSearchLastUsedProjects(search);
    }

}
