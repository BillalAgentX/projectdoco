package com.projectdocupro.mobile.service;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.IDefectSyncTaskComplete;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectTradeModel;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.receivers.WorkerResultReceiver;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.projectdocupro.mobile.fragments.DefectsListFragment.BR_ACTION_UPDATE_DEFECT_DATA;

public class syncLocalDefetcsDataService extends JobIntentService {
    public static final String FEED_ID = "feed_Id";
    public static final String DELETED_FEED_IDs = "deleted_feed_Ids";
    public static final String DELETED_COMMENT_IDs = "deleted_comment_Ids";
    private static final String TAG = syncLocalDefetcsDataService.class.getSimpleName();
    public final static int NOTIFICATION_ID = 20140820;
    String mes;
    private Handler handler;
    Context context;
    private Long logID;
    private String feedID;
    private CountDownLatch signal;
    CountDownLatch latch = new CountDownLatch(1);
    CountDownLatch latch2 = new CountDownLatch(1);

    List<String> feedandLocalIDList = new ArrayList<>();
    String likeType = "";
    private String localID;
    private ProjectsDatabase projectsDatabase;
    PhotoDao photoDao;
    private String projectId = "";
    private List<WordModel> wordsSimpleList;
    private DefectsModel defectModelOBJ;
    private List<PhotoModel> flawPhotosList;
    private List<DefectTradeModel> defectTradeList;
    private String pdphotoid = "";

    public static final String RECEIVER = "receiver";
    public static final String PROJECT_ID = "projectId";
    public static final String DEFECT_OBJ = "defect_obj";
    public static final String IS_PHOTOS_AUTO_SYNC = "photos_auto_sync";
    public static final int SHOW_RESULT = 123;
    static final int JOB_ID = 1000;
    private static final String ACTION_DOWNLOAD = "action.DOWNLOAD_DATA";
    ResultReceiver mResultReceiver;
    private boolean isPhotosAutoSync;
    private boolean isMobileDataActive;

    private PdFlawFlagRepository pdFlawFlagRepository;
    SharedPrefsManager sharedPrefsManager;

    DefectsModel getDefectModelOBJ = null;


    public static final String BR_ACTION_UPDATE_PROJECT_LIST = "br_update_projects";
    public static final int SHOW_RESULT_SUCCESS = 1231;
    public static final int SHOW_RESULT_FAILURE = 1232;
    static IDefectSyncTaskComplete iDefectSyncTaskComplete;

    static List<DefectsModel> defectsModelList = new ArrayList<>();
    int counter = 0;
    private Pdflawflag flawFlagObjWithoutPhoto;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        handler = new Handler();
        projectsDatabase = ProjectsDatabase.getDatabase(getApplicationContext());
        photoDao = projectsDatabase.photoDao();
        sharedPrefsManager = new SharedPrefsManager(getApplicationContext());
//		if (signal == null)
//			signal= new CountDownLatch(1);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(context, "Service onCreate()", Toast.LENGTH_SHORT).show();
//            }
//        }, 500);

    }

    public static void enqueueWork(Context context, WorkerResultReceiver workerResultReceiver, String projectId, Boolean is_photo_auto_sync) {
        Intent intent = new Intent(context, syncLocalDefetcsDataService.class);
        intent.putExtra(RECEIVER, workerResultReceiver);
        intent.putExtra(PROJECT_ID, projectId);
        intent.putExtra(IS_PHOTOS_AUTO_SYNC, is_photo_auto_sync);
        enqueueWork(context, syncLocalDefetcsDataService.class, JOB_ID, intent);
    }

    public static void enqueueWork(Context context, WorkerResultReceiver workerResultReceiver, DefectsModel defectsModelObj) {
        Intent intent = new Intent(context, syncLocalDefetcsDataService.class);
        intent.putExtra(RECEIVER, workerResultReceiver);
        intent.putExtra(DEFECT_OBJ, defectsModelObj);
        intent.putExtra(PROJECT_ID, defectsModelObj.getProjectId());

        enqueueWork(context, syncLocalDefetcsDataService.class, JOB_ID, intent);
    }

    public static void enqueueWork(Context context, IDefectSyncTaskComplete iDefectSyncTask, DefectsModel defectsModelObj) {
        Intent intent = new Intent(context, syncLocalDefetcsDataService.class);
        iDefectSyncTaskComplete = iDefectSyncTask;
        intent.putExtra(DEFECT_OBJ, defectsModelObj);
        intent.putExtra(PROJECT_ID, defectsModelObj.getProjectId());
        enqueueWork(context, syncLocalDefetcsDataService.class, JOB_ID, intent);
    }


    public static void enqueueWork(Context context) {
        Intent intent = new Intent(context, syncLocalDefetcsDataService.class);
        enqueueWork(context, syncLocalDefetcsDataService.class, JOB_ID, intent);
    }

    @Override
    public void onDestroy() {
//        defectsModelList = null;
        super.onDestroy();

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getApplicationContext(),"onHandleWork",Toast.LENGTH_SHORT).show();
//            }
//        });

        mResultReceiver = intent.getParcelableExtra(RECEIVER);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            projectId = extras.getString(PROJECT_ID, "");
            getDefectModelOBJ = extras.getParcelable(DEFECT_OBJ);
            isPhotosAutoSync = extras.getBoolean(IS_PHOTOS_AUTO_SYNC, false);
        }
//		if(Navigator.userInfoPrefs.contains("isNewsFeedServiceRunninig")&& !Navigator.userInfoPrefs.getBoolean("isNewsFeedServiceRunninig",false)) {

        Log.d(TAG, "onHandleIntent: intent" + intent);

        List<PhotoModel> photoModelList;

//        if (projectId == null)
//            return;

        if (ProjectNavigator.wlanIsConnected(context) || ProjectNavigator.mobileNetworkIsConnected(context)) {

            if (getDefectModelOBJ != null) {

//                    DefectsModel defectsModel = defectsModelList.get(0);
                projectId = getDefectModelOBJ.getProjectId();
                Log.d(TAG, "onHandleIntentDefect: " + counter);


//                if (iDefectSyncTaskComplete != null) {
//                    new UpdateDefectsWithoutPhotoAsyncTask().execute(getDefectModelOBJ);
//                } else {
                new UpdateDefectsAsyncTask().execute(getDefectModelOBJ);
//                }
//                while (counter < defectsModelList.size()) {
//
//                counter++;
//                }
            } else {
                stopSelf();
            }
            /*if (projectId != null && !projectId.equalsIgnoreCase("")) {

                if (isPhotosAutoSync) {
                    if (photoDao.autoUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {
                        while (photoDao.autoUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {
                            if (sharedPrefsManager != null && sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, false)) {
                                photoModelList = photoDao.autoUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                            } else {
                                photoModelList = photoDao.autoUploadinglocalPhotosCountDESC(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                            }
                            if (photoModelList == null && photoModelList.size() > 0) {
                                Log.d(TAG, "onHandleIntent: newsFeedLogsList1 null");
                                continue;
                            }

                            for (int i = 0; i < photoModelList.size(); i++) {
                                callCheckPhotoQualityAPI(photoModelList.get(i));
                                try {
                                    Log.d(TAG, "onHandleIntent: signal.await start");
                                    signal = new CountDownLatch(1);
                                    signal.await();
                                    Log.d(TAG, "onHandleIntent: signal.await end");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.d(TAG, "onHandleIntent: while loop end" + photoDao.autoUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size());
                        }
                    } else {
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(),"Auto no item found",Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        stopSelf(JOB_ID);
                    }
                } else {
                    if (photoDao.userActionUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {

                        while (photoDao.userActionUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {

                            if (sharedPrefsManager != null && sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, false)) {
                                photoModelList = photoDao.userActionUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                            } else {
                                photoModelList = photoDao.userActionUploadinglocalPhotosCountDESC(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                            }
                            if (photoModelList == null && photoModelList.size() > 0) {
                                Log.d(TAG, "onHandleIntent: newsFeedLogsList1 null");
                                continue;
                            }
                            for (int i = 0; i < photoModelList.size(); i++) {
                                callCheckPhotoQualityAPI(photoModelList.get(i));
                                try {
                                    Log.d(TAG, "onHandleIntent: signal.await start");
                                    signal = new CountDownLatch(1);
                                    signal.await();
                                    Log.d(TAG, "onHandleIntent: signal.await end");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }


                            Log.d(TAG, "onHandleIntent: while loop end" + photoDao.userActionUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size());
                        }
                    } else {

//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(),"Manual no item found",Toast.LENGTH_SHORT).show();
//                            }
//                        });
                        stopSelf(JOB_ID);
                    }

                }

            } else {

                if (photoDao.autoUploadingLocalPhotosCountAllProjects(LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {
                    while (photoDao.autoUploadingLocalPhotosCountAllProjects(LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {
                        photoModelList = photoDao.autoUploadinglocalPhotosAllProjects(LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                        if (photoModelList == null && photoModelList.size() > 0) {
                            Log.d(TAG, "onHandleIntent: newsFeedLogsList1 null");
                            continue;
                        }

                        for (int i = 0; i < photoModelList.size(); i++) {
                            callCheckPhotoQualityAPI(photoModelList.get(i));
                            try {
                                Log.d(TAG, "onHandleIntent: signal.await start");
                                signal = new CountDownLatch(1);
                                signal.await();
                                Log.d(TAG, "onHandleIntent: signal.await end");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d(TAG, "onHandleIntent: while loop end" + photoDao.autoUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size());
                    }
                }

            }*/

        } else {

        }
//        Navigator.userInfoPrefs.edit().putBoolean("isNewsFeedServiceRunninig", false).commit();
//		}

    }


    private class UpdateDefectsAsyncTask extends AsyncTask<DefectsModel, Void, Void> {
        private DefectsDao mAsyncTaskDao;
        private PhotoDao photoDao;

        UpdateDefectsAsyncTask() {
            mAsyncTaskDao = projectsDatabase.defectsDao();
            photoDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final DefectsModel... params) {
            //  mAsyncTaskDao.update(params[0]);
            List<PhotoModel> photoModelList = photoDao.getDefectPhotosAndUnSyncLocalPhotosList(projectId, params[0].getDefectLocalId() + "");
            if (photoModelList != null && photoModelList.size() > 0) {
                for (int i = 0; i < photoModelList.size(); i++) {
                    PhotoModel photoModel = photoModelList.get(i);
                    photoModel.setUserSelectedStatus(true);
                    photoModel.setPhotoSynced(false);
                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                    photoDao.update(photoModel);
                }
                SyncLocalPhotosService.enqueueWork(context, mResultReceiver, projectId, false);

            } else {


                Utils.showLogger("syncing wihtout uploading img");

                defectTradeList = projectsDatabase.defectTradeDao().getAllDefectTradeWithStatusONModel(projectId, params[0].defectLocalId + "");
                flawPhotosList = photoDao.getPhotosForSyncing(projectId, params[0].defectLocalId + "");
                callUpdateFlawsAPI(params[0],true);//calling if no image
                //    stopSelf();
//                try {
//                    signal = new CountDownLatch(1);
//                    signal.await();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            }

            return null;
        }
    }



    public void showToast() {
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_LONG).show();
            }
        });
    }

    String hash = "", quality = "";


    private void syncingState(PhotoModel photoModel) {
        photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);

    }

    String photoQuality = LocalPhotosRepository.MISSING_PHOTO_QUALITY;
    ExifInterface exifInterface = null;
    int photoWidth = 0;
    int photoHeight = 0;
    int orientation = 0;


    private void unSyncedPhoto(DefectsModel photoModel) {

        photoModel.setUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);

        new UpdateAsyncTask().execute(photoModel);
        signalCountDown();


        Bundle intent1 = new Bundle();
        intent1.putBoolean(IS_PHOTOS_AUTO_SYNC, isPhotosAutoSync);
        intent1.putString(PROJECT_ID, projectId);
        if (mResultReceiver != null)
            mResultReceiver.send(SHOW_RESULT, intent1);

        if (iDefectSyncTaskComplete != null)
            iDefectSyncTaskComplete.onReceiveDefectResult(SHOW_RESULT, intent1);

    }

    private void defectSynced(DefectsModel photoModel) {

//        photoModel.setUploadStatus(LocalPhotosRepository.SYNCED_PHOTO);

        new UpdateAsyncTask().execute(photoModel);

        signalCountDown();
        Intent intent = new Intent("updateProfile");
        sendBroadcast(intent);
        Bundle intent1 = new Bundle();
        intent1.putBoolean(IS_PHOTOS_AUTO_SYNC, isPhotosAutoSync);
        intent1.putString(PROJECT_ID, projectId);
        if (mResultReceiver != null)
            mResultReceiver.send(SHOW_RESULT, intent1);

        if (iDefectSyncTaskComplete != null)
            iDefectSyncTaskComplete.onReceiveDefectResult(SHOW_RESULT, intent1);
//        if (!ProjectNavigator.isPhotoActivityForground) {
//            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
//            notificationHelper.createNotification("Photo Syncing.", "Photo Syncing Complete successfully");
//        }
    }


    private void callUpdateFlawsAPI(DefectsModel defectModelOBJ, boolean isUpdateCordinateWithoutImage) {
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getApplication());
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }
        SimpleDateFormat yyyMMddHHmmssFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
        String photoDate = yyyMMddHHmmssFormat.format(new Date());
        JsonObject params = new JsonObject();


        params.addProperty("pdflawid", defectModelOBJ.getDefectId());
        params.addProperty("projectid", defectModelOBJ.getProjectId());
        params.addProperty("pduserid", defectModelOBJ.getUserId());

        if (defectModelOBJ.getDefectType() != null)
            params.addProperty("flawtype", defectModelOBJ.getDefectType());
        else
            params.addProperty("flawtype", "");

        if (defectModelOBJ.getDefectDate() != null)
            params.addProperty("flawdate", defectModelOBJ.getDefectDate());
        else
            params.addProperty("flawdate", "");

        if (defectModelOBJ.getStatus() != null)
            params.addProperty("status", defectModelOBJ.getStatus());
        else
            params.addProperty("status", "");

        if (defectModelOBJ.getStatus() != null)
            params.addProperty("creator", defectModelOBJ.getCreator());
        else
            params.addProperty("creator", "");

        if (defectModelOBJ.getDescription() != null)
            params.addProperty("description", defectModelOBJ.getDescription());
        else
            params.addProperty("description", "");

        if (defectModelOBJ.getDefectName() != null)
            params.addProperty("flawname", defectModelOBJ.getDefectName());
        else
            params.addProperty("flawname", "");

        if (defectModelOBJ.getCreated() != null)
            params.addProperty("created", defectModelOBJ.getCreated());
        else
            params.addProperty("created", "");

        if (defectModelOBJ.fristdate_df > 0) {

            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(defectModelOBJ.fristdate_df);
            String date = DateFormat.format("yyyy-MM-dd hh:mm:ss", cal).toString();
            params.addProperty("fristdate", date);
        } else
            params.addProperty("fristdate", "");

        if (defectModelOBJ.getDeleted() != null)
            params.addProperty("deleted", defectModelOBJ.getDeleted());
        else
            params.addProperty("deleted", "");

        if (defectModelOBJ.getNoticeDate() != null)
            params.addProperty("noticedate", defectModelOBJ.getNoticeDate());
        else
            params.addProperty("noticedate", "");

        if (defectModelOBJ.getNotifieDate() != null)
            params.addProperty("notifiedate", defectModelOBJ.getNotifieDate());
        else
            params.addProperty("notifiedate", "");

        if (defectModelOBJ.getSecondFristDate() != null)
            params.addProperty("secondfristdate", defectModelOBJ.getSecondFristDate());
        else
            params.addProperty("secondfristdate", "");

        if (defectModelOBJ.donedate_df > 0) {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(defectModelOBJ.donedate_df);
            String date = DateFormat.format("yyyy-MM-dd hh:mm:ss", cal).toString();
            params.addProperty("donedate", date);
        } else
            params.addProperty("donedate", "");

        if (defectModelOBJ.getResponsibleUser() != null)
            params.addProperty("responsibleuser", defectModelOBJ.getResponsibleUser());
        else
            params.addProperty("responsibleuser", "");


        if (flawPhotosList != null && flawPhotosList.size() > 0) {
            JsonArray jsonElements = new JsonArray();
            for (int i = 0; i < flawPhotosList.size(); i++) {
                JsonObject jsonObject = new JsonObject();
                Log.d("DEFECTS_DUPLICATE", "callUpdateFlawsAPI: PhotoId = " + flawPhotosList.get(i).getPdphotoid());
                jsonObject.addProperty("pdphotoid", flawPhotosList.get(i).getPdphotoid());
                if (flawPhotosList.get(i).getDescription() != null)
                    jsonObject.addProperty("pdphototext", flawPhotosList.get(i).getDescription());
                else
                    jsonObject.addProperty("pdphototext", "");
                jsonElements.add(jsonObject);
            }
            params.add("flawitems", jsonElements);
        }

        if (defectTradeList != null && defectTradeList.size() > 0) {
            JsonArray jsonElements = new JsonArray();
            for (int i = 0; i < defectTradeList.size(); i++) {
                JsonObject jsonObject = new JsonObject();
                if (defectTradeList.get(i).getSelectvalue() != null)
                    jsonObject.addProperty("selectedvalue", defectTradeList.get(i).getSelectvalue());
                else
                    jsonObject.addProperty("selectedvalue", "");
                jsonElements.add(jsonObject);
            }
            params.add("trades", jsonElements);
        }



        String localFlawID = defectModelOBJ.defectLocalId+"";

        pdFlawFlagRepository = new PdFlawFlagRepository(context, projectId,localFlawID);

        //flawFlagObjWithoutPhoto = pdFlawFLagListDao.getFlawFlagOBJExist(projectId, defectModelOBJ.getDefectId());
        flawFlagObjWithoutPhoto = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagOBJExist(projectId, localFlawID);



        addPdfCoordinates(params);

        Utils.showLogger("DefectSserviceuploadingNewObject"+params.toString());

        Call<JsonObject> call = retroApiInterface.getUpdateFlawsAPI(authToken, Utils.DEVICE_ID, params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        String runid = response.body().getAsJsonObject("data").get("runid").toString().replace("\"", "");
                        String pdflawid = response.body().getAsJsonObject("data").get("pdflawid").toString().replace("\"", "");

                        // Toast.makeText(getApplication(), "Finally Success Only Mangel", Toast.LENGTH_SHORT).show();
                        stopSelf();
                        defectModelOBJ.setDeleted("0");
                        defectModelOBJ.setDefectId(pdflawid);
                        if (runid != null && !runid.equals("")) {
                            defectModelOBJ.setRunidInt(Integer.valueOf(runid));
                            defectModelOBJ.setRunId(runid);
                        }
                        defectSynced(defectModelOBJ);

                        Log.d("Finally Success", "Finally Successs");

                    } else {
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    Log.d("Login", "Not Success : " + response.toString());
//                    Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    unSyncedPhoto(defectModelOBJ);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                unSyncedPhoto(defectModelOBJ);
                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(getApplication(), "Failures Success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPdfCoordinates(JsonObject params) {
        if (flawFlagObjWithoutPhoto != null && flawFlagObjWithoutPhoto.getXcoord() != null) {
            JsonArray jsonElements = new JsonArray();

            JsonObject jsonObject = new JsonObject();

            if (flawFlagObjWithoutPhoto.getPdFlawFlagServerId() != null)
                jsonObject.addProperty("pdflawflagid", flawFlagObjWithoutPhoto.getPdFlawFlagServerId());
            else
                jsonObject.addProperty("pdflawflagid", "");
            if (flawFlagObjWithoutPhoto.getPdplanid() != null)
                jsonObject.addProperty("pdplanid", flawFlagObjWithoutPhoto.getPdplanid());
            else
                jsonObject.addProperty("pdplanid", "");

            if (flawFlagObjWithoutPhoto.getXcoord() != null)
                jsonObject.addProperty("xcoord", flawFlagObjWithoutPhoto.getXcoord());
            else
                jsonObject.addProperty("xcoord", "");

            if (flawFlagObjWithoutPhoto.getYcoord() != null)
                jsonObject.addProperty("ycoord", flawFlagObjWithoutPhoto.getYcoord());
            else
                jsonObject.addProperty("ycoord", "");
            jsonElements.add(jsonObject);

            params.add("flawflags", jsonElements);
        }
        else
        {
            Utils.showLogger("No X Coordingates found");
        }
    }


/*
    private class CreateOrUpdateLocalFlawFlag extends AsyncTask<Pdflawflag, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        CreateOrUpdateLocalFlawFlag() {
        }

        @Override
        protected Void doInBackground(final Pdflawflag... params) {
            pdFlawFlagRepository = new PdFlawFlagRepository(context, projectId);

            if (params[0] != null && params[0].getPdFlawFlagServerId() != null) {
                pdFlawFlagRepository.getmDefectsPhotoDao().update(params[0]);
            }
            latch2.countDown();

            return null;
        }
    }
*/

    private class UpdateAsyncTask extends AsyncTask<DefectsModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        private DefectsDao defectsDao;

        UpdateAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
            defectsDao = projectsDatabase.defectsDao();
        }

        @Override
        protected Void doInBackground(final DefectsModel... params) {
            defectsDao.update(params[0]);

            if (params[0].getDefectLocalId() != 0 && params[0].getDefectLocalId() > 0) {
                if (mAsyncTaskDao.getDefectPhotosAndUnSyncLocalPhotosCount(projectId, params[0].getDefectLocalId() + "") == 0) {

//                    DefectsModel defectsModel = defectsDao.getDefectsOBJ(projectId, params[0].getDefectLocalId() + "");
                    if (params[0] != null) {
                        params[0].setUploadStatus(DefectRepository.SYNCED_PHOTO);
                        defectsDao.update(params[0]);
                    }
                    Intent intent = new Intent("updateDefectPhotos");
                    sendBroadcast(intent);

                    Intent intentt = new Intent(BR_ACTION_UPDATE_DEFECT_DATA);

                    intentt.putExtra("flawId", params[0].getDefectLocalId() + "");
                    intentt.putExtra("uploadStatus", DefectRepository.SYNCED_PHOTO);
                    sendBroadcast(intentt);


                } else {
                    DefectsModel defectsModel = defectsDao.getDefectsOBJ(projectId, params[0].getDefectLocalId() + "");
                    if (defectsModel != null) {
                        defectsModel.setUploadStatus(DefectRepository.UN_SYNC_PHOTO);
                        defectsDao.update(defectsModel);
                    }
                    Intent intent = new Intent("updateDefectPhotos");
                    sendBroadcast(intent);

                    Intent intentt = new Intent(BR_ACTION_UPDATE_DEFECT_DATA);
                    intentt.putExtra("flawId", params[0].getDefectLocalId());
                    intentt.putExtra("uploadStatus", DefectRepository.UN_SYNC_PHOTO);
                    sendBroadcast(intentt);

                }
            }

//            if (iDefectSyncTaskComplete != null) {
            if (mAsyncTaskDao.getUnSyncedPhotoCount(params[0].getProjectId()) == 0) {

                Intent intentt = new Intent(BR_ACTION_UPDATE_PROJECT_LIST);
                intentt.putExtra(PROJECT_ID, params[0].getProjectId());
                sendBroadcast(intentt);
                iDefectSyncTaskComplete = null;
            }

//            }

            return null;
        }
    }

    private void signalCountDown() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.d(TAG, "signalCountDown: signal.countDown start");
//                signal.countDown();
                Log.d(TAG, "signalCountDown: signal.countDown end");

            }
        }.start();
    }

    private Bitmap getCompressedBitmap(Bitmap bitmap) throws IOException {
        float oldImageWidth = 0.0f;
        float oldImageHeight = 0.0f;

        float fixedImageWidth = 1080;
        float fixedImageHeight = 1080;

        float newImageWidth = 0.0f;
        float newImageHeight = 0.0f;

        Bitmap scaledBitmap = null;
        if (bitmap != null) {
            oldImageHeight = bitmap.getHeight();//1920x1080
            oldImageWidth = bitmap.getWidth();

            if (oldImageHeight > oldImageWidth && oldImageHeight > fixedImageHeight) {

                newImageHeight = (int) (oldImageWidth * (fixedImageHeight / oldImageHeight));
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) fixedImageWidth, (int) newImageHeight, true);

            } else if (oldImageWidth > oldImageHeight && oldImageWidth > fixedImageWidth) {

                newImageWidth = (int) (oldImageHeight * (fixedImageWidth / oldImageWidth));
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) newImageWidth, (int) fixedImageHeight, true);
            }

            if (scaledBitmap == null)
                return bitmap;
        }
        return scaledBitmap;
    }







}
