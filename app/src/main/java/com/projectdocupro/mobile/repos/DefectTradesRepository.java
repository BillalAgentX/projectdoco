package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.adapters.DefectedProjectPhotosRecyclerAdapter;
import com.projectdocupro.mobile.dao.DefectsTradesDao;
import com.projectdocupro.mobile.models.DefectTradeModel;
import com.projectdocupro.mobile.models.DefectsModel;

import java.util.List;

public class DefectTradesRepository {

    private final String mProject;
    private DefectsTradesDao mDefectsPhotoDao;
    private LiveData<List<DefectTradeModel>> mDefectedPhotos;
    private String imagePath;
    DefectedProjectPhotosRecyclerAdapter photosRecyclerAdapter;
    Context mContext;
    private List<DefectTradeModel> defectPhotoModelList;

    public DefectsTradesDao getmDefectsTradeDao() {
        return mDefectsPhotoDao;
    }

    public void setmDefectsPhotoDao(DefectsTradesDao mDefectsPhotoDao) {
        this.mDefectsPhotoDao = mDefectsPhotoDao;
    }

    public LiveData<List<DefectTradeModel>> getmDefectedTrades() {
        return mDefectedPhotos;
    }

    public void setmDefectedPhotos(LiveData<List<DefectTradeModel>> mDefectedPhotos) {
        this.mDefectedPhotos = mDefectedPhotos;
    }


    public DefectTradesRepository(Context context, String projectId) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.defectTradeDao();
        mContext = context;
        mProject = projectId;
//        mDefectedPhotos = mDefectsPhotoDao.getDefectPhotoModel(projectId);
    }

    public void insert(DefectTradeModel allPlansModel) {
        new insertAsyncTask(mDefectsPhotoDao).execute(allPlansModel);
    }

    public void insert(DefectTradeModel allPlansModel, String projectId, String flawId) {
        new insertAsyncTask2(mDefectsPhotoDao, projectId, flawId).execute(allPlansModel);
    }

    public void deleteROwsUsingProjectId(String projectId) {
        new DeleteUsingProjectIdAsyncTask(mDefectsPhotoDao).execute(projectId);
    }

    public void deleteAllROws() {

        new DeleteAsyncTask(mDefectsPhotoDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<DefectTradeModel, Void, Void> {
        private DefectsTradesDao mAsyncTaskDao;

        insertAsyncTask(DefectsTradesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DefectTradeModel... params) {
            mAsyncTaskDao.insert(params[0]);

            return null;
        }
    }

    private static class insertAsyncTask2 extends AsyncTask<DefectTradeModel, Void, Void> {
        private DefectsTradesDao mAsyncTaskDao;
        private String projectId;
        private String flawId;

        insertAsyncTask2(DefectsTradesDao dao, String projectId, String flawId) {
            mAsyncTaskDao = dao;
            this.projectId = projectId;
            this.flawId = flawId;
        }

        @Override
        protected Void doInBackground(final DefectTradeModel... params) {
            List<DefectTradeModel> existingDefectsTrades = mAsyncTaskDao.getAllDefectTradeWithStatusONModel(projectId, flawId);
            DefectTradeModel defectTradeModel = params[0];

            boolean alreadyHasDefectTrade = false;
            for (DefectTradeModel tempDefectTradeModel : existingDefectsTrades) {
                if (tempDefectTradeModel.getPdservicetitle().equals(defectTradeModel.getPdservicetitle())) {
                    alreadyHasDefectTrade = true;
                    break;
                }
            }

            if (!alreadyHasDefectTrade) {
                mAsyncTaskDao.insert(params[0]);
            }

            return null;
        }
    }


    private static class UpdateAsyncTask extends AsyncTask<DefectTradeModel, Void, Void> {
        private DefectsTradesDao mAsyncTaskDao;

        UpdateAsyncTask(DefectsTradesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DefectTradeModel... params) {

            mAsyncTaskDao.update(params[0]);

            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {
        private DefectsTradesDao mAsyncTaskDao;

        DeleteAsyncTask(DefectsTradesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... params) {

            mAsyncTaskDao.deleteAll();

            return null;
        }
    }

    private static class DeleteUsingProjectIdAsyncTask extends AsyncTask<String, Void, Void> {
        private DefectsTradesDao mAsyncTaskDao;

        DeleteUsingProjectIdAsyncTask(DefectsTradesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(String... params) {

            mAsyncTaskDao.deleteUsingProjectId(params[0]);

            return null;
        }
    }

    public void UpdateDeciplineOfSelectedFlaw(String projectId, String flawId) {
        new RetrieveAsyncTask(mDefectsPhotoDao).execute(projectId, flawId);
    }


    private class RetrieveAsyncTask extends AsyncTask<String, Void, Void> {
        private DefectsTradesDao mAsyncTaskDao;

        RetrieveAsyncTask(DefectsTradesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)

            List<DefectTradeModel> defectTradeModelList = mAsyncTaskDao.getAllDefectTradeWithStatusONModel(params[0], params[1]);
            if (defectTradeModelList != null) {
                StringBuilder sb = new StringBuilder();
                StringBuilder sb_ids = new StringBuilder();
                int counter = 0;
                for (int i = 0; i < defectTradeModelList.size(); i++) {
                    counter++;
                    String str_title = "";
                    String str_ids = "";
                    if (defectTradeModelList.size() == counter) {
                        str_title = defectTradeModelList.get(i).getPdservicetitle();
                        str_ids = defectTradeModelList.get(i).getSelectvalue();
                    } else {
                        str_title = defectTradeModelList.get(i).getPdservicetitle() + ", ";
                        str_ids = defectTradeModelList.get(i).getSelectvalue() + ", ";
                    }
                    sb.append(str_title);
                    sb_ids.append(str_ids);

                }
                DefectRepository defectRepository = new DefectRepository(mContext, mProject);
                DefectsModel defectsModel = defectRepository.getmDefectDao().getDefectsObjectWithDecipline(params[0], params[1]);
                if (defectsModel != null && !defectsModel.getDefectId().equals("")) {

                    defectsModel.setDiscipline(sb.toString());
                    defectsModel.setDiscipline_id(sb_ids.toString());
                    defectRepository.getmDefectDao().update(defectsModel);
                }

            }
            return null;
        }
    }

    public void addLocalMangelGewerk(DefectsTradesDao dao, String projectID, String localFlawId) {
        new AddLocalMangelGeewerksAsyncTask(dao).execute(projectID, localFlawId);
    }

    private class AddLocalMangelGeewerksAsyncTask extends AsyncTask<String, Void, Void> {
        private DefectsTradesDao mAsyncTaskDao;

        AddLocalMangelGeewerksAsyncTask(DefectsTradesDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)

            DefectTradeModel defectTradeModelList = mAsyncTaskDao.getFirstDefectTradeWithServerID(params[0]);
            if (defectTradeModelList != null && defectTradeModelList.getPdflawid() != null) {
                List<DefectTradeModel> defectTradeModelListt = mAsyncTaskDao.getAllDefectTradeList(params[0], defectTradeModelList.getPdflawid());

                if (defectTradeModelListt != null && defectTradeModelListt.size() > 0) {
                    for (int i = 0; i < defectTradeModelListt.size(); i++) {
                        defectTradeModelListt.get(i).setPdflawid("");
                        defectTradeModelListt.get(i).setLocalpdflawid(params[1]);
                        defectTradeModelListt.get(i).setPdprojectid(params[0]);
                        defectTradeModelListt.get(i).setSelected(0);
                        mAsyncTaskDao.insert(defectTradeModelListt.get(i));
                    }
                }


            }
            return null;
        }
    }
}
