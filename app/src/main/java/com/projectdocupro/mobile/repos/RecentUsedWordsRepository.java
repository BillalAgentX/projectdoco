package com.projectdocupro.mobile.repos;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.utility.Utils;

import java.util.List;

public class RecentUsedWordsRepository {

    private WordDao wordDao;
    private LiveData<List<WordModel>> listLiveData;
    private ProjectsDatabase db;

    public RecentUsedWordsRepository(Application application, String  projectId,String photoID) {
        db = ProjectsDatabase.getDatabase(application);
        wordDao = db.wordDao();
        listLiveData = wordDao.getFavouriteWordsList(projectId);
        new getSimpleListAsyncTask(wordDao).execute(projectId,photoID);
    }

    public RecentUsedWordsRepository(Application application, String  projectId) {
        db = ProjectsDatabase.getDatabase(application);
        wordDao = db.wordDao();
        listLiveData = wordDao.getRecentUsedWordsList(projectId);


    }

    public LiveData<List<WordModel>> getRecentWordsList() {
        return listLiveData;
    }

    public void update (WordModel wordModel) {
        Utils.showLogger("updating words");
        new updateAsyncTask(wordDao).execute(wordModel);
    }

    private class updateAsyncTask extends AsyncTask<WordModel, Void, Void> {
        private WordDao mAsyncTaskDao;
        updateAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WordModel... params) {
                mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private class getSimpleListAsyncTask extends AsyncTask<String, Void, Void> {
        private WordDao mAsyncTaskDao;
        getSimpleListAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
             List<WordModel> wordModelList= mAsyncTaskDao.getWordsSimpleList(params[0]);

            return null;
        }
    }

}
