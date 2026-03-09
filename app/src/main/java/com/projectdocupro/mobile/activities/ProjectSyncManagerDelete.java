package com.projectdocupro.mobile.activities;

import android.app.Dialog;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.interfaces.ISyncTaskComplete;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;
import com.projectdocupro.mobile.repos.AllPlansRepository;
import com.projectdocupro.mobile.repos.DefectPhotoRepository;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.DefectTradesRepository;
import com.projectdocupro.mobile.repos.PlansPhotoRepository;
import com.projectdocupro.mobile.repos.ProjectDetailRepository;
import com.projectdocupro.mobile.repos.WordsRepository;

import java.util.List;

public class ProjectSyncManagerDelete implements ISyncTaskComplete {
    Context context;
    String project_ID;
    AppCompatActivity activity;
    ISyncTaskComplete iSyncTaskComplete;
    Dialog progressView;

    ProjectDetailRepository projectDetailRepository;
    private WordsRepository mRepository;
    private AllPlansRepository plansRepository;
    private DefectRepository mDefectRepository;

    private LiveData<List<DefectsModel>> mDefectOfProjects;

    public LiveData<List<ProjectUserModel>> getProjectDetailModel() {
        return projectDetailRepository.getListLiveData();
    }

    private LiveData<List<WordModel>> listLiveData;
    private boolean isSyncData;

    public ProjectSyncManagerDelete(Context mContext, String ProjectId, ISyncTaskComplete syncTaskComplete, boolean isSyncProjectData) {
        context = mContext;
        project_ID = ProjectId;
        activity = (AppCompatActivity) context;
        iSyncTaskComplete = syncTaskComplete;
        isSyncData = isSyncProjectData;
        progressView = ProjectNavigator.showCustomProgress(mContext, "", false);
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
        plansRepository = new AllPlansRepository(activity, project_ID);
        mDefectRepository = new DefectRepository(activity, project_ID);

        if (isSyncData) {
//            usersProjectRepo();
        } else {
            performUnsyncAction();
        }
    }

    private void performUnsyncAction() {
        // delete detail data
        projectDetailRepository.deleteAllProjectData();
        projectDetailRepository.getmRepository().deleteAllWords();
        // delete plans data
        plansRepository.deleteAll();
        PlansPhotoRepository plansPhotoRepository = new PlansPhotoRepository(activity);
        plansPhotoRepository.deleteAllROws();
        // delete Defect data
        mDefectRepository.deleteAllDefects();
        DefectPhotoRepository mRepository = new DefectPhotoRepository(activity);
        DefectTradesRepository mRepositoryDefecttrade = new DefectTradesRepository(activity, project_ID);
        mRepositoryDefecttrade.deleteAllROws();
        mRepository.deleteAllROws();
        onSuccess(project_ID,isSyncData);
    }

    boolean isUserProjectCalled = false;
    boolean isWordCalled = false;
//
//    private void usersProjectRepo() {
//
//
//        projectDetailRepository.getListLiveData().observe(activity, projectUserModels -> {
//
//            if (projectUserModels == null || projectUserModels.size() == 0) {
//                projectDetailRepository.callGetListAPI(context.getApplicationContext(), project_ID);
//            } else {
//                getAllPlanRepo();
//
//            }
//        });
//
//
//        projectDetailRepository.isProjectDetailSuccess.observe(activity, aBoolean ->
//        {
//            if (aBoolean) {
//                getAllPlanRepo();
//
//            } else {
//                Toast.makeText(activity, "Project Detail API Failure found.", Toast.LENGTH_SHORT).show();
//                onFailure();
//            }
//        });
//
//
//    }
//
//    private void getAllWordsRepo() {
//        projectDetailRepository.getWordsList().observe(activity, projectUserModels -> {
//
//            if (projectUserModels == null || projectUserModels.size() == 0) {
//                projectDetailRepository.callGetListAPIWord(context.getApplicationContext(), project_ID);
//            } else {
//                getAllPlanRepo();
//            }
//
//        });
//
//        projectDetailRepository.isProjectWordSuccess.observe(activity, aBoolean ->
//        {
//            if (aBoolean) {
//                getAllPlanRepo();
//            } else {
//                Toast.makeText(activity, "Project Words API Failure found.", Toast.LENGTH_SHORT).show();
//                onFailure();
//            }
//        });
//    }
//
//    private void getAllPlanRepo() {
//        plansRepository.getAllProjects().observe(activity, plansModels -> {
//            if (plansModels == null || plansModels.size() == 0) {
//                plansRepository.callGetListAPI(activity, project_ID);
//            } else {
//                getAllDefectRepo();
//            }
//        });
//
//
//        plansRepository.isAllPlansSuccess.observe(activity, aBoolean ->
//        {
//            if (aBoolean) {
//                getAllDefectRepo();
//            } else {
//                Toast.makeText(activity, "Project Plans API Failure found.", Toast.LENGTH_SHORT).show();
//                onFailure();
//            }
//        });
//    }
//
//
//    private void getAllDefectRepo() {
//        //        plansRepository. deleteAll();
//        mDefectRepository.getAllDefects().observe(activity, plansModels -> {
//            if (plansModels == null || plansModels.size() == 0) {
//
//                mDefectRepository.callGetDefectsAPI(activity, project_ID);
//
//            } else {
//                onSuccess();
//            }
//        });
//        mDefectRepository.getIsProgressComplete().observe(activity, aBoolean -> {
//            if (aBoolean) {
//                onSuccess();
//            } else {
//                Toast.makeText(activity, "Project Defects API Failure found.", Toast.LENGTH_SHORT).show();
//                onFailure();
//            }
//
//        });
//    }


    @Override
    public void onSuccess(String project_ID,boolean isSync) {
        if (progressView != null)
            progressView.dismiss();
        ((HomeActivity) activity).onSuccess(project_ID,isSyncData);
    }

    @Override
    public void onFailure(String project_ID,boolean isSync) {
        if (progressView != null)
            progressView.dismiss();
//        ((HomeActivity) activity).onSuccess();

    }
}
