package com.projectdocupro.mobile.repos;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.models.PlansModel;

import java.util.List;

public class FavoritePlansRepository {

    private PlansDao mPlanDao;
    private LiveData<List<PlansModel>> mFavoritePlans;

    public FavoritePlansRepository(Context context) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mPlanDao = db.plansDao();
        mFavoritePlans = mPlanDao.getFavoritePlans();
    }

    public FavoritePlansRepository(Context context,String projectId) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mPlanDao = db.plansDao();
        mFavoritePlans = mPlanDao.getFavoritePlans(projectId);
    }

    public LiveData<List<PlansModel>> getFavoritePlans() {
        return mFavoritePlans;
    }

}
