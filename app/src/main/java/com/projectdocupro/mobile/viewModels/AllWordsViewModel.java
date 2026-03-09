package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.adapters.WordListRecyclerAdapter;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
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

public class AllWordsViewModel extends AndroidViewModel {

    private MediatorLiveData<List<WordModel>> mSectionLive = new MediatorLiveData<>();
    private LiveData<List<WordModel>> listLiveData;
    private List<String>    keys;
    private Map<String,List<WordModel>> map;
    private WordsRepository mRepository;
    private WordListRecyclerAdapter wordListRecyclerAdapter;
    private long photoId;
    private boolean isChanged=false;



    public AllWordsViewModel(@NonNull Application application) {
        super(application);
    }

    public void InitRepo(String projectId,long  photoId){
        mRepository = new WordsRepository(getApplication(),photoId,projectId);
        this.photoId=photoId;
        map =   new HashMap<>();
        keys    =   new ArrayList<>();

        listLiveData    =   mRepository.getWordsList(projectId);

        wordListRecyclerAdapter =   new WordListRecyclerAdapter(photoId, keys, map, wordModel -> {

            wordModel.setPhotoType(LocalPhotosRepository.TYPE_LOCAL_PHOTO);
//            wordModel.setPhotoIds(","+photoId+"");
            mRepository.update(wordModel);
            mRepository.updatePhotoModel(photoId);
            new UpdatePhotosAsyncTask( projectId,String.valueOf(photoId)).execute();
        });



        mSectionLive.addSource(listLiveData, wordModels -> {
            if(wordModels == null || wordModels.isEmpty()) {
                // Fetch data from API
                Log.d("words list","null plans");
                callGetListAPI(getApplication(),projectId);
            }else{
                Log.d("words list db",wordModels.size()+" plans");
                mSectionLive.removeSource(listLiveData);
                mSectionLive.setValue(wordModels);
                if (listLiveData.getValue()==null   ||  listLiveData.getValue().size()==0){
                    if (listLiveData.getValue()!=null) {
                        Log.d("plans list", listLiveData.getValue().size() + " plans");
                        for (WordModel  wordModel:listLiveData.getValue()){
                            List<WordModel> list    =   map.get(wordModel.getGroup());
                            if (list==null){
                                list    =   new ArrayList<>();
                            }
                            list.add(wordModel);
                            map.put(wordModel.getGroup(),list);
                            if (!keys.contains(wordModel.getGroup())){
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
    class UpdatePhotosAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        String projectID,photoID;

        UpdatePhotosAsyncTask(String projectId,String photoId) {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getApplication()).photoDao();
            projectID=projectId;
            photoID=photoId;
        }

        @Override
        protected Void doInBackground(final String... params) {

            List<PhotoModel> defectTradeModelListt = mAsyncTaskDao.getDefectPhotosListUsingLoalID(projectID, photoID);

            if (defectTradeModelListt != null && defectTradeModelListt.size() > 0) {

                defectTradeModelListt.get(0).setPhotoSynced(false);//done
                defectTradeModelListt.get(0).setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);
                mAsyncTaskDao.update(defectTradeModelListt.get(0));

            }

            return null;
        }
    }

    public void callGetListAPI(Context context, String   projectId){
        SharedPrefsManager sharedPrefsManager  =   new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String  authToken   =   sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN,"");

        if (authToken.length()>2){
            authToken   =   authToken.substring(1,authToken.length()-1);
        }

        Call<JsonObject> call = retroApiInterface.getPlanDetailsAPI(authToken, Utils.DEVICE_ID,projectId);
        Log.d("call url",call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (response.body()!=null) {
                        Log.d("List", "Success : " + response.body());
                        try {
                            listLiveData.getValue().addAll(new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonObject("params").getAsJsonArray("projectparam"),new TypeToken<List<WordModel>>(){}.getType()));
                            Log.d("words list api",listLiveData.getValue().size()+" words");
                            mRepository.insertAll(listLiveData.getValue());
                        }catch (Exception   e){
                            e.printStackTrace();
                            Toast.makeText(context,"No Words Found",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.d("List", "Empty response");
                    }
                }else{
                    if (response.errorBody()!=null) {
                        try {
                            Log.d("List","Not Success : "+ response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        Log.d("List","Not Success : "+ response.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("List","failed : "+t.getMessage());
            }
        });
    }

    public LiveData<List<WordModel>> getWordsList() {
        return listLiveData;
    }

    public void insert(WordModel wordModel){
        mRepository.insert(wordModel);
    }

    public WordListRecyclerAdapter getAdapter() {
        return wordListRecyclerAdapter;
    }

    public Map<String,List<WordModel>> getWordsMap() {
        return map;
    }

    public  List<String> getKeysList() {
        return keys;
    }

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public boolean isChanged() {
        return isChanged;
    }
}
