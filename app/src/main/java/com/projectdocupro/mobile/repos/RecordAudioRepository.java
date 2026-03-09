package com.projectdocupro.mobile.repos;

import android.app.Application;
import android.os.AsyncTask;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.RecordAudioDao;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.RecordAudioModel;

import java.util.List;

public class RecordAudioRepository {

    private RecordAudioDao recordAudioDao;
    private ProjectsDatabase db;
    private PhotoModel  photoModel;
    private long    photoId;

    public RecordAudioRepository(Application application, long  photoId) {
        this.photoId=photoId;
        db = ProjectsDatabase.getDatabase(application);
        recordAudioDao = db.recordAudioDao();
        new getPhotoAsyncTask(photoId).execute();
    }

    public RecordAudioDao getRecordAudioDao() {
        return recordAudioDao;
    }

    public void insert (RecordAudioModel   recordAudioModel) {
        new insertAsyncTask(recordAudioDao).execute(recordAudioModel);
        new updatePhotoAsyncTask(photoId).execute();
    }

    public void update(RecordAudioModel   recordAudioModel) {
        new    updateAsyncTask(recordAudioDao).execute(recordAudioModel);
    }

    public void deleteUsingRecordingId(RecordAudioModel   recordAudioModel) {
        new    deleteAsyncTask(recordAudioDao).execute(recordAudioModel);
        new updatePhotoAsyncTask(photoId).execute();
    }

    private static class insertAsyncTask extends AsyncTask<RecordAudioModel, Void, Void> {
        private RecordAudioDao recordAudioDao;
        insertAsyncTask(RecordAudioDao recordAudioDao) {
            this.recordAudioDao =   recordAudioDao;
        }

        @Override
        protected Void doInBackground(RecordAudioModel...  params) {
            recordAudioDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<RecordAudioModel, Void, Void> {
        private RecordAudioDao recordAudioDao;
        deleteAsyncTask(RecordAudioDao recordAudioDao) {
            this.recordAudioDao =   recordAudioDao;
        }

        @Override
        protected Void doInBackground(RecordAudioModel...  params) {
            recordAudioDao.deleteWithRecordingId(params[0].getRecordId());
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<RecordAudioModel, Void, Void> {
        private RecordAudioDao audioDao;
        updateAsyncTask(RecordAudioDao audioDao) {
            this.audioDao = audioDao;
        }

        @Override
        protected Void doInBackground(RecordAudioModel...  params) {
            audioDao.update(params[0]);
            return null;
        }
    }

    public class getPhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        private long photoId;
        getPhotoAsyncTask(long    photoId) {
            this.photoId = photoId;
        }

        @Override
        protected Void doInBackground(Void...  params) {
            photoModel  =   db.photoDao().getPhotoModel(photoId);
            return null;
        }
    }


    public class updatePhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        private long photoId;
        updatePhotoAsyncTask(long    photoId) {
            this.photoId = photoId;
        }

        @Override
        protected Void doInBackground(Void...  params) {
            List<RecordAudioModel> recordAudioModels  =   recordAudioDao.getRecordingsAsync(photoId);
            if (recordAudioModels.size()>0){
                photoModel.setRecordingAdded(true);
                photoModel.setPhotoSynced(false);
                photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
            }else{
                photoModel.setRecordingAdded(false);
            }
            db.photoDao().update(photoModel);
            return null;
        }
    }

}
