package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.interfaces.FinishCallback;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.CustomersModel;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomersRepository {

//    private String  projectId;

    ProjectDao  projectDao;
    private List<CustomersModel> mAllCustomers;
    private List<String> customersTitles;
//    private MediatorLiveData<List<CustomersModel>> mSectionLive = new MediatorLiveData<>();

    public CustomersRepository(Context context, FinishCallback finishCallback) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        projectDao  =   db.projectDao();

//        customerDao = db.customerDao();
//        mAllCustomers = customerDao.getCustomersList();

//        this.projectId  =   projectId;

        mAllCustomers   =   new ArrayList<>();
        customersTitles   =   new ArrayList<>();
        customersTitles.add(context.getString(R.string.select_customer));
        callGetCustomersAPI(context, finishCallback);

        Log.d("REPO","init");
    }

    public List<CustomersModel> getAllCustomers() {
        return mAllCustomers;
    }

    public List<String> getCustomersTitles() {
        return customersTitles;
    }

    public void callGetCustomersAPI(Context context, FinishCallback finishCallback){
        SharedPrefsManager sharedPrefsManager  =   new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String  authToken   =   sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN,"");

        if (authToken.length()>2){
            authToken   =   authToken.substring(1,authToken.length()-1);
        }

        Call<JsonObject> call = retroApiInterface.getCustomersAPI(authToken, Utils.DEVICE_ID);
        Log.d("call url",call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (response.body()!=null) {
                        Log.d("Customers", "Success : " + response.body());
                        mAllCustomers.addAll(new Gson().fromJson(response.body().getAsJsonArray("data"),new TypeToken<List<CustomersModel>>(){}.getType()));

                        for (int a=0;a<mAllCustomers.size();a++){
                            customersTitles.add(mAllCustomers.get(a).getCompanyName());
                        }

                        finishCallback.onFinishSuccess();

//                        insertAll(mAllCustomers.getValue());
                    }else{
                        finishCallback.onFinishFailure();
                        Log.d("Customers", "Empty response");
                    }
                }else{
                    finishCallback.onFinishFailure();
                    if (response.errorBody()!=null) {
                        try {
                            Log.d("Customers","Not Success : "+ response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        Log.d("Customers","Not Success : "+ response.toString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                finishCallback.onFinishFailure();
                Log.d("Customers","failed : "+t.getMessage());
            }
        });
    }

    public void saveProject(Context context, String  customerId,String  companyName, String   name, String  city, FinishCallback    finishCallback) {
        SharedPrefsManager sharedPrefsManager  =   new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String  authToken   =   sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN,"");

        if (authToken.length()>2){
            authToken   =   authToken.substring(1,authToken.length()-1);
        }

        JsonObject  params  =   new JsonObject();
        params.addProperty("customer_id",customerId);
        params.addProperty("name",name);
        params.addProperty("city",city);

        Call<JsonObject> call = retroApiInterface.createProject(authToken,Utils.DEVICE_ID,params);
        Log.d("call url",call.request().url().toString());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (response.body()!=null) {
                        Log.d("New Project", "Success : " + response.body());
                        JsonObject  data    =   response.body().getAsJsonObject("data");
                        insert(new ProjectModel(data.get("projectid").getAsString(),name,"","","",city,"",""
                                ,data.get("lastupdated").getAsString(),"","","",customerId,"","",companyName
                        ,"","0","0",false, 0L));
                        finishCallback.onFinishSuccess();
                    }else{
                        Log.d("New Project", "Empty response");
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


    public void insert (ProjectModel projectModel) {
        new insertAsyncTask(projectDao).execute(projectModel);
    }

    private static class insertAsyncTask extends AsyncTask<ProjectModel, Void, Void> {

        private ProjectDao mAsyncTaskDao;

        insertAsyncTask(ProjectDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProjectModel... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
