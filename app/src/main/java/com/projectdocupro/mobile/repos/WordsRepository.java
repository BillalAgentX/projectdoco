package com.projectdocupro.mobile.repos;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.JsonObject;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.interfaces.FinishCallback;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WordsRepository {

    private WordDao wordDao;
    private PhotoModel photoModel;
    private ProjectsDatabase db;
    private String  projectId;

    public WordsRepository(Application application, long  photoId,String    projectId) {
        db = ProjectsDatabase.getDatabase(application);
        wordDao = db.wordDao();
        this.projectId=projectId;
        new getPhotoAsyncTask(photoId).execute();
    }

    public WordsRepository(Application application, String    projectId) {
        db = ProjectsDatabase.getDatabase(application);
        wordDao = db.wordDao();
        this.projectId=projectId;
    }

    public void saveWord(Context context, String  group, String   wordName, FinishCallback finishCallback) {
        SharedPrefsManager sharedPrefsManager  =   new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String  authToken   =   sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN,"");

        if (authToken.length()>2){
            authToken   =   authToken.substring(1,authToken.length()-1);
        }

        JsonObject params  =   new JsonObject();
        params.addProperty("group",group);
        params.addProperty("name",wordName);
        params.addProperty("type",0);

        Call<JsonObject> call = retroApiInterface.createWord(authToken, Utils.DEVICE_ID,projectId,params);
        Log.d("call url",call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (response.body()!=null) {
                        Log.d("New Word", "Success : " + response.body());
                        JsonObject  data    =   response.body().getAsJsonObject("data");
                        insert(new WordModel(data.get("projectparamid").getAsString(),projectId,group,wordName,"0","","","1","",""));
                        finishCallback.onFinishSuccess();
                    }else{
                        Log.d("New Word", "Empty response");
                    }
                }else{
                    finishCallback.onFinishFailure();
                    if (response.errorBody()!=null) {
                        try {
                            Log.d("New Project","Not Success : "+ response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        Log.d("New Project","Not Success : "+ response.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("New Project","failed : "+t.getMessage());
            }
        });
    }

    public void insert (WordModel   wordModel) {
        new insertAsyncTask(wordDao).execute(wordModel);
    }

    public LiveData<List<WordModel>> getWordsList(String    projectId) {
        return wordDao.getWordsList(projectId);
    }

    public LiveData<List<WordModel>> getFavoriteWordsList(String    projectId) {
        return wordDao.getFavoriteWordsList(projectId);
    }

    private static class insertAsyncTask extends AsyncTask<WordModel, Void, Void> {
        private WordDao wordDao;
        insertAsyncTask(WordDao wordDao) {
            this.wordDao    =   wordDao;
        }

        @Override
        protected Void doInBackground(WordModel...  params) {
            wordDao.insert(params[0]);
            return null;
        }
    }

    public void insertAll (List<WordModel> allPlansModel) {
        new insertAllAsyncTask(wordDao).execute(allPlansModel);
    }

    private class insertAllAsyncTask extends AsyncTask<List<WordModel>, Void, Void> {
        private WordDao mAsyncTaskDao;
        insertAllAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final List<WordModel>... params) {
            for (WordModel   wordModel:params[0]) {
                mAsyncTaskDao.insert(wordModel);
            }
            return null;
        }
    }

    public void deleteAllWords () {
        new deleteAllWordsTask(wordDao).execute();
    }

    public void deleteWordsUsingProjectId (String projectId) {
        new deleteWordsUsingProjectIdTask(wordDao).execute(projectId);
    }

    private class deleteAllWordsTask extends AsyncTask<Void, Void, Void> {
        private WordDao mAsyncTaskDao;
        deleteAllWordsTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {

                mAsyncTaskDao.deleteAll();

            return null;
        }
    }

    private class deleteWordsUsingProjectIdTask extends AsyncTask<String, Void, Void> {
        private WordDao mAsyncTaskDao;
        deleteWordsUsingProjectIdTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {

            mAsyncTaskDao.deleteUsingProjectId(params[0]);

            return null;
        }
    }

    public void update (WordModel wordModel) {
        new updateAsyncTask(wordDao).execute(wordModel);
    }

    public void updatePhotoModel (long  photoId) {
        new updatePhotoAsyncTask(photoId).execute();
    }

    private class updateAsyncTask extends AsyncTask<WordModel, Void, Void> {
        private WordDao mAsyncTaskDao;
        updateAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WordModel... params) {
                mAsyncTaskDao.update(params[0]);
            return null;
        }
    }



    public class getPhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        private long photoId;
        getPhotoAsyncTask(long    photoId) {
            this.photoId = photoId;
        }

        @Override
        protected Void doInBackground(Void...  params) {
            photoModel  =   db.photoDao().getPhotoModel(photoId);
            return null;
        }
    }


    public class updatePhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        private long photoId;
        updatePhotoAsyncTask(long    photoId) {
            this.photoId = photoId;
        }

        @Override
        protected Void doInBackground(Void...  params) {
            List<WordModel> wordModels  =   wordDao.getWordsListIncludesPhotoId("%,"+photoId+"%",projectId);
            if (wordModels.size()>0){
                photoModel.setWordAdded(true);//When update the task
            }else{
                photoModel.setWordAdded(false);
            }
            db.photoDao().update(photoModel);
            return null;
        }
    }

}
