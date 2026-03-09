package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.repos.DefectRepository;

public class DefectDetailsDatesFraagViewModel extends AndroidViewModel {


    String projectID,defectID;
    private DefectRepository defectRepository;
   public  MutableLiveData<DefectsModel> listLiveData=new MutableLiveData<>();

    public MutableLiveData<DefectsModel> getListLiveData() {
        return listLiveData;
    }

    public void setListLiveData(MutableLiveData<DefectsModel> listLiveData) {
        this.listLiveData = listLiveData;
    }

    public DefectDetailsDatesFraagViewModel(@NonNull Application application) {
        super(application);

    }

    public void init(String projectId,String defectId){
        defectRepository   =   new DefectRepository(getApplication(), projectId);
        projectID=projectId;
        defectID=defectId;
//        loadPhotosData();
    }

/*    private class RetrieveAsyncTask extends AsyncTask<Void, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        RetrieveAsyncTask() {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getApplication()).defectsDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)
           DefectsModel defectsModel= mAsyncTaskDao.getDefectsObjectt(projectID,defectID);
          listLiveData.postValue(defectsModel) ;
            return null;
        }
    }

    public void loadPhotosData(){
        new RetrieveAsyncTask().execute();
    }*/
}
