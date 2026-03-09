package com.projectdocupro.mobile.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.adapters.WordListRecyclerAdapter;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.repos.WordsRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteWordsViewModel extends AndroidViewModel {

    private LiveData<List<WordModel>> listLiveFavoriteData;
    private List<String>    keys;
    private Map<String,List<WordModel>> map;
    private WordsRepository mRepository;
    private WordListRecyclerAdapter wordListRecyclerAdapter;
    private long photoId;
    private boolean isChanged=false;

    public FavoriteWordsViewModel(@NonNull Application application) {
        super(application);
    }

    public void InitRepo(long    photoId,String projectId){
        mRepository = new WordsRepository(getApplication(),photoId,projectId);


        listLiveFavoriteData = mRepository.getFavoriteWordsList(projectId);
        this.photoId=photoId;
        map =   new HashMap<>();
        keys    =   new ArrayList<>();

        wordListRecyclerAdapter =   new WordListRecyclerAdapter(photoId, keys, map, wordModel -> {
            mRepository.update(wordModel);
            updatePhotoModel();
        });
    }

    public void updatePhotoModel(){
        mRepository.updatePhotoModel(photoId);
    }


    public LiveData<List<WordModel>> getWordsList(String    projectId) {
        return mRepository.getWordsList(projectId);
    }

    public LiveData<List<WordModel>> getFavoriteWordsList() {
        return listLiveFavoriteData;
    }

    public void insert(WordModel wordModel){
        mRepository.insert(wordModel);
    }

    public WordListRecyclerAdapter getAdapter() {
        return wordListRecyclerAdapter;
    }

    public Map<String,List<WordModel>> getWordsMap() {
        return map;
    }

    public  List<String> getKeysList() {
        return keys;
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
