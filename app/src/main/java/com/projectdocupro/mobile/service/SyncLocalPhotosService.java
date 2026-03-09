package com.projectdocupro.mobile.service;


import static com.projectdocupro.mobile.fragments.DefectsListFragment.BR_ACTION_UPDATE_DEFECT_DATA;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.DefectsTradesDao;
import com.projectdocupro.mobile.dao.PdFlawFLagListDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.dao.RecordAudioDao;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.IPhotosSyncTaskComplete;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectTradeModel;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.RecordAudioModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.localFilters.ImageId_VS_Input;
import com.projectdocupro.mobile.models.localFilters.WordContentModel;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.utility.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncLocalPhotosService extends JobIntentService {
    public static final String FEED_ID = "feed_Id";
    public static final String DELETED_FEED_IDs = "deleted_feed_Ids";
    public static final String DELETED_COMMENT_IDs = "deleted_comment_Ids";
    private static final String TAG = SyncLocalPhotosService.class.getSimpleName();
    public final static int NOTIFICATION_ID = 20140820;
    String mes;
    private Handler handler;
    Context context;
    private Long logID;
    private String feedID;
    private CountDownLatch signal;
    CountDownLatch latch = null;
    CountDownLatch latch2 = null;

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
    public static final String BR_ACTION_UPDATE_PROJECT_LIST = "br_update_projects";
    public static final String IS_PHOTOS_AUTO_SYNC = "photos_auto_sync";
    public static final int SHOW_RESULT = 123;
    public static final int SHOW_RESULT_SUCCESS = 1231;
    public static final int SHOW_RESULT_FAILURE = 1232;
    static final int JOB_ID = 1000;
    private static final String ACTION_DOWNLOAD = "action.DOWNLOAD_DATA";
    ResultReceiver mResultReceiver;
    static IPhotosSyncTaskComplete iPhotosSyncTaskComplete;
    private boolean isPhotosAutoSync;
    private boolean isMobileDataActive;
    private PdFlawFlagRepository pdFlawFlagRepository;
    SharedPrefsManager sharedPrefsManager;
    private Pdflawflag flawFlagObj;
    private Pdflawflag flawFlagObjWithoutPhoto;
    private String updateProfile;

    private ArrayList<String> uploadingAudios = new ArrayList<>();

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

    public static void enqueueWork(Context context, ResultReceiver workerResultReceiver, String projectId, Boolean is_photo_auto_sync) {
        Utils.showLogger("enqueueWork task1");
        Intent intent = new Intent(context, SyncLocalPhotosService.class);
        intent.putExtra(RECEIVER, workerResultReceiver);
        intent.putExtra(PROJECT_ID, projectId);
        intent.putExtra(IS_PHOTOS_AUTO_SYNC, is_photo_auto_sync);
        enqueueWork(context, SyncLocalPhotosService.class, JOB_ID, intent);
    }

    public static void enqueueWork(Context context, IPhotosSyncTaskComplete workerResultReceiver, String projectId, Boolean is_photo_auto_sync) {
        Utils.showLogger("enqueueWork task2");
        Intent intent = new Intent(context, SyncLocalPhotosService.class);
        intent.putExtra(PROJECT_ID, projectId);
        intent.putExtra(IS_PHOTOS_AUTO_SYNC, is_photo_auto_sync);
        iPhotosSyncTaskComplete = workerResultReceiver;
        enqueueWork(context, SyncLocalPhotosService.class, JOB_ID, intent);
    }

    public static void enqueueWork(Context context) {
        Utils.showLogger("enqueueWork task3");
        Intent intent = new Intent(context, SyncLocalPhotosService.class);
        enqueueWork(context, SyncLocalPhotosService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        Utils.showLogger("SynchLocalPhotosService onHandleWork");

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
            isPhotosAutoSync = extras.getBoolean(IS_PHOTOS_AUTO_SYNC, false);
        }
        Utils.showLogger("SynchLocalPhotosService isPhotosAutoSync" + isPhotosAutoSync);

//		if(Navigator.userInfoPrefs.contains("isNewsFeedServiceRunninig")&& !Navigator.userInfoPrefs.getBoolean("isNewsFeedServiceRunninig",false)) {

        Log.d(TAG, "onHandleIntent: intent" + intent);

        List<PhotoModel> photoModelList;

//        if (projectId == null)
//            return;

        if (ProjectNavigator.wlanIsConnected(context) || ProjectNavigator.mobileNetworkIsConnected(context)) {

            if (projectId != null && !projectId.equalsIgnoreCase("")) {

                //boolean  isTest = true;

                if (isPhotosAutoSync) {
                    int autoSyncTrueCount = photoDao.autoUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size();
                    Utils.showLogger("AutoSyncTrueCount" + autoSyncTrueCount + "");
                    if (autoSyncTrueCount > 0) {
                        while (photoDao.autoUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {
                            if (sharedPrefsManager != null && sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE)) {
                                photoModelList = photoDao.autoUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                            } else {
                                photoModelList = photoDao.autoUploadinglocalPhotosCountDESC(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                            }
                            if (photoModelList == null && photoModelList.size() > 0) {
                                Log.d(TAG, "onHandleIntent: newsFeedLogsList1 null");
                                continue;
                            }

                            for (PhotoModel photoModel : photoModelList) {
                                Utils.showLogger("updloading local crea=>" + photoModel.created_df + photoModel.getOrigName());
                            }


                            for (int i = 0; i < photoModelList.size(); i++) {
                                //                       for (int i = photoModelList.size() - 1; i >= 0; i--) {
                                callCheckPhotoQualityAPI(photoModelList.get(i));//plus
                                try {
                                    Utils.showLogger("onHandleIntent: signal.await start");
                                    signal = new CountDownLatch(1);
                                    signal.await();
                                    Log.d(TAG, "onHandleIntent: signal.await end");
                                    Utils.showLogger("onHandleIntent: signal.await end");
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
                    int autoSyncFalseCount = photoDao.userActionUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size();
                    Utils.showLogger("AutoSyncFalseCount>>" + autoSyncFalseCount + "::" + projectId);
                    if (autoSyncFalseCount > 0) {

                        //boolean isTestBreak = true;
                        while (photoDao.userActionUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {

                            if (sharedPrefsManager != null && sharedPrefsManager.getBooleanValue(AppConstantsManager.SYNC_PHOTO_ASC_ORDER, AppConstantsManager.SYNC_PHOTO_ASC_ORDER_DEFAULT_VALUE)) {
                                photoModelList = photoDao.userActionUploadinglocalPhotosCount(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                            } else {
                                photoModelList = photoDao.userActionUploadinglocalPhotosCountDESC(projectId, LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                            }


                        /*    if(isTestBreak)
                                return;*/

                            if (photoModelList == null && photoModelList.size() > 0) {
                                Log.d(TAG, "onHandleIntent: newsFeedLogsList1 null");
                                continue;
                            }
                            for (int i = 0; i < photoModelList.size(); i++) {
                                //                           for (int i = photoModelList.size() - 1; i >= 0; i--) {
                                callCheckPhotoQualityAPI(photoModelList.get(i));//plus
                                try {
                                    Utils.showLogger("onHandleIntent: signal.await start");
                                    Log.d(TAG, "onHandleIntent: signal.await start");
                                    signal = new CountDownLatch(1);
                                    signal.await();
                                    Log.d(TAG, "onHandleIntent: signal.await end");
                                    Utils.showLogger("onHandleIntent: signal.await end");

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
//                        stopSelf(JOB_ID);
                    }

                }

            } else {

                if (photoDao.autoUploadingLocalPhotosCountAllProjects(LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {
                    while (photoDao.autoUploadingLocalPhotosCountAllProjects(LocalPhotosRepository.UPLOADING_PHOTO, LocalPhotosRepository.TYPE_LOCAL_PHOTO).size() > 0) {
                        photoModelList = photoDao.autoUploadinglocalPhotosAllProjects(LocalPhotosRepository.TYPE_LOCAL_PHOTO);
                        if (photoModelList == null && photoModelList.size() > 0) {
                            Log.d(TAG, "onHandleIntent: newsFeedLogsList1 null");
                            continue;
                        }

                        for (int i = 0; i < photoModelList.size(); i++) {
                            //                      for (int i = photoModelList.size() - 1; i >= 0; i--) {
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
                }

            }


        } else {

        }
//        Navigator.userInfoPrefs.edit().putBoolean("isNewsFeedServiceRunninig", false).commit();
//		}

    }

    public void showToast() {
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_LONG).show();
            }
        });
    }

    String hash = "", quality = "";

    private void callCheckPhotoQualityAPI(PhotoModel photoModel) {

        Utils.showLogger("callCheckPhotoQualityAPI");
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
            //Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "upload_pic_id_time_serv" + photoModel.getPdphotolocalId() + " - " + photoModel.getCreated());
        if (ProjectNavigator.mobileNetworkIsConnected(getApplicationContext()))
            isMobileDataActive = true;
        else
            isMobileDataActive = false;
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getApplication());
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        JsonObject params = new JsonObject();

        JsonArray jsonElements = new JsonArray();

        jsonElements.add(photoModel.getHash());

        params.add("hashlist", jsonElements);

        Call<JsonObject> call = retroApiInterface.checkPhotoQualities(authToken, Utils.DEVICE_ID, photoModel.getProjectId(), params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {


                Utils.logResponse(call, response);

                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        if (photoModel.getHash() != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                if (jsonObject.has("versions")) {

                                    if (jsonObject.getJSONObject("versions").has(photoModel.getHash())) {
                                        JSONObject jsonObject1 = jsonObject.getJSONObject("versions").getJSONObject(photoModel.getHash());
                                        if (jsonObject1.has("hash")) {
                                            hash = jsonObject1.getString("hash");
                                        }
                                        if (jsonObject1.has("pdphotoid")) {
                                            pdphotoid = jsonObject1.getString("pdphotoid");
                                        }
                                        if (jsonObject1.has("quality")) {
                                            quality = jsonObject1.getString("quality");
                                            if (!pdphotoid.equalsIgnoreCase("null"))
                                                photoModel.setPdphotoid(pdphotoid);

                                            //BILLAL
                                            boolean isReduceOn4G = sharedPrefsManager.getBooleanValue(AppConstantsManager.MINIMIZE_PHOTO_VIA_MOBILE_DATA, false);

                                            if (ProjectNavigator.mobileNetworkIsConnected(getApplicationContext())
                                                    && pdphotoid.equalsIgnoreCase("null")
                                                    && quality != null && quality.equalsIgnoreCase(LocalPhotosRepository.MISSING_PHOTO_QUALITY)) {
                                                callUploadImageAPI(photoModel);
                                            } else if (!ProjectNavigator.mobileNetworkIsConnected(getApplicationContext())//if wifi
                                                    && quality != null && (quality.equalsIgnoreCase(LocalPhotosRepository.MISSING_PHOTO_QUALITY)
                                                    || quality.equalsIgnoreCase(LocalPhotosRepository.REDUCED_PHOTO_QUALITY))) {
                                                callUploadImageAPI(photoModel);

                                            } else if (!isReduceOn4G && quality != null && (quality.equalsIgnoreCase(LocalPhotosRepository.MISSING_PHOTO_QUALITY)
                                                    || quality.equalsIgnoreCase(LocalPhotosRepository.REDUCED_PHOTO_QUALITY))) {
                                                Utils.showLogger2("last time less quality");
                                                callUploadImageAPI(photoModel);
                                            } else {
                                                Utils.showLogger("Check photo quality api");
                                                callUpdatePhotoAPI(photoModel, pdphotoid);//Check photo quality api
                                            }

//                                            if (quality != null && (quality.equalsIgnoreCase(LocalPhotosRepository.MISSING_PHOTO_QUALITY) || quality.equalsIgnoreCase(LocalPhotosRepository.REDUCED_PHOTO_QUALITY))) {
//                                                callUploadImageAPI(photoModel);
//                                            } else {
//                                                callUpdatePhotoAPI(photoModel);
//                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                unSyncedPhoto(photoModel);
                            }
                        }


                    } else {
                        unSyncedPhoto(photoModel);
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    unSyncedPhoto(photoModel);
//                    Log.d("Login", "Not Success : " + response.toString());
//                    Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                unSyncedPhoto(photoModel);
//                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncingState(PhotoModel photoModel) {
        photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);

    }

    String photoQuality = LocalPhotosRepository.MISSING_PHOTO_QUALITY;
    ExifInterface exifInterface = null;
    int photoWidth = 0;
    int photoHeight = 0;
    int orientation = 0;

    //region upload image api
    private void callUploadImageAPI(PhotoModel photoModel) {
        Utils.showLogger("callUploadImageAPI SynchLocalPhotosService");
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
//            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            return;
        }
        JsonObject params = new JsonObject();
        syncingState(photoModel);
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getApplication());
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");
        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
//        String photoDate = simpleDateFormat.format(new Date());
        String photoDate = photoModel.getPhotoDate();
        //String photoDate = "2025-07-21 22:10:49";

        String photoDateSplit = "";
        if (photoDate.contains(" ")) {
            String[] photoDateTime = photoDate.split(" ");
            if (photoDateTime.length > 0) {
                photoDateSplit = photoDateTime[0];
            }
        }

        SimpleDateFormat simpleDateFormatName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMAN);
        String filename = "";
        try {
            if (photoModel.getExtra1() != null && photoModel.getExtra1().equals("1")) {
                filename = "PD_" + simpleDateFormatName.format(simpleDateFormat.parse(photoDate)) + "_drawing" + ".jpg";
            } else {
                filename = "PD_" + simpleDateFormatName.format(simpleDateFormat.parse(photoDate)) + ".jpg";
            }
        } catch (ParseException e) {
            e.printStackTrace();
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                    if (photoModel.getExtra1() != null && photoModel.getExtra1().equals("1")) {
                        filename = "PD_" + simpleDateFormatName.format(inputFormat.parse(photoDate)) + "_drawing" + ".jpg";
                    } else {
                        filename = "PD_" + simpleDateFormatName.format(inputFormat.parse(photoDate)) + ".jpg";
                    }
                }catch (Exception ed){
                    ed.printStackTrace();
                }


        }
//        String filename = "PD_" + photoDate + ".jpg";

        File imageFile = new File(photoModel.getPohotPath());

        int file_size = Integer.parseInt(String.valueOf(imageFile.length() / 1024));

        try {
            exifInterface = new ExifInterface(imageFile.getAbsolutePath());
       //     exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
//            orientation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
            if (photoModel.getExifOrientation() != null && !photoModel.getExifOrientation().equals(""))
                orientation = Integer.parseInt(photoModel.getExifOrientation());
            photoWidth = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
            photoHeight = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));


        } catch (IOException e) {
            e.printStackTrace();
        }
        float degrees = 0; //rotation degree
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degrees = 270;
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                degrees = 0;
        }

        Bitmap bInput = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        Bitmap bitmapObj = null;
        if (bInput != null)
            bitmapObj = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);

//        try {
//            exifInterface = new ExifInterface(imageFile.getAbsolutePath());
//            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,90+"");
//            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_WIDTH,photoWidth+"");
//            exifInterface.setAttribute(ExifInterface.TAG_IMAGE_LENGTH,photoHeight+"");
//            exifInterface.saveAttributes();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (imageFile.exists()) {
            //Convert bitmap to byte array
            if (ProjectNavigator.mobileNetworkIsConnected(getApplicationContext()) && sharedPrefsManager.getBooleanValue(AppConstantsManager.MINIMIZE_PHOTO_VIA_MOBILE_DATA, false)) {
                isMobileDataActive = true;

                ByteArrayOutputStream streamm = new ByteArrayOutputStream();
//                try {
//                    bitmapObj = getCompressedBitmap(bitmapObj);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                assert bitmapObj != null;
                Bitmap reducedBitmap = isResizingRequired(bitmapObj);
                reducedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, streamm);
                bitmapObj = reducedBitmap;
                byte[] imageInBytee = streamm.toByteArray();
                long lengthbmp = imageInBytee.length;

                //write the bytes in file
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(imageFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fos.write(imageInBytee);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                photoQuality = LocalPhotosRepository.REDUCED_PHOTO_QUALITY;
            } else {
                isMobileDataActive = false;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                int photoQualityy = 100;
                if (!sharedPrefsManager.getStringValue(AppConstantsManager.USER_SELECTED_CAMERA_RESOLUTION, "100").equals("")) {
                    photoQualityy = Integer.valueOf(sharedPrefsManager.getStringValue(AppConstantsManager.USER_SELECTED_CAMERA_RESOLUTION, "100"));
                }
                bitmapObj.compress(Bitmap.CompressFormat.JPEG, photoQualityy, stream);
                byte[] imageInByte = stream.toByteArray();
                long lengthbmpOrg = imageInByte.length;

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(imageFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fos.write(imageInByte);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                photoQuality = LocalPhotosRepository.ORIGNAL_PHOTO_QUALITY;
            }


            int file_sizee = Integer.parseInt(String.valueOf(imageFile.length() / 1024));
        }


        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), imageFile);

        MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, fileReqBody);

        Map<String, RequestBody> map = new HashMap<>();

        //Create request body with text description and text media type
//        RequestBody hash = RequestBody.create(MediaType.parse("text/plain"), ProjectDocuUtilities.givenFile_MD5_Hash(photoModel.getPohotPath()));
        RequestBody hash = RequestBody.create(MediaType.parse("text/plain"), photoModel.getHash());
        debugRequestbody("hash", photoModel.getHash());

        RequestBody photodate = RequestBody.create(MediaType.parse("text/plain"), photoDate);
        debugRequestbody("photodate", photoDate);

        RequestBody quality = RequestBody.create(MediaType.parse("text/plain"), photoQuality);
        debugRequestbody("quality", photoQuality);

        RequestBody filetype = RequestBody.create(MediaType.parse("text/plain"), "photo");
        debugRequestbody("filetype", "photo");

        RequestBody exifHeight, exifWidth, useorientation, exifhasgps, exifgpsx,
                exifgpsy, exifhasgpsdirection, exifgpsdirection, exifdump, exiforientation, gpsAccuracy;


        if (photoModel.getExifWidth() != null) {
            debugRequestbody("exifwidth", photoModel.getExifWidth());
            exifWidth = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifWidth());
        } else {
            debugRequestbody("exifwidth", "");
            exifWidth = RequestBody.create(MediaType.parse("text/plain"), "");
        }


        if (photoModel.getExifHeight() != null) {
            debugRequestbody("exifheight", photoModel.getExifHeight());
            exifHeight = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifHeight());
        } else {
            debugRequestbody("exifheight", "");
            exifHeight = RequestBody.create(MediaType.parse("text/plain"), "");
        }


        if (photoModel.getExifHasGps() != null) {
            debugRequestbody("exifhasgps", photoModel.getExifHasGps());
            exifhasgps = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifHasGps());
        } else {
            debugRequestbody("exifhasgps", "0");
            exifhasgps = RequestBody.create(MediaType.parse("text/plain"), "0");
        }

        if (photoModel.getExifGpsX() != null) {
            debugRequestbody("exifgpsx", photoModel.getExifGpsX());
            exifgpsx = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifGpsX());
        } else {
            debugRequestbody("exifgpsx", "");
            exifgpsx = RequestBody.create(MediaType.parse("text/plain"), "");
        }


        if (photoModel.getExifGpsY() != null) {
            debugRequestbody("exifgpsy", photoModel.getExifGpsY());
            exifgpsy = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifGpsY());
        } else {
            debugRequestbody("exifgpsy", "");
            exifgpsy = RequestBody.create(MediaType.parse("text/plain"), "");
        }


        if (photoModel.getExifGpsDirection() != null) {
            debugRequestbody("exifgpsdirection", photoModel.getExifGpsDirection());
            Log.d(TAG, "gps direction: " + photoModel.getExifGpsDirection());
            exifgpsdirection = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifGpsDirection());
        } else {
            debugRequestbody("exifgpsdirection", "");
            exifgpsdirection = RequestBody.create(MediaType.parse("text/plain"), "");
        }


        if (photoModel.getExifHasGpsDirection() != null) {
            debugRequestbody("exifhasgpsdirection", photoModel.getExifHasGpsDirection());
            exifhasgpsdirection = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifHasGpsDirection());
        } else {
            debugRequestbody("exifhasgpsdirection", "0");
            exifhasgpsdirection = RequestBody.create(MediaType.parse("text/plain"), "0");
        }


        if (photoModel.getExifOrientation() != null) {
            debugRequestbody("exiforientation", degrees + "");
            exiforientation = RequestBody.create(MediaType.parse("text/plain"), degrees + "");
        } else {
            debugRequestbody("exiforientation", "");
            exiforientation = RequestBody.create(MediaType.parse("text/plain"), "");
        }


        if (photoModel.getExifUseOrientation() != null) {
            debugRequestbody("useorientation", photoModel.getExifUseOrientation());
            useorientation = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifUseOrientation());
        } else {
            debugRequestbody("useorientation", "0");
            useorientation = RequestBody.create(MediaType.parse("text/plain"), "0");
        }

        if (photoModel.getExifDump() != null)
            exifdump = RequestBody.create(MediaType.parse("text/plain"), photoModel.getExifDump());
        else
            exifdump = RequestBody.create(MediaType.parse("text/plain"), "");
        if (photoModel.getGpsAccuracy() != null) {
            debugRequestbody("gpsaccuracy", photoModel.getGpsAccuracy());
            gpsAccuracy = RequestBody.create(MediaType.parse("text/plain"), photoModel.getGpsAccuracy());
        } else {
            debugRequestbody("gpsaccuracy", "");
            gpsAccuracy = RequestBody.create(MediaType.parse("text/plain"), "");
        }


        map.put("quality", quality);
        map.put("hash", hash);
        map.put("photodate", photodate);
        map.put("filetype", filetype);
        Log.d("date", photoDate);

        Map<String, RequestBody> paramsMap = new HashMap<>();

        paramsMap.put("quality", quality);
        paramsMap.put("filetype", filetype);
        paramsMap.put("hash", hash);
        paramsMap.put("photodate", photodate);

        paramsMap.put("exifwidth", exifWidth);
        paramsMap.put("exifheight", exifHeight);

        if (photoModel.getExifHasGps().equals("1")) {
            paramsMap.put("exifgpsx", exifgpsx);
            paramsMap.put("exifgpsy", exifgpsy);
            paramsMap.put("exifhasgps", exifhasgps);
            paramsMap.put("gpsaccuracy", gpsAccuracy);
            if (photoModel.getExifHasGpsDirection().equals("")) {
                paramsMap.put("exifhasgpsdirection", exifhasgpsdirection);
            } else {
                paramsMap.put("exifgpsdirection", exifgpsdirection);
                paramsMap.put("exifhasgpsdirection", exifhasgpsdirection);
            }
        } else {
            paramsMap.put("exifhasgps", exifhasgps);
        }

        paramsMap.put("exiforientation", exiforientation);
        paramsMap.put("useorientation", useorientation);


        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
//        Call<JsonObject> call = retroApiInterface.addPhotoAPIWithOptionalParamsUsingMap(authToken, Utils.DEVICE_ID,
//                sharedPrefsManager.getLastProjectId(getApplication()), part, paramsMap);

        Call<JsonObject> call = retroApiInterface.addPhotoAPIWithOptionalParamsUsingMap(authToken, Utils.DEVICE_ID,
                photoModel.getProjectId(), part, paramsMap);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("upload Image", "Success : " + response.body());
//                            Toast.makeText(getApplication(), "Image Uploaded", Toast.LENGTH_SHORT).show();
//                        String dateString = response.body().getAsJsonObject("data").get("photoid").toString();
                        String photoId = "";
                        JsonObject jsonObject = response.body();
                        try {
                            if (jsonObject.has("data")) {
                                JsonObject jsonObjectData = null;

                                jsonObjectData = jsonObject.getAsJsonObject("data");

                                if (jsonObjectData.has("photoid")) {
                                    photoId = jsonObject.getAsJsonObject("data").get("photoid").getAsString();
                                    Utils.showLogger("obtainNewImageID>>" + photoId);
                                }
                            }

                        } catch (JsonParseException e) {
                            unSyncedPhoto(photoModel);
                        } catch (ClassCastException e) {
                            unSyncedPhoto(photoModel);
                        }
                        if (!photoId.equalsIgnoreCase("")) {
                            photoModel.setPdphotoid(photoId);
                            Utils.showLogger2("uploadingQuality"+photoQuality);
                            photoModel.setQuality(photoQuality);
                            new UpdateAsyncTask().execute(photoModel);
                            Utils.showLogger("Call upload image api");
                            callUpdatePhotoAPI(photoModel, photoId);//call when upload image
                        } else {
                            unSyncedPhoto(photoModel);
                        }
                        setOriginalDimensionPhoto(bInput, imageFile);
                    } else {
                        unSyncedPhoto(photoModel);
                        Log.d("upload Image", "Empty response");
//                        Toast.makeText(getApplication(),  getApplication().getResources().getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }

//                    try {
//                        exifInterface = new ExifInterface(imageFile.getAbsolutePath());
//                        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation + "");
//                        exifInterface.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, photoWidth + "");
//                        exifInterface.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, photoHeight + "");
//                        exifInterface.saveAttributes();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                } else {
                    Log.d("upload Image", "Not Success : " + response.toString());
//                    Toast.makeText(getApplication(),  getApplication().getResources().getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("upload Image", "failed : " + t.getMessage());
                unSyncedPhoto(photoModel);
//                Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setOriginalDimensionPhoto(Bitmap bInput, File imageFile) {
        ByteArrayOutputStream streamm = new ByteArrayOutputStream();
        bInput.compress(Bitmap.CompressFormat.JPEG, 100, streamm);
        byte[] imageInBytee = streamm.toByteArray();
        long lengthbmp = imageInBytee.length;

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(imageInBytee);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region unsynced photo
    private void unSyncedPhoto(PhotoModel photoModel) {
        photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
        int failCount = photoModel.getFailedCount();
        failCount++;
        photoModel.setFailedCount(failCount);
        new UpdateAsyncTask().execute(photoModel);
        signalCountDown();//When one photo is unsynch
        Log.d("upload Image_succ", "id " + photoModel.getPdphotolocalId() + " time " + photoModel.getCreated());
        Intent intent = new Intent(updateProfile);
        sendBroadcast(intent);

        Bundle intent1 = new Bundle();
        intent1.putBoolean(IS_PHOTOS_AUTO_SYNC, isPhotosAutoSync);
        intent1.putString(PROJECT_ID, projectId);
        if (mResultReceiver != null)
            mResultReceiver.send(SHOW_RESULT, intent1);

        if (iPhotosSyncTaskComplete != null)
            iPhotosSyncTaskComplete.onReceiveResult(SHOW_RESULT, intent1);

    }
    //endregion

    //region synced photo
    private void syncedPhoto(PhotoModel photoModel) {
        boolean isReduceOn4G = sharedPrefsManager.getBooleanValue(AppConstantsManager.MINIMIZE_PHOTO_VIA_MOBILE_DATA, false);

        if (isMobileDataActive&&isReduceOn4G) {
            photoModel.setPhotoUploadStatus(LocalPhotosRepository.SHORTLY_SYNCED_PHOTO);
            photoModel.setPhotoSynced(false);
        } else {
            photoModel.setPhotoUploadStatus(LocalPhotosRepository.SYNCED_PHOTO);
            photoModel.setPhotoSynced(true);
        }
        new UpdateAsyncTask().execute(photoModel);
        signalCountDown();//When photo is sync

        Intent intent = new Intent("updateProfile");
        sendBroadcast(intent);
        Bundle intent1 = new Bundle();
        intent1.putBoolean(IS_PHOTOS_AUTO_SYNC, isPhotosAutoSync);
        intent1.putString(PROJECT_ID, projectId);

        if (mResultReceiver != null)
            mResultReceiver.send(SHOW_RESULT, intent1);

        if (iPhotosSyncTaskComplete != null) {
            iPhotosSyncTaskComplete.onReceiveResult(SHOW_RESULT, intent1);
        }

//        if (!ProjectNavigator.isPhotoActivityForground) {
//            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
//            notificationHelper.createNotification("Photo Syncing.", "Photo Syncing Complete successfully");
//        }
    }
    //endregion

    //region update photo api
    private void callUpdatePhotoAPI(PhotoModel photoModel, String globalImgID) {
        Utils.showLogger("callUpdatePhotoAPI");
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
            //            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getApplication());
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");
        syncingState(photoModel);
        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }
        SimpleDateFormat yyyMMddHHmmssFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
//        String photoDate = yyyMMddHHmmssFormat.format(new Date());
        JsonObject params = new JsonObject();

        if (photoModel.getDescription() != null)
            params.addProperty("description", photoModel.getDescription());
        else
            params.addProperty("description", "");
        if (photoModel.getCreated() != null)
            params.addProperty("photodate", photoModel.getPhotoDate());
        else
            params.addProperty("photodate", "");

        Log.i("getPhotoDate",photoModel.getPhotoDate());

        Utils.showLogger(photoModel.getPhotoDate());

        if (photoModel.getDailyDocu() != null)
            params.addProperty("dailydocu", photoModel.getDailyDocu());
        else
            params.addProperty("dailydocu", "");

        if (photoModel.getExifHasGps() != null)
            params.addProperty("exifhasgps", photoModel.getExifHasGps());
        else
            params.addProperty("exifhasgps", "");

        if (photoModel.getExifGpsY() != null)
            params.addProperty("exifgpsy", photoModel.getExifGpsY());
        else
            params.addProperty("exifgpsy", "");

        if (photoModel.getExifGpsX() != null)
            params.addProperty("exifgpsx", photoModel.getExifGpsX());
        else
            params.addProperty("exifgpsx", "");

        if (photoModel.getExifHasGpsDirection() != null)
            params.addProperty("exifhasgpsdirection", photoModel.getExifHasGpsDirection());
        else
            params.addProperty("exifhasgpsdirection", "");

        if (photoModel.getExifGpsDirection() != null)
            params.addProperty("exifgpsdirection", photoModel.getExifGpsDirection());
        else
            params.addProperty("exifgpsdirection", "");
        if (photoModel.getGpsAccuracy() != null)
            params.addProperty("gpsaccuracy", photoModel.getGpsAccuracy());
        else
            params.addProperty("gpsaccuracy", "");

        if (photoModel.getPdphotolocalId() != 0) {

            new GetKeywordsAsyncTask(photoModel.getProjectId(), String.valueOf(photoModel.getPdphotolocalId())).execute();
            try {
                latch = new CountDownLatch(1);
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if (wordsSimpleList != null && wordsSimpleList.size() > 0) {
            JsonArray jsonElements = new JsonArray();
            for (int i = 0; i < wordsSimpleList.size(); i++) {
                Utils.showLogger("newWordsList=>" + wordsSimpleList.get(i).getName());
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("projectparamid", wordsSimpleList.get(i).getProjectParamId());
                jsonObject.addProperty("projectid", wordsSimpleList.get(i).getProjectId());

                if (wordsSimpleList.get(i).getGroup() != null)
                    jsonObject.addProperty("group", wordsSimpleList.get(i).getGroup());
                else
                    jsonObject.addProperty("group", "");

                if (wordsSimpleList.get(i).getName() != null)
                    jsonObject.addProperty("name", wordsSimpleList.get(i).getName());
                else
                    jsonObject.addProperty("name", "");

                if (wordsSimpleList.get(i).getType() != null)
                    jsonObject.addProperty("type", wordsSimpleList.get(i).getType());
                else
                    jsonObject.addProperty("type", "");

                if (wordsSimpleList.get(i).getOrder() != null)
                    jsonObject.addProperty("order", wordsSimpleList.get(i).getOrder());
                else
                    jsonObject.addProperty("order", "");

                if (wordsSimpleList.get(i).getType() != null && wordsSimpleList.get(i).getType().equalsIgnoreCase("0"))
                    jsonObject.addProperty("value", "1");
                else {
                    WordModel wordModel = wordsSimpleList.get(i);
                    long photoId = photoModel.getPdphotolocalId();
                    if (wordModel != null && wordModel.getType() != null && wordModel.getType().equals("1")) {
                        try {
                            if (wordModel.getOpen_field_content() != null && wordModel.getOpen_field_content().contains(String.valueOf(photoId))) {
//                            wordModel.setFavorite(true);
                                wordModel.setPhotoIds("," + photoId + "");


                                String content = wordModel.getOpen_field_content();
                                WordContentModel list = null;
                                Gson gson = new Gson();
                                try {
                                    list = gson.fromJson(content, WordContentModel.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Utils.showLogger("gson conversion failed");
                                }
                                if (list != null) {
                                    ImageId_VS_Input result = list.findByImageId(photoId + "", wordModel.getName());
                                    if (result != null) {
                                        jsonObject.addProperty("value", result.getInputFields());

                                    } else
                                        continue;
                                } else
                                    continue;

                      /*      List<String> items = new LinkedList<String>(Arrays.asList(wordModel.getOpen_field_content().split("\\s*,\\s*")));
                            if (items != null && items.size() > 0) {
                                for (int ii = 0; ii < items.size(); ii++) {
                                    if (items.get(ii).contains(photoId + "")) {

                                        if (items.get(ii).split("##").length > 1) {
                                            String strCon = items.get(ii).split("##")[1];
//
                                            jsonObject.addProperty("value", strCon);
                                        }
                                    }
                                }
                            }
*/
                            } else
                                continue;
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.showLogger("sending error is");
                            //   continue;
                        }
                    }

                }
                if (wordsSimpleList.get(i).getIsUsed() != null)
                    jsonObject.addProperty("isUsed", wordsSimpleList.get(i).getIsUsed());
                else
                    jsonObject.addProperty("isUsed", "");
                if (wordsSimpleList.get(i).getParamType() != null)
                    jsonObject.addProperty("paramType", wordsSimpleList.get(i).getParamType());
                else
                    jsonObject.addProperty("paramType", "");

                jsonObject.addProperty("visible", wordsSimpleList.get(i).getVisible());
                jsonElements.add(jsonObject);
            }
            params.add("params", jsonElements);
            Utils.showLogger("uploadingNewObject" + jsonElements.toString());
        }


        Utils.showLogger("paramsAre>>" + params.toString());

        Call<JsonObject> call = retroApiInterface.getUpdatePhotoAPI(authToken, Utils.DEVICE_ID, photoModel.getPdphotoid(), params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {

//                        Toast.makeText(getApplication(), "GetFlawFlagUsingPhotoId", Toast.LENGTH_SHORT).show();
                        new GetFlawFlagUsingPhotoId(photoModel, globalImgID).execute();
                        Utils.showLogger("GetAudioFileAsyncTask");
                        new GetAudioFileAsyncTask().execute(photoModel);
//

                    } else {
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                        Utils.showLogger("API Error 1");
                        unSyncedPhoto(photoModel);

                    }
                } else {
                    Utils.showLogger("API Error 2");
                    unSyncedPhoto(photoModel);
//                    Log.d("Login", "Not Success : " + response.toString());
//                    Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                unSyncedPhoto(photoModel);
                Log.d("Login", "failed : " + t.getMessage());
                Utils.showLogger("Api Failed>>" + t.getMessage());
                t.printStackTrace();
//                Toast.makeText(getApplication(), "Failures Success", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //endregion

    //region update flaws api
    private void callUpdateFlawsAPI(PhotoModel photoModel) {
        Utils.showLogger("update Flaws Cords=>" + photoModel.getPdphotoid());
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
//            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
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

        if (defectModelOBJ.getFristDate() != null) {
            String fristDate = defectModelOBJ.getFristDate();
            if (!defectModelOBJ.getDefectDate().equals("")) {
                SimpleDateFormat englishDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    try {
                        Date date = simpleDateFormat.parse(defectModelOBJ.getFristDate());
                        fristDate = englishDateFormat.format(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            params.addProperty("fristdate", fristDate);
        } else {
            params.addProperty("fristdate", "");
        }


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
            params.addProperty("notifiedate", "0000-00-00 00:00:00");

        if (defectModelOBJ.getSecondFristDate() != null)
            params.addProperty("secondfristdate", defectModelOBJ.getSecondFristDate());
        else
            params.addProperty("secondfristdate", "0000-00-00 00:00:00");

        if (defectModelOBJ.getDoneDate() != null)
            params.addProperty("donedate", defectModelOBJ.getDoneDate());
        else
            params.addProperty("donedate", "0000-00-00 00:00:00");

        if (defectModelOBJ.getResponsibleUser() != null)
            params.addProperty("responsibleuser", defectModelOBJ.getResponsibleUser());
        else
            params.addProperty("responsibleuser", "");


        if (flawPhotosList != null && flawPhotosList.size() > 0) {
            JsonArray jsonElements = new JsonArray();
//            flawPhotosList.add(0, photoModel);
            for (int i = 0; i < flawPhotosList.size(); i++) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("pdphotoid", flawPhotosList.get(i).getPdphotoid());
                if (flawPhotosList.get(i).getDescription() != null)
                    jsonObject.addProperty("pdphototext", flawPhotosList.get(i).getDescription());
                else
                    jsonObject.addProperty("pdphototext", "");
                jsonElements.add(jsonObject);

            }
            params.add("flawitems", jsonElements);

            //Utils.showLogger("PDF_Coordinates>>"+jsonElements.getAsString());
        }

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
        } else {
            Utils.showLogger("No X Coordingates found");
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

        Call<JsonObject> call = retroApiInterface.getUpdateFlawsAPI(authToken, Utils.DEVICE_ID, params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {

//                      Toast.makeText(getApplication(), "Finally Success Photo Defect sync", Toast.LENGTH_SHORT).show();


                        String runid = response.body().getAsJsonObject("data").get("runid").toString().replace("\"", "");
                        String pdflawid = response.body().getAsJsonObject("data").get("pdflawid").toString().replace("\"", "");
                        JsonArray pdflawflagid = response.body().getAsJsonObject("data").getAsJsonArray("pdflawflags");
                        for (int i = 0; i < pdflawflagid.size(); i++) {
                            String pdflawflagid1 = pdflawflagid.get(i).getAsJsonObject().get("pdflawflagid").toString().replace("\"", "");
                            flawFlagObjWithoutPhoto.setPdFlawFlagServerId(pdflawflagid1);
                        }
                        // Toast.makeText(getApplication(), "Finally Success Only Mangel", Toast.LENGTH_SHORT).show();
                        defectModelOBJ.setDefectId(pdflawid);
                        defectModelOBJ.setDeleted("0");
                        if (runid != null && !runid.equals("")) {
                            defectModelOBJ.setRunidInt(Integer.valueOf(runid));
                            defectModelOBJ.setRunId(runid);
                        }

                        new UpdateDefectStatusAsyncTask().execute(defectModelOBJ);
                        new UpdateFlawFlagStatusAsyncTask().execute(flawFlagObjWithoutPhoto);
                        syncedPhoto(photoModel); //
                        stopSelf();

                    } else {
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    Log.d("Login", "Not Success : " + response.toString());
//                    Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    unSyncedPhoto(photoModel);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                unSyncedPhoto(photoModel);
                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(getApplication(), "Failures Success", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //endregion


    //region update plan flags api
    private void callUpdatePlanFlagsAPI(PhotoModel photoModel, Pdflawflag pdflawflag, String imgIDgLOBAL) {
        Utils.showLogger("callUpdatePlanFlagsAPI=>" + photoModel.getPdphotoid() + "::" + photoModel.getPdphotolocalId());
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
//            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
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


//        if (pdflawflag.getPdFlawFlagServerId() != null && !pdflawflag.getPdFlawFlagServerId().equals("") && !pdflawflag.getPdFlawFlagServerId().equals("0"))
//            params.addProperty("pdflagid", Integer.valueOf(pdflawflag.getPdFlawFlagServerId()));
//        else
//            params.addProperty("pdflagid", 0);

        if (pdflawflag.getPdFlagId() != null && !pdflawflag.getPdFlagId().equals("") && !pdflawflag.getPdFlagId().equals("0"))
            params.addProperty("pdflagid", Integer.valueOf(pdflawflag.getPdFlagId()));
        else
            params.addProperty("pdflagid", 0);


        params.addProperty("pdphotoid", Integer.valueOf(photoModel.getPdphotoid()));
        params.addProperty("pdplanid", Integer.valueOf(photoModel.getPlan_id()));

        if (pdflawflag.getXcoord() != null)
            params.addProperty("xcoord", pdflawflag.getXcoord());
        else
            params.addProperty("xcoord", "");

        if (pdflawflag.getYcoord() != null)
            params.addProperty("ycoord", pdflawflag.getYcoord());
        else
            params.addProperty("ycoord", "");

        if (pdflawflag.getViewx() != null)
            params.addProperty("viewx", pdflawflag.getViewx());
        else
            params.addProperty("viewx", "");

        if (pdflawflag.getViewy() != null)
            params.addProperty("viewy", pdflawflag.getViewy());
        else
            params.addProperty("viewy", "");


        Utils.showLogger("Loc Params>>>" + params.toString());

        Call<JsonObject> call = retroApiInterface.getUpdatePlansParamsAPI(authToken, Utils.DEVICE_ID, photoModel.getProjectId(), params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                String pdflagid = "";
                Utils.logResponse(call, response);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        JsonObject jsonObject = response.body();
                        try {
                            if (jsonObject.has("data")) {
                                JsonObject jsonObjectData = null;

                                jsonObjectData = jsonObject.getAsJsonObject("data");

                                if (jsonObjectData.has("pdflagid")) {
                                    pdflagid = jsonObject.getAsJsonObject("data").get("pdflagid").getAsString();
//                                    pdflawflag.setPdFlawFlagServerId(pdflagid);
                                    pdflawflag.setPdFlagId(pdflagid);
                                    new CreateOrUpdateLocalFlawFlag().execute(pdflawflag);

//                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(getApplicationContext(), "CreateOrUpdateLocalFlawFlag", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });

                                    try {
                                        latch2 = new CountDownLatch(1);
                                        Utils.showLogger("latch2WaitStarted");
                                        latch2.await();
                                        Utils.showLogger("latch2Wait");


                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        } catch (JsonParseException e) {
                            unSyncedPhoto(photoModel);
                        } catch (ClassCastException e) {
                            unSyncedPhoto(photoModel);
                        }
//                        if (pdflawflag != null && pdflawflag.getPdFlawFlagServerId() != null && !pdflawflag.getPdFlawFlagServerId().equals("0"))
                        new GetFlawsAsyncTask(photoModel).execute();
//                        Toast.makeText(getApplication(), "Finally Success", Toast.LENGTH_SHORT).show();
                    } else {
                        unSyncedPhoto(photoModel);
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopSelf();
                    unSyncedPhoto(photoModel);

//                    Log.d("Login", "Not Success : " + response.toString());
                    //  Toast.makeText(context, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                unSyncedPhoto(photoModel);
                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(getApplication(), "Failures Success", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //endregion

    private class CreateOrUpdateLocalFlawFlag extends AsyncTask<Pdflawflag, Void, Void> {
        private DefectsDao mAsyncTaskDao;
        Pdflawflag pdflawflag;

        String serverId = "";

        CreateOrUpdateLocalFlawFlag() {
        }

        @Override
        protected Void doInBackground(final Pdflawflag... params) {
            pdFlawFlagRepository = new PdFlawFlagRepository(context, projectId);
            serverId = params[0].getPdFlawFlagServerId();
            if (params[0] != null && params[0].getPdFlawFlagServerId() != null) {
                pdFlawFlagRepository.getmDefectsPhotoDao().update(params[0]);
            }
            latch2.countDown();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (pdflawflag != null && pdflawflag.getPdFlawFlagServerId() != null)
                        Toast.makeText(getApplicationContext(), "serverId " + serverId + "serverIdStored " + pdflawflag.getPdFlawFlagServerId(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class UpdateAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        private DefectsDao defectsDao;
        private ProjectDao projectDao;

        UpdateAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
            defectsDao = projectsDatabase.defectsDao();
            projectDao = projectsDatabase.projectDao();
        }

        @Override
        protected Void doInBackground(final PhotoModel... params) {

            mAsyncTaskDao.update(params[0]);
            if (params[0].getLocal_flaw_id() != null && !params[0].getLocal_flaw_id().equals("")) {
                if (mAsyncTaskDao.getDefectPhotosAndUnSyncLocalPhotosCount(projectId, params[0].getLocal_flaw_id()) == 0) {

                    DefectsModel defectsModel = defectsDao.getDefectsOBJ(projectId, params[0].getLocal_flaw_id());
                    if (defectsModel != null) {
                        defectsModel.setUploadStatus(DefectRepository.SYNCED_PHOTO);
                        defectsDao.update(defectsModel);
                    }
                    Intent intent = new Intent("updateDefectPhotos");
                    sendBroadcast(intent);

                    Intent intentt = new Intent(BR_ACTION_UPDATE_DEFECT_DATA);
                    intentt.putExtra("flawId", params[0].getLocal_flaw_id());
                    intentt.putExtra("uploadStatus", DefectRepository.SYNCED_PHOTO);
                    sendBroadcast(intentt);
                } else if (mAsyncTaskDao.getDefectPhotosAndUploadingLocalPhotosCount(projectId, params[0].getLocal_flaw_id()) == 0) {
                    DefectsModel defectsModel = defectsDao.getDefectsOBJ(projectId, params[0].getLocal_flaw_id());
                    if (defectsModel != null) {
                        defectsModel.setUploadStatus(DefectRepository.UN_SYNC_PHOTO);
                        defectsDao.update(defectsModel);
                    }
                    Intent intent = new Intent("updateDefectPhotos");
                    sendBroadcast(intent);

                    Intent intentt = new Intent(BR_ACTION_UPDATE_DEFECT_DATA);
                    intentt.putExtra("flawId", params[0].getLocal_flaw_id());
                    intentt.putExtra("uploadStatus", DefectRepository.UN_SYNC_PHOTO);
                    sendBroadcast(intentt);

                }
            }

            //  if(iPhotosSyncTaskComplete!=null){
            if (mAsyncTaskDao.getUnSyncedPhotoCount(params[0].getProjectId()) == 0) {
//                   ProjectModel projectModel= projectDao.getSpecificProject(params[0].getProjectId());
//                    if(projectModel!=null)
//                    projectModel.setIsPhotoSynced("Y");
//                    projectModel.setSyncStatus(LocalPhotosRepository.SYNCED_PHOTO);
//                    projectDao.update(projectModel);

                Intent intentt = new Intent(BR_ACTION_UPDATE_PROJECT_LIST);
                intentt.putExtra(PROJECT_ID, params[0].getProjectId());
                sendBroadcast(intentt);
                iPhotosSyncTaskComplete = null;
                //   }

            }


            return null;
        }
    }

    //In background Call API to update coordinates
    private class GetFlawFlagUsingPhotoId extends AsyncTask<String, Void, Pdflawflag> {
        private DefectsDao mAsyncTaskDao;
        PhotoModel photoModelObj;
        String imgID;

        GetFlawFlagUsingPhotoId(PhotoModel photoModel, String imgID) {
            photoModelObj = photoModel;
            this.imgID = imgID;
        }

        @Override
        protected Pdflawflag doInBackground(final String... params) {

            pdFlawFlagRepository = new PdFlawFlagRepository(context, projectId);


            flawFlagObj = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagObjUsingPhotoID(photoModelObj.getProjectId(), photoModelObj.getPlan_id(), photoModelObj.getPdphotolocalId() + "");

            if (flawFlagObj != null && flawFlagObj.getXcoord() != null && !flawFlagObj.getXcoord().equals("")) {

                callUpdatePlanFlagsAPI(photoModelObj, flawFlagObj, imgID);
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "callUpdatePlanFlagsAPI", Toast.LENGTH_SHORT).show();
//                    }
//                });

            } else {
                Utils.showLogger("Else plan update called");
                if (flawFlagObj == null)
                    Utils.showLogger("flaFlagObj is null>>>");
                new GetFlawsAsyncTask(photoModelObj).execute();
            }

            return flawFlagObj;
        }

        @Override
        protected void onPostExecute(Pdflawflag pdflawflag) {
            super.onPostExecute(pdflawflag);


        }
    }

    public class GetKeywordsAsyncTask extends AsyncTask<String, Void, List<WordModel>> {
        private long photoId;
        WordDao mAysncWordDao;
        String projectID, photoID;

        GetKeywordsAsyncTask(String projectId, String photoId) {

            mAysncWordDao = ProjectsDatabase.getDatabase(getApplication()).wordDao();
            projectID = projectId;
            photoID = photoId;
        }

        @Override
        protected List<WordModel> doInBackground(String... params) {
            wordsSimpleList = mAysncWordDao.getWordsListIncludesPhotoId("%" + photoID + "%", projectID);
            ArrayList<String> wordsST = new ArrayList<>();
            for (WordModel words : wordsSimpleList) {
                wordsST.add(words.getName());
            }
            Utils.showLogger("fetchedWordsAre=>" + wordsST.toString());
            // wordsSimpleList = mAysncWordDao.getRecentUsedWordsList()
            latch.countDown();
            return wordsSimpleList;
        }

        @Override
        protected void onPostExecute(List<WordModel> wordModels) {
            super.onPostExecute(wordModels);

        }
    }

    public class GetFlawsAsyncTask extends AsyncTask<Void, Void, DefectsModel> {
        private long photoId;
        DefectsDao defectsDao;
        PhotoDao photoDao;
        DefectsTradesDao defectTradeDao;
        PdFlawFLagListDao pdFlawFLagListDao;
        String projectID, local_flaw_ID;
        PhotoModel photoModelObj;

        GetFlawsAsyncTask(PhotoModel photoModel) {

            defectsDao = ProjectsDatabase.getDatabase(getApplication()).defectsDao();
            photoDao = ProjectsDatabase.getDatabase(getApplication()).photoDao();
            defectTradeDao = ProjectsDatabase.getDatabase(getApplication()).defectTradeDao();
            pdFlawFLagListDao = ProjectsDatabase.getDatabase(getApplication()).pdFlawFLagDao();

            projectID = photoModel.getProjectId();
            local_flaw_ID = photoModel.getLocal_flaw_id();
            photoModelObj = photoModel;
        }

        @Override
        protected DefectsModel doInBackground(Void... params) {
            defectModelOBJ = defectsDao.getDefectsOBJ(projectID, local_flaw_ID);
            flawPhotosList = photoDao.getPhotosForSyncing2ServerId(projectID, local_flaw_ID);
            defectTradeList = defectTradeDao.getAllDefectTradeWithStatusONModel(projectID, local_flaw_ID);
            flawFlagObjWithoutPhoto = pdFlawFLagListDao.getFlawFlagOBJExist(photoModelObj.getProjectId(), local_flaw_ID);

            return defectModelOBJ;
        }

        @Override
        protected void onPostExecute(DefectsModel defectsModel) {
            super.onPostExecute(defectsModel);
            if (defectModelOBJ != null) {
                if (defectModelOBJ.getDefectName() != null && !defectModelOBJ.getDefectName().equals("")) {
                    callUpdateFlawsAPI(photoModelObj);
                } else {
                    photoModelObj.setFailedCount(0);
                    isPhotosAutoSync = false;
                    unSyncedPhoto(photoModelObj);
                    stopSelf();

                }
            } else {
                Utils.showLogger("1721 failed else SyncLocalPhotos");
                syncedPhoto(photoModelObj); //

            }
        }
    }

    private void signalCountDown() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.d(TAG, "signalCountDown: signal.countDown start");
                signal.countDown();
                Log.d(TAG, "signalCountDown: signal.countDown end");

            }
        }.start();
    }

    private Bitmap isResizingRequired(Bitmap bitmap) {
        float oldImageWidth = 0.0f;
        float oldImageHeight = 0.0f;
        float maxImageSize = 1000;
        Bitmap scaledBitmap = null;
        if (bitmap != null) {
            oldImageHeight = bitmap.getHeight();//1920x1080
            oldImageWidth = bitmap.getWidth();

            if (oldImageHeight > maxImageSize || oldImageWidth > maxImageSize) {
                scaledBitmap = resizeImage(bitmap, 1000, false);
            }

            if (scaledBitmap == null)
                return bitmap;
        }
        return scaledBitmap;
    }

    public static Bitmap resizeImage(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap realBitmapCopy = realImage;
        Bitmap newBitmap = Bitmap.createScaledBitmap(realBitmapCopy, width,
                height, filter);

        return newBitmap;
    }

    private Bitmap getCompressedBitmap(Bitmap bitmap) throws IOException {
        float oldImageWidth = 0.0f;
        float oldImageHeight = 0.0f;

        float fixedImageWidth = 1000;
        float fixedImageHeight = 1000;

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


    private class GetUnSyncAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        GetUnSyncAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final PhotoModel... params) {

            List<PhotoModel> photoModelList = mAsyncTaskDao.markUnSyncUploadingPhotos(LocalPhotosRepository.TYPE_LOCAL_PHOTO);
            ;

            for (int i = 0; i < photoModelList.size(); i++) {
                PhotoModel photoModel = photoModelList.get(i);
                if (photoModel.getFailedCount() < 6)
                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                else
                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                photoModel.setUserSelectedStatus(false);
                mAsyncTaskDao.update(photoModel);
//                new UpdateAsyncTask().execute(photoModel);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intentt = new Intent(BR_ACTION_UPDATE_PROJECT_LIST);
            sendBroadcast(intentt);
        }
    }


    private class UpdateDefectStatusAsyncTask extends AsyncTask<DefectsModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        private DefectsDao defectsDao;
        private Pdflawflag pdflawflag;

        UpdateDefectStatusAsyncTask() {
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

            return null;
        }
    }

    private class UpdateFlawFlagStatusAsyncTask extends AsyncTask<Pdflawflag, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        private DefectsDao defectsDao;
        private PdFlawFLagListDao pdflawflagDao;

        UpdateFlawFlagStatusAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            pdflawflagDao = projectsDatabase.pdFlawFLagDao();
        }

        @Override
        protected Void doInBackground(final Pdflawflag... params) {
            if (params[0] != null) {
                pdflawflagDao.update(params[0]);
            }

/*
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
*/
            return null;
        }
    }

    private class GetAudioFileAsyncTask extends AsyncTask<PhotoModel, Void, List<RecordAudioModel>> {
        private RecordAudioDao mAsyncTaskDao;
        String serverPhotoId = "";

        GetAudioFileAsyncTask() {
            Utils.showLogger("uploading audio file start");
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.recordAudioDao();

        }

        @Override
        protected List<RecordAudioModel> doInBackground(final PhotoModel... params) {
            Utils.showLogger("uploading audio file background");

            serverPhotoId = params[0].getPdphotoid();
            List<RecordAudioModel> photoModelList = mAsyncTaskDao.getRecordingsAsync(params[0].getPdphotolocalId());

            Utils.showLogger("photoModelListCount>>>" + photoModelList.size());
            if (photoModelList != null) {
                for (int i = 0; i < photoModelList.size(); i++) {

                    if (!uploadingAudios.contains(photoModelList.get(i).getRecordId() + "")) {
                        if (uploadingAudios.add(photoModelList.get(i).getRecordId() + ""))
                            callUploadAudioFileAPI(photoModelList.get(i), params[0]);
                    }

                }
            }
            return photoModelList;
        }

        @Override
        protected void onPostExecute(List<RecordAudioModel> aVoid) {
            super.onPostExecute(aVoid);


        }
    }

    RecordAudioModel recordAudioModel = null;
    PhotoModel photoModelMemoOBJ = null;

    private void callUploadAudioFileAPI(RecordAudioModel recordAudioObj, PhotoModel photoModel) {
        recordAudioModel = recordAudioObj;
        RecordAudioModel localPhotoModel = recordAudioModel;
        photoModelMemoOBJ = photoModel;
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
//            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject params = new JsonObject();

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getApplication());
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");
        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
        String photoDate = simpleDateFormat.format(new Date());

        SimpleDateFormat simpleDateFormatName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMAN);
        String filename = "PD_" + simpleDateFormatName.format(new Date()) + ".mp3";

        File imageFile = new File(recordAudioObj.getPath());

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(recordAudioObj.getTimeStamp());

        String audioDate = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();


        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("audio/*"), imageFile);

        MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, fileReqBody);

        Map<String, RequestBody> map = new HashMap<>();

        //  String md5= ProjectDocuUtilities.getMD5(photoModel.getPath());

        //Create request body with text description and text media type
//        RequestBody hash = RequestBody.create(MediaType.parse("text/plain"), ProjectDocuUtilities.givenFile_MD5_Hash(photoModel.getPohotPath()));
        RequestBody hash = RequestBody.create(MediaType.parse("text/plain"), ProjectDocuUtilities.getMD5(photoModel.getPath()));
        RequestBody photodate = RequestBody.create(MediaType.parse("text/plain"), audioDate);
        RequestBody photoID = RequestBody.create(MediaType.parse("text/plain"), photoModel.getPdphotoid());
        String lastProjectID = sharedPrefsManager.getLastProjectId(getApplication());

        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        Call<JsonObject> call = retroApiInterface.addMemoAPI(authToken, Utils.DEVICE_ID,
                sharedPrefsManager.getLastProjectId(getApplication()),
                part, hash, photodate, photoID);


        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    Utils.logResponse(call, response);
                    if (response.body() != null) {
                        Log.d("upload Image", "Success : " + response.body());
//                            Toast.makeText(getApplication(), "Image Uploaded", Toast.LENGTH_SHORT).show();
//                        String dateString = response.body().getAsJsonObject("data").get("photoid").toString();
                        String pdmemoid = "";
                        JsonObject jsonObject = response.body();
                        response.body().toString();
                        try {
                            if (jsonObject.has("data")) {
                                JsonObject jsonObjectData = null;

                                jsonObjectData = jsonObject.getAsJsonObject("data");

                                if (jsonObjectData.has("pdmemoid")) {
                                    pdmemoid = jsonObject.getAsJsonObject("data").get("pdmemoid").getAsString();
                                    if (pdmemoid != null && !pdmemoid.equals("")) {
                                        localPhotoModel.setRecordServerId(pdmemoid);
                                        new UpdateAudioFileAsyncTask(photoModelMemoOBJ).execute(localPhotoModel);
                                    }

                                }
                            }

                        } catch (JsonParseException e) {
//                            unSyncedPhoto(photoModel);
                        } catch (ClassCastException e) {
//                            unSyncedPhoto(photoModel);
                        }


                    } else {

                        Log.d("upload Image", "Empty response");
//                        Toast.makeText(getApplication(),  getApplication().getResources().getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Log.d("upload Image", "Not Success : " + response.toString());
//                    Toast.makeText(getApplication(),  getApplication().getResources().getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                new UpdateAudioFileAsyncTask(photoModelMemoOBJ).execute(localPhotoModel);

                Log.d("upload Image", "failed : " + t.getMessage());

//                Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class UpdateAudioFileAsyncTask extends AsyncTask<RecordAudioModel, Void, Void> {
        private RecordAudioDao mAsyncTaskDao;
        String serverPhotoId = "";
        long unSyncAudioCount = 0;
        PhotoModel photoModelObj;

        UpdateAudioFileAsyncTask(PhotoModel photoModel) {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.recordAudioDao();
            photoModelObj = photoModel;

        }

        @Override
        protected Void doInBackground(final RecordAudioModel... params) {
            mAsyncTaskDao.update(params[0]);
            unSyncAudioCount = mAsyncTaskDao.getRecordingUNSyncCount(params[0].getPhotoId());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (unSyncAudioCount == 0) {
                syncedPhoto(photoModelObj);
            } else {
                unSyncedPhoto(photoModelObj);
            }
        }
    }

    private void debugRequestbody(String paramName, String paramValue) {
        Log.v(TAG, "createRequestbody: " + paramName + " : " + paramValue);
    }


}