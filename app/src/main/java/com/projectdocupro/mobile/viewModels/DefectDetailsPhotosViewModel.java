package com.projectdocupro.mobile.viewModels;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.adapters.DefectedProjectPhotosRecyclerAdapter;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.repos.DefectPhotoRepository;

import java.util.List;

public class DefectDetailsPhotosViewModel extends AndroidViewModel {

    private DefectPhotoRepository localPhotosRepository;

    public DefectPhotoRepository getLocalPhotosRepository() {
        return localPhotosRepository;
    }
    public MutableLiveData<List<PhotoModel>> photoModelMutableLiveData = new MutableLiveData<>();
    public void setLocalPhotosRepository(DefectPhotoRepository localPhotosRepository) {
        this.localPhotosRepository = localPhotosRepository;
    }

    String mProjectId;
    String flaw_id;

    public DefectDetailsPhotosViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(String projectId, String defectId) {
        localPhotosRepository = new DefectPhotoRepository(getApplication());
        localPhotosRepository.initAdapter(projectId , defectId);

//        loadPhotosData();
        mProjectId = projectId;
        flaw_id = defectId;
//        initAdapter(projectId, defectId);
//        getPhotosFromGallery(projectId);
    }

    public MutableLiveData<List<PhotoModel>> getAllPhotos() {

        return   photoModelMutableLiveData;
    }

    public void loadPhotosData(){
        new RetrieveAsyncTask().execute();
    }

    public DefectedProjectPhotosRecyclerAdapter getAdapter() {
        return localPhotosRepository.getPhotosRecyclerAdapter();
    }

    public void initAdapter(String projectId, String defectID) {
        mProjectId = projectId;
        localPhotosRepository.initAdapter(projectId, defectID);
    }

    private class RetrieveAsyncTask extends AsyncTask<Void, Void, Void> {
        private PhotoDao mAsyncTaskDao;

        RetrieveAsyncTask() {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getApplication()).photoDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)

            photoModelMutableLiveData.postValue(localPhotosRepository.getmDefectsPhotoDao().getDefectPhotosAndLocalPhotosList(mProjectId, flaw_id));

            return null;
        }
    }
}
