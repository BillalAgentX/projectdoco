package com.projectdocupro.mobile.repos;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.ProjectUsersDao;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectDetailRepository {


    private MediatorLiveData<List<WordModel>> mSectionLiveWords = new MediatorLiveData<>();
    private LiveData<List<WordModel>> listLiveDataWords;
    private List<String> keys;
    private Map<String, List<WordModel>> map;
    private WordsRepository mRepository;

    public WordsRepository getmRepository() {
        return mRepository;
    }

    public void setmRepository(WordsRepository mRepository) {
        this.mRepository = mRepository;
    }

    private ProjectUsersDao wordDao;

    public ProjectUsersDao getWordDao() {
        return wordDao;
    }

    public void setWordDao(ProjectUsersDao wordDao) {
        this.wordDao = wordDao;
    }

    public LiveData<List<ProjectUserModel>> listLiveData;
    public MediatorLiveData<Boolean> isProjectDetailSuccess = new MediatorLiveData<>();
    public MediatorLiveData<Boolean> isProjectWordSuccess = new MediatorLiveData<>();

    private MediatorLiveData<List<ProjectUserModel>> mSectionLive = new MediatorLiveData<>();
    private ProjectsDatabase db;
    String ProjectID;

    public LiveData<List<ProjectUserModel>> getListLiveData() {
        return listLiveData;
    }


    public void setListLiveData(LiveData<List<ProjectUserModel>> listLiveData) {
        this.listLiveData = listLiveData;
    }

    public ProjectDetailRepository(Application application) {
        db = ProjectsDatabase.getDatabase(application);
        wordDao = db.projectUsersDao();
    }

    public ProjectDetailRepository(Application application, String projectId) {
        db = ProjectsDatabase.getDatabase(application);
        wordDao = db.projectUsersDao();
        ProjectID = projectId;
        mRepository = new WordsRepository(application, projectId);
        listLiveDataWords = mRepository.getWordsList(projectId);
        listLiveData = wordDao.getUserProjectLDataList(projectId);
        map = new HashMap<>();
        keys = new ArrayList<>();
        mSectionLive.addSource(listLiveData, plansModels -> {
            if (plansModels == null || plansModels.isEmpty()) {
                // Fetch data from API
                Log.d("plans list", "null plans");
                callGetListAPI(application, projectId);
            } else {
                Log.d("plans list", plansModels.size() + " plans");
                mSectionLive.removeSource(listLiveData);
                mSectionLive.setValue(plansModels);
                if (listLiveData.getValue() == null || listLiveData.getValue().size() == 0) {
                    if (listLiveData.getValue() != null) {
                        Log.d("plans list", listLiveData.getValue().size() + " plans");

                    } else
                        Log.d("plans list", "null plans");
                    callGetListAPI(application, projectId);
                }
            }
        });

        mSectionLiveWords.addSource(listLiveDataWords, wordModels -> {
            if (wordModels == null || wordModels.isEmpty()) {
                // Fetch data from API
                Log.d("words list", "null plans");
                callGetListAPIWord(application, projectId);
            } else {
                Log.d("words list db", wordModels.size() + " plans");
                mSectionLiveWords.removeSource(listLiveDataWords);
                mSectionLiveWords.setValue(wordModels);
                if (listLiveDataWords.getValue() == null || listLiveDataWords.getValue().size() == 0) {
                    if (listLiveDataWords.getValue() != null) {
                        Log.d("plans list", listLiveDataWords.getValue().size() + " plans");
                        for (WordModel wordModel : listLiveDataWords.getValue()) {
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
                        callGetListAPIWord(application, projectId);
                    }
                }
            }
        });

    }


    public void insert(ProjectUserModel wordModel) {
        if (listLiveData.getValue() == null || listLiveData.getValue().size() == 0) {
            new insertAsyncTask(wordDao).execute(wordModel);
        }
    }


    private static class insertAsyncTask extends AsyncTask<ProjectUserModel, Void, Void> {
        private ProjectUsersDao wordDao;

        insertAsyncTask(ProjectUsersDao wordDao) {
            this.wordDao = wordDao;
        }

        @Override
        protected Void doInBackground(ProjectUserModel... params) {
            wordDao.insert(params[0]);
            return null;
        }
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
                            if (listLiveData.getValue() != null) {
                                deleteAllProjectData();

                                listLiveData.getValue().addAll(new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonObject("params").getAsJsonArray("userlist"), new TypeToken<List<ProjectUserModel>>() {
                                }.getType()));
                                insertAll(listLiveData.getValue());

                            }
//                            if (listLiveDataWords.getValue() != null) {
//                                getmRepository().deleteAllWords();
//                                listLiveDataWords.getValue().addAll(new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonObject("params").getAsJsonArray("projectparam"), new TypeToken<List<WordModel>>() {
//                                }.getType()));
//                                Log.d("words list api", listLiveData.getValue().size() + " words");
//                                mRepository.insertAll(listLiveDataWords.getValue());
//                            }
                            isProjectDetailSuccess.postValue(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            isProjectDetailSuccess.postValue(true);
//                            Toast.makeText(context, "No Words Found", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        Log.d("List", "Empty response");
                        isProjectDetailSuccess.postValue(false);
                    }

                } else {
                    isProjectDetailSuccess.postValue(false);
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
                isProjectDetailSuccess.postValue(false);
            }
        });
    }


    public void callGetListAPIWord(Context context, String projectId) {
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
//                            if (listLiveData.getValue() != null) {
//                                deleteAllProjectData();
//
//                                listLiveData.getValue().addAll(new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonObject("params").getAsJsonArray("userlist"), new TypeToken<List<ProjectUserModel>>() {
//                                }.getType()));
//                                insertAll(listLiveData.getValue());
//
//                            }
                            if (listLiveDataWords.getValue() != null) {
                                getmRepository().deleteAllWords();
                                listLiveDataWords.getValue().addAll(new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonObject("params").getAsJsonArray("projectparam"), new TypeToken<List<WordModel>>() {
                                }.getType()));
                                Log.d("words list api", listLiveData.getValue().size() + " words");
                                mRepository.insertAll(listLiveDataWords.getValue());
                            }
                            isProjectWordSuccess.postValue(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            isProjectWordSuccess.postValue(true);
//                            Toast.makeText(context, "No Words Found", Toast.LENGTH_SHORT).show();
                        }

                        isProjectWordSuccess.postValue(true);
                    } else {
                        Log.d("List", "Empty response");
                        isProjectWordSuccess.postValue(false);
                    }

                } else {
                    isProjectWordSuccess.postValue(false);
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
                isProjectWordSuccess.postValue(false);
            }
        });
    }


    public void insertAll(List<ProjectUserModel> allPlansModel) {
        new insertAllAsyncTask(wordDao).execute(allPlansModel);
    }

    private class insertAllAsyncTask extends AsyncTask<List<ProjectUserModel>, Void, Void> {
        private ProjectUsersDao mAsyncTaskDao;

        insertAllAsyncTask(ProjectUsersDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final List<ProjectUserModel>... params) {
            for (ProjectUserModel wordModel : params[0]) {
                wordModel.setProjectId(ProjectID);
                mAsyncTaskDao.insert(wordModel);
            }
            return null;
        }
    }

    public void deleteAllProjectData() {
        new deleteAllAsyncTask(wordDao).execute();
    }

    public void deleteUsingProjectIdProjectData(String projectID) {
        new deleteUsingProjectIdAsyncTask(wordDao).execute(projectID);
    }

    private class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProjectUsersDao mAsyncTaskDao;

        deleteAllAsyncTask(ProjectUsersDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    private class deleteUsingProjectIdAsyncTask extends AsyncTask<String, Void, Void> {
        private ProjectUsersDao mAsyncTaskDao;

        deleteUsingProjectIdAsyncTask(ProjectUsersDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteUsingProjectId(params[0]);
            return null;
        }
    }

    public LiveData<List<WordModel>> getWordsList() {
        return listLiveDataWords;
    }

    public void insert(WordModel wordModel) {
        mRepository.insert(wordModel);
    }


    public Map<String, List<WordModel>> getWordsMap() {
        return map;
    }

    public List<String> getKeysList() {
        return keys;
    }

}
