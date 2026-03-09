package com.projectdocupro.mobile.repos;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.OnlinePhotosRecyclerAdapter;
import com.projectdocupro.mobile.dao.OnlinePhotoDao;
import com.projectdocupro.mobile.fragments.OnlinePhotosFragment;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.OnlinePhotoModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.mangel_filters.GroupheadingModel;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.OnlinePhotosViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ONlinePhotoRepository {

    private OnlinePhotoDao mDefectsPhotoDao;
    private LiveData<List<OnlinePhotoModel>> mDefectedPhotos;
    private String imagePath;
    OnlinePhotosRecyclerAdapter photosRecyclerAdapter;
    Context mContext;
    OnlinePhotosViewModel photosViewModel;
    int insertionCount = 0;

    public MediatorLiveData<List<OnlinePhotoModel>> getmSectionLive() {
        return mSectionLive;
    }

    public void setmSectionLive(MediatorLiveData<List<OnlinePhotoModel>> mSectionLive) {
        this.mSectionLive = mSectionLive;
    }

    int photoCacheCount = 0;
    private MediatorLiveData<List<OnlinePhotoModel>> mSectionLive = new MediatorLiveData<>();
    private List<OnlinePhotoModel> defectPhotoModelList;

    public List<OnlinePhotoModel> getDefectPhotoModelList() {
        return defectPhotoModelList;
    }

    public void setDefectPhotoModelList(List<OnlinePhotoModel> defectPhotoModelList) {
        this.defectPhotoModelList = defectPhotoModelList;
    }

    public OnlinePhotoDao getmDefectsPhotoDao() {
        return mDefectsPhotoDao;
    }

    public void setmDefectsPhotoDao(OnlinePhotoDao mDefectsPhotoDao) {
        this.mDefectsPhotoDao = mDefectsPhotoDao;
    }

    public LiveData<List<OnlinePhotoModel>> getmDefectedPhotos() {
        return mDefectedPhotos;
    }

    public void setmDefectedPhotos(LiveData<List<OnlinePhotoModel>> mDefectedPhotos) {
        this.mDefectedPhotos = mDefectedPhotos;
    }

    public OnlinePhotosRecyclerAdapter getPhotosRecyclerAdapter() {
        return photosRecyclerAdapter;
    }

    public void setPhotosRecyclerAdapter(OnlinePhotosRecyclerAdapter photosRecyclerAdapter) {
        this.photosRecyclerAdapter = photosRecyclerAdapter;
    }

    public void initAdapter(String projectId, String flaw_id) {


    }

    public ONlinePhotoRepository(Context context) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.onlinePhotoDao();
        mContext = context;
//        mDefectedPhotos = mDefectsPhotoDao.getDefectPhotoModel(projectId);
    }

    public ONlinePhotoRepository(Context context, OnlinePhotosViewModel onlinePhotosViewModel) {
        ProjectsDatabase db = ProjectsDatabase.getDatabase(context);
        mDefectsPhotoDao = db.onlinePhotoDao();
        mContext = context;
        photosViewModel = onlinePhotosViewModel;
        mDefectedPhotos = new MutableLiveData<>();

        new RetrieveAllAsyncTask(mDefectsPhotoDao).execute();
//        if (mDefectedPhotos.getValue() != null)
//            photosViewModel.photoPaths.setValue(mDefectedPhotos.getValue());
//        else
//            photosViewModel.photoPaths.setValue(onlinePhotoModelList);
//        mDefectedPhotos = mDefectsPhotoDao.getDefectPhotoModel(projectId);

    }


    public void insertALL(List<OnlinePhotoModel> allPlansModel, List<GroupheadingModel> groupheadingModelList) {
        new insertAsyncTask(mDefectsPhotoDao, groupheadingModelList).execute(allPlansModel);
    }

    public void deleteAllROws() {
        new DeleteAsyncTask(mDefectsPhotoDao).execute();
    }

    public void cacheImages(OnlinePhotoModel onlinePhotoModel) {
        new CachePhotoAsyncTask(mDefectsPhotoDao).execute(onlinePhotoModel);
    }


    private static class UpdateAsyncTask extends AsyncTask<OnlinePhotoModel, Void, Void> {
        private OnlinePhotoDao mAsyncTaskDao;

        UpdateAsyncTask(OnlinePhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final OnlinePhotoModel... params) {

            mAsyncTaskDao.update(params[0]);

            return null;
        }
    }

    private class CachePhotoAsyncTask extends AsyncTask<OnlinePhotoModel, Void, Void> {
        private OnlinePhotoDao mAsyncTaskDao;

        CachePhotoAsyncTask(OnlinePhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final OnlinePhotoModel... params) {

            // defectPhotoModelList = getmDefectsPhotoDao().getPhotosList(params[0]);
            OnlinePhotoModel onlinePhotoModelOBJ = mAsyncTaskDao.getOnlinePhotoModelOBJ(photosViewModel.projectId, params[0].getPdphotoid());
            cacheProjectImages(mContext, onlinePhotoModelOBJ);

            return null;
        }
    }

    private class insertAsyncTask extends AsyncTask<List<OnlinePhotoModel>, Void, Void> {
        private OnlinePhotoDao mAsyncTaskDao;
        List<GroupheadingModel> groupheadingModelList;
        insertAsyncTask(OnlinePhotoDao dao, List<GroupheadingModel> groupheadingModelList) {
            mAsyncTaskDao = dao;
            this.groupheadingModelList = groupheadingModelList;
        }

        @Override
        protected Void doInBackground(final List<OnlinePhotoModel>... params) {
            insertionCount = params[0].size();
            photoCacheCount = 0;
            for (int i = 0; i < params[0].size(); i++) {

                OnlinePhotoModel onlinePhotoModel = params[0].get(i);
                String temp[] = onlinePhotoModel.getParams().replace("|", " ").split(" ");

                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < temp.length; j++) {
                    String temp2[] = temp[j].split("=");
                    if (!temp2[0].equals(""))
                        stringBuilder.append(temp2[0] + ",");

                }

//                onlinePhotoModel.setParams(temp);
//                String strComa = onlinePhotoModel.getParams().replace("=1", ",");
                onlinePhotoModel.setParams(stringBuilder.toString());
                List<String> items = Arrays.asList(onlinePhotoModel.getParams().split("\\s*,\\s*"));

                ProjectsDatabase db = ProjectsDatabase.getDatabase(mContext);
                List<WordModel> wordModelList = db.wordDao().getWordsListUsingIds(photosViewModel.projectId, items);

                if (wordModelList != null) {
                    for (int j = 0; j < wordModelList.size(); j++) {
                        WordModel wordModel = wordModelList.get(j);
                        wordModel.setOnlinePhotoIds(onlinePhotoModel.getPdphotoid());
                        wordModel.setPhotoType(LocalPhotosRepository.TYPE_ONLINE_PHOTO);
                        wordModel.setUseCount(1);
                        db.wordDao().update(wordModel);
                    }
                }
                if (onlinePhotoModel.getCreated() != null && !onlinePhotoModel.getCreated().equals("")) {
                    SimpleDateFormat yyyMMddHHmmssFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
                    Date parsedDate = null;
                    try {
                        parsedDate = yyyMMddHHmmssFormat.parse(onlinePhotoModel.getCreated());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                    onlinePhotoModel.setCreated_df(timestamp.getTime());
                }
                onlinePhotoModel.setPhoto_type(LocalPhotosRepository.TYPE_ONLINE_PHOTO);
                mAsyncTaskDao.insert(onlinePhotoModel);
//                cacheImages(params[0].get(i));
            }

            new RetrieveAsyncTask(mDefectsPhotoDao, groupheadingModelList).execute();


            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {
        private OnlinePhotoDao mAsyncTaskDao;

        DeleteAsyncTask(OnlinePhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... params) {

            mAsyncTaskDao.deleteAll();

            return null;
        }
    }

    private class RetrieveAsyncTask extends AsyncTask<String, Void, List<OnlinePhotoModel>> {
        private OnlinePhotoDao mAsyncTaskDao;
        private List<GroupheadingModel> groupheadingModelList;

        RetrieveAsyncTask(OnlinePhotoDao dao, List<GroupheadingModel> groupheadingModelList) {
            mAsyncTaskDao = dao;
            this.groupheadingModelList = groupheadingModelList;
        }

        @Override
        protected List<OnlinePhotoModel> doInBackground(final String... params) {


//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)
            String offset = "";
            if (photosViewModel.currentPage == 0) {
                offset = "0";
            } else {
                offset = String.valueOf(photosViewModel.currentPage * photosViewModel.pageSize);
            }

//            List<OnlinePhotoModel> onlinePhotoModelList =  mAsyncTaskDao.getFilterListViaQuery(getQuery(groupheadingModelList, photosViewModel.projectId));
          //  List<OnlinePhotoModel> onlinePhotoModelList =  mAsyncTaskDao.getAllOnlinePhotos(photosViewModel.projectId);

            List<OnlinePhotoModel> onlinePhotoModelList = mAsyncTaskDao.getOnlinePhotoUsingLimitList(photosViewModel.projectId, offset, String.valueOf(photosViewModel.pageSize));
//            List<OnlinePhotoModel> onlinePhotoModelList = mAsyncTaskDao.getOnlinePhotoUsingLimitList2(photosViewModel.projectId);
            OnlinePhotosFragment.onlinePhotosDBCount = mAsyncTaskDao.getOnlinePhotoDBCount(photosViewModel.projectId);
            return onlinePhotoModelList;
        }

        @Override
        protected void onPostExecute(List<OnlinePhotoModel> onlinePhotoModels) {
            super.onPostExecute(onlinePhotoModels);
            Utils.showLogger("db1 addition size"+onlinePhotoModels.size());
            for(OnlinePhotoModel tempOnlinePhotoModel: onlinePhotoModels) {
//                photosViewModel.photoPaths.getValue().addAll(onlinePhotoModels);

                photosViewModel.photoPaths.getValue().add(tempOnlinePhotoModel);
            }

//            Collections.sort(photosViewModel.photoPaths.getValue(), new Comparator<OnlinePhotoModel>() {
//                @Override
//                public int compare(OnlinePhotoModel lhs, OnlinePhotoModel rhs) {
//                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
//                    return lhs.getCreated_df() > rhs.getCreated_df() ? -1 : (lhs.getCreated_df() < rhs.getCreated_df() ) ? 1 : 0;
//                }
//            });
            Utils.showLogger("online photoes from db1");
            photosViewModel.photoPaths.setValue(photosViewModel.getPhotoPaths().getValue());//setting from local db
        }
    }

    public void retrievePhotosUsingIds(OnlinePhotoDao onlinePhotoDao, String projectId, List<String> stringList) {
        new RetrieveUsingPhotoIdsAsyncTask(onlinePhotoDao, stringList).execute(projectId);
    }

    private class RetrieveAllAsyncTask extends AsyncTask<String, Void, List<OnlinePhotoModel>> {
        private OnlinePhotoDao mAsyncTaskDao;

        RetrieveAllAsyncTask(OnlinePhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<OnlinePhotoModel> doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskD ao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)
            List<OnlinePhotoModel> onlinePhotoModelList = mAsyncTaskDao.getPhotosList(photosViewModel.projectId);

            return onlinePhotoModelList;
        }

        @Override
        protected void onPostExecute(List<OnlinePhotoModel> onlinePhotoModels) {
            super.onPostExecute(onlinePhotoModels);
            if (onlinePhotoModels != null && onlinePhotoModels.size() > 0) {

                photosViewModel.photoPaths.setValue(onlinePhotoModels);// from local db2
                try {


                }
                catch (Exception e){
                    e.printStackTrace();
                }

                photosViewModel.isCached = true;

            } else {
                List<OnlinePhotoModel> onlinePhotoModelList = new ArrayList<>();
                photosViewModel.photoPaths.setValue(onlinePhotoModelList);//local db3

                try {
                //    Utils.showLogger("online photoes from db3"+onlinePhotoModels.size());
                //    Utils.showLogger("online photoes from db3--" + onlinePhotoModels.get(0).getPdPhotoName());

                }
                catch (Exception e){

                }


            }
        }
    }


    private class RetrieveUsingPhotoIdsAsyncTask extends AsyncTask<String, Void, List<OnlinePhotoModel>> {
        private OnlinePhotoDao mAsyncTaskDao;
        List<String> list;

        RetrieveUsingPhotoIdsAsyncTask(OnlinePhotoDao dao, List<String> stringList) {
            mAsyncTaskDao = dao;
            list = stringList;
        }

        @Override
        protected List<OnlinePhotoModel> doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)
            List<OnlinePhotoModel> onlinePhotoModelList = mAsyncTaskDao.getPhotosListUsingPhotoIds(params[0], list);

            return onlinePhotoModelList;
        }

        @Override
        protected void onPostExecute(List<OnlinePhotoModel> onlinePhotoModels) {
            super.onPostExecute(onlinePhotoModels);
            mSectionLive.setValue(onlinePhotoModels);
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String projectId) {
        imagePath = "";
        try {
            // todo change the file location/name according to your needs

//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_defects_" + projectId);

            File dir = mContext.getExternalFilesDir("/projectDocu/project_online_photos" + projectId);
            if (dir == null) {
                dir = mContext.getFilesDir();
            }
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[15000];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(photo);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("A TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }


                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


    public void cacheProjectImages(Context context, OnlinePhotoModel projectModel) {
        callGetPlanImageAPI(context, projectModel, projectModel.getPdphotoid());
    }

    private void callGetPlanImageAPI(Context context, OnlinePhotoModel projectModel, String fileId) {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getDefectPhotosWithSize(authToken, Utils.DEVICE_ID, fileId, "sm");
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        if (writeResponseBodyToDisk(response.body(), projectModel.getProjectId())) {
                            if (imagePath != null && !imagePath.equals("")) {
                                projectModel.setPohotPath(imagePath);
                                projectModel.setPath(imagePath);
                                projectModel.setPhotoCached(true);
                                new UpdateAsyncTask(mDefectsPhotoDao).execute(projectModel);


                                photoCacheCount++;


                            }
//                            Bitmap bitmap  =   BitmapFactory.decodeFile(imagePath);
//                            imageView.setImageBitmap(bitmap);
                        }
                    } else {
                        Log.d("List", "Empty response");
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            Log.d("List", "Not Success : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        Log.d("List", "Not Success : " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }

    private SimpleSQLiteQuery getQuery(List<GroupheadingModel> groupheadingModelList, String projectId) {

        List<String> selectedStatus = new ArrayList<>();
        List<String> selectedArt = new ArrayList<>();
        List<String> selectedGewerk = new ArrayList<>();
        List<String> selectedDeadline = new ArrayList<>();
        List<String> selectedResponsible = new ArrayList<>();
        List<String> selectedCreator = new ArrayList<>();
        List<String> selectedDate = new ArrayList<>();
        List<String> selectedPlan = new ArrayList<>();

        String PREDICATE_PHOTO_TYPE = LocalPhotosRepository.TYPE_ONLINE_PHOTO;
        String PREDICATE_SORTING = "";
        String PREDICATE_DESCRIPTION = "";
        String PREDICATE_DESCRIPTION_SWITCH = "";
        String PREDICATE_KEYWORD = "";
        String PREDICATE_KEYWORD_SWITCH = "";
        String PREDICATE_LOCALIZED_SWITCH = "";
        String PREDICATE_DEADLINE = "";
        String PREDICATE_KEYWORDS = "";
        String PREDICATE_CREATOR = "";
        String PREDICATE_CREATED_DATE = "";
        String PREDICATE_DATE_RANGE = "";

        long date = 0;
        long start_date = 0;
        long end_date = 0;

        String art = "";
        String tag_keyword = "";

        boolean isApplyGewerk = false;
        if (groupheadingModelList != null) {

            for (int i = 0; i < groupheadingModelList.size(); i++) {
                if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_desc))) {
                    if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                        art = groupheadingModelList.get(i).getKeyword();
//                            PREDICATE_DESCRIPTION = " AND description LIKE '%" + art+"%'" ;
                        PREDICATE_DESCRIPTION = " AND description LIKE '%" + art + "%'";
                    }

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_number))) {
                    if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                        tag_keyword = groupheadingModelList.get(i).getKeyword();
                        PREDICATE_KEYWORD = " AND pdphotolocalId  =" + tag_keyword;
                    }

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_deadline))) {
                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedDeadline.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        date = groupheadingModelList.get(i).getStart_date();
                        PREDICATE_DEADLINE = " AND fristdate_df <= " + date;
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_creator))) {
                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedResponsible.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        String inClause = selectedResponsible.toString();
                        inClause = inClause.replace("[", "(");
                        inClause = inClause.replace("]", ")");
                        PREDICATE_CREATOR = " AND pdUserId in " + inClause;

                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_decs))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {

                        PREDICATE_DESCRIPTION_SWITCH = " AND description !='' ";
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_localized_photo))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {

                        PREDICATE_LOCALIZED_SWITCH = " AND plan_id !='' ";
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_keyword))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {

                        PREDICATE_KEYWORD_SWITCH = " AND wordAdded = 1 ";
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_keyword))) {

                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedCreator.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        String inClause = selectedCreator.toString();
                        inClause = inClause.replace("[", "(");
                        inClause = inClause.replace("]", ")");
                        PREDICATE_KEYWORDS = " AND pdphotolocalId in " + inClause;

                    }

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_plans))) {
                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedPlan.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        String inClause = selectedPlan.toString();
                        inClause = inClause.replace("[", "(");
                        inClause = inClause.replace("]", ")");
                        PREDICATE_CREATOR = " AND pdUserId in " + inClause;
//                            "operand": „EQ", "field": "pdplanid", "value": „26"

                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_created_date))) {

//                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
//                            selectedDate.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                    start_date = groupheadingModelList.get(i).getStart_date();
                    end_date = groupheadingModelList.get(i).getEnd_date();

                    if (start_date > 0 && end_date > 0) {
                        PREDICATE_CREATED_DATE = " AND created_df >= " + start_date + " AND created_df <= " + end_date;
                    }
//                        }
                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_sorting))) {

                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedStatus.addAll(groupheadingModelList.get(i).getListChildDataSelected());

//                            String inClause = selectedStatus.toString();
//                            inClause = inClause.replace("[", "(");
//                            inClause = inClause.replace("]", ")");
//                            PREDICATE_SORTING = " AND status in " + inClause;

                        if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("1")) {
                            PREDICATE_SORTING = "  ORDER BY created_df ASC ";

                        } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("2")) {
                            PREDICATE_SORTING = "  ORDER BY created_df DESC ";
                        } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("3")) {
                            PREDICATE_SORTING = "  ORDER BY created_df DESC ";
                        } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("4")) {
                            PREDICATE_SORTING = "  ORDER BY created_df ASC ";
                        }

                    }

                }
            }
        }
        if (PREDICATE_SORTING.equals("")) {
            PREDICATE_SORTING = "  ORDER BY created_df DESC ";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date startDate = new Date();
        Date endDate = new Date();

        try {
            startDate = simpleDateFormat.parse("2017-03-29");
            endDate = simpleDateFormat.parse("2022-03-30");
        } catch (ParseException e) {
            e.printStackTrace();
        }


        String predicateDateRange = "AND created_df >= " + startDate.getTime() + " AND created_df <= " + endDate.getTime();

        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM  onlinePhotoModel WHERE projectId = ? AND photo_type='" + PREDICATE_PHOTO_TYPE + "'" + PREDICATE_KEYWORD + PREDICATE_DESCRIPTION + PREDICATE_KEYWORDS + PREDICATE_CREATOR + PREDICATE_DEADLINE + PREDICATE_CREATED_DATE + PREDICATE_DESCRIPTION_SWITCH + PREDICATE_KEYWORD_SWITCH + PREDICATE_LOCALIZED_SWITCH + predicateDateRange + PREDICATE_SORTING,
                new Object[]{projectId});

        return query;

    }


}
