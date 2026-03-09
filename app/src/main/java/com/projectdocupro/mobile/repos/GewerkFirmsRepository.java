package com.projectdocupro.mobile.repos;

public class GewerkFirmsRepository {

//    private GewerkFirmDao wordDao;
//
//    public GewerkFirmDao getWordDao() {
//        return wordDao;
//    }
//
//    public void setWordDao(GewerkFirmDao wordDao) {
//        this.wordDao = wordDao;
//    }
//
//    public LiveData<List<GewerkFirmModel>> listLiveData;
//
//    private MediatorLiveData<List<GewerkFirmModel>> mSectionLive = new MediatorLiveData<>();
//    private ProjectsDatabase db;
//    String ProjectID;
//
//    public LiveData<List<GewerkFirmModel>> getListLiveData() {
//        return listLiveData;
//    }
//
//    public void setListLiveData(LiveData<List<GewerkFirmModel>> listLiveData) {
//        this.listLiveData = listLiveData;
//    }
//
//    public GewerkFirmsRepository(Application application, String  projectId) {
//        db = ProjectsDatabase.getDatabase(application);
//        wordDao = db.gewerkFirmDao();
//        listLiveData = wordDao.getGewerkFirmLDataList();
//        ProjectID=projectId;
//        mSectionLive.addSource(listLiveData, plansModels -> {
//            if(plansModels == null || plansModels.isEmpty()) {
//                // Fetch data from API
//                Log.d("plans list","null plans");
//                callGetListAPI(application,projectId);
//            }else{
//                Log.d("plans list",plansModels.size()+" plans");
//                mSectionLive.removeSource(listLiveData);
//                mSectionLive.setValue(plansModels);
//                if (listLiveData.getValue()==null   ||  listLiveData.getValue().size()==0){
//                    if (listLiveData.getValue()!=null) {
//                        Log.d("plans list", listLiveData.getValue().size() + " plans");
//
//                    } else
//                        Log.d("plans list","null plans");
//                    callGetListAPI(application,projectId);
//                }
//            }
//        });
//    }
//
//
//    public void insert (ProjectUserModel   wordModel) {
//        if (listLiveData.getValue()==null   ||  listLiveData.getValue().size()==0){
//            new insertAsyncTask(wordDao).execute(wordModel);
//        }
//    }
//
//
//    private static class insertAsyncTask extends AsyncTask<ProjectUserModel, Void, Void> {
//        private ProjectUsersDao wordDao;
//        insertAsyncTask(ProjectUsersDao wordDao) {
//            this.wordDao    =   wordDao;
//        }
//
//        @Override
//        protected Void doInBackground(ProjectUserModel...  params) {
//            wordDao.insert(params[0]);
//            return null;
//        }
//    }
//
//    public void callGetListAPI(Context context, String   projectId){
//        SharedPrefsManager sharedPrefsManager  =   new SharedPrefsManager(context);
//        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
//        String  authToken   =   sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN,"");
//
//        if (authToken.length()>2){
//            authToken   =   authToken.substring(1,authToken.length()-1);
//        }
//
//        Call<JsonObject> call = retroApiInterface.getPlanDetailsAPI(authToken,Utils.DEVICE_ID,projectId);
//        Log.d("call url",call.request().url().toString());
//
//        call.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                if (response.isSuccessful()){
//                    if (response.body()!=null) {
//                        Log.d("List", "Success : " + response.body());
//                        try {
//                            listLiveData.getValue().addAll(new Gson().fromJson(response.body().getAsJsonObject("data").getAsJsonObject("params").getAsJsonArray("userlist"),new TypeToken<List<ProjectUserModel>>(){}.getType()));
//                        }catch (Exception   e){
//                            e.printStackTrace();
//                            Toast.makeText(context,"No Words Found",Toast.LENGTH_SHORT).show();
//                        }
//                        insertAll(listLiveData.getValue());
//                    }else{
//                        Log.d("List", "Empty response");
//                    }
//                }else{
//                    if (response.errorBody()!=null) {
//                        try {
//                            Log.d("List","Not Success : "+ response.errorBody().string());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    else
//                        Log.d("List","Not Success : "+ response.toString());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d("List","failed : "+t.getMessage());
//            }
//        });
//    }
//
//    public void insertAll (List<ProjectUserModel> allPlansModel) {
//        new insertAllAsyncTask(wordDao).execute(allPlansModel);
//    }
//
//    private class insertAllAsyncTask extends AsyncTask<List<ProjectUserModel>, Void, Void> {
//        private ProjectUsersDao mAsyncTaskDao;
//        insertAllAsyncTask(ProjectUsersDao dao) {
//            mAsyncTaskDao = dao;
//        }
//
//        @Override
//        protected Void doInBackground(final List<ProjectUserModel>... params) {
//            for (ProjectUserModel   wordModel:params[0]) {
//                wordModel.setProjectId(ProjectID);
//                mAsyncTaskDao.insert(wordModel);
//            }
//            return null;
//        }
//    }


}
