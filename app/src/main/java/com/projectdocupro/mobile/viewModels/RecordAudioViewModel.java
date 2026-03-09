package com.projectdocupro.mobile.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.models.RecordAudioModel;
import com.projectdocupro.mobile.repos.RecordAudioRepository;

import java.util.List;

public class RecordAudioViewModel extends AndroidViewModel {


    private RecordAudioRepository mRepository;
    private String audioPath, projectId;
    private long photoId;
    private boolean isAdded=false;
    private LiveData<List<RecordAudioModel>> recordAudioModels;
    private RecordAudioModel    currentRecording;

    public RecordAudioRepository getmRepository() {
        return mRepository;
    }

    public void setmRepository(RecordAudioRepository mRepository) {
        this.mRepository = mRepository;
    }

    public RecordAudioViewModel(@NonNull Application application) {
        super(application);
    }

    public void InitRepo(long  photoId){
        mRepository = new RecordAudioRepository(getApplication(),photoId);
        recordAudioModels   =   mRepository.getRecordAudioDao().getRecordings(photoId);
    }

    public void insert(RecordAudioModel photoModel){
        isAdded=true;
        currentRecording=photoModel;
        mRepository.insert(currentRecording);
    }

    public LiveData<List<RecordAudioModel>> getRecordModel() {
        return recordAudioModels;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public void updatePhotoModel(RecordAudioModel   recordAudioModel) {
        mRepository.update(recordAudioModel);
    }

    public RecyclerView.Adapter getAdapter() {
        return null;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public RecordAudioModel getCurrentRecording() {
        return currentRecording;
    }
}
