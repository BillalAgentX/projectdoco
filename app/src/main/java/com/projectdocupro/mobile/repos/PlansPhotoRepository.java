package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.PlansPhotosDao;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PlansPhotoModel;
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

public class PlansPhotoRepository {

    private PlansPhotosDao mDefectsPhotoDao;
    private LiveData<List<PlansPhotoModel>> mDefectedPhotos;
    private String imagePath;
    Context mContext;
    private List<PlansPhotoModel> defectPhotoModelList;
    private PlansPhotoModel planPhotoObj;

    public PlansPhotosDao getmDefectsPhotoDao() {
        return mDefectsPhotoDao;
    }

    public void setmDefectsPhotoDao(PlansPhotosDao mDefectsPhotoDao) {
        this.mDefectsPhotoDao = mDefectsPhotoDao;
    }

    public LiveData<List<PlansPhotoModel>> getmDefectedPhotos() {
        return mDefectedPhotos;
    }

    public void setmDefectedPhotos(LiveData<List<PlansPhotoModel>> mDefectedPhotos) {
        this.mDefectedPhotos = mDefectedPhotos;
    }

    public PlansPhotoRepository(Context context) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.planPhotosDao();
        mContext=context;
//        mDefectedPhotos = mDefectsPhotoDao.getDefectPhotoModel(projectId);
    }
    public void insert (PlansPhotoModel allPlansModel) {
        new insertAsyncTask(mDefectsPhotoDao).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,allPlansModel);
    }
    public void deleteAllROws () {
        new DeleteAsyncTask(mDefectsPhotoDao).execute();
    }

    public void deleteUsingProjectId (String projectId) {
        new DeleteUsingProjectIdAsyncTask(mDefectsPhotoDao).execute(projectId);
    }
  public void cacheImages () {
        new CachePhotoAsyncTask(mDefectsPhotoDao).execute();
    }

    private  class insertAsyncTask extends AsyncTask<PlansPhotoModel, Void, Void> {
        private PlansPhotosDao mAsyncTaskDao;
        insertAsyncTask(PlansPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PlansPhotoModel... params) {

                mAsyncTaskDao.insert(params[0]);
            cacheProjectImages(mContext,params[0]);

            return null;
        }
    }

    public void updatePhotosModel(PlansPhotoModel plansPhotoModel){

        new UpdateAsyncTask(mDefectsPhotoDao).execute(plansPhotoModel);

    }
    private static class UpdateAsyncTask extends AsyncTask<PlansPhotoModel, Void, Void> {
        private PlansPhotosDao mAsyncTaskDao;
        UpdateAsyncTask(PlansPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PlansPhotoModel... params) {

            mAsyncTaskDao.update(params[0]);

            return null;
        }
    }
    private  class CachePhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        private PlansPhotosDao mAsyncTaskDao;
        CachePhotoAsyncTask(PlansPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            defectPhotoModelList=getmDefectsPhotoDao().getAllPlansPhotoModel();
            if(defectPhotoModelList!=null)
                for (int i = 0; i <defectPhotoModelList.size() ; i++) {
                    if(defectPhotoModelList.get(i).getPohotPath()!=null&&!defectPhotoModelList.get(i).getPohotPath().equals("")&&defectPhotoModelList.get(i).isPhotoCached()){

                    }else{
                        cacheProjectImages(mContext,defectPhotoModelList.get(i));
                    }
                }



            return null;
        }
    }
    private static class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {
        private PlansPhotosDao mAsyncTaskDao;
        DeleteAsyncTask(PlansPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void ... params) {

            mAsyncTaskDao.deleteAll();

            return null;
        }
    }


    private static class DeleteUsingProjectIdAsyncTask extends AsyncTask<String, Void, Void> {
        private PlansPhotosDao mAsyncTaskDao;
        DeleteUsingProjectIdAsyncTask(PlansPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(String ... params) {

            mAsyncTaskDao.deleteRowUsingId(params[0]);

            return null;
        }
    }


    private  class RetrieveAsyncTask extends AsyncTask<String, Void, Void> {
        private PlansPhotosDao mAsyncTaskDao;
        RetrieveAsyncTask(PlansPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
           // if( getmDefectedPhotos().getValue()!=null)

             planPhotoObj= mAsyncTaskDao.getPlansPhotoObject(params[0]);
              if(planPhotoObj!=null&&!planPhotoObj.isPhotoCached()&&planPhotoObj.equals("")){
                  cacheProjectImages(mContext,planPhotoObj);
              }

            return null;
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String projectId) {
        imagePath = "";
        try {
            // todo change the file location/name according to your needs
            File dir =mContext. getExternalFilesDir("/projectDocu/project_plans_" + projectId);
            if (dir == null) {
                dir = mContext.getFilesDir();
            }
//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_plans_" + projectId);
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


    public void cacheProjectImages(Context context, PlansPhotoModel projectModel) {
        callGetPlanImageAPI(context, projectModel, projectModel.getPlanId());
    }

    private void callGetPlanImageAPI(Context context, PlansPhotoModel projectModel, String fileId) {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getPlanImage(authToken, Utils.DEVICE_ID,fileId);

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
                                projectModel.setPhotoCached(true);
                                new UpdateAsyncTask(mDefectsPhotoDao).execute(projectModel);

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

}
