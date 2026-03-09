package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.adapters.DefectedProjectPhotosRecyclerAdapter;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DefectPhotoRepository {
    public MutableLiveData<PhotoModel> reloadThePage = new MutableLiveData<>();
    private PhotoDao mDefectsPhotoDao;
    private LiveData<List<PhotoModel>> mDefectedPhotos = new MediatorLiveData<>();
    private String imagePath;
    DefectedProjectPhotosRecyclerAdapter photosRecyclerAdapter;
    Context mContext;
    private List<PhotoModel> defectPhotoModelList;

    public List<PhotoModel> getDefectPhotoModelList() {
        return defectPhotoModelList;
    }

    public void setDefectPhotoModelList(List<PhotoModel> defectPhotoModelList) {
        this.defectPhotoModelList = defectPhotoModelList;
    }

    public PhotoDao getmDefectsPhotoDao() {
        return mDefectsPhotoDao;
    }

    public void setmDefectsPhotoDao(PhotoDao mDefectsPhotoDao) {
        this.mDefectsPhotoDao = mDefectsPhotoDao;
    }

    public LiveData<List<PhotoModel>> getmDefectedPhotos() {
        return mDefectedPhotos;
    }

    public void setmDefectedPhotos(LiveData<List<PhotoModel>> mDefectedPhotos) {
        this.mDefectedPhotos = mDefectedPhotos;
    }

    public DefectedProjectPhotosRecyclerAdapter getPhotosRecyclerAdapter() {
        return photosRecyclerAdapter;
    }

    public void setPhotosRecyclerAdapter(DefectedProjectPhotosRecyclerAdapter photosRecyclerAdapter) {
        this.photosRecyclerAdapter = photosRecyclerAdapter;
    }

    public void initAdapter(String projectId, String flaw_id) {
        mDefectedPhotos = mDefectsPhotoDao.getDefectPhotosAndLocalPhotosListLiveData(projectId, flaw_id);
//        new RetrieveAsyncTask(mDefectsPhotoDao).execute(projectId, flaw_id);
    }

    public DefectPhotoRepository(Context context) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.photoDao();
        mContext = context;
//        mDefectedPhotos = mDefectsPhotoDao.getDefectPhotoModel(projectId);

    }

    public void insertParalel(PhotoModel allPlansModel) {
        var allLocalSaved = getmDefectsPhotoDao().getDefectPhotosList(allPlansModel.getProjectId(), allPlansModel.getFlaw_id());
        boolean isAlreadyAnyImageSaved = false;

        for (var obj : allLocalSaved) {
            if (obj.isPhotoCached())
                isAlreadyAnyImageSaved = true;
        }

        var oldImg = mDefectsPhotoDao.getPhotoModelGlobal(Long.parseLong(allPlansModel.getPdphotoid()));

        if (oldImg != null) {

            Utils.showLogger("imgAlreadyExists");
            if (!oldImg.isPhotoCached() && !isAlreadyAnyImageSaved) {
                cacheImages(allPlansModel.getProjectId(), allPlansModel.getFlaw_id());
                //mAsyncTaskDao.insert(params[0]);
            }
        } else {
            Utils.showLogger("AddingimgAlreadyExists");
            mDefectsPhotoDao.insert(allPlansModel);
            if (!isAlreadyAnyImageSaved)
                cacheImages(allPlansModel.getProjectId(), allPlansModel.getFlaw_id());
        }

        // new insertAsyncTask(mDefectsPhotoDao).execute(allPlansModel);
    }

    public void insertSerial(PhotoModel allPlansModel) {
        var allLocalSaved = getmDefectsPhotoDao().getDefectPhotosList(allPlansModel.getProjectId(), allPlansModel.getFlaw_id());
        boolean isAlreadyAnyImageSaved = false;

        for (var obj : allLocalSaved) {
            if (obj.isPhotoCached())
                isAlreadyAnyImageSaved = true;
        }

        var oldImg = mDefectsPhotoDao.getPhotoModelGlobal(Long.parseLong(allPlansModel.getPdphotoid()));

        if (oldImg != null) {

            Utils.showLogger("imgAlreadyExists");
            if (!oldImg.isPhotoCached() && !isAlreadyAnyImageSaved) {
                cacheImages2(allPlansModel.getProjectId(), allPlansModel.getFlaw_id());
                //mAsyncTaskDao.insert(params[0]);
            }
        } else {
            Utils.showLogger("AddingimgAlreadyExists");
            mDefectsPhotoDao.insert(allPlansModel);
            if (!isAlreadyAnyImageSaved)
                cacheImages2(allPlansModel.getProjectId(), allPlansModel.getFlaw_id());
        }

        // new insertAsyncTask(mDefectsPhotoDao).execute(allPlansModel);
    }

    public void deleteAllROws() {
        new DeleteAsyncTask(mDefectsPhotoDao).execute();
    }

    public void cacheImages(String projectId, String flaw_id) {
        //Utils.showLogger("startCacheInGalary");
        new CachePhotoAsyncTask(mDefectsPhotoDao).execute(projectId, flaw_id);
    }

    public void cacheImages2(String projectId, String flaw_id) {

        defectPhotoModelList = getmDefectsPhotoDao().getDefectPhotosList(projectId, flaw_id);
        if (defectPhotoModelList != null)
            for (int i = 0; i < defectPhotoModelList.size(); i++) {
                if (defectPhotoModelList.get(i).getPohotPath() != null && !defectPhotoModelList.get(i).getPohotPath().equals("") && defectPhotoModelList.get(i).isPhotoCached()) {

                } else {
                    cacheProjectImages2(mContext, defectPhotoModelList.get(i));
                }
            }

        //Utils.showLogger("startCacheInGalary");
        new CachePhotoAsyncTask(mDefectsPhotoDao).execute(projectId, flaw_id);
    }

    private class insertAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        insertAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PhotoModel... params) {


            var oldImg = mAsyncTaskDao.getPhotoModel(Long.parseLong(params[0].getPdphotoid()));

            if (oldImg != null) {
                if (!oldImg.isPhotoCached()) {
                    cacheImages(params[0].getProjectId(), params[0].getFlaw_id());
                    //mAsyncTaskDao.insert(params[0]);
                }
            } else {
                mAsyncTaskDao.insert(params[0]);
                cacheImages(params[0].getProjectId(), params[0].getFlaw_id());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }


    private static class UpdateAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        private MutableLiveData<PhotoModel> reloadThePage;

        UpdateAsyncTask(PhotoDao dao, MutableLiveData<PhotoModel> reloadThePage) {
            mAsyncTaskDao = dao;
            this.reloadThePage = reloadThePage;
        }

        @Override
        protected Void doInBackground(final PhotoModel... params) {

            mAsyncTaskDao.update(params[0]);

//            reloadThePage.postValue(params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            // reloadThePage.postValue(true);
        }
    }

    private class CachePhotoAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        CachePhotoAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {

            defectPhotoModelList = getmDefectsPhotoDao().getDefectPhotosList(params[0], params[1]);
            if (defectPhotoModelList != null)
                for (int i = 0; i < defectPhotoModelList.size(); i++) {
                    if (defectPhotoModelList.get(i).getPohotPath() != null && !defectPhotoModelList.get(i).getPohotPath().equals("") && defectPhotoModelList.get(i).isPhotoCached()) {

                    } else {
                        cacheProjectImages(i == 0, mContext, defectPhotoModelList.get(i));
                    }
                }

            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        DeleteAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... params) {

            mAsyncTaskDao.deleteAll();

            return null;
        }
    }

    private class RetrieveAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        RetrieveAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=

            photosRecyclerAdapter = new DefectedProjectPhotosRecyclerAdapter(mAsyncTaskDao.getDefectPhotosAndLocalPhotosList(params[0], params[1]), params[1], null);

            return null;
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String projectId) {
        imagePath = "";
        try {
            // todo change the file location/name according to your needs

//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_defects_" + projectId);

            File dir = mContext.getExternalFilesDir("/projectDocu/project_defects_" + projectId);
            if (dir == null) {
                dir = mContext.getFilesDir();
            }
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[15000];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(photo);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("A TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }


                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


    public void cacheProjectImages(boolean isFirst, Context context, PhotoModel projectModel) {
        callGetPlanImageAPI(isFirst, context, projectModel, projectModel.getPdphotoid());
    }

    public void cacheProjectImages2(Context context, PhotoModel projectModel) {
        callGetPlanImageAPI2(context, projectModel, projectModel.getPdphotoid());
    }

    private void callGetPlanImageAPI(boolean isFirst, Context context, PhotoModel projectModel, String fileId) {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        if (fileId == null || fileId.equals(""))
            return;

        Call<ResponseBody> call = retroApiInterface.getDefectPhotos(authToken, Utils.DEVICE_ID, fileId);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        if (writeResponseBodyToDisk(response.body(), projectModel.getProjectId())) {
                            if (imagePath != null && !imagePath.equals("")) {
                                projectModel.setPohotPath(imagePath);
                                projectModel.setPath(imagePath);
                                projectModel.setPhotoCached(true);
                                Utils.showLogger2("firing" + projectModel.getLocal_flaw_id());
                                if (isFirst)
                                    reloadThePage.postValue(projectModel);
                                new UpdateAsyncTask(mDefectsPhotoDao, reloadThePage).execute(projectModel);

                            }
//                            Bitmap bitmap  =   BitmapFactory.decodeFile(imagePath);
//                            imageView.setImageBitmap(bitmap);
                        }
                    } else {
                        Log.d("List", "Empty response");
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            Log.d("List", "Not Success : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        Log.d("List", "Not Success : " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }


    private void callGetPlanImageAPI2(Context context, PhotoModel projectModel, String fileId) {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        if (fileId == null || fileId.equals(""))
            return;

        Call<ResponseBody> call = retroApiInterface.getDefectPhotos(authToken, Utils.DEVICE_ID, fileId);
        Log.d("call url", call.request().url().toString());

        try {
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    Log.d("List", "Success : " + response.body());
                    if (writeResponseBodyToDisk(response.body(), projectModel.getProjectId())) {
                        if (imagePath != null && !imagePath.equals("")) {
                            projectModel.setPohotPath(imagePath);
                            projectModel.setPath(imagePath);
                            projectModel.setPhotoCached(true);
                            reloadThePage.postValue(projectModel);
                            mDefectsPhotoDao.update(projectModel);


                        }
//                            Bitmap bitmap  =   BitmapFactory.decodeFile(imagePath);
//                            imageView.setImageBitmap(bitmap);
                    }
                } else {
                    Log.d("List", "Empty response");
                }
            } else {
                if (response.errorBody() != null) {
                    try {
                        Log.d("List", "Not Success : " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    Log.d("List", "Not Success : " + response.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


}
