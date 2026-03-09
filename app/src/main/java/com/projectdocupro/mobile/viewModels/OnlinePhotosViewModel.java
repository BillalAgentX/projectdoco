package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.adapters.OnlinePhotosRecyclerAdapter;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.OnlinePhotoModel;
import com.projectdocupro.mobile.models.mangel_filters.GroupheadingModel;
import com.projectdocupro.mobile.repos.ONlinePhotoRepository;
import com.projectdocupro.mobile.utility.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnlinePhotosViewModel extends AndroidViewModel {

    public MutableLiveData<List<OnlinePhotoModel>> photoPaths;
    public boolean isCached;
    public OnlinePhotosRecyclerAdapter adapter;
    public final int pageSize = 50;
    public int pageNo = 0;
    public int currentPage = 0;
    public int totalCount = 0;
    public boolean isLoading;
    public boolean isLastPage;
    public int TOTAL_PAGES;
    public String projectId;
    private Context mContext;
    public boolean isFilterApplied;
    private ONlinePhotoRepository oNlinePhotoRepository;
    public String totalCountString;
    private int apiStatusCode;
    private String response;
    public MutableLiveData<Map<String, String>> minMaxDateMap = new MutableLiveData<>();


    public MutableLiveData<Boolean> getIsLoadPhotos() {
        return isLoadPhotos;
    }

    public void setIsLoadPhotos(MutableLiveData<Boolean> isLoadPhotos) {
        this.isLoadPhotos = isLoadPhotos;
    }

    MutableLiveData<Boolean> isLoadPhotos = new MutableLiveData<>();

    public MutableLiveData<List<OnlinePhotoModel>> getPhotoPaths() {
        return photoPaths;
    }

    public void setPhotoPaths(MutableLiveData<List<OnlinePhotoModel>> photoPaths) {
        this.photoPaths = photoPaths;
    }

    public OnlinePhotosViewModel(@NonNull Application application) {
        super(application);

    }

    public void init(Context context, String projectID) {
        projectId = projectID;
        mContext = context;
        photoPaths = new MutableLiveData<>();

        oNlinePhotoRepository = new ONlinePhotoRepository(mContext, this);
//        oNlinePhotoRepository.deleteAllROws();

    }

    List<OnlinePhotoModel> getAllPhotos() {
        return photoPaths.getValue();
    }

    public OnlinePhotosRecyclerAdapter getAdapter() {
        return adapter;
    }

    public MutableLiveData<Map<String, String>> getMinMaxDateMap() {
        return minMaxDateMap;
    }

    public void setMinMaxDateMap(MutableLiveData<Map<String, String>> minMaxDateMap) {
        this.minMaxDateMap = minMaxDateMap;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
        } catch (IOException e) {
        }
        return sb.toString();
    }

    public void callGetPhotosAPI(Context context, String projectId, JSONObject filter, JSONArray orderList, List<GroupheadingModel> groupheadingModelList) {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        JSONObject params = new JSONObject();
        try {
            params.put("page", String.valueOf(currentPage));

            params.put("limit", String.valueOf(pageSize));
            if (filter != null) {
                params.put("filter", filter);
                isFilterApplied = true;
            }
            if (orderList != null) {
                params.put("order", orderList);
                isFilterApplied = true;

            } else
                isFilterApplied = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        try {
//            callService(context,projectId,filter,orderList);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        RequestBody body =
//                RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), params.toString());

        JsonParser jsonParser = new JsonParser();
        JsonObject gsonObject = (JsonObject) jsonParser.parse(params.toString());

        Call<JsonObject> call = retroApiInterface.getPhotosAPI(authToken, Utils.DEVICE_ID, projectId, gsonObject);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
//                        JsonObject convertedObject = new Gson().fromJson(response.toString(), JsonObject.class);

                        List<OnlinePhotoModel> onlinePhotoModelList = new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonArray("photos"), new TypeToken<List<OnlinePhotoModel>>() {
                        }.getType());

                        Utils.showLogger("newdatafirst"+onlinePhotoModelList.get(0).getPdPhotoName());


                        isLoading = false;
                        String strtotalCount = response.body().getAsJsonObject("data").get("total").toString();
                        totalCountString = strtotalCount.replace("\"", "");
                        if (totalCountString != null && !totalCountString.equals(""))
                            totalCount = Integer.valueOf(totalCountString);

                        Utils.showLogger("totalCountFromServer"+totalCount);

                        int tempQuotient = totalCount / pageSize;
                        int tempRemainder = totalCount % pageSize;
                        if (tempRemainder != 0) {
                            TOTAL_PAGES = tempQuotient + 1;
                        } else {
                            TOTAL_PAGES = tempQuotient;
                        }
                        if (totalCount == photoPaths.getValue().size()) {
                            isLastPage = true;
                        }
                        sharedPrefsManager.setStringValue(AppConstantsManager.ONLINE_PHOTO_COUNT, totalCountString);
                        isLoadPhotos.setValue(true);


                        oNlinePhotoRepository.insertALL(onlinePhotoModelList, groupheadingModelList);


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
                    isLoadPhotos.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
                isLoadPhotos.setValue(false);
            }
        });
    }

    private void callService(Context context, String projectId, JSONObject filter, JSONArray orderList) throws IOException, JSONException {
        if (ProjectDocuUtilities.isNetworkConnected(getApplication())) {

            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
            RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
            String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

            JSONObject params = new JSONObject();
            try {
                params.put("page", String.valueOf(currentPage));

                params.put("limit", String.valueOf(pageSize));
                if (filter != null) {
                    params.put("filter", filter);
                    isFilterApplied = true;
                }
                if (orderList != null) {
                    params.put("order", orderList);
                    isFilterApplied = true;

                } else
                    isFilterApplied = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }


            int timeoutConnection = 60000;
            int timeoutSockets = 60000;
            String url = "https://devapi.projectdocu.net/project/getphotogallerie?=" + projectId;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSockets);
            HttpClient client = new DefaultHttpClient(httpParameters);
            HttpPost post = new HttpPost(url);
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("apitoken", authToken));
            pairs.add(new BasicNameValuePair("deviceid", ""));

            post.setEntity(new UrlEncodedFormEntity(pairs));

            post.setEntity(new StringEntity(params.toString()));

            HttpResponse httpresponse = client.execute(post);
            HttpEntity entity = httpresponse.getEntity();
            apiStatusCode = httpresponse.getStatusLine().getStatusCode();
            if (entity != null) {
                InputStream instream = entity.getContent();

                response = convertStreamToString(instream);

                Gson gson = new Gson();
                JSONObject jsonObject = null;

//                jsonObject = new JSONObject(response);
//                if (jsonObject.has("apiRespCode")) {
//                    apiResponseCode = "00";
//                    apiResponseCode = jsonObject.getString("apiRespCode");
//                }
//
//                String code = "";
//                if (jsonObject.has("code")) {
//                    code = jsonObject.getString("code");
//                }
//                if (code.equalsIgnoreCase("06")) {
//                    Navigator.requestResponse.responseResultCode = Integer.parseInt(code);
//                    Navigator.clearAllData();
//                    Navigator.startSplash(Navigator.myContext);
//                    return;
//
//                }
//
//                if (jsonObject.has("code")) {
//
//                    codeGlobal = "";
//
//                    codeGlobal = jsonObject.getString("code");
//                    if (codeGlobal.equalsIgnoreCase("00")) {
//                        if (jsonObject.has("msg")) {
//                            codeMsg = jsonObject.getString("msg");
//                        }
//                    }
//                }
//                if (!codeGlobal.equalsIgnoreCase("00")) {
//                    if (jsonObject.has("seatsFrom")) {
//                        Navigator.seatsFrom = jsonObject.getString("seatsFrom");
//
//
//                    }
//
//                    if (jsonObject.has("isFoodAndBeveragesAllowed")) {
//                        data.setIsFoodAndBeveragesAllowed(jsonObject.getString("isFoodAndBeveragesAllowed"));
//
//
//                    }
//
//                    if (Navigator.seatsFrom.equals("superSoft")) {
//                        runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                //							wheelChairLayout.setVisibility(View.GONE);
//                                //							houseSeatLayout.setVisibility(View.GONE);
//                                cabinLyout.setVisibility(View.VISIBLE);
//                                seatDisableLayout.setVisibility(View.VISIBLE);
//                            }
//                        });
//                        dataCineGoldSeating = gson.fromJson(response, CineGoldSeating.class);
//                        Navigator.CineGoldShowId = dataCineGoldSeating.getCineGoldShowId();
//                    } else {
//                        data = parseData(response);
//                        //	data=gson.fromJson(response, SeatingData.class);
//                        if (data.getData() == null) {
//                            data = gson.fromJson(responseSta, SeatingData.class);
//                        } else {
//                            responseSta = response;
//                        }
//                    }
//
//                }
//            }
//        } else {
//            context.runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    //			Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
//                    context.finish();
//                }
//            });
//
//        }
            }
        }
    }

    public void callGetPhotosAPIForMaxMinDate(Context context, String projectId) {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }


        JsonObject gsonObject = new JsonObject();
        gsonObject.addProperty("page", "0");
        gsonObject.addProperty("limit", "1");
        gsonObject.addProperty("limit", "1");

        Call<JsonObject> call = retroApiInterface.getPhotosAPI(authToken, Utils.DEVICE_ID, projectId, gsonObject);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String minDate = "";
                        String maxDate = "";
                        try {
                            JSONObject mainJsonObject = new JSONObject(response.body().toString());
                            if (mainJsonObject.has("data")) {
                                if (mainJsonObject.get("data") instanceof JSONObject) {
                                    JSONObject jsonObject = mainJsonObject.getJSONObject("data");
                                    if (jsonObject.has("meta")) {
                                        if (jsonObject.get("meta") instanceof JSONObject) {
                                            JSONObject metaJsonObject = jsonObject.getJSONObject("meta");
                                            if (metaJsonObject.has("mindate")) {
                                                if (metaJsonObject.get("mindate") instanceof String) {
                                                    minDate = metaJsonObject.getString("mindate");
                                                }
                                            }

                                            if (metaJsonObject.has("maxdate")) {
                                                if (metaJsonObject.get("maxdate") instanceof String) {
                                                    maxDate = metaJsonObject.getString("maxdate");
                                                }
                                            }
                                        }
                                        if (!minDate.equals("") && !maxDate.equals("")) {
                                            Map<String, String> minMaxMap = new HashMap<>();
                                            if (minDate.contains(" ")) {
                                                if (minDate.split(" ").length > 0) {
                                                    minDate = minDate.split(" ")[0];
                                                }
                                            }

                                            if (maxDate.contains(" ")) {
                                                if (maxDate.split(" ").length > 0) {
                                                    maxDate = maxDate.split(" ")[0];
                                                }
                                            }
                                            minMaxMap.put("min_date", minDate);
                                            minMaxMap.put("max_date", maxDate);
                                            minMaxDateMap.setValue(minMaxMap);

//                                            JSONArray jsonElements = new JSONArray();
//                                            jsonElements.put("photodate desc");
//                                            callGetPhotosAPI(context, projectId, createJsonObjectWithoutFilters(maxDate, minDate), jsonElements);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
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
                    isLoadPhotos.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
                isLoadPhotos.setValue(false);
            }
        });
    }

}
