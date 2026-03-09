package com.projectdocupro.mobile.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.projectdocupro.mobile.models.RecordAudioModel;

import java.util.List;

@Dao
public interface RecordAudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecordAudioModel recordAudioModel);

    @Query("DELETE FROM recordaudiomodel  WHERE recordId = :recordingId")
    void deleteWithRecordingId(long recordingId);

    @Query("DELETE FROM recordaudiomodel")
    void deleteAll();

    @Query("SELECT * from recordaudiomodel WHERE photoId = :photoId")
    LiveData<List<RecordAudioModel>> getRecordings(long photoId);

    @Query("SELECT Count(*) from recordaudiomodel WHERE photoId = :photoId AND recordServerId ='' ")
    long getRecordingUNSyncCount(long photoId);

    @Query("SELECT * from recordaudiomodel WHERE photoId = :photoId AND recordServerId ='' ")
    List<RecordAudioModel> getRecordingsAsync(long photoId);

    @Query("SELECT * from recordaudiomodel WHERE photoId = :photoId")
    List<RecordAudioModel> getRecordingsToDuplicate(long photoId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(RecordAudioModel recordAudioModel);

    @Query("DELETE FROM recordaudiomodel WHERE projectId = :projectId")
    void deleteByProjectId(String projectId);

}
