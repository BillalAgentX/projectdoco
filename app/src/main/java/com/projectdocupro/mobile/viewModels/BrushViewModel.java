package com.projectdocupro.mobile.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.repos.SavePhotosRepository;

public class BrushViewModel extends AndroidViewModel {

    private SavePhotosRepository mRepository;

    public SavePhotosRepository getmRepository() {
        return mRepository;
    }

    public void setmRepository(SavePhotosRepository mRepository) {
        this.mRepository = mRepository;
    }

    private String  imagePath, projectId;
    private long    originalPhotoId;
    private long    updatedPhotoId=0;

    public long getUpdatedPhotoId() {
        return updatedPhotoId;
    }

    public void setUpdatedPhotoId(long updatedPhotoId) {
        this.updatedPhotoId = updatedPhotoId;
    }

    public BrushViewModel(@NonNull Application application) {
        super(application);
    }

    public void initRepo(String  projectId){
        mRepository = new SavePhotosRepository(getApplication(),projectId);
    }

    public void insert(PhotoModel photoModel){
        //photoModel.setBrushImageAdded(false);
        photoModel.setExtra1("1");
        mRepository.duplicateForBrushInsert(photoModel,originalPhotoId);

      updatedPhotoId=   mRepository.getPhotoModel().getPdphotolocalId();
    }

    public void setPhotoModel(PhotoModel photoModel) {
        photoModel.setPdphotolocalId(0);
        mRepository.setPhotoModel(photoModel);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void updatePhotoModel() {
        mRepository.update();
    }

    public PhotoModel getPhotoModel() {
        return mRepository.getPhotoModel();
    }

    public long getOriginalPhotoId() {
        return originalPhotoId;
    }

    public void setOriginalPhotoId(long originalPhotoId) {
        this.originalPhotoId = originalPhotoId;
    }
}
