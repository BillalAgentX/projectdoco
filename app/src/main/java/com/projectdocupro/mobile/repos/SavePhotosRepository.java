package com.projectdocupro.mobile.repos;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PdFlawFLagListDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.RecordAudioDao;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.RecordAudioModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.localFilters.ImageId_VS_Input;
import com.projectdocupro.mobile.models.localFilters.WordContentModel;
import com.projectdocupro.mobile.utility.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavePhotosRepository {

    private SharedPrefsManager sharedPrefsManager;
    private PhotoDao photoDao;
    private PhotoModel photoModel;
    private ProjectsDatabase db;
    private String projectId;
    Application applicationCtx;
    public MutableLiveData<Long> newlyInsertedImageID = new MutableLiveData<>();

    public PhotoDao getPhotoDao() {
        return photoDao;
    }

    public void setPhotoDao(PhotoDao photoDao) {
        this.photoDao = photoDao;
    }

    public SavePhotosRepository(Application application, String projectId) {
        db = ProjectsDatabase.getDatabase(application);
        photoDao = db.photoDao();
        this.projectId = projectId;
        applicationCtx = application;
        sharedPrefsManager = new SharedPrefsManager(application);
    }

    public PhotoModel getPhotoModel() {
        return photoModel;
    }

    public void insert(PhotoModel photoModel) {
        this.photoModel = photoModel;

        new insertAsyncTask(photoDao).execute(photoModel);
    }

    public void duplicateForBrushInsert(PhotoModel photoModel, long originalPhotoId) {
        Utils.showLogger("duplicateForBrushInsert");
        this.photoModel = photoModel;
        new duplicateInsertAsyncTask(photoDao, originalPhotoId).execute(photoModel);
        new updateOriginalPhotoAsyncTask(originalPhotoId).execute();//Change by billal on 14 June
    }

    public void update() {
        new updateAsyncTask(photoDao).execute(photoModel);
    }

    public void setExistingPhotoModel(long photoId) {
        new getPhotoModelAsyncTask(photoDao).execute(photoId);
    }

    public void setPhotoModel(PhotoModel photoModel) {
        Utils.showLogger("setPhotoModel");
        this.photoModel = photoModel;
    }

    public LiveData<PhotoModel> getUpdaatedPhotoModel() {
        if (photoModel != null) {
            return photoDao.getUpdatedPhotoModel(photoModel.getPdphotolocalId());
        } else {
            return photoDao.getUpdatedPhotoModel(0);
        }
    }

    private class insertAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao photoDao;
        private WordDao wordDao;

        insertAsyncTask(PhotoDao photoDao) {
            this.photoDao = photoDao;
            this.wordDao = db.wordDao();
        }

        @Override
        protected Void doInBackground(PhotoModel... params) {
            long newImageID = photoDao.insert(params[0]);
            Utils.showLogger("addingNewItem>>>" + newImageID);
            photoModel.setPdphotolocalId(newImageID);
            newlyInsertedImageID.postValue(newImageID);


            List<WordModel> wordModels = wordDao.getClockedWordsList(projectId);
            for (WordModel wordModel : wordModels) {
                wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoModel.getPdphotolocalId());
                photoModel.setWordAdded(true);

                if (wordModel.getType().equals("1") && wordModel.getValue() != null)
                    wordModel.addOrUpdateInputField(newImageID + "", wordModel.getValue());


            }
            photoDao.update(photoModel);
            Utils.showLogger("insert updateAll");
            wordDao.updateAll(wordModels);//insert async task
            return null;
        }
    }

    private class duplicateInsertAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao photoDao;
        private WordDao wordDao;
        private RecordAudioDao recordAudioDao;
        private DefectsDao defectsDao;
        private PdFlawFLagListDao pdFlawFLagListDao;
        private long photoId;

        duplicateInsertAsyncTask(PhotoDao photoDao, long photoId) {
            this.photoDao = photoDao;
            this.wordDao = db.wordDao();
            this.recordAudioDao = db.recordAudioDao();
            this.defectsDao = db.defectsDao();
            this.pdFlawFLagListDao = db.pdFlawFLagDao();
            this.photoId = photoId;
        }

        @Override
        protected Void doInBackground(PhotoModel... params) {
            photoModel.setBrushImageAdded(true);//done by billal
            Utils.showLogger("adding new brush true");
            photoModel.setPdphotolocalId(photoDao.insert(params[0]));
            List<WordModel> wordModelList = wordDao.getWordsListIncludesPhotoId("%," + photoId + "%", projectId);
            Gson g = new Gson();

            for (WordModel wordModel : wordModelList) {
                if (wordModel != null && wordModel.getType().equals("1")) {
                    String oldContentStr = wordModel.getOpen_field_content();

                    try {
                        Utils.showLogger("oldContentIS>>" + oldContentStr);
                        WordContentModel wordModelClass = g.fromJson(oldContentStr, WordContentModel.class);
                        if (wordModelClass == null)
                            wordModelClass = new WordContentModel();

                        ImageId_VS_Input oldObj = wordModelClass.findByImageId(photoId + "", wordModel.getName());

                        if (oldObj == null)
                            continue;


                        Utils.showLogger("addingNew>>" + photoModel.getPdphotolocalId());
                        wordModelClass.getInputsList().add(new ImageId_VS_Input(photoModel.getPdphotolocalId() + "", oldObj.getInputFields(), wordModel.getName()));
                        wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoModel.getPdphotolocalId());
                        wordModel.setOpen_field_content(g.toJson(wordModelClass));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showLogger("ErrorWhilePar" + e.getMessage());
                    }
                } else {
                    wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoModel.getPdphotolocalId());

                }
            }
            if (wordModelList.size() > 0) {
                photoModel.setWordAdded(true);

                Utils.showLogger("setIdWordsAddedTrue");
            } else {
                Utils.showLogger("setIdWordsAddedFalse");
            }

            Utils.showLogger("duplicate updateAll");
            wordDao.updateAll(wordModelList);//duplicate insert task
//            List<WordModel> wordModels = ProjectsDatabase.getDatabase(applicationCtx).wordDao().getFavoriteWordsSimpleList(projectId);
//            if (wordModels != null && sharedPrefsManager.getBooleanValue(AppConstantsManager.IS_OPEN_FIELD_KEYWORD_CLOCKED, false)) {
//                for (int i = 0; i < wordModels.size(); i++) {
//
//                    if (wordModels.get(i).getType() != null && wordModels.get(i).getType().equals("1")) {
//                        WordModel wordModel = wordModels.get(i);
//                        if (wordModels.get(i).getOpen_field_content() != null && !wordModels.get(i).getOpen_field_content().contains(String.valueOf(photoModel.getPdphotolocalId()))) {
//
//                        } else {
//
//                            wordModel.setPhotoIds("," + photoModel.getPdphotolocalId() + "");
//                            if (!wordModel.getOpen_field_content().equals(""))
//                                wordModel.setOpen_field_content(wordModel.getOpen_field_content() + "," + photoModel.getPdphotolocalId() + "##" + wordModel.getValue()
//                                        .toString());
//                            else
//                                wordModel.setOpen_field_content(photoModel.getPdphotolocalId() + "##" + wordModel.getValue()
//                                        .toString());
//                            wordModel.setUseCount(wordModel.getUseCount() + 1);
//                            wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoModel.getPdphotolocalId());
//                            ProjectsDatabase.getDatabase(applicationCtx).wordDao().update(wordModel);
//                        }
//                    }
//                }
//
//            }


            List<RecordAudioModel> recordAudioModels = recordAudioDao.getRecordingsToDuplicate(photoId);
            for (RecordAudioModel recordAudioModel : recordAudioModels) {
                recordAudioModel.setPhotoId(photoModel.getPdphotolocalId());
                recordAudioModel.setRecordId(0);
                recordAudioDao.insert(recordAudioModel);
                photoModel.setRecordingAdded(true);

            }

            DefectsModel defectsModel = defectsDao.getDefectsOBJ(photoModel.getProjectId(), photoModel.getLocal_flaw_id());

            if (defectsModel != null && defectsModel.getDefectLocalId() != 0) {
                Utils.showLogger("SavePhotoesRepository228>>");
                photoModel.setLocal_flaw_id(defectsDao.insert(defectsModel) + "");
                photoModel.setDefectAdded(true);
            }

            Pdflawflag pdflawflag = pdFlawFLagListDao.getFlawFlagOBJExistWithPlanId(photoModel.getProjectId(), photoModel.getPlan_id(), photoId + "");

            if (pdflawflag != null && pdflawflag.getXcoord() != null) {
                pdflawflag.setLocalPdflawflagId(0);
                pdflawflag.setLocal_photo_id(photoModel.getPdphotolocalId() + "");

                pdFlawFLagListDao.insert(pdflawflag);
                photoModel.setPlanAdded(true);
                Utils.showLogger("localPhotoID>>" + pdflawflag.getLocal_photo_id());
            } else
                Utils.showLogger("No coordintates values found");
            photoModel.setPhotoSynced(false);//for new duplicate imgs tatus is false




            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

            Utils.showLogger("myCurrentTime>>>>"+currentTime);

            photoModel.setPhotoTime(currentTime);
            Utils.showLogger("SavePhotosRepository=>setPhotoUploadStatus");
            photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);//logged
            photoDao.update(photoModel);
            return null;
        }
    }

    public class updateOriginalPhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        private long photoId;

        updateOriginalPhotoAsyncTask(long photoId) {
            this.photoId = photoId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            PhotoModel photoModel = photoDao.getPhotoModel(photoId);
           photoModel.setBrushImageAdded(false);//brush added 2
            Utils.showLogger("syncStatus>>>"+photoModel.isPhotoSynced());
            photoDao.update(photoModel);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao photoDao;

        updateAsyncTask(PhotoDao photoDao) {
            this.photoDao = photoDao;
        }

        @Override
        protected Void doInBackground(PhotoModel... params) {
            photoDao.update(params[0]);
            return null;
        }
    }

    private class getPhotoModelAsyncTask extends AsyncTask<Long, Void, Void> {
        private PhotoDao photoDao;

        getPhotoModelAsyncTask(PhotoDao photoDao) {
            this.photoDao = photoDao;
        }

        @Override
        protected Void doInBackground(Long... params) {
            Utils.showLogger("getPhotoModelAsyncTask setPhotoModel");
            photoModel = photoDao.getPhotoModel(params[0]);
            return null;
        }
    }

    public void allPhotosOfProject(Context context, String projectId) {
        new GetAllPhotosProjectAsyncTask(context).execute(projectId);
    }

    private class GetAllPhotosProjectAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao photoDao;

        GetAllPhotosProjectAsyncTask(Context context) {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(context);
            this.photoDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(String... params) {
            List<PhotoModel> photosList = photoDao.getPhotosList(params[0]);
            return null;
        }
    }

}
