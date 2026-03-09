package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.adapters.PhotosGroupRecyclerAdapter;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.interfaces.LocalPhotosListItemClickListener;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.util.List;

public class LocalPhotosRepository {


    public static final String TYPE_LOCAL_PHOTO = "local_photo";
    public static final String TYPE_MANGEL_PHOTO = "mangel_photo";
    public static final String TYPE_ONLINE_PHOTO = "online_photo";

    public static final String UN_SYNC_PHOTO = "0";
    public static final String UPLOADING_PHOTO = "1";
    public static final String SYNCED_PHOTO = "2";
    public static final String SHORTLY_SYNCED_PHOTO = "3";

    public static final String MISSING_PHOTO_QUALITY = "missing";
    public static final String ORIGNAL_PHOTO_QUALITY = "original";
    public static final String REDUCED_PHOTO_QUALITY = "reduced";

    public static final int MAX_PHOTO_FAILED_COUNT = 5;

    public PhotoDao getPhotoDao() {
        return photoDao;
    }

    public void setPhotoDao(PhotoDao photoDao) {
        this.photoDao = photoDao;
    }

    private PhotoDao photoDao;
    private LiveData<List<PhotoModel>> listLiveData;
    private MediatorLiveData<List<PhotoModel>> mSectionLive = new MediatorLiveData<>();
    private ProjectsDatabase db;
    private PhotosGroupRecyclerAdapter photosRecyclerAdapter;

    public LocalPhotosRepository(Context application, String projectId) {
        db = ProjectsDatabase.getDatabase(application);
        photoDao = db.photoDao();
        listLiveData = photoDao.getPhotoModel(projectId);

    }

    public void refreshData(String projectId) {
        listLiveData = photoDao.getPhotoModel(projectId);
        try {
            if (listLiveData.getValue() == null)
                Utils.showLogger("listlivedatanull");
            else
                Utils.showLogger("listlivedataNotNull");
            Utils.showLogger("listLiveData size" + listLiveData.getValue().size() + "");
        } catch (Exception e) {
            Utils.showLogger("error while" + e.getMessage());
        }
    }

    public void initAdapter(String projectId, List<PhotoModel> photoModels) {
        photosRecyclerAdapter = new PhotosGroupRecyclerAdapter(projectId, photoModels, new LocalPhotosListItemClickListener() {
            @Override
            public void onListItemClick(PhotoModel photoModel) {

            }
        });
    }

    public void initAdapter(String projectId, List<PhotoModel> photoModels, LocalPhotosListItemClickListener clickListener) {
        photosRecyclerAdapter = new PhotosGroupRecyclerAdapter(projectId, photoModels, clickListener);
    }

    public LiveData<List<PhotoModel>> getAllPhotos() {
        return listLiveData;
    }

    public LiveData<List<PhotoModel>> getListLiveData() {
        return listLiveData;
    }

/*    public void insert (PhotoModel   wordModel) {
        if (listLiveData.getValue()==null   ||  listLiveData.getValue().size()==0){
            new insertAsyncTask(photoDao).execute(wordModel);
        }
    }*/

    public PhotosGroupRecyclerAdapter getAdapter() {
        return photosRecyclerAdapter;
    }

/*
    private static class insertAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao photoDao1;
        insertAsyncTask(PhotoDao photoDao) {
            this.photoDao1 =   photoDao;
        }

        @Override
        protected Void doInBackground(PhotoModel...  params) {
            photoDao1.insert(params[0]);
            return null;
        }
    }
*/

/*    public void insertAll (List<PhotoModel> allPlansModel) {
        new insertAllAsyncTask(photoDao).execute(allPlansModel);
    }

    private class insertAllAsyncTask extends AsyncTask<List<PhotoModel>, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        insertAllAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final List<PhotoModel>... params) {
            for (PhotoModel   photoModel:params[0]) {
                mAsyncTaskDao.insert(photoModel);
            }
            return null;
        }
    }*/

    public void deleteAllRows(PhotoDao photoDao) {
        new deleteAllAsyncTask(photoDao).execute();
    }

    public void deleteUsingProjectId(PhotoDao photoDao, String project_id) {

        new deleteUsingProjectIdAllAsyncTask(photoDao).execute(project_id);
    }

    private class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        deleteAllAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            mAsyncTaskDao.deleteAll();

            return null;
        }

    }

    private class deleteUsingProjectIdAllAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        deleteUsingProjectIdAllAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {

            List<PhotoModel> allImages = mAsyncTaskDao.allPhotosOfProject(params[0]);
            for (PhotoModel p : allImages) {
                try {
                    new File(p.getPath()).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mAsyncTaskDao.deleteUsingProjectId(params[0]);

            return null;
        }

    }

}
