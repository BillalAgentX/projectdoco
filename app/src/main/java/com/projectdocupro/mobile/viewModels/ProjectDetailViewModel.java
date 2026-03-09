package com.projectdocupro.mobile.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;
import com.projectdocupro.mobile.repos.ProjectDetailRepository;
import com.projectdocupro.mobile.repos.ProjectDetailsRepository;

import java.util.List;

public class ProjectDetailViewModel extends AndroidViewModel {

    private ProjectDetailsRepository mRepository;
    ProjectDetailRepository projectDetailRepository;
    public ProjectDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public ProjectDetailRepository getProjectDetailRepository() {
        return projectDetailRepository;
    }

    public void setProjectDetailRepository(ProjectDetailRepository projectDetailRepository) {
        this.projectDetailRepository = projectDetailRepository;
    }

    public void InitRepo(String  projectId){
        mRepository = new ProjectDetailsRepository(getApplication(),projectId);
        projectDetailRepository=new ProjectDetailRepository(getApplication(),projectId);

    }

    public LiveData<ProjectModel> getProjectModel() {
        return mRepository.getProject();
    }
    public LiveData<List<ProjectUserModel>> getProjectDetailModel() {
        return projectDetailRepository.getListLiveData();
    }
    public void setLastOpen(ProjectModel    projectModel){
        mRepository.setLastOpen(projectModel);
    }

    public void update(ProjectModel projectModel){}

}
