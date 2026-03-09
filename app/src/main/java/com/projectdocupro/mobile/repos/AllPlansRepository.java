package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.dao.ReferPointPlansDao;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.PlansPhotoModel;
import com.projectdocupro.mobile.models.ReferPointJSONPlanModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AllPlansRepository {

    static List<String> lastSavedIds = new ArrayList<>();

    PlansPhotoRepository plansPhotoRepository;
    private String projectId;
    private PlansDao mPlanDao;
    private LiveData<List<PlansModel>> mAllPlans;
    private MediatorLiveData<List<PlansModel>> mAllPlansDefectSpecific = new MediatorLiveData<>();
    private String imagePath;
    private PlansModel plansModelOBJ;
    private ArrayList<PlansModel> plansModelArrayList;
    int entriesCount = 0;
    private CountDownLatch signal;

    public LiveData<List<PlansModel>> getmAllPlansDefectSpecific() {
        return mAllPlansDefectSpecific;
    }

    public void setmAllPlansDefectSpecific(MediatorLiveData<List<PlansModel>> mAllPlansDefectSpecific) {
        this.mAllPlansDefectSpecific = mAllPlansDefectSpecific;
    }

    private MediatorLiveData<List<PlansModel>> mSectionLive = new MediatorLiveData<>();
    static Context mContext;
    public MediatorLiveData<Boolean> isAllPlansSuccess = new MediatorLiveData<>();

    public AllPlansRepository(Context context, String projectID) {
        mContext = context;
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        plansPhotoRepository = new PlansPhotoRepository(mContext);
        mPlanDao = db.plansDao();
        mAllPlans = mPlanDao.getPlansList(projectID);
        projectId = projectID;
        mSectionLive.addSource(mAllPlans, plansModels -> {
            if (plansModels == null || plansModels.isEmpty()) {
                // Fetch data from API
                Log.d("plans list", "null plans");
                callGetListAPI(context, projectId);
            } else {
                Log.d("plans list", plansModels.size() + " plans");
                mSectionLive.removeSource(mAllPlans);
                mSectionLive.setValue(plansModels);
                if (mAllPlans.getValue() == null || mAllPlans.getValue().size() == 0) {
                    if (mAllPlans.getValue() != null)
                        Log.d("plans list", mAllPlans.getValue().size() + " plans");
                    else
                        Log.d("plans list", "null plans");
                    callGetListAPI(context, projectId);
                }
            }
        });
    }

    public LiveData<List<PlansModel>> getAllProjects() {
        return mAllPlans;
    }

    /*public void insert(PlansModel projectModel) {
        new insertAsyncTask(mPlanDao).execute(projectModel);
    }*/

    public void insertAll(List<PlansModel> allPlansModel) {
        new insertAllAsyncTask(mPlanDao).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, allPlansModel);
    }

    public void deleteAll() {
        new deleteAsyncTask(mPlanDao).execute();
    }

    public void deleteUsingProjectId(String projectId) {
        new deleteUsingProjectIdAsyncTask(mPlanDao).execute(projectId);
    }

/*
    private static class insertAsyncTask extends AsyncTask<PlansModel, Void, Void> {
        private PlansDao mAsyncTaskDao;

        insertAsyncTask(PlansDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PlansModel... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
*/

    private static class deleteAsyncTask extends AsyncTask<Void, Void, Void> {
        private PlansDao mAsyncTaskDao;

        deleteAsyncTask(PlansDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }


    private static class deleteUsingProjectIdAsyncTask extends AsyncTask<String, Void, Void> {
        private PlansDao mAsyncTaskDao;

        deleteUsingProjectIdAsyncTask(PlansDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            List<PlansModel> values = mAsyncTaskDao.getFavoritePlansBG(params[0]);


            if (values != null) {
                for (PlansModel p : values) {
                    lastSavedIds.add(p.getPlanId());
                }
            }
            mAsyncTaskDao.deleteUsingProjectId(params[0]);
            return null;
        }
    }

    private class insertAllAsyncTask extends AsyncTask<List<PlansModel>, Void, Void> {
        private PlansDao mAsyncTaskDao;
        private ReferPointPlansDao referPointPlansDao;
        List<PlansModel> plansModelList = null;

        insertAllAsyncTask(PlansDao dao) {
            mAsyncTaskDao = dao;
            referPointPlansDao = ProjectsDatabase.getDatabase(mContext).referPointPlansDao();
        }

        @Override
        protected Void doInBackground(final List<PlansModel>... params) {

            for (PlansModel plansModel : params[0]) {
                Utils.showLogger2("photoModelISFav" + plansModel.getPlanId() + plansModel.isFavorite());
                entriesCount++;
                plansModelOBJ = plansModel;
                callGetPlanImageAPI(mContext, plansModelOBJ.getPlanId());
                signal = new CountDownLatch(1);
                try {
                    signal.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /*PlansModel oldPlan = mAsyncTaskDao.getPlansUsingPlanIDOnly(plansModel.getPlanId());
                if (oldPlan != null) {
                    plansModel.setFavorite(oldPlan.isFavorite());
                }*/

                if (AllPlansRepository.lastSavedIds != null) {
                    if (AllPlansRepository.lastSavedIds.contains(plansModel.getPlanId())) {
                        plansModel.setFavorite(true);
                    }
                }

                mAsyncTaskDao.insert(plansModel);


                PlansPhotoModel plansPhotoModel = new PlansPhotoModel();


                plansPhotoModel.setPlanId(plansModel.getPlanId());


                plansPhotoModel.setProjectId(plansModel.getProjectId());
                plansPhotoRepository.insert(plansPhotoModel);
                plansPhotoRepository.cacheProjectImages(mContext, plansPhotoModel);//Auto Start caching
                if (plansModelOBJ != null && plansModelOBJ.getRefPointsJson() != null) {
                    for (int i = 0; i < plansModelOBJ.getRefPointsJson().size(); i++) {
                        ReferPointJSONPlanModel referPointJSONPlanModel = new ReferPointJSONPlanModel();
                        referPointJSONPlanModel = plansModelOBJ.getRefPointsJson().get(i);
                        referPointJSONPlanModel.setPdProjectId(projectId);
                        referPointJSONPlanModel.setPdplanid(plansModelOBJ.getPlanId());
                        referPointPlansDao.insert(referPointJSONPlanModel);
                    }
                }
            }
            if (AllPlansRepository.lastSavedIds != null)
                AllPlansRepository.lastSavedIds.clear();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (entriesCount == plansModelArrayList.size()) {
                plansModelArrayList.clear();
                isAllPlansSuccess.setValue(true);
            }

        }
    }

    public void retrievePlanUsingFlawFlag(Context context, String projectId, List<Pdflawflag> plansModelList, boolean isFromDefectListing) {

        new RetrievePlansUsingFlawFlagAsyncTask(context, projectId, isFromDefectListing).execute(plansModelList);
    }

    private class RetrievePlansUsingFlawFlagAsyncTask extends AsyncTask<List<Pdflawflag>, Void, List<PlansModel>> {
        private PlansDao mAsyncTaskDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        boolean isFromDefectListing;

        RetrievePlansUsingFlawFlagAsyncTask(Context context, String project_id, boolean isFromFlawListing) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.plansDao();
            projectId = project_id;
            isFromDefectListing = isFromFlawListing;

        }

        @Override
        protected List<PlansModel> doInBackground(final List<Pdflawflag>... params) {

            for (Pdflawflag plansModel : params[0]) {
                stringList.add(plansModel.getPdplanid());
            }
            List<PlansModel> plansModelList = mAsyncTaskDao.getPlansUsingDefect(projectId, stringList);


            return plansModelList;
        }

        @Override
        protected void onPostExecute(List<PlansModel> plansModelList) {
            super.onPostExecute(plansModelList);
            if (mAllPlans.getValue() != null && isFromDefectListing) {
                mAllPlans.getValue().clear();
                mAllPlans.getValue().addAll(plansModelList);

            } else
                mAllPlansDefectSpecific.setValue(plansModelList);
        }
    }

    public void callGetListAPI(Context context, String projectId) {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<JsonObject> call = retroApiInterface.getPlanAPI(authToken, Utils.DEVICE_ID, projectId);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Utils.showLogger("callGetListAPI response" + response.body());
                        Log.d("List", "Success : " + response.body());
                        try {
                            mAllPlans.getValue().addAll(new Gson().fromJson(response.body().getAsJsonArray("data"), new TypeToken<List<PlansModel>>() {
                            }.getType()));
                            plansModelArrayList = new ArrayList<>();
                            plansModelArrayList.addAll(mAllPlans.getValue());
                            entriesCount = 0;
                            insertAll(mAllPlans.getValue());

                        } catch (Exception e) {
                            e.printStackTrace();
                            isAllPlansSuccess.setValue(true);
                            return;
//                            Toast.makeText(context, "No Plans Found", Toast.LENGTH_SHORT).show();
                        }
//                        isAllPlansSuccess.setValue(true);
                    } else {
                        Log.d("List", "Empty response");
                        isAllPlansSuccess.setValue(true);
                    }

                } else {
                    isAllPlansSuccess.setValue(false);
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
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
                isAllPlansSuccess.setValue(false);
            }
        });
    }


    private void callGetPlanImageAPI(Context context, String fileId) {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getPlanImageWithSize(authToken, Utils.DEVICE_ID, fileId, "xl");

        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());


                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        if (bmp == null) {
                            signal.countDown();
                            return;
                        }
                        if (bmp != null)
                            writeResponseBodyToDisk(bmp, "44");


                        Utils.showLogger2("path>>>" + imagePath);
                        File imgFile = new File(imagePath);

                        if (imgFile.exists()) {

                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            Log.d("List", "Empty response");
                        }

                    } else {
                        Log.d("List", "Empty response");
                        signal.countDown();
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
                    signal.countDown();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
                signal.countDown();
            }
        });
    }


    private void writeResponseBodyToDisk(Bitmap planBitmap, String projectId) {

        // todo change the file location/name according to your needs
        File dir = mContext.getExternalFilesDir("/projectDocu/project_plans_" + projectId);
        if (dir == null) {
            dir = mContext.getFilesDir();
        }
//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_plans_" + projectId);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        File photo = new File(dir, "Download_" + new Date().getTime() + ".jpg");

        imagePath = photo.getAbsolutePath();
        FileOutputStream planFileOutputStream = null;

        try {
            planFileOutputStream = new FileOutputStream(photo);

            System.gc();

            Thread.yield();

            planBitmap.compress(Bitmap.CompressFormat.JPEG, 100, planFileOutputStream);
            //System.out.println("ProductDocuUpdatePlan:"+planFileOutputStream);
            signal.countDown();
        } catch (Exception e) {
            e.printStackTrace();
            signal.countDown();
            //System.out.println("Exception ProductDocuUpdatePlan:"+e);
        } finally {
            try {
                if (planFileOutputStream != null) {
                    planFileOutputStream.close();
                    if (plansModelOBJ != null) {
                        plansModelOBJ.setPlanPhotoPathLargeSize(imagePath);
                        new updatePlansAsyncTask(mPlanDao).execute(plansModelOBJ);
                    }
                    signal.countDown();
                }
            } catch (IOException e) {
                e.printStackTrace();
                signal.countDown();
                //System.out.println("Exception ProductDocuUpdatePlan:"+e);
            }
        }

    }

    private class updatePlansAsyncTask extends AsyncTask<PlansModel, Void, Void> {
        private PlansDao mAsyncTaskDao;

        updatePlansAsyncTask(PlansDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final PlansModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }
}
