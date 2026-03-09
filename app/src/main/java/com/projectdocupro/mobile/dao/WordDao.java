package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.WordModel;

import java.util.List;

@Dao
public interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WordModel wordModel);

    @Query("DELETE FROM wordmodel")
    void deleteAll();

    @Query("DELETE FROM wordmodel WHERE projectId = :projectId")
    void deleteUsingProjectId(String projectId);

    @Query("SELECT * from wordmodel WHERE projectId = :projectId")
    LiveData<List<WordModel>> getWordsList(String projectId);

    @Query("SELECT * from wordmodel WHERE projectId = :projectId")
    List<WordModel> getAllWordsList(String projectId);

    @Query("SELECT * from wordmodel WHERE projectId = :projectId  AND useCount > 0")
    List<WordModel> getWordsSimpleList(String projectId);

    @Query("SELECT * from wordmodel WHERE  projectId = :projectId AND isFavorite = 1")
    LiveData<List<WordModel>> getFavoriteWordsList(String projectId);

    @Query("SELECT * from wordmodel WHERE  projectId = :projectId AND isFavorite = 1  ")
    List<WordModel> getFavoriteWordsSimpleList(String projectId);

    //recent used
    @Query("SELECT * from wordmodel WHERE projectId = :projectId AND useCount > 0 ORDER BY useCount   DESC LIMIT 5")
    LiveData<List<WordModel>> getRecentUsedWordsList(String projectId);

    @Query("SELECT * from wordmodel WHERE projectId = :projectId   AND isFavorite = 1 ")
    LiveData<List<WordModel>> getFavouriteWordsList(String projectId);

    @Query("SELECT * from wordmodel WHERE projectId = :projectId AND clocked = 1")
    List<WordModel> getClockedWordsList(String projectId);

    @Query("SELECT * from wordmodel WHERE projectId = :projectId AND projectParamId in (:ids)")
    List<WordModel> getWordsListUsingIds(String projectId, List<String> ids);

    @Query("SELECT * from wordmodel WHERE projectId = :projectId AND photoIds  LIKE :photoId")
    List<WordModel> getWordsListIncludesPhotoId(String photoId, String projectId);

    @Query("SELECT * from wordmodel WHERE projectId = :projectId AND photoIds  LIKE :photoId ")
    List<WordModel> getWordsListIncludesPhotoIdWithTypeZero(String photoId, String projectId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(WordModel wordModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAll(List<WordModel> wordModels);

    @Query("SELECT * from wordmodel WHERE isFavorite = 1")
    int getFavoriteWordsCount();

    @Query("SELECT * from wordmodel WHERE projectId = :projectId  AND useCount > 0 GROUP BY wordmodel.`group`")
    List<WordModel> getWordsSimpleGroupByList(String projectId);
}
