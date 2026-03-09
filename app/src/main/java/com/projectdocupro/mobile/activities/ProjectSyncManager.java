package com.projectdocupro.mobile.activities;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.ReferPointPlansDao;
import com.projectdocupro.mobile.interfaces.ISyncTaskComplete;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;
import com.projectdocupro.mobile.repos.AllPlansRepository;
import com.projectdocupro.mobile.repos.DefectPhotoRepository;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.DefectTradesRepository;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.repos.ProjectDetailRepository;
import com.projectdocupro.mobile.repos.WordsRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.util.List;

public class ProjectSyncManager implements ISyncTaskComplete {
    Context context;
    String project_ID;
    AppCompatActivity activity;
    ISyncTaskComplete iSyncTaskComplete;
    Dialog progressView;

    ProjectDetailRepository projectDetailRepository;
    DefectTradesRepository defectTradesRepository;
    private WordsRepository mRepository;
    private AllPlansRepository plansRepository;

    private LocalPhotosRepository localPhotosRepository;
    private DefectPhotoRepository defectPhotoRepository;
    private DefectRepository mDefectRepository;

    private LiveData<List<DefectsModel>> mDefectOfProjects;

    private boolean isSuccessCall;

    public LiveData<List<ProjectUserModel>> getProjectDetailModel() {
        return projectDetailRepository.getListLiveData();
    }

    private LiveData<List<WordModel>> listLiveData;
    private boolean isSyncData;

    boolean isProjDetailCalled = false;
    boolean isPlansCalled = false;
    boolean isDefectCalled = false;

    public ProjectSyncManager(Context mContext, String ProjectId) {
        context = mContext;
        project_ID = ProjectId;
        activity = (AppCompatActivity) context;
        initializeRepos();
    }

    public ProjectSyncManager(Context mContext, String ProjectId, ISyncTaskComplete syncTaskComplete, boolean isSyncProjectData) {
        Utils.showLogger("ProjectSyncManager");
        context = mContext;
        project_ID = ProjectId;
        activity = (AppCompatActivity) context;
        iSyncTaskComplete = syncTaskComplete;
        isSyncData = isSyncProjectData;
        isSuccessCall = true;
//        progressView = ProjectNavigator.showCustomProgress(mContext, "", false);
//        progressView.show();

//        new AlertDialog.Builder(context)
//                .setTitle("Delete entry")
//                .setMessage("Are you sure you want to delete this entry?")
//
//                // Specifying a listener allows you to take an action before dismissing the dialog.
//                // The dialog is automatically dismissed when a dialog button is clicked.
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // Continue with delete operation
//                    }
//                })
//
//                // A null listener allows the button to dismiss the dialog and take no further action.
//                .setNegativeButton(android.R.string.no, null)
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
        initializeRepos();
    }


    private void initializeRepos() {
        projectDetailRepository = new ProjectDetailRepository(activity.getApplication(), project_ID);
        defectTradesRepository = new DefectTradesRepository(activity.getApplication(), project_ID);
        plansRepository = new AllPlansRepository(activity, project_ID);
        localPhotosRepository = new LocalPhotosRepository(activity, project_ID);
        defectPhotoRepository = new DefectPhotoRepository(activity);
        mDefectRepository = new DefectRepository(activity, project_ID);

        if (isSyncData) {
            usersProjectRepo();
        } else {
            performUnsyncAction(project_ID, true);
        }
    }

    public void performUnsyncAction(String project_ID, boolean isSuccessCalled) {
        isSuccessCall = isSuccessCalled;

        // delete detail data
        projectDetailRepository.deleteUsingProjectIdProjectData(project_ID);
        projectDetailRepository.getmRepository().deleteWordsUsingProjectId(project_ID);
        // delete plans datafsd
        plansRepository.deleteUsingProjectId(project_ID);

        PhotoDao photoDao = ProjectsDatabase.getDatabase(context).photoDao();

        localPhotosRepository.deleteUsingProjectId(photoDao,project_ID);
//        PlansPhotoRepository plansPhotoRepository = new PlansPhotoRepository(activity);
//        plansPhotoRepository.deleteUsingProjectId(project_ID);
        // delete Defect data
        mDefectRepository.deleteDefectsUsingProjectId(project_ID);


        localPhotosRepository = new LocalPhotosRepository(activity.getApplication(), project_ID);
//        localPhotosRepository.deleteUsingProjectId(localPhotosRepository.getPhotoDao(), project_ID);
//        DefectPhotoRepository mRepository = new DefectPhotoRepository(activity);
//        mRepository.deleteAllROws();
        DefectTradesRepository mRepositoryDefecttrade = new DefectTradesRepository(activity, project_ID);
        mRepositoryDefecttrade.deleteROwsUsingProjectId(project_ID);
        PdFlawFlagRepository pdFlawFlagRepository = new PdFlawFlagRepository(activity);
        pdFlawFlagRepository.deleteROwsUsingProjectId(project_ID);

        defectTradesRepository.deleteROwsUsingProjectId(project_ID);
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        if (project_ID.equalsIgnoreCase(sharedPrefsManager.getLastProjectId(context))) {
            sharedPrefsManager.sharedPreferences.edit().remove(AppConstantsManager.USER_LAST_PROJECT_ID).commit();
            sharedPrefsManager.sharedPreferences.edit().remove(AppConstantsManager.USER_LAST_PROJECT_NAME).commit();
            sharedPrefsManager.sharedPreferences.edit().remove(AppConstantsManager.USER_LAST_PROJECT_PHOTO_URL).commit();
        }
        new DeleteUsingProjectIdAsyncTask().execute(project_ID);


    }

    boolean isUserProjectCalled = false;
    boolean isWordCalled = false;

    private void usersProjectRepo() {
        Utils.showLogger("usersProjectRepo");

        projectDetailRepository.isProjectDetailSuccess.observe(activity, aBoolean ->
        {
            if (aBoolean) {

                getAllWordsRepo();

            } else {
//                Toast.makeText(activity, "Project Detail API Failure found.", Toast.LENGTH_SHORT).show();
                onFailure(project_ID, isSyncData);
            }
        });

        projectDetailRepository.getListLiveData().observe(activity, projectUserModels -> {
            if (projectDetailRepository.isProjectDetailSuccess.getValue() == null || (projectDetailRepository.isProjectDetailSuccess.getValue() != null && !projectDetailRepository.isProjectDetailSuccess.getValue())) {

                if (projectUserModels == null || projectUserModels.size() == 0) {
                    projectDetailRepository.callGetListAPI(context.getApplicationContext(), project_ID);
                    projectDetailRepository.getListLiveData().removeObserver(projectUserModel -> {
                    });
                } else {
                    getAllWordsRepo();
                }
            } else {
                getAllWordsRepo();
            }
            projectDetailRepository.getListLiveData().removeObservers(activity);

        });


    }

    private void getAllWordsRepo() {
        projectDetailRepository.getWordsList().observe(activity, projectUserModels -> {

            if (projectUserModels == null || projectUserModels.size() == 0) {
                projectDetailRepository.callGetListAPIWord(context.getApplicationContext(), project_ID);
                projectDetailRepository.getWordsList().removeObserver(projectUserModel -> {
                });
            } else {
                getAllPlanRepo();
            }
            projectDetailRepository.getWordsList().removeObservers(activity);

        });

        projectDetailRepository.isProjectWordSuccess.observe(activity, aBoolean ->
        {
            if (aBoolean) {
//                onSuccess(project_ID, isSyncData);
//                getAllDefectRepo();
                getAllPlanRepo();
            } else {
//                Toast.makeText(activity, "Project Words API Failure found.", Toast.LENGTH_SHORT).show();
                onFailure(project_ID, isSyncData);
            }
        });
    }

    private void getAllPlanRepo() {
        Utils.showLogger("getAllPlanRepo");
//        Observer observer = new Observer<List<PlansModel>>() {
//            @Override
//            public void onChanged(@Nullable List<PlansModel> plansModels) {
//                if (plansRepository.isAllPlansSuccess.getValue() == null || (plansRepository.isAllPlansSuccess.getValue() != null && !plansRepository.isAllPlansSuccess.getValue())) {
//                    if (plansModels == null || plansModels.size() == 0) {
//                        plansRepository.callGetListAPI(activity, project_ID);
//                        plansRepository.getAllProjects().removeObserver(plansModels1 -> {
//                        });
//                    } else {
//                        getAllDefectRepo();
//                    }
//                } else {
//                    getAllDefectRepo();
//                }
//                plansRepository.getAllProjects().removeObservers((AppCompatActivity)context);
//            }
//        };
//        plansRepository.getAllProjects().observeForever(observer);


        plansRepository.getAllProjects().observe(activity, plansModels -> {
            if (plansRepository.isAllPlansSuccess.getValue() == null || (plansRepository.isAllPlansSuccess.getValue() != null && !plansRepository.isAllPlansSuccess.getValue())) {
                if (plansModels == null || plansModels.size() == 0) {
                    plansRepository.callGetListAPI(activity, project_ID);
//                    plansRepository.getAllProjects().removeObserver(plansModels1 -> {
//                    });
                } else {
                    getAllDefectRepo();
                }
            } else {
                getAllDefectRepo();
            }

            plansRepository.getAllProjects().removeObservers(activity);

        });


        plansRepository.isAllPlansSuccess.observe(activity, aBoolean ->
        {
            if (aBoolean) {
                getAllDefectRepo();
//                Toast.makeText(activity, "Project Plans Success", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(activity, "Project Plans API Failure found.", Toast.LENGTH_SHORT).show();
                onFailure(project_ID, isSyncData);
            }
        });
    }


    private void getAllDefectRepo() {
        //        plansRepository. deleteAll();

        mDefectRepository.getAllDefects().observe(activity, plansModels -> {

            if (mDefectRepository.getIsProgressComplete().getValue() == null || (mDefectRepository.getIsProgressComplete().getValue() != null && !mDefectRepository.getIsProgressComplete().getValue())) {


                if (plansModels == null || plansModels.size() == 0) {

                    mDefectRepository.callGetDefectsAPI(activity, project_ID);
//                    mDefectRepository.getAllDefects().removeObserver(defectsModels -> {
//                    });
                } else {
                    onSuccess(project_ID, isSyncData);
//                    Toast.makeText(activity, " Success "+project_ID, Toast.LENGTH_SHORT).show();

                }
            } else {
                onFailure(project_ID, isSyncData);
            }
            mDefectRepository.getAllDefects().removeObservers(activity);
        });
        mDefectRepository.getIsProgressComplete().observe(activity, aBoolean -> {
            if (aBoolean) {
                onSuccess(project_ID, isSyncData);
//                Toast.makeText(activity, "Project Defects API Success "+project_ID, Toast.LENGTH_SHORT).show();

            } else {
//                Toast.makeText(activity, "Project Defects API Failure found.", Toast.LENGTH_SHORT).show();
                onFailure(project_ID, isSyncData);
            }

        });
    }


    @Override
    public void onSuccess(String project_ID, boolean isSync) {

        Log.d("ProjectSyncSuccess", "Success" + project_ID);
        ((HomeActivity) activity).onSuccess(project_ID, isSyncData);
//        if (progressView != null)
//            progressView.dismiss();
    }

    @Override
    public void onFailure(String project_ID, boolean isSync) {
//        if (progressView != null)
//            progressView.dismiss();
        Log.d("ProjectSyncFailure", "Failure" + project_ID);
        ((HomeActivity) activity).onFailure(project_ID, isSyncData);

    }

    private class DeleteUsingProjectIdAsyncTask extends AsyncTask<String, Void, Void> {
        private ReferPointPlansDao mAsyncTaskDao;

        DeleteUsingProjectIdAsyncTask() {

            mAsyncTaskDao = ProjectsDatabase.getDatabase(context).referPointPlansDao();
        }

        @Override
        protected Void doInBackground(String... params) {

            mAsyncTaskDao.deleteUsingProjectId(params[0]);
//            List<PhotoModel>photoModelList= localPhotosRepository.getPhotoDao().getPhotoLocalList(project_ID);
//            if(photoModelList!=null) {
//                for (int i = 0; i < photoModelList.size(); i++) {
//                    File fdelete = new File(photoModelList.get(i).getPohotPath());
//                    if (fdelete.exists()) {
//                        if (fdelete.delete()) {
//                        } else {
//                        }
//                    }
//                }
//            }

//            localPhotosRepository.deleteUsingProjectId(localPhotosRepository.getPhotoDao(), project_ID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isSuccessCall)
                onSuccess(project_ID, isSyncData);
        }
    }


}
