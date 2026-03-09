package com.projectdocupro.mobile.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.projectdocupro.mobile.adapters.RecentUsedWordsRecyclerAdapter;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.repos.RecentUsedWordsRepository;

import java.util.List;

public class RecentUsedWordsViewModel extends AndroidViewModel {

    public RecentUsedWordsRepository mRepository;
    private RecentUsedWordsRecyclerAdapter wordListRecyclerAdapter;
    private long photoId;
    private boolean isChanged = false;
    public WordModel wordModelObj = null;
    public MutableLiveData<Boolean> isWordSelected = new MutableLiveData<>();
    public MutableLiveData<Boolean> isOpneFieldKeywordWordSelected = new MutableLiveData<>();

    public RecentUsedWordsViewModel(@NonNull Application application) {
        super(application);
    }

    public void InitRepo(String projectId) {
        mRepository = new RecentUsedWordsRepository(getApplication(), projectId);
    }

    public void InitRepo(String projectId, String photoID) {
        mRepository = new RecentUsedWordsRepository(getApplication(), projectId, photoID);
    }

    public void initAdapter(List<WordModel> list) {
        wordListRecyclerAdapter = new RecentUsedWordsRecyclerAdapter(photoId, list, wordModel -> {
            isChanged = true;
//            wordModel.setPhotoIds(wordModel.getPhotoIds()+","+photoId+"");
            if (wordModel.getType() != null && wordModel.getType().equals("1")) {
                wordModelObj = wordModel;
                isOpneFieldKeywordWordSelected.postValue(true);
            }
            mRepository.update(wordModel);
            isWordSelected.postValue(true);

        });
    }


    public LiveData<List<WordModel>> getWordsList() {
        return mRepository.getRecentWordsList();
    }

    public RecentUsedWordsRecyclerAdapter getAdapter() {
        return wordListRecyclerAdapter;
    }

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public boolean isChanged() {
        return isChanged;
    }
}
