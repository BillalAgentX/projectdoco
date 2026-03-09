package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.adapters.DefectedProjectPhotosRecyclerAdapter;
import com.projectdocupro.mobile.dao.DefectsPhotosDao;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectPhotoModel;
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

public class DefectPhotoRepository_copy {

    private DefectsPhotosDao mDefectsPhotoDao;
    private LiveData<List<DefectPhotoModel>> mDefectedPhotos;
    private String imagePath;
    DefectedProjectPhotosRecyclerAdapter    photosRecyclerAdapter;
    Context mContext;
    private List<DefectPhotoModel> defectPhotoModelList;

    public List<DefectPhotoModel> getDefectPhotoModelList() {
        return defectPhotoModelList;
    }

    public void setDefectPhotoModelList(List<DefectPhotoModel> defectPhotoModelList) {
        this.defectPhotoModelList = defectPhotoModelList;
    }

    public DefectsPhotosDao getmDefectsPhotoDao() {
        return mDefectsPhotoDao;
    }

    public void setmDefectsPhotoDao(DefectsPhotosDao mDefectsPhotoDao) {
        this.mDefectsPhotoDao = mDefectsPhotoDao;
    }

    public LiveData<List<DefectPhotoModel>> getmDefectedPhotos() {
        return mDefectedPhotos;
    }

    public void setmDefectedPhotos(LiveData<List<DefectPhotoModel>> mDefectedPhotos) {
        this.mDefectedPhotos = mDefectedPhotos;
    }

    public DefectedProjectPhotosRecyclerAdapter getPhotosRecyclerAdapter() {
        return photosRecyclerAdapter;
    }

    public void setPhotosRecyclerAdapter(DefectedProjectPhotosRecyclerAdapter photosRecyclerAdapter) {
        this.photosRecyclerAdapter = photosRecyclerAdapter;
    }

    public void initAdapter(String  projectId,String flaw_id){

//         new RetrieveAsyncTask(mDefectsPhotoDao).execute(projectId,flaw_id);
    }
    public DefectPhotoRepository_copy(Context context) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.defectsPhotosDao();
        mContext=context;
//        mDefectedPhotos = mDefectsPhotoDao.getDefectPhotoModel(projectId);
    }
    public void insert (DefectPhotoModel allPlansModel) {
        new insertAsyncTask(mDefectsPhotoDao).execute(allPlansModel);
    }
    public void deleteAllROws () {
        new DeleteAsyncTask(mDefectsPhotoDao).execute();
    }
  public void cacheImages () {
        new CachePhotoAsyncTask(mDefectsPhotoDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<DefectPhotoModel, Void, Void> {
        private DefectsPhotosDao mAsyncTaskDao;
        insertAsyncTask(DefectsPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DefectPhotoModel... params) {

                mAsyncTaskDao.insert(params[0]);

            return null;
        }
    }


    private static class UpdateAsyncTask extends AsyncTask<DefectPhotoModel, Void, Void> {
        private DefectsPhotosDao mAsyncTaskDao;
        UpdateAsyncTask(DefectsPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DefectPhotoModel... params) {

            mAsyncTaskDao.update(params[0]);

            return null;
        }
    }
    private  class CachePhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        private DefectsPhotosDao mAsyncTaskDao;
        CachePhotoAsyncTask(DefectsPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            defectPhotoModelList=getmDefectsPhotoDao().getAllDefectPhotoModel();
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
        private DefectsPhotosDao mAsyncTaskDao;
        DeleteAsyncTask(DefectsPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void ... params) {

            mAsyncTaskDao.deleteAll();

            return null;
        }
    }

//    private  class RetrieveAsyncTask extends AsyncTask<String, Void, Void> {
//        private DefectsPhotosDao mAsyncTaskDao;
//        RetrieveAsyncTask(DefectsPhotosDao dao) {
//            mAsyncTaskDao = dao;
//        }
//
//        @Override
//        protected Void doInBackground(final String... params) {
////            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
//           // if( getmDefectedPhotos().getValue()!=null)
//
//            photosRecyclerAdapter =   new DefectedProjectPhotosRecyclerAdapter(  mAsyncTaskDao.getDefectPhotoObject(params[0],params[1]),null);
//
//            return null;
//        }
//    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String projectId) {
        imagePath = "";
        try {
            // todo change the file location/name according to your needs

//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_defects_" + projectId);

            File dir =mContext. getExternalFilesDir("/projectDocu/project_defects_" + projectId);
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


    public void cacheProjectImages(Context context, DefectPhotoModel projectModel) {
        callGetPlanImageAPI(context, projectModel, projectModel.getPdphotoid());
    }

    private void callGetPlanImageAPI(Context context, DefectPhotoModel projectModel, String fileId) {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getDefectPhotos(authToken, Utils.DEVICE_ID, fileId);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        if (writeResponseBodyToDisk(response.body(), projectModel.getProjectid())) {
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
