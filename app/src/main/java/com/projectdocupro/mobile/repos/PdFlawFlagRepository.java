package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.DefectsPhotosDao;
import com.projectdocupro.mobile.dao.PdFlawFLagListDao;
import com.projectdocupro.mobile.models.DefectPhotoModel;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;

import java.util.ArrayList;
import java.util.List;

public class PdFlawFlagRepository {

    private  ProjectsDatabase db=null;
    private String flawId = "";
    private String project_id = "";
    private PdFlawFLagListDao mDefectsPhotoDao;
    private LiveData<List<Pdflawflag>> mDefectedPhotos;
    private String imagePath;
    Context mContext;
    private List<Pdflawflag> defectPhotoModelList;

    public MutableLiveData<List<Pdflawflag>> getListMutableLiveData() {
        return listMutableLiveData;
    }

    public void setListMutableLiveData(MutableLiveData<List<Pdflawflag>> listMutableLiveData) {
        this.listMutableLiveData = listMutableLiveData;
    }

    private MutableLiveData< List<Pdflawflag>>  listMutableLiveData= new MutableLiveData<>();

    public List<Pdflawflag> getDefectPhotoModelList() {
        return defectPhotoModelList;
    }

    public void setDefectPhotoModelList(List<Pdflawflag> defectPhotoModelList) {
        this.defectPhotoModelList = defectPhotoModelList;
    }

    public PdFlawFLagListDao getmDefectsPhotoDao() {
        return mDefectsPhotoDao;
    }

    public void setmDefectsPhotoDao(PdFlawFLagListDao mDefectsPhotoDao) {
        this.mDefectsPhotoDao = mDefectsPhotoDao;
    }

    public LiveData<List<Pdflawflag>> getmDefectedPhotos() {
        return mDefectedPhotos;
    }

    public void setmDefectedPhotos(LiveData<List<Pdflawflag>> mDefectedPhotos) {
        this.mDefectedPhotos = mDefectedPhotos;
    }

    public PdFlawFlagRepository(Context context) {
         db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.pdFlawFLagDao();
        mContext = context;
    }

    public PdFlawFlagRepository(Context context, String projectId) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.pdFlawFLagDao();
        mContext = context;
        project_id = projectId;
        retrieveFlawFlag(projectId);

    }

    public PdFlawFlagRepository(Context context, String projectId, String flawID) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.pdFlawFLagDao();
        mContext = context;
        project_id = projectId;
        flawId = flawID;
        List<String> defectList= new ArrayList<>();
        defectList.add(flawID);

        new RetrieveAsyncTask(mDefectsPhotoDao, defectList).execute(project_id);

    }

    public PdFlawFlagRepository(Context context, String projectId, List<String> flawIDStringList) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.pdFlawFLagDao();
        mContext = context;
        project_id = projectId;

        new RetrieveAsyncTask(mDefectsPhotoDao, flawIDStringList).execute(project_id);

    }

    public void insert(Pdflawflag allPlansModel) {
        new insertAsyncTask(mDefectsPhotoDao).execute(allPlansModel);
    }

    public void deleteAllROws() {
        new DeleteAsyncTask(mDefectsPhotoDao).execute();
    }
  public void deleteROwsUsingProjectId(String projectId) {
        new DeleteUsingProjectIdAsyncTask(mDefectsPhotoDao).execute(projectId);
    }

    private static class insertAsyncTask extends AsyncTask<Pdflawflag, Void, Void> {
        private PdFlawFLagListDao mAsyncTaskDao;

        insertAsyncTask(PdFlawFLagListDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Pdflawflag... params) {

            mAsyncTaskDao.insert(params[0]);

            return null;
        }
    }


    private static class UpdateAsyncTask extends AsyncTask<DefectPhotoModel, Void, Void> {
        private DefectsPhotosDao mAsyncTaskDao;

        UpdateAsyncTask(DefectsPhotosDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DefectPhotoModel... params) {

            mAsyncTaskDao.update(params[0]);

            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {
        private PdFlawFLagListDao mAsyncTaskDao;

        DeleteAsyncTask(PdFlawFLagListDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... params) {

            mAsyncTaskDao.deleteAll();

            return null;
        }
    }

    private static class DeleteUsingProjectIdAsyncTask extends AsyncTask<String, Void, Void> {
        private PdFlawFLagListDao mAsyncTaskDao;

        DeleteUsingProjectIdAsyncTask(PdFlawFLagListDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(String... params) {

            mAsyncTaskDao.deleteUsingProjectId(params[0]);

            return null;
        }
    }

    public void retrieveFlawFlag(String projectID){
        new RetrieveFlawListAsyncTask(mContext).execute(projectID);

    }

    private class RetrieveFlawListAsyncTask extends AsyncTask<String, Void, List<DefectsModel>> {
        private DefectsDao mAsyncTaskDao;
        private PdFlawFLagListDao pdFlawFLagListDao;
        ProjectsDatabase db;

        RetrieveFlawListAsyncTask(Context context) {
            db = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = db.defectsDao();
            pdFlawFLagListDao = db.pdFlawFLagDao();

        }

        @Override
        protected List<DefectsModel> doInBackground(final String... params) {
            List<DefectsModel> flawFlagList = mAsyncTaskDao.getDefectsListtt(params[0]);
            return flawFlagList;
        }

        @Override
        protected void onPostExecute(List<DefectsModel> pdflawflags) {
            super.onPostExecute(pdflawflags);
            List<String> defectList = new ArrayList<>();
            for (int i = 0; i < pdflawflags.size(); i++) {
                defectList.add(pdflawflags.get(i).getDefectLocalId()+"");
            }


            new RetrieveAsyncTask(pdFlawFLagListDao, defectList).execute(project_id);
        }
    }

    private class RetrieveAsyncTask extends AsyncTask<String, Void, List<Pdflawflag>> {
            private PdFlawFLagListDao mAsyncTaskDao;
            List<String> defectlist;

            RetrieveAsyncTask(PdFlawFLagListDao dao, List<String> stringList) {
                mAsyncTaskDao = dao;
                defectlist = stringList;
            }

            @Override
            protected List<Pdflawflag> doInBackground(final String... params) {

                List<String> listOfDistinctPlanIds = mAsyncTaskDao.getDistinctPlanIdsList(params[0], defectlist);
                List<Pdflawflag> flawFlagList = mAsyncTaskDao.getFlawFlagUsingPlanIdList(params[0], listOfDistinctPlanIds);

                for (int i = 0; i < flawFlagList.size(); i++) {
                    for (int j = i + 1; j < flawFlagList.size(); j++) {
                        if (flawFlagList.get(i).getPdplanid()!=null&&flawFlagList.get(i).getPdplanid().equals(flawFlagList.get(j).getPdplanid())) {
                            flawFlagList.remove(j);
                            j--;
                        }
                    }
                }
                return flawFlagList;
            }

            @Override
            protected void onPostExecute(List<Pdflawflag> pdflawflags) {
                super.onPostExecute(pdflawflags);
                listMutableLiveData.setValue(pdflawflags);

            }
        }
    }

