package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.interfaces.IGetProjectsList;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.repos.ProjectRepository;

import java.util.List;

public class ProjectsViewModel extends AndroidViewModel {

    private ProjectRepository mRepository;
    private LiveData<List<ProjectModel>> mAllProjects;

    Context context;
    public ProjectsViewModel(@NonNull Application application) {
        super(application);
        context=application;

    }
    public void init(Context mContext, IGetProjectsList iGetProjectsList){
        mRepository = new ProjectRepository(mContext,iGetProjectsList);
        mAllProjects = mRepository.getAllProjects();
    }

    LiveData<List<ProjectModel>> getAllWords() { return mAllProjects; }

    public void insert(ProjectModel projectModel) { mRepository.insert(projectModel); }

}
