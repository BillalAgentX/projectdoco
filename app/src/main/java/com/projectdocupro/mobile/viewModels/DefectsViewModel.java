package com.projectdocupro.mobile.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.repos.DefectRepository;

import java.util.List;

public class DefectsViewModel extends AndroidViewModel {

    private DefectRepository mRepository;
    private LiveData<List<DefectsModel>> mAllProjects;
    public boolean isAutoSyncPhoto = true;
    public MutableLiveData<Boolean> isStartUploadPhoto = new MutableLiveData<>();

    public DefectsViewModel(@NonNull Application application) {
        super(application);
    }

    public void InitRepo(String  projectId){
        mRepository = new DefectRepository(getApplication(),projectId);
        mAllProjects = mRepository.getAllDefects();
    }

    public LiveData<List<DefectsModel>> getAllWords() { return mAllProjects; }

    //public void insert(DefectsModel projectModel) { mRepository.insert(projectModel); }

    public DefectRepository getDefectsRepository(){
        return mRepository;
    }

}
