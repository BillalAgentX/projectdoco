package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.utility.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DefectRepository {


    public static final String UN_SYNC_PHOTO = "0";
    public static final String UPLOADING_PHOTO = "1";
    public static final String SYNCED_PHOTO = "2";
    public static final String SHORTLY_SYNCED_PHOTO = "3";
    private ArrayList<DefectsModel> defectsModelList;
    int entriesCount = 0;
    public DefectPhotoRepository defectPhotoRepository;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    private String projectId;
    private DefectsDao mDefectDao;
    private LiveData<List<DefectsModel>> mAllDefects;

    public MutableLiveData<List<DefectsModel>> loadDefectOnUi = new MutableLiveData<>();
    private MutableLiveData<Boolean> isProgressComplete = new MutableLiveData<>();
    public MutableLiveData<Boolean> isSyncAllDefects = new MutableLiveData<>();
    public MutableLiveData<Boolean> reloadPage = new MutableLiveData<>();

    private MediatorLiveData<List<DefectsModel>> mSectionLive = new MediatorLiveData<>();
    static Context myApplication;

    public DefectsDao getmDefectDao() {
        return mDefectDao;
    }

    public void setmDefectDao(DefectsDao mDefectDao) {
        this.mDefectDao = mDefectDao;
    }

    public MutableLiveData<Boolean> getIsProgressComplete() {
        return isProgressComplete;
    }

    public MutableLiveData<Boolean> getIsSyncAllDefects() {
        return isSyncAllDefects;
    }

    public void setIsSyncAllDefects(MutableLiveData<Boolean> isSyncAllDefects) {
        this.isSyncAllDefects = isSyncAllDefects;
    }

    public void setIsProgressComplete(MutableLiveData<Boolean> isProgressComplete) {
        this.isProgressComplete = isProgressComplete;
    }

    public DefectRepository(Context application, String projectId) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(application);
        myApplication = application;
        mDefectDao = db.defectsDao();
        defectPhotoRepository = new DefectPhotoRepository(myApplication);

        mAllDefects = mDefectDao.getDefectsList(projectId);
        this.projectId = projectId;

        mSectionLive.addSource(mAllDefects, defectsModels -> {
            if (defectsModels == null || defectsModels.isEmpty()) {
                // Fetch data from API
                Log.d("defects list", "null defects");
                callGetDefectsAPI(application, projectId);
            } else {
                Log.d("defects list", defectsModels.size() + " defects");
                mSectionLive.removeSource(mAllDefects);
                mSectionLive.setValue(defectsModels);
                if (mAllDefects.getValue() == null || mAllDefects.getValue().size() == 0) {
                    if (mAllDefects.getValue() != null)
                        Log.d("defects list", mAllDefects.getValue().size() + " defects");
                    else
                        Log.d("defects list", "null defects");
                    callGetDefectsAPI(application, projectId);
                }
            }
        });

//        if(mAllDefects.getValue()!=null&&mAllDefects.getValue().size()>0)
//         updateDefectSyncStatus(mAllDefects.getValue());
    }

    private void updateDefectSyncStatus(List<DefectsModel> defectsModelList) {

        PhotoDao photoDao = ProjectsDatabase.getDatabase(myApplication).photoDao();

        for (int i = 0; i < defectsModelList.size(); i++) {

            if (photoDao.getDefectPhotosAndUnSyncLocalPhotosCount(projectId, defectsModelList.get(i).getDefectLocalId() + "") > 0) {
                mAllDefects.getValue().get(i).setSynced(false);
            } else
                mAllDefects.getValue().get(i).setSynced(true);

        }

    }


    public LiveData<DefectsModel> getDefectObject(String projectId) {
        return mDefectDao.getDefectsObject(projectId);

    }

    public LiveData<List<DefectsModel>> getAllDefects() {


        return mAllDefects;
    }

    public void insert(DefectsModel projectModel) {
        new insertAsyncTask(mDefectDao).execute(projectModel);
    }

    private static class insertAsyncTask extends AsyncTask<DefectsModel, Void, Void> {

        private DefectsDao mAsyncTaskDao;

        insertAsyncTask(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DefectsModel... params) {
            Utils.showLogger("DefectsRepository158>>");
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    public void insertAll(List<DefectsModel> allPlansModel) {
        new insertAllAsyncTask(mDefectDao).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, allPlansModel);
    }

    private class insertAllAsyncTask extends AsyncTask<List<DefectsModel>, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        insertAllAsyncTask(DefectsDao dao) {
            mAsyncTaskDao = dao;


        }

        @Override
        protected Void doInBackground(final List<DefectsModel>... params) {

            //isProgressComplete.postValue(false);
            Utils.showLogger("insertAllAsyncTask");


            PdFlawFlagRepository pdFlawFlagRepository = new PdFlawFlagRepository(myApplication);
            DefectTradesRepository mRepositoryDefecttrade = new DefectTradesRepository(myApplication, getProjectId());
//            mRepository.deleteAllROws();
//            mRepositoryDefecttrade.deleteAllROws();
            long frist_date = 0, created_date = 0, notice_date = 0, notified_date = 0, second_frist_date = 0, done_date = 0;
            long local_flaw_id = 0;
            String flaw_status = "";
            for (DefectsModel defectsModel : params[0]) {
                entriesCount++;
                if (defectsModel.getFristDate() != null && !defectsModel.getFristDate().equals("")) {

                    frist_date = Utils.convertStringToTimestamp(defectsModel.getFristDate()).getTime();
                }

                if (defectsModel.getNoticeDate() != null && !defectsModel.getNoticeDate().equals("")) {

                    notice_date = Utils.convertStringToTimestamp(defectsModel.getNoticeDate()).getTime();
                }

                if (defectsModel.getFristDate() != null && !defectsModel.getFristDate().equals("")) {

                    notified_date = Utils.convertStringToTimestamp(defectsModel.getNotifieDate()).getTime();
                }

                if (defectsModel.getSecondFristDate() != null && !defectsModel.getSecondFristDate().equals("")) {

                    second_frist_date = Utils.convertStringToTimestamp(defectsModel.getSecondFristDate()).getTime();
                }

                if (defectsModel.getDoneDate() != null && !defectsModel.getDoneDate().equals("")) {

                    done_date = Utils.convertStringToTimestamp(defectsModel.getDoneDate()).getTime();
                }

                if (defectsModel.getCreated() != null && !defectsModel.getCreated().equals("")) {

                    created_date = Utils.convertStringToTimestamp(defectsModel.getCreated()).getTime();
                }
                flaw_status = defectsModel.getStatus();
                defectsModel.fristdate_df = frist_date;
                defectsModel.secondFristDate_df = second_frist_date;
                defectsModel.noticeDate_df = notice_date;
                defectsModel.notifiedate_df = notified_date;
                defectsModel.donedate_df = done_date;
                defectsModel.createDate_df = created_date;
                if (defectsModel.getRunId() != null && !defectsModel.getRunId().equals("")) {
                    defectsModel.setRunidInt(Integer.valueOf(defectsModel.getRunId()));
                }
                if (defectsModel.getLastupdate() == null) {
                    defectsModel.setLastupdate(defectsModel.getDefectDate());
                }

                defectsModel.setUploadStatus(SYNCED_PHOTO);
                Utils.showLogger("DefectRepository>>231");
                local_flaw_id = mAsyncTaskDao.insert(defectsModel);

                List<PhotoModel> photosToSyncLater = new ArrayList<>();


                if (defectsModel.pdflawflagList != null && defectsModel.pdflawflagList.size() > 0) {
                    for (int i = 0; i < defectsModel.pdflawflagList.size(); i++) {

                        defectsModel.pdflawflagList.get(i).setPdProjectid(defectsModel.getProjectId());
                        defectsModel.pdflawflagList.get(i).setFlaw_Id(defectsModel.getDefectId());
                        defectsModel.pdflawflagList.get(i).setLocal_flaw_Id(local_flaw_id + "");
                        defectsModel.pdflawflagList.get(i).setFlaw_status(flaw_status);
                        pdFlawFlagRepository.insert(defectsModel.pdflawflagList.get(i));
                    }
                }


                defectPhotoRepository.insertAllImagesOneByOne(photosToSyncLater);

                Utils.showLogger("addingImagesLogic");
                if (defectsModel.defectPhotoModelList != null) {
                    Utils.showLogger(defectsModel.getDefectPhotoModelList().size() + ">>SizeOfImages");
                }
                if (defectsModel.defectPhotoModelList != null && defectsModel.defectPhotoModelList.size() > 0) {

                    for (int i = 0; i < defectsModel.defectPhotoModelList.size(); i++) {
                        Utils.showLogger("insertingTheImage" + defectsModel.defectPhotoModelList.get(i).getPdphotoid());
                        defectsModel.defectPhotoModelList.get(i).setProjectId(defectsModel.getProjectId());
                        defectsModel.defectPhotoModelList.get(i).setFlaw_id(defectsModel.getDefectId());
                        defectsModel.defectPhotoModelList.get(i).setPhoto_type(LocalPhotosRepository.TYPE_MANGEL_PHOTO);
                        defectsModel.defectPhotoModelList.get(i).setPhotoUploadStatus(LocalPhotosRepository.SYNCED_PHOTO);
                        defectsModel.defectPhotoModelList.get(i).setLocal_flaw_id(local_flaw_id + "");

                        //if (i == 0)
                          //  defectPhotoRepository.insertParalel(defectsModel.defectPhotoModelList.get(i));

                        photosToSyncLater.add(defectsModel.defectPhotoModelList.get(i));
                    }
                }

                if (defectsModel.defectTradeModelList != null && defectsModel.defectTradeModelList.size() > 0) {
                    for (int i = 0; i < defectsModel.defectTradeModelList.size(); i++) {
                        try {
                            defectsModel.defectTradeModelList.get(i).setPdflawid(defectsModel.getDefectId());
                            defectsModel.defectTradeModelList.get(i).setLocalpdflawid(local_flaw_id + "");
                            defectsModel.defectTradeModelList.get(i).setPdprojectid(projectId);
                            if (defectsModel.defectTradeModelList.get(i).getCompany() != null
                                    && defectsModel.defectTradeModelList.get(i).getServicenumber() != null
                                    && defectsModel.defectTradeModelList.get(i).getPdservicetitle() != null) {
                                if (!defectsModel.defectTradeModelList.get(i).getServicenumber().equals("")
                                        && !defectsModel.defectTradeModelList.get(i).getCompany().equals("")) {
                                    defectsModel.defectTradeModelList.get(i).setPdservicetitle(defectsModel.defectTradeModelList.get(i).getPdservicetitle() + " [" + defectsModel.defectTradeModelList.get(i).getServicenumber() + "]" + " - " + defectsModel.defectTradeModelList.get(i).getCompany());
                                } else
                                    defectsModel.defectTradeModelList.get(i).setPdservicetitle(defectsModel.defectTradeModelList.get(i).getPdservicetitle() + " [" + defectsModel.defectTradeModelList.get(i).getServicenumber() + "]");
                            } else if (defectsModel.defectTradeModelList.get(i).getPdservicetitle() != null
                                    && defectsModel.defectTradeModelList.get(i).getServicenumber() != null) {
                                if (!defectsModel.defectTradeModelList.get(i).getServicenumber().equals("")) {
                                    defectsModel.defectTradeModelList.get(i).setPdservicetitle(defectsModel.defectTradeModelList.get(i).getPdservicetitle() + " [" + defectsModel.defectTradeModelList.get(i).getServicenumber() + "]");
                                }
                            }

//                        mRepositoryDefecttrade.insert(defectsModel.defectTradeModelList.get(i));
                            mRepositoryDefecttrade.insert(defectsModel.defectTradeModelList.get(i), projectId, local_flaw_id + "");


//                       mRepository.cacheProjectImages(myApplication,defectsModel.defectPhotoModelList.get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                mRepositoryDefecttrade.UpdateDeciplineOfSelectedFlaw(projectId, local_flaw_id + "");


            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isProgressComplete.postValue(true);

            if (entriesCount == defectsModelList.size()) {
                defectsModelList.clear();
//                isProgressComplete.setValue(true);
                isSyncAllDefects.setValue(true);

            }
        }
    }

    private class insertDefectIfNotExist extends AsyncTask<List<DefectsModel>, Void, List<DefectsModel>> {
        private DefectsDao mAsyncTaskDao;

        insertDefectIfNotExist(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<DefectsModel> doInBackground(final List<DefectsModel>... params) {
            ArrayList<DefectsModel> listNotExistINDB = new ArrayList<>();
            for (int count = 0; count < params[0].size(); count++) {
                DefectsModel serverobj = params[0].get(count);
                DefectsModel dbObj = mDefectDao.getDefectsExitOrNot(serverobj.getDefectId());
                if (dbObj != null) {
                    if (serverobj.getLastupdate() == null) {
                        serverobj.setLastupdate(serverobj.getDefectDate());
                    }
                    if (dbObj.getLastupdate() == null || dbObj.getLastupdate().equals("")) {
                        dbObj.setLastupdate(dbObj.getDefectDate());
                    }
                    Utils.showLogger(serverobj.getLastupdate() + "vs" + dbObj.getLastupdate());

                    if (dateDifferece(serverobj.getLastupdate(), dbObj.getLastupdate())) {
                        Utils.showLogger(dbObj.getDefectId() + "newObjectUpdate");
                        Long localDbId = dbObj.getDefectLocalId();
                        mDefectDao.deleteUsingLocalDefectId(localDbId);
                        serverobj.setDefectLocalId(localDbId);
                        listNotExistINDB.add(serverobj);
                    }
                } else {
                    listNotExistINDB.add(params[0].get(count));
                }
            }
            return listNotExistINDB;
        }

        @Override
        protected void onPostExecute(List<DefectsModel> list) {
            super.onPostExecute(list);
            if (list != null && list.size() > 0) {
                defectsModelList = new ArrayList<>();
                entriesCount = 0;
                defectsModelList.addAll(list);
                insertAll(list);

            } else {
                isProgressComplete.setValue(true);
                isSyncAllDefects.setValue(true);
            }

        }
    }

    public boolean dateDifferece(String serverDate, String DbDate) {
        boolean isDifference = false;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date serverDateFormate = null, DbDateFormate = null;
        try {
            serverDateFormate = sdf.parse(serverDate);
            DbDateFormate = sdf.parse(DbDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (serverDateFormate.after(DbDateFormate)) {
            isDifference = true;
        }
        return isDifference;
    }

    public void getDefectsAPIAndIsertIntoTheTable(Context context, String projectId) {
        isProgressComplete.setValue(false);
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Utils.showLogger("callingDefectapi1");
        Call<JsonObject> call = retroApiInterface.getDefectsAPI(authToken, Utils.DEVICE_ID, projectId);
        Log.d("call url", call.request().url().toString());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                ArrayList<DefectsModel> serverList = new ArrayList<>();
                //serverList.get(0).getPdflawflagList()
//                ArrayList<DefectsModel> dbList = new ArrayList<>();
//                dbList.addAll(mAllDefects.getValue());
                if (response.isSuccessful()) {
                    Utils.showLogger("callingDefectapi1SUCCCESS");
                    if (response.body() != null) {
                        Utils.showLogger(response.body().toString());
                        try {
                            Log.d("List", "Success : " + response.body());
                            Log.d("List", "Success : " + response.body());
                            Gson gson = new Gson();
                            String abc = gson.toJson(response.body());
                            //  var abc = response.body();
                            String fixedString = abc.replaceAll(", false", "").replace("false", "");
                            serverList.addAll(new Gson().fromJson(new JSONObject(fixedString).getJSONObject("data").getJSONArray("flaws").toString(), new TypeToken<List<DefectsModel>>() {
                            }.getType()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        for (var a : serverList) {
                            // Utils.showLogger("PhotosAttachCount" + a.getDefectPhotoModelList().size() + "");
                        }
                        checkDefectExistOrNot(serverList);
                    }
                } else {
                    isProgressComplete.setValue(true);
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
                isProgressComplete.setValue(true);
                Log.d("List", "failed : " + t.getMessage());
            }
        });

    }


/*
    private class SynchFirstPictureOfEveryDefect extends AsyncTask<Void, Void, String> {
        List<DefectsModel> allModels;

        public SynchFirstPictureOfEveryDefect(List<DefectsModel> models) {
            this.allModels = models;
        }

        @Override
        protected String doInBackground(Void... voids) {
            // Perform background operation

            for (DefectsModel defectsModel : allModels) {
                if (!defectsModel.getDefectPhotoModelList().isEmpty()) {
                    var firstImageId = defectsModel.getDefectPhotoModelList().get(0).getPdphotoid();

                }
            }

            return "Task completed";
        }

        @Override
        protected void onPostExecute(String result) {
            // Update UI with result
            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }
*/


    public void checkDefectExistOrNot(List<DefectsModel> list) {
        new insertDefectIfNotExist(mDefectDao).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, list);
    }

    public void callGetDefectsAPI(Context context, String projectId) {
//        isProgressComplete.setValue(false);
        isProgressComplete.postValue(false);
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Utils.showLogger("defectapi2");
        Call<JsonObject> call = retroApiInterface.getDefectsAPI(authToken, Utils.DEVICE_ID, projectId);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        try {
                            mAllDefects.getValue().addAll(new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonArray("flaws"), new TypeToken<List<DefectsModel>>() {
                            }.getType()));
                            defectsModelList = new ArrayList<>();
                            entriesCount = 0;
                            defectsModelList.addAll(mAllDefects.getValue());
                            isProgressComplete.setValue(true);
                            loadDefectOnUi.postValue(defectsModelList);
                            insertAll(mAllDefects.getValue());

                        } catch (Exception e) {
                            e.printStackTrace();
                            isProgressComplete.setValue(true);
                            return;
                            // Toast.makeText(context, "No Flaws Found", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        Log.d("List", "Empty response");
                        isProgressComplete.setValue(true);
                    }


                } else {
                    isProgressComplete.setValue(false);
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
                isProgressComplete.setValue(false);
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }

    public void deleteAllDefects() {
        new DeleteAllDefectsAsyncTask(getmDefectDao()).execute();
    }

    public void deleteDefectsUsingProjectId(String projectId) {
        new DeleteDefectsUsingProjectIdAsyncTask(getmDefectDao()).execute(projectId);
    }

    private static class DeleteAllDefectsAsyncTask extends AsyncTask<Void, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        DeleteAllDefectsAsyncTask(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            mAsyncTaskDao.deleteAll();


            return null;
        }
    }

    private static class DeleteDefectsUsingProjectIdAsyncTask extends AsyncTask<String, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        DeleteDefectsUsingProjectIdAsyncTask(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {

            mAsyncTaskDao.deleteUsingProjectId(params[0]);


            return null;
        }
    }
}
