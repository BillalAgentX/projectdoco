package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.projectdocupro.mobile.interfaces.FinishCallback;
import com.projectdocupro.mobile.models.CustomersModel;
import com.projectdocupro.mobile.repos.CustomersRepository;

import java.util.List;

public class NewProjectViewModel extends AndroidViewModel {

    private CustomersRepository mRepository;

    public NewProjectViewModel(@NonNull Application application) {
        super(application);
    }

    public void initRepo(Context context, FinishCallback finishCallback){
        mRepository = new CustomersRepository(context,finishCallback);
    }


    public CustomersRepository getCustomersRepository(){
        return mRepository;
    }

    public void SaveProject(String  customerId,String  companyName, String  name, String  city, FinishCallback  finishCallback){
        mRepository.saveProject(getApplication(),customerId,companyName,name,city,finishCallback);
    }

    public List<CustomersModel> getAllCustomers() {
        return mRepository.getAllCustomers();
    }

    public List<String> getCustomerTitles() {
        return mRepository.getCustomersTitles();
    }

}
