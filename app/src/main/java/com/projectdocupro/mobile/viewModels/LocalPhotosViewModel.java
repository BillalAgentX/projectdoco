package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.PhotosGroupRecyclerAdapter;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.DefectsTradesDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.LocalPhotosListItemClickListener;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectTradeModel;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.utility.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class LocalPhotosViewModel extends AndroidViewModel implements LocalPhotosListItemClickListener {

    private static SimpleDateFormat simpleDateFormat;
    private LocalPhotosRepository localPhotosRepository;
    private List<WordModel> wordsSimpleList;
    private DefectsModel defectModelOBJ;
    private List<PhotoModel> flawPhotosList;
    private List<DefectTradeModel> defectTradeList;
    public MutableLiveData<Boolean> isStartUploadPhoto = new MutableLiveData<>();
    public boolean isAutoSyncPhoto = false;
    private Dialog customDialog;
    Date date = null;
    private static SharedPrefsManager sharedPrefsManager;

    public LocalPhotosViewModel(@NonNull Application application) {
        super(application);
    }

    public void refreshData(String projectID){
      localPhotosRepository.refreshData(projectID);

    }

    public void init(String projectId) {
        localPhotosRepository = new LocalPhotosRepository(getApplication(), projectId);
        sharedPrefsManager = new SharedPrefsManager(getApplication());

    }

    public LiveData<List<PhotoModel>> getAllPhotos() {
        return localPhotosRepository.getListLiveData();
    }

    public PhotosGroupRecyclerAdapter getAdapter() {
        return localPhotosRepository.getAdapter();
    }
    public void notifyDataSetChange(){
        localPhotosRepository.getAdapter().notifyDataSetChanged();
    }

    public void initAdapter(String projectId, List<PhotoModel> photoModels) {
        sharedPrefsManager = new SharedPrefsManager(getApplication());
        localPhotosRepository.initAdapter(projectId, photoModels, this);


    }

    @Override
    public void onListItemClick(PhotoModel photoModel) {
        Utils.showLogger("onListItemClick");
        isAutoSyncPhoto = false;//onListItemClick
//        if (ProjectDocuUtilities.isNetworkConnected(getApplication()) || ProjectNavigator.wlanIsConnected(getApplication())) {

            if (photoModel != null && photoModel.isPhotoSynced() && !photoModel.getPdphotoid().equalsIgnoreCase("")) {
            } else {
                if (!photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO)) {
                    updatePhotoStatus(photoModel, LocalPhotosRepository.UPLOADING_PHOTO);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isStartUploadPhoto.postValue(true);
                        }
                    }, 100);

                } else {
//                showCustomDialogUnSyncData(LocalPhotosFragment.context, "Project Docu", "Are you sure you want to unSync Photo.", 2, 0,photoModel);
                    ;
                }
                ;
//            callCheckPhotoQualityAPI(photoModel);

            }
//        } else {
//            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
//        }
    }

    public void deletePhotoFromList(PhotoModel photoModel){
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        date = new Date(photoModel.created_df);
        date = trim(date);
        if(localPhotosRepository.getAdapter()!=null) {
            List<PhotoModel> photoModelList = localPhotosRepository.getAdapter().getPhotosGroups().get(date);
            if (photoModelList != null) {
                for (int count = 0; count < photoModelList.size();count++){
                    if(photoModelList.get(count).getPdphotolocalId() == photoModel.getPdphotolocalId()){
                        photoModelList.remove(photoModelList.get(count));
                    }
                }
                if(photoModelList.size()==0){
//                    localPhotosRepository.getAdapter().getPhotosGroups().remove(date);
                   int pos= localPhotosRepository.getAdapter().groupNames.indexOf(date);
                    localPhotosRepository.getAdapter().groupNames.remove(pos);
                }else {
                    localPhotosRepository.getAdapter().getPhotosGroups().put(date, photoModelList);
                }

            }

        }
        localPhotosRepository.getAdapter().notifyDataSetChanged();
    }

    private void updatePhotoStatus(PhotoModel photoModel, String photoStatus) {
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        date = new Date(photoModel.created_df);
        date = trim(date);
        boolean isAddGallaryImage=true;
        if(localPhotosRepository.getAdapter()!=null) {
            List<PhotoModel> photoModelList = localPhotosRepository.getAdapter().getPhotosGroups().get(date);
            if (photoModelList !=null) {
                photoModel.setPhotoUploadStatus(photoStatus);
                if (photoModel != null && photoModel.isFromGallery()) {
                    for (int i = 0; i <photoModelList.size() ; i++) {
                        if(photoModelList.get(i).getPdphotolocalId()==photoModel.getPdphotolocalId()){
                            photoModelList.set(i, photoModel);
                            isAddGallaryImage=false;
                            break;
                        }else {
                            isAddGallaryImage=true;
                        }
                    }
                    if (isAddGallaryImage){
                        photoModelList.add(photoModel);
                    }

                } else {
                    photoModelList.set(photoModel.getClickedPosition(), photoModel);
                }
                localPhotosRepository.getAdapter().getPhotosGroups().put(date, photoModelList);
            }
            localPhotosRepository.getAdapter().notifyDataSetChanged();
        }
//        localPhotosRepository.getAdapter().notifyItemChanged(photoModel.getClickedPosition(), photoModel);
        photoModel.setPhotoUploadStatus(photoStatus);
        photoModel.setUserSelectedStatus(true);
        isAutoSyncPhoto = false;//Updatephoto status
        new UpdateAsyncTask().execute(photoModel);
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


    }

    public static Date trim(Date date) {
        Date dateWithoutTime = null;
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");
        }
        try {
            dateWithoutTime = simpleDateFormat.parse(simpleDateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateWithoutTime);


        return calendar.getTime();
    }

    public void showCustomDialogUnSyncData(final Context act, String title, String msgToShow, Integer noOfButtons, Integer flag, PhotoModel photoModel) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);
        if (flag == 2) {
            customDialog.setCancelable(false);
        }
        customDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.y = 10;
        // set the custom dialog components - text, image and button
        TextView titleTxt = (TextView) customDialog.findViewById(R.id.customDialog_titleText);
        if (!title.equals(""))
            titleTxt.setText(title);
        TextView text = (TextView) customDialog.findViewById(R.id.movie_name);
        text.setText(msgToShow);
        //		if(Navigator.showCustomDialogType ==3)
        //		{
        //			text.setTextSize(getDipValue(8));
        //		}
        Button bt = (Button) customDialog.findViewById(R.id.customDialog_okBtn);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePhotoStatus(photoModel, LocalPhotosRepository.UN_SYNC_PHOTO);
                customDialog.dismiss();
            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                }
            });
        }
        customDialog.show();
    }

    CountDownLatch latch = new CountDownLatch(1);

    String hash = "", quality = "";

/*
    private void callCheckPhotoQualityAPI(PhotoModel photoModel) {
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
                                        if (jsonObject1.has("quality")) {
                                            quality = jsonObject1.getString("quality");
                                            if (quality != null && !quality.equalsIgnoreCase("original")) {
                                                callUploadImageAPI(photoModel);
                                            } else {
                                                callUpdatePhotoAPI(photoModel);
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }


                    } else {
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    Log.d("Login", "Not Success : " + response.toString());
//                    Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

//                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
*/

/*
    private void syncingState(PhotoModel photoModel) {
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        date = new Date(photoModel.created_df);
        date = trim(date);
        photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
        List<PhotoModel> photoModelList = localPhotosRepository.getAdapter().getPhotosGroups().get(date);
        photoModelList.set(photoModel.getClickedPosition(), photoModel);
        localPhotosRepository.getAdapter().getPhotosGroups().put(date, photoModelList);
        localPhotosRepository.getAdapter().notifyDataSetChanged();
    }
*/

/*
    private void callUploadImageAPI(PhotoModel photoModel) {
        if (!ProjectDocuUtilities.isNetworkConnected(getApplication())) {
            Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            return;
        }
        syncingState(photoModel);
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getApplication());
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");
        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
        String photoDate = simpleDateFormat.format(new Date());

        File imageFile = new File(photoModel.getPohotPath());

        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), imageFile);

        MultipartBody.Part part = MultipartBody.Part.createFormData("file", imageFile.getName(), fileReqBody);

        Map<String, RequestBody> map = new HashMap<>();

        //Create request body with text description and text media type
//        RequestBody hash = RequestBody.create(MediaType.parse("text/plain"), ProjectDocuUtilities.givenFile_MD5_Hash(photoModel.getPohotPath()));
        RequestBody hash = RequestBody.create(MediaType.parse("text/plain"), photoModel.getHash());
        RequestBody photodate = RequestBody.create(MediaType.parse("text/plain"), photoDate);
        RequestBody quality = RequestBody.create(MediaType.parse("text/plain"), LocalPhotosRepository.ORIGNAL_PHOTO_QUALITY);
        RequestBody filetype = RequestBody.create(MediaType.parse("text/plain"), "photo");

        map.put("quality", quality);
        map.put("hash", hash);
        map.put("photodate", photodate);
        map.put("filetype", filetype);
        Log.d("date", photoDate);

        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        Call<JsonObject> call = retroApiInterface.addPhotoAPI(authToken, Utils.DEVICE_ID, sharedPrefsManager.getLastProjectId(getApplication()), part, quality, filetype, hash, photodate);
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
                        if (jsonObject.has("data")) {
                            JsonObject jsonObjectData = null;

                            jsonObjectData = jsonObject.getAsJsonObject("data");

                            if (jsonObjectData.has("photoid")) {
                                photoId = jsonObject.getAsJsonObject("data").get("photoid").toString();
                            }
                        }
                        if (!photoId.equalsIgnoreCase("")) {
                            photoModel.setPdphotoid(photoId);

                            callUpdatePhotoAPI(photoModel);

                        } else {
                            unSyncedPhoto(photoModel);
                        }

                    } else {
                        unSyncedPhoto(photoModel);
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
                Log.d("upload Image", "failed : " + t.getMessage());
                unSyncedPhoto(photoModel);
//                Toast.makeText(getApplication(), getApplication().getResources().getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
*/

    private void unSyncedPhoto(PhotoModel photoModel) {
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        date = new Date(photoModel.created_df);
        date = trim(date);

        photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
        List<PhotoModel> photoModelList = localPhotosRepository.getAdapter().getPhotosGroups().get(date);
        photoModelList.set(photoModel.getClickedPosition(), photoModel);
        localPhotosRepository.getAdapter().getPhotosGroups().put(date, photoModelList);
        localPhotosRepository.getAdapter().notifyDataSetChanged();
    }

    private void syncedPhoto(PhotoModel photoModel) {
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }

        date = new Date(photoModel.created_df);
        date = trim(date);
        photoModel.setPhotoUploadStatus(LocalPhotosRepository.SYNCED_PHOTO);
        photoModel.setPhotoSynced(true);
        List<PhotoModel> photoModelList = localPhotosRepository.getAdapter().getPhotosGroups().get(date);
        photoModelList.set(photoModel.getClickedPosition(), photoModel);
        localPhotosRepository.getAdapter().getPhotosGroups().put(date, photoModelList);
        localPhotosRepository.getAdapter().notifyItemChanged(photoModel.getClickedPosition(), photoModel);
        new UpdateAsyncTask().execute(photoModel);
    }

/*
    private void callUpdatePhotoAPI(PhotoModel photoModel) {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getApplication());
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");
        syncingState(photoModel);
        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }
        SimpleDateFormat yyyMMddHHmmssFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
        String photoDate = yyyMMddHHmmssFormat.format(new Date());
        JsonObject params = new JsonObject();

        if (photoModel.getDescription() != null)
            params.addProperty("description", photoModel.getDescription());
        else
            params.addProperty("description", "");
        if (photoModel.getCreated() != null)
            params.addProperty("photodate", photoModel.getCreated());
        else
            params.addProperty("photodate", "");

        if (photoModel.getDailyDocu() != null)
            params.addProperty("dailydocu", photoModel.getDailyDocu());
        else
            params.addProperty("dailydocu", "");

        if (photoModel.getExifHasGps() != null)
            params.addProperty("exifhasgps", photoModel.getExifHasGps());
        else
            params.addProperty("exifhasgps", "");

        if (photoModel.getExifGpsX() != null)
            params.addProperty("exifgpsx", photoModel.getExifGpsX());
        else
            params.addProperty("exifgpsx", "");

        if (photoModel.getExifGpsY() != null)
            params.addProperty("exifgpsy", photoModel.getExifGpsY());
        else
            params.addProperty("exifgpsy", "");

        if (photoModel.getExifGpsDirection() != null)
            params.addProperty("exifhasgpsdirection", photoModel.getExifGpsDirection());
        else
            params.addProperty("exifhasgpsdirection", "");

        if (photoModel.getExifHasGpsDirection() != null)
            params.addProperty("exifgpsdirection", photoModel.getExifHasGpsDirection());
        else
            params.addProperty("exifgpsdirection", "");
        if (photoModel.getGpsAccuracy() != null)
            params.addProperty("gpsaccuracy", photoModel.getGpsAccuracy());
        else
            params.addProperty("gpsaccuracy", "");

        if (photoModel.getPdphotolocalId() != 0) {

            new GetKeywordsAsyncTask(photoModel.getProjectId(), String.valueOf(photoModel.getPdphotolocalId())).execute();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (wordsSimpleList != null && wordsSimpleList.size() > 0) {
            JsonArray jsonElements = new JsonArray();
            for (int i = 0; i < wordsSimpleList.size(); i++) {
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
                    jsonObject.addProperty("value", "0");
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
        }

        Call<JsonObject> call = retroApiInterface.getUpdatePhotoAPI(authToken, Utils.DEVICE_ID, photoModel.getPdphotoid(), params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        new GetFlawsAsyncTask(photoModel).execute();

                    } else {
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                        unSyncedPhoto(photoModel);
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
                Log.d("Login", "failed : " + t.getMessage());
//                Toast.makeText(getApplication(), "Failures Success", Toast.LENGTH_SHORT).show();
            }
        });
    }
*/

    private void callUpdateFlawsAPI(PhotoModel photoModel) {

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

        if (defectModelOBJ.getFristDate() != null)
            params.addProperty("fristdate", defectModelOBJ.getFristDate());
        else
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

        if (defectModelOBJ.getDoneDate() != null)
            params.addProperty("donedate", defectModelOBJ.getDoneDate());
        else
            params.addProperty("donedate", "");

        if (defectModelOBJ.getResponsibleUser() != null)
            params.addProperty("responsibleuser", defectModelOBJ.getResponsibleUser());
        else
            params.addProperty("responsibleuser", "");


        if (flawPhotosList != null && flawPhotosList.size() > 0) {
            JsonArray jsonElements = new JsonArray();
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

        Call<JsonObject> call = retroApiInterface.getUpdatePhotoAPI(authToken, Utils.DEVICE_ID, photoModel.getPdphotoid(), params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        syncedPhoto(photoModel);
                        Toast.makeText(getApplication(), "Finally Success", Toast.LENGTH_SHORT).show();

                    } else {
//                        Log.d("Login", "Empty response");
//                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    Log.d("Login", "Not Success : " + response.toString());
//                    Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
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

    private class UpdateAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        UpdateAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final PhotoModel... params) {

            mAsyncTaskDao.update(params[0]);
            latch.countDown();
            return null;
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
            wordsSimpleList = mAysncWordDao.getWordsListIncludesPhotoId("%," + photoID + "%", projectID);
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
        String projectID, local_flaw_ID;
        PhotoModel photoModelObj;

        GetFlawsAsyncTask(PhotoModel photoModel) {

            defectsDao = ProjectsDatabase.getDatabase(getApplication()).defectsDao();
            photoDao = ProjectsDatabase.getDatabase(getApplication()).photoDao();
            defectTradeDao = ProjectsDatabase.getDatabase(getApplication()).defectTradeDao();

            projectID = photoModel.getProjectId();
            local_flaw_ID = photoModel.getLocal_flaw_id();
            photoModelObj = photoModel;
        }

        @Override
        protected DefectsModel doInBackground(Void... params) {
            defectModelOBJ = defectsDao.getDefectsOBJ(projectID, local_flaw_ID);
            flawPhotosList = photoDao.getPhotosForSyncing(projectID, local_flaw_ID);
            defectTradeList = defectTradeDao.getAllDefectTradeWithStatusONModel(projectID, local_flaw_ID);

            return defectModelOBJ;
        }

        @Override
        protected void onPostExecute(DefectsModel defectsModel) {
            super.onPostExecute(defectsModel);
            if (defectModelOBJ != null) {
                callUpdateFlawsAPI(photoModelObj);
            } else {
                syncedPhoto(photoModelObj);
            }

        }
    }

    private class ReterivePhotoObjAsyncTask extends AsyncTask<String, Void, PhotoModel> {
        private PhotoDao mAsyncTaskDao;

        ReterivePhotoObjAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected PhotoModel doInBackground(final String... params) {

            PhotoModel photoModel = mAsyncTaskDao.getPhotoModel(Long.valueOf(params[0]));

            return photoModel;
        }
    }
}
