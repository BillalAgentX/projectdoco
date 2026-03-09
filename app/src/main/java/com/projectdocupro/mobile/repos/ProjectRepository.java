package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.activities.HomeActivity;
import com.projectdocupro.mobile.dao.CustomerDao;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.DefectsPhotosDao;
import com.projectdocupro.mobile.dao.DefectsTradesDao;
import com.projectdocupro.mobile.dao.GewerkFirmDao;
import com.projectdocupro.mobile.dao.OnlinePhotoDao;
import com.projectdocupro.mobile.dao.PdFlawFLagListDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.dao.PlansPhotosDao;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.dao.ProjectUsersDao;
import com.projectdocupro.mobile.dao.RecordAudioDao;
import com.projectdocupro.mobile.dao.ReferPointPlansDao;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.interfaces.IGetProjectsList;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectRepository {

    private ProjectDao mProjectDao;
    private LiveData<List<ProjectModel>> mAllProjects = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingComplete = new MutableLiveData<>();
    private MediatorLiveData<List<ProjectModel>> mSectionLive = new MediatorLiveData<>();
    private String imagePath;
    public boolean isPlansLoadInDB;
    private ProjectsDatabase db;

    public MutableLiveData<Boolean> getIsLoadingComplete() {
        return isLoadingComplete;
    }

    public void setIsLoadingComplete(MutableLiveData<Boolean> isLoadingComplete) {
        this.isLoadingComplete = isLoadingComplete;
    }

    public static Context mContext = null;

    public ProjectRepository(Context context, boolean isLoadProjects) {


        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mProjectDao = db.projectDao();
        mContext = context;
        if (isLoadProjects) {
            mAllProjects = mProjectDao.getAllProjects();

            mSectionLive.addSource(mAllProjects, projectModels -> {
                if (projectModels == null || projectModels.isEmpty()) {
                    // Fetch data from API
                    Log.d("project list", "null projects");
                    callGetListAPI(context);
                } else {
                    Log.d("project list", projectModels.size() + " projects");
                    mSectionLive.removeSource(mAllProjects);
                    mSectionLive.setValue(projectModels);
                    if (mAllProjects.getValue() == null || mAllProjects.getValue().size() == 0) {
                        if (mAllProjects.getValue() != null)
                            Log.d("project list", mAllProjects.getValue().size() + " projects");
                        else
                            Log.d("project list", "null projects");

                        callGetListAPI(context);
                    }
                }
            });
        }
    }

    public IGetProjectsList iGetProjectsList;

    public ProjectRepository(Context context, IGetProjectsList IgetProjectsList) {
        db = ProjectsDatabase.getDatabase(context);
        mProjectDao = db.projectDao();
        mAllProjects = mProjectDao.getAllProjects();
        iGetProjectsList = IgetProjectsList;
        mContext = context;

        mSectionLive.addSource(mAllProjects, projectModels -> {
            if (projectModels == null || projectModels.isEmpty()) {
                // Fetch data from API
                Log.d("project list", "null projects");
                callGetListAPI(context);
            } else {
                Log.d("project list", projectModels.size() + " projects");
                mSectionLive.removeSource(mAllProjects);
                mSectionLive.setValue(projectModels);
                if (mAllProjects.getValue() == null || mAllProjects.getValue().size() == 0) {
                    if (mAllProjects.getValue() != null)
                        Log.d("project list", mAllProjects.getValue().size() + " projects");
                    else
                        Log.d("project list", "null projects");

                    callGetListAPI(context);
                }
            }
        });
    }


    public LiveData<List<ProjectModel>> getAllProjects() {
        return mAllProjects;
    }

    public LiveData<List<ProjectModel>> getSearchResults(String query) {
        return mProjectDao.getSearchedProjects(query);
    }

    public void insert(ProjectModel projectModel) {
        new insertAsyncTask(mProjectDao).execute(projectModel);
    }

    public void insertAll(List<ProjectModel> allProjectModel) {
        new insertAllAsyncTask(mProjectDao).execute(allProjectModel);
    }



    public void deleteAllPlans() {
        new DeleteAllPlansAsyncTask(mProjectDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<ProjectModel, Void, Void> {
        private ProjectDao mAsyncTaskDao;

        insertAsyncTask(ProjectDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProjectModel... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private class insertAllAsyncTask extends AsyncTask<List<ProjectModel>, Void, Void> {
        private ProjectDao mAsyncTaskDao;

        private CustomerDao customerDao;
        private DefectsPhotosDao defectsPhotosDao;
        private DefectsTradesDao defectsTradesDao;
        private DefectsDao defectsDao;
        private GewerkFirmDao gewerkFirmDao;
        private OnlinePhotoDao onlinePhotoDao;
        private PdFlawFLagListDao pdFlawFLagListDao;
        private PhotoDao photoDao;
        private PlansDao plansDao;
        private PlansPhotosDao plansPhotosDao;
        private ProjectUsersDao projectUsersDao;
        private RecordAudioDao recordAudioDao;
        private ReferPointPlansDao referPointPlansDao;
        private WordDao wordDao;


        List<String> projectIdsForSyncing = new ArrayList<>();
        List<ProjectModel> projectModelList = new ArrayList<>();

        insertAllAsyncTask(ProjectDao dao) {
            mAsyncTaskDao = dao;
            customerDao = db.customerDao();
            defectsPhotosDao = db.defectsPhotosDao();
            defectsTradesDao = db.defectTradeDao();
            defectsDao = db.defectsDao();
            gewerkFirmDao = db.gewerkFirmDao();
            onlinePhotoDao = db.onlinePhotoDao();
            pdFlawFLagListDao = db.pdFlawFLagDao();
            photoDao = db.photoDao();
            plansDao = db.plansDao();
            plansPhotosDao = db.planPhotosDao();
            projectUsersDao = db.projectUsersDao();
            recordAudioDao = db.recordAudioDao();
            referPointPlansDao = db.referPointPlansDao();
            wordDao = db.wordDao();
        }

        @Override
        protected Void doInBackground(final List<ProjectModel>... params) {
//            for (ProjectModel projectModel : params[0]) {
//                projectModel.setSyncStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
//
//                mAsyncTaskDao.insert(projectModel);
//            }

            List<ProjectModel> deletedProjects = new ArrayList<>();
            List<ProjectModel> projectsInDb = new ArrayList<>();

            List<ProjectModel> allProjectsInDb = mAsyncTaskDao.getAllProjectsList();

            List<ProjectModel> projectsFromApiCall = params[0];

            for (ProjectModel tempProjectsInDb : allProjectsInDb) {
                boolean isAdded = false;
                for (ProjectModel tempProjectModel : projectsFromApiCall) {
                    if (tempProjectModel.getProjectid().equals(tempProjectsInDb.getProjectid())) {
                        projectsInDb.add(tempProjectsInDb);
                        isAdded = true;
                    }
                }

                if (!isAdded) {
                    deletedProjects.add(tempProjectsInDb);
                }
            }

            for (ProjectModel tempDeletedProject : deletedProjects) {
                String projectId = tempDeletedProject.getProjectid();

                defectsDao.deleteUsingProjectId(projectId);
                defectsPhotosDao.deleteByProjectId(projectId);
                defectsTradesDao.deleteUsingProjectId(projectId);
                defectsDao.deleteUsingProjectId(projectId);
                gewerkFirmDao.deleteByProjectId(projectId);
                onlinePhotoDao.deleteByProjectId(projectId);
                pdFlawFLagListDao.deleteUsingProjectId(projectId);
                photoDao.deleteUsingProjectId(projectId);
                plansDao.deleteUsingProjectId(projectId);
                plansPhotosDao.deleteRowUsingId(projectId);
                projectUsersDao.deleteByProjectId(projectId);
                recordAudioDao.deleteByProjectId(projectId);
                referPointPlansDao.deleteUsingProjectId(projectId);
                wordDao.deleteUsingProjectId(projectId);
                mAsyncTaskDao.deleteByProjectId(projectId);

            }

            for (int i = 0; i < projectsFromApiCall.size(); i++) {

                boolean isInsert = true;

                params[0].get(i).setSyncStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                ProjectModel projectModel = mAsyncTaskDao.getProjectOBJ(params[0].get(i).getProjectid());
                long last_update_timeStamp = 0, last_update_timeStamp_current = 0;
                if (projectModel != null) {

                    params[0].get(i).setIsPhotoSynced(projectModel.getIsPhotoSynced());
                    params[0].get(i).setSyncStatus(projectModel.getSyncStatus());
                    params[0].get(i).setLastOpen(projectModel.getLastOpen());
                    params[0].get(i).setCacheImagePath(projectModel.getCacheImagePath());
                    params[0].get(i).setFavorite(projectModel.isFavorite());
                    params[0].get(i).setImageCache(projectModel.isImageCache());

                    if (params[0].get(i).getLastupdated() != null && !params[0].get(i).getLastupdated().equals("")) {
                        last_update_timeStamp_current = Utils.convertStringToTimestampUpdated(params[0].get(i).getLastupdated()).getTime();
                    }
                    if (projectModel.getLastupdated() != null && !projectModel.getLastupdated().equals("")) {
                        last_update_timeStamp = Utils.convertStringToTimestampUpdated(projectModel.getLastupdated()).getTime();
                    }
                    if ((last_update_timeStamp_current > last_update_timeStamp && projectModel.isFavorite()) || (projectModel.getSyncStatus().equals(LocalPhotosRepository.UPLOADING_PHOTO) && projectModel.isFavorite())) {
                        params[0].get(i).setSyncStatus(LocalPhotosRepository.UN_SYNC_PHOTO);// We are resynching the project thats why its status is cahnged to unsynch
                        Utils.showLogger("settings status unsynch");
                        Utils.showLogger("Project model oldIDName"+projectModel.getProjectid()+"::"+projectModel.getProject_name());
                        projectIdsForSyncing.add(projectModel.getProjectid());

                        params[0].get(i).setImageCache(false);
                        isInsert = false;
                    }

                }
                if (isInsert)
                    mAsyncTaskDao.insert(params[0].get(i));
                else
                    mAsyncTaskDao.insert(params[0].get(i));
            }
            ///projectModelList = mAsyncTaskDao.getAllProjectsList();

            projectModelList.addAll(mAsyncTaskDao.getAllActive());

            projectModelList.addAll(mAsyncTaskDao.getAllInActive());

            if(projectModelList.size()==0)
                projectModelList.addAll(mAsyncTaskDao.getAllItems());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            if (iGetProjectsList != null && projectModelList != null)
                iGetProjectsList.onLoadProjectList(projectModelList);
//            projectIdsForSyncing.add("160");
//            projectIdsForSyncing.add("159");
//            projectIdsForSyncing.add("161");
//            projectIdsForSyncing.add("40");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < projectIdsForSyncing.size(); i++) {
                if (i == projectIdsForSyncing.size() - 1) {
                    sb.append(projectIdsForSyncing.get(i) + "");
                } else {
                    sb.append(projectIdsForSyncing.get(i) + ",");
                }
            }
            String str = sb.toString();

            Intent intentt = new Intent(HomeActivity.BR_ACTION_LAST_UPDATED_PROJECT);
            if (str != null && !str.equals(""))
                intentt.putExtra(HomeActivity.KEY_LAST_UPDATED_PROJECT_IDS_LIST, str.toString());
            mContext.sendBroadcast(intentt);
        }
    }


    private static class DeleteAllPlansAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProjectDao mAsyncTaskDao;

        DeleteAllPlansAsyncTask(ProjectDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    public void callGetListAPI(Context context) {
       // Utils.showLogger("ProjectModel callGetListAPI");
        isLoadingComplete.setValue(false);
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

//        authToken="bd9c32493e3fde42d65c2975da3c5cea";bd4cdbe04bdedbc667c54c4fc05f40d8

        Call<JsonObject> call = retroApiInterface.getListAPI(authToken, Utils.DEVICE_ID);

        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

             //   Utils.showLogger("ProjectModel callGetListAPI onResponse" + response.isSuccessful());

                HomeActivity.isAllProjectsFromServerLoaded.postValue(true);


                Log.d("Header", call.request().header("apitoken"));
                isLoadingComplete.setValue(true);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());

//                        mAllProjects.getValue().addAll(new Gson().fromJson(response.body().getAsJsonArray("data"), new TypeToken<List<ProjectModel>>() {
//                        }.getType()));

                        List<ProjectModel> projectModelList = new Gson().fromJson(response.body().getAsJsonArray("data"), new TypeToken<List<ProjectModel>>() {
                        }.getType());
                        if (projectModelList != null)
                            insertAll(projectModelList);

                        isPlansLoadInDB = true;
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
            public void onFailure(Call<JsonObject> call, Throwable t) {
                isLoadingComplete.setValue(true);
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }


}
