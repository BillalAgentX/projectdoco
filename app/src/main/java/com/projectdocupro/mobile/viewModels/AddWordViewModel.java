package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.interfaces.FinishCallback;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.repos.WordsRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddWordViewModel extends AndroidViewModel {

    private MediatorLiveData<List<WordModel>> mSectionLive = new MediatorLiveData<>();
    private LiveData<List<WordModel>> listLiveData;
    private List<String> keys;
    private Map<String, List<WordModel>> map;
    private WordsRepository mRepository;

    public AddWordViewModel(@NonNull Application application) {
        super(application);
    }

    public void InitRepo(String projectId, long photoId) {
        mRepository = new WordsRepository(getApplication(), photoId, projectId);

        listLiveData = mRepository.getWordsList(projectId);
        map = new HashMap<>();
        keys = new ArrayList<>();

        mSectionLive.addSource(listLiveData, plansModels -> {
            if (plansModels == null || plansModels.isEmpty()) {
                // Fetch data from API
                Log.d("plans list", "null plans");
                callGetListAPI(getApplication(), projectId);
            } else {
                Log.d("plans list", plansModels.size() + " plans");
                mSectionLive.removeSource(listLiveData);
                mSectionLive.setValue(plansModels);
                if (listLiveData.getValue() == null || listLiveData.getValue().size() == 0) {
                    if (listLiveData.getValue() != null) {
                        Log.d("plans list", listLiveData.getValue().size() + " plans");
                        for (WordModel wordModel : listLiveData.getValue()) {
                            List<WordModel> list = map.get(wordModel.getGroup());
                            if (list == null) {
                                list = new ArrayList<>();
                            }
                            list.add(wordModel);
                            map.put(wordModel.getGroup(), list);
                            if (!keys.contains(wordModel.getGroup())) {
                                keys.add(wordModel.getGroup());
                            }
                        }
                    } else {
                        Log.d("plans list", "null plans");
                        callGetListAPI(getApplication(), projectId);
                    }
                }
            }
        });
    }

    public void callGetListAPI(Context context, String projectId) {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<JsonObject> call = retroApiInterface.getPlanDetailsAPI(authToken, Utils.DEVICE_ID, projectId);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        try {
                            listLiveData.getValue().addAll(new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonObject("params").getAsJsonArray("projectparam"), new TypeToken<List<WordModel>>() {
                            }.getType()));
                            mRepository.insertAll(listLiveData.getValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "No Words Found", Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }


    public LiveData<List<WordModel>> getWordsList(String projectId) {
        return mRepository.getWordsList(projectId);
    }

    public void saveWord(Context context, String group, String wordName, FinishCallback finishCallback) {
        mRepository.saveWord(context, group, wordName, finishCallback);
    }

    public Map<String, List<WordModel>> getWordsMap() {
        return map;
    }

    public List<String> getKeysList() {
        return keys;
    }
}
