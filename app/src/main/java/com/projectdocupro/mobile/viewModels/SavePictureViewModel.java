package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.repos.SavePhotosRepository;
import com.projectdocupro.mobile.utility.Utils;

public class SavePictureViewModel extends AndroidViewModel {

    private SavePhotosRepository mRepository;
    private Uri imageUri;

    public SavePhotosRepository getmRepository() {
        return mRepository;
    }

    public void setmRepository(SavePhotosRepository mRepository) {
        this.mRepository = mRepository;
    }

    private String imagePath = "", projectId;

    public SavePictureViewModel(@NonNull Application application) {
        super(application);
    }

    public void InitRepo(String projectId) {
        mRepository = new SavePhotosRepository(getApplication(), projectId);


    }


    public MutableLiveData<Long> getNewPhotoInsert(){
       return mRepository.newlyInsertedImageID;
    }

 /*   public void insert(PhotoModel photoModel) {
        mRepository.insert(photoModel);
    }*/

    public void setInsertPhotoModel(PhotoModel photoModel) {
        Utils.showLogger("setInsertPhotoModel");
        photoModel.setPath(imagePath);
        photoModel.setPohotPath(imagePath);
        mRepository.insert(photoModel);
        Utils.showLogger("newSavedPhoto>>"+photoModel.getPdphotoid());
    }

    public void setPhotoModel(PhotoModel photoModel) {
        photoModel.setPath(imagePath);
    }

    public void updatePhotoModelInViewModel(PhotoModel photoModel) {
        mRepository.setPhotoModel(photoModel);
    }

    public void setExistingPhotoModel(long photoId) {
        mRepository.setExistingPhotoModel(photoId);
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
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

    public LiveData<PhotoModel> getUpdatedPhotoModel() {
        return mRepository.getUpdaatedPhotoModel();
    }
}
