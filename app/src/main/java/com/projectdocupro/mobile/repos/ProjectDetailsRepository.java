package com.projectdocupro.mobile.repos;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.dao.ProjectDetailDao;
import com.projectdocupro.mobile.models.ProjectModel;

public class ProjectDetailsRepository {

    private ProjectDetailDao mProjectDao;
    private DefectsDao  defectsDao;
    private PlansDao    plansDao;
    private LiveData<ProjectModel> mProjects;
    private ProjectsDatabase db;

    public ProjectDetailsRepository(Application application,    String  projectId) {
        db = ProjectsDatabase.getDatabase(application);
        mProjectDao = db.projectDetailDao();
        defectsDao  =   db.defectsDao();
        mProjects = mProjectDao.getProject(projectId);
        getCounts(defectsDao,plansDao);
        update(mProjects.getValue());
    }

    public LiveData<ProjectModel> getProject() {
        return mProjects;
    }

    public void update (ProjectModel projectModel) {
        if (projectModel!=null){
            new updateAsyncTask(mProjectDao).execute(projectModel);
        }
    }

    public void setLastOpen(ProjectModel projectModel) {
        update(projectModel);
    }

    private static class updateAsyncTask extends AsyncTask<ProjectModel, Void, Void> {

        private ProjectDetailDao mAsyncTaskDao;

        updateAsyncTask(ProjectDetailDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProjectModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private void getCounts(DefectsDao defectsDao, PlansDao plansDao) {

//        numberDefects   =   defectsDao.getDefectsCount();
//        numberPlans =   plansDao.getPlansCount();
    }

    public static class getDefectsCountAsyncTask extends AsyncTask<Void, Void, Integer> {

        private DefectsDao defectsDao;

        getDefectsCountAsyncTask(DefectsDao defectsDao) {
            this.defectsDao =   defectsDao;
        }

        @Override
        protected Integer doInBackground(final Void... params) {

            return defectsDao.getDefectsCount();
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);

        }
    }

}
