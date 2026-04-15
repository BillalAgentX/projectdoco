package com.projectdocupro.mobile.fragments;

import static com.projectdocupro.mobile.activities.DefectsActivity.IS_CREATED_MANGEL_KEY;
import static com.projectdocupro.mobile.service.SyncLocalPhotosService.SHOW_RESULT;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.DefectDetailsActivity2;
import com.projectdocupro.mobile.activities.DefectsActivity;
import com.projectdocupro.mobile.adapters.DefectsRecyclerAdapter;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.DefectsListItemClickListener;
import com.projectdocupro.mobile.interfaces.UpdateDefectFilterResults;
import com.projectdocupro.mobile.models.DefectTradeModel;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.receivers.WorkerResultReceiver;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.DefectTradesRepository;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.service.SyncLocalPhotosService;
import com.projectdocupro.mobile.service.syncLocalDefetcsDataService;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.DefectsViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DefectsListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DefectsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DefectsListFragment extends Fragment implements DefectsListItemClickListener, UpdateDefectFilterResults, WorkerResultReceiver.Receiver {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PROJECT_ID = "param1";
    public static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String projectId;
    private String mParam2;
    private BroadcastReceiver updateData = null;
    private OnFragmentInteractionListener mListener;

    public DefectsRecyclerAdapter adapter;
    public DefectsViewModel defectsViewModel;

    public static String BR_ACTION_UPDATE_DEFECT_DATA = "updateDefectData";
    private WorkerResultReceiver mWorkerResultReceiver;
    private String flawIdBr = "";
    private String uploadStatusBr = "";
    private boolean isAutoUploadBr;

    RelativeLayout pbar;

    ContentLoadingProgressBar progressBar;

    public boolean fetchDefectsFromServerAfterSynchingDefects = false;
    private boolean defectsAlreadyLoaded=false;

    public DefectsRecyclerAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(DefectsRecyclerAdapter adapter) {
        this.adapter = adapter;
    }

    HashMap<String, String> defectIdVsImage = new HashMap<>();

    public DefectsViewModel getDefectsViewModel() {
        return defectsViewModel;
    }

    public void setDefectsViewModel(DefectsViewModel defectsViewModel) {
        this.defectsViewModel = defectsViewModel;
    }

    List<DefectsModel> unSyncdefectsQueue = new ArrayList<>();

    private View progress_bar_view;

    private RecyclerView recyclerView;

    public boolean isFilterApplied;
    private boolean isfromPhoto;
    public String photoId;
    private AsyncTask<String, Void, List<DefectsModel>> updatePhotoAsyncTask;

    public DefectsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DefectsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DefectsListFragment newInstance(String param1, String param2) {
        DefectsListFragment fragment = new DefectsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static DefectsListFragment newInstance(String param1, String param2, String photoId) {
        DefectsListFragment fragment = new DefectsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString("photoId", photoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            mParam2 = getArguments().getString(ARG_PARAM2);
            photoId = getArguments().getString("photoId");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_defects_list, container, false);
        pbar = view.findViewById(R.id.rl_pb_parent);

        progressBar = view.findViewById(R.id.progressBar);
        bindView(view);
        recyclerViewSetting();
        defectsViewModel = ViewModelProviders.of(this).get(DefectsViewModel.class);
        defectsViewModel.InitRepo(projectId);
        mWorkerResultReceiver = new WorkerResultReceiver(new Handler());
        mWorkerResultReceiver.setReceiver(this);
        if (ProjectDocuUtilities.isNetworkConnected(getActivity()) || ProjectNavigator.wlanIsConnected(getActivity())) {
            //  defectsViewModel.getDefectsRepository().callGetDefectsAPI(getContext(), projectId);

            DefectsActivity defectsActivity = (DefectsActivity) getActivity();
            defectsActivity.iv_sync_all.callOnClick();
        }


        defectsViewModel.getDefectsRepository().defectPhotoRepository.reloadThePage.observe(this, new Observer<PhotoModel>() {
            @Override
            public void onChanged(PhotoModel photoModel) {
                handleNewImage(photoModel);
                //  loadDefects();
            }
        });

        defectsViewModel.getDefectsRepository().getIsProgressComplete().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    hideProgressbar();
                } else {
                    showProgressbar();
                }
            }
        });

/*        defectsViewModel.getDefectsRepository().defectPhotoRepository.reloadThePage.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });*/

        defectsViewModel.getDefectsRepository().getIsSyncAllDefects().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            syncAllDefects();
                            defectsViewModel.getDefectsRepository().isSyncAllDefects.setValue(false);
                        }
                    });

                }
            }
        });


//       loadDefects();

//        defectsViewModel.getDefectsRepository().deleteAllDefects();
        defectsViewModel.getDefectsRepository().loadDefectOnUi.observe(this, new Observer<List<DefectsModel>>() {
            @Override
            public void onChanged(List<DefectsModel> models) {
                updateUI(models);
            }
        });
        defectsViewModel.getDefectsRepository().getAllDefects().observe(this, defectModels -> {
            // update UI
            if (defectModels == null || defectModels.size() == 0) {
                Utils.showLogger("callAPI");

                defectsViewModel.getDefectsRepository().callGetDefectsAPI(getContext(), projectId);

            } else {
                Utils.showLogger("DontcallAPI");
                if(!defectsAlreadyLoaded) {
                    loadDefects("DontcallAPI");
                    defectsAlreadyLoaded = true;
                }
                    // defectsViewModel.getDefectsRepository().getAllDefects().removeObserver(getActivity());
//                if (photoId != null && !photoId.equals("")) {
//                    updatePhotoAsyncTask = new UpdatePhotoAsyncTask(getActivity(), defectModels).execute(photoId);
//                } else
//                    updateUI(defectModels);
            }
        });
        defectsViewModel.isStartUploadPhoto.observe(this, aBoolean -> {

            if (aBoolean) {
//                startBackgroundTask(getContext(), mWorkerResultReceiver, projectId, defectsViewModel.isAutoSyncPhoto);
            }

        });

        addEvent();

        return view;
    }

     synchronized  void handleNewImage(PhotoModel photoModel) {
        defectIdVsImage.put(photoModel.getFlaw_id(), photoModel.getPath());
         Utils.showLogger2("recceiving"+photoModel.getLocal_flaw_id());

         if (adapter != null) {
           // adapter.notifyDataSetChanged();
            List<DefectsModel> adapterItems = adapter.getDefectsModels();
            int newImageIndex = -1;
            for (int i = 0; i < adapterItems.size(); i++) {
                if (adapterItems.get(i).getDefectId().equals(photoModel.getFlaw_id())) {
                    newImageIndex = i;
                    break;
                }
            }

            if (newImageIndex != -1)
                adapter.notifyItemChanged(newImageIndex);
        }
    }

    private void recyclerViewSetting() {
        int orientation = getResources().getConfiguration().orientation;
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        /*1- Configuration.SCREENLAYOUT_SIZE_LARGE
         * 2- Configuration.SCREENLAYOUT_SIZE_XLARGE
         * 3- Configuration.SCREENLAYOUT_SIZE_NORMAL*/


        if ((screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE)
                && orientation == Configuration.ORIENTATION_PORTRAIT) {
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 1);
            recyclerView.setLayoutManager(glm);
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 1);
            recyclerView.setLayoutManager(glm);
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 1);
            recyclerView.setLayoutManager(glm);
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 1);
            recyclerView.setLayoutManager(glm);
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager glm = new GridLayoutManager(getActivity(), 1);
                recyclerView.setLayoutManager(glm);
            }
        }


    }


    public void loadDefects(String caller) {
        Utils.showLogger2("loadDefects:"+caller);
        new RetrieveAsyncTask().execute(projectId);
    }

    private void bindView(View bindSource) {
        progress_bar_view = bindSource.findViewById(R.id.progress_bar_view);
        recyclerView = bindSource.findViewById(R.id.defects_rv);


        DefectsActivity defectsActivity = (DefectsActivity) getActivity();
        if (defectsActivity != null) {
            defectsActivity.img_Or_List.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adapter != null)
                        adapter.setShowImages(defectsActivity.img_Or_List.isSelected());
                    defectsActivity.img_Or_List.setSelected(!defectsActivity.img_Or_List.isSelected());
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private class RetrieveAsyncTask extends AsyncTask<String, Void, List<DefectsModel>> {
        private DefectsDao mDefectDao;
        private PhotoDao photoDao;
        List<DefectsModel> defectsModelList = new ArrayList<>();

        RetrieveAsyncTask() {
            mDefectDao = ProjectsDatabase.getDatabase(getActivity()).defectsDao();
            photoDao = ProjectsDatabase.getDatabase(getActivity()).photoDao();
        }


        @Override
        protected List<DefectsModel> doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)

            defectsModelList.addAll(mDefectDao.getDefectsListByRunId(projectId));
//            defectsModelList.addAll(mDefectDao.getDefectsSimpleList(projectId));
            List<DefectsModel> defectsModels = mDefectDao.getDefectsListEmptyRunId(projectId);
            if (defectsModels != null) {
                for (int i = 0; i < defectsModels.size(); i++) {
                    defectsModelList.add(0, defectsModels.get(i));
                }
            }
//            if (photoId != null && !photoId.equals("")) {
//                for (int j = 0; j < defectsModelList.size(); j++) {
//                    List<String> defectTradeModelListt = photoDao.getDefectIdsAttachedWithPhotos(projectId, defectsModelList.get(j).getDefectLocalId() + "", photoId);
//                    Log.d("after_defect_attach", "projectID " + projectId + " Local_flaw_id " + defectsModelList.get(j).getDefectLocalId() + " photoId " + photoId);
//                    if (defectTradeModelListt.size() > 0) {
//                        Log.d("after_defect_attach", "projectID " + projectId + " Local_flaw_id " + defectsModelList.get(j).getDefectLocalId() + " photoId " + photoId);
//                        defectsModelList.get(j).setPhotoAttach(true);
//                    } else {
//                        defectsModelList.get(j).setPhotoAttach(false);
//                    }
//                }
//            }
            return defectsModelList;
        }

        @Override
        protected void onPostExecute(List<DefectsModel> photoModel) {
            super.onPostExecute(photoModel);
            Utils.showLogger2("UpdatePhotoAsyncTask");
            updatePhotoAsyncTask = new UpdatePhotoAsyncTask(getActivity(), defectsModelList).execute(photoId);

      /*      if (getActivity() != null) {
                if (photoId != null && !photoId.equals("")) {
                    Utils.showLogger("imagesAreFetching");
//                   getActivity().runOnUiThread(new Runnable() {
//                       @Override
//                       public void run() {
                    updatePhotoAsyncTask = new UpdatePhotoAsyncTask(getActivity(), defectsModelList).execute(photoId);

//                       }
//                   });
                } else {

                    if (defectsModelList != null && defectsModelList.size() > 0) {
                        //new UpdatePhotoAsyncTask()
                         updateUI(defectsModelList);
                    }
                }
            }*/

        }
    }


    DefectsModel updateObject = null;

    private void addEvent() {


        updateData = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {

                    String action = intent.getAction();

                    Log.i("Receiver", "Broadcast received: " + action);

                    if (action.equals(BR_ACTION_UPDATE_DEFECT_DATA)) {

                        //for single cellupdate

//                        flawIdBr = intent.getExtras().getString("flawId");
//                        uploadStatusBr = intent.getExtras().getString("uploadStatus");
//                        if (intent.hasExtra("isAutoUpload"))
//                            isAutoUploadBr = intent.getExtras().getBoolean("isAutoUpload");
//                        new UpdateDefectCellAsyncTask().execute(flawIdBr);

                        Utils.showLogger2("loadWhileBroadcast");
                        loadDefects("loadWhileBroadcast");
                    }

                    Intent intentt = new Intent(SyncLocalPhotosService.BR_ACTION_UPDATE_PROJECT_LIST);
                    intentt.putExtra(SyncLocalPhotosService.PROJECT_ID, projectId);
                    getActivity().sendBroadcast(intentt);
                } catch (Exception e) {
                }

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(updateData, new IntentFilter(BR_ACTION_UPDATE_DEFECT_DATA), Context.RECEIVER_EXPORTED);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {

                    for (DefectsModel defectsModel : adapter.getDefectsModels()) {
                        if (!defectsModel.isSynced())
                            onSyncIconClick(defectsModel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);


//        if (photoId != null && !photoId.equals("")) {
//            defectsViewModel.getDefectsRepository().getAllDefects().observe(this, defectModels -> {
//                // update UI
//                if (defectModels == null || defectModels.size() == 0) {
//                    defectsViewModel.getDefectsRepository().callGetDefectsAPI(getContext(), projectId);
//                } else {
//                    if (photoId != null && !photoId.equals("")) {
//                        new UpdatePhotoAsyncTask(getActivity(), defectModels).execute(photoId);
//                    } else
//                        updateUI(defectModels);
//                }
//            });
//        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        switch (resultCode) {
            case SHOW_RESULT:
                boolean isAutoSyncPhoto = false;
                if (resultData != null) {
                    if (getActivity() != null && unSyncdefectsQueue != null) {
//                        localPhotosViewModel.isAutoSyncPhoto = resultData.getBoolean(SyncLocalPhotosService.IS_PHOTOS_AUTO_SYNC);
                        if (ProjectDocuUtilities.isNetworkConnected(getActivity()) || ProjectNavigator.wlanIsConnected(getActivity())) {
                            new Handler().postDelayed(() -> {
                                //your code here

                                if (unSyncdefectsQueue != null && unSyncdefectsQueue.size() > 0)
                                    unSyncdefectsQueue.remove(0);
                                else if (unSyncdefectsQueue != null && unSyncdefectsQueue.size() == 0)
                                    unSyncdefectsQueue.clear();
                                if (unSyncdefectsQueue.size() > 0)
                                    startDefectSyncing(unSyncdefectsQueue.get(0));

                            }, 100);
                            //      SyncLocalPhotosService.enqueueWork(getActivity(), null, projectId, true);

                        }
                    }
                }
                break;
        }

    }


    private class UpdatePhotoAsyncTask extends AsyncTask<String, Void, List<DefectsModel>> {
        private PhotoDao mAsyncTaskDao;
        private DefectsDao mDefectDao;
        List<DefectsModel> defectsList = new ArrayList<>();

        boolean isFound;

        UpdatePhotoAsyncTask(Context context, List<DefectsModel> defectsModelList) {
            defectsList = defectsModelList;
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.photoDao();
            mDefectDao = projectsDatabase.defectsDao();

            showProgressbar();
//            defectsList=new ArrayList<>();
        }

        @Override
        protected List<DefectsModel> doInBackground(final String... params) {
            Utils.showLogger2("attachingimages");

            DefectTradesRepository defectTradesRepository = new DefectTradesRepository(getActivity(), projectId);


//            List<String> defectTradeModelListt = mAsyncTaskDao.getDefectIdsAttachedWithPhotos(projectId);

//            if (defectTradeModelListt != null && defectTradeModelListt.size() > 0) {
//
//                for (int i = 0; i < defectTradeModelListt.size(); i++) {
            //FetchPhotoesOfDetefcts
            for (int j = 0; j < defectsList.size(); j++) {

                List<DefectTradeModel> listOfGewerk = defectTradesRepository.getmDefectsTradeDao().getAllDefectTradeWithStatusONModel(projectId, defectsList.get(j).getDefectLocalId() + "");


                defectsList.get(j).setDefectTradeModelList(listOfGewerk);

                List<String> defectTradeModelListt = mAsyncTaskDao.getDefectIdsAttachedWithPhotos(projectId, defectsList.get(j).getDefectLocalId() + "", photoId);
                //                Log.d("after_defect_attach","projectID "+projectId+" Local_flaw_id "+defectsList.get(j).getDefectLocalId()+" photoId "+photoId);


             //   Utils.showLogger2(projectId + defectsList.get(j).getDefectId());
                PhotoModel photoModel = mAsyncTaskDao.getDefectPhotosAndLocalPhotosOBj(projectId, defectsList.get(j).getDefectLocalId() + "");
                if (photoModel != null) {
                   // Utils.showLogger2("imageAttach");
                    defectIdVsImage.put(defectsList.get(j).getDefectId(), photoModel.getPath2());

                }

                if (defectTradeModelListt.size() > 0) {
                    Utils.showLogger2("settingsImg");
//                    Log.d("after_defect_attach","projectID "+projectId+" Local_flaw_id "+defectsList.get(j).getDefectLocalId()+" photoId "+photoId);
                    defectsList.get(j).setPhotoAttach(true);


                    isFound = true;
                } else {
                    defectsList.get(j).setPhotoAttach(false);
                }
            }
//            }
            return defectsList;
        }

        @Override
        protected void onPostExecute(List<DefectsModel> defectsModels) {
            super.onPostExecute(defectsModels);
            hideProgressbar();
//            if(isFound)
//                Toast.makeText(getActivity(), "isFound.", Toast.LENGTH_LONG).show();
            if (getActivity() != null)
                updateUI(defectsModels);
        }
    }

    private void updateUI(List<DefectsModel> defectModels) {
        Utils.showLogger2("updateUI:" + fetchDefectsFromServerAfterSynchingDefects);
//        List<DefectsModel> defectsModelList=defectModels;
        if (adapter == null) {
            adapter = new DefectsRecyclerAdapter(defectModels, this, defectIdVsImage);
//            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setData(defectModels);
            adapter.notifyDataSetChanged();
        }

        if (fetchDefectsFromServerAfterSynchingDefects) {
            boolean isSynchAll = true;
            for (DefectsModel defectsModel : defectModels) {
                if (!defectsModel.getUploadStatus().equals(DefectRepository.SYNCED_PHOTO)) {
                    isSynchAll = false;
                    break;
                }

            }
            Utils.showLogger2("isAllSynch>>" + isSynchAll);
            if (isSynchAll) {
                Utils.showLogger2("allDefectsUploadedNoFetch");
                DefectsActivity defectsActivity = (DefectsActivity) getActivity();
                assert defectsActivity != null;
                defectsActivity.iv_sync_all.callOnClick();
                fetchDefectsFromServerAfterSynchingDefects = false;
            }
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        if (updatePhotoAsyncTask != null)
            updatePhotoAsyncTask.cancel(true);


        if (updateData != null) {
            getActivity().unregisterReceiver(updateData);
            updateData = null;
        }
        super.onDestroy();
    }

    @Override
    public void onListItemClick(DefectsModel defectsModel) {

        if (getActivity() != null) {

            if ((DefectsActivity) getActivity() instanceof DefectsActivity) {
                DefectsActivity defectsActivity = (DefectsActivity) getActivity();
                if (adapter != null) {
                    if (!defectsActivity.img_Or_List.isSelected()) {
                        // DefectsModel defectsModel = defectsModelListt.get(currentObjectPos);
                        Intent intent = new Intent(getActivity(), DefectDetailsActivity2.class);
                        intent.putExtra(ARG_PROJECT_ID, defectsModel.getProjectId());
                        intent.putExtra(DefectsListFragment.ARG_PARAM2, defectsModel.getDefectLocalId() + "");
                        intent.putExtra("flaw_id", defectsModel.getDefectId());
                        intent.putExtra("photoId", photoId);
                        intent.putExtra(IS_CREATED_MANGEL_KEY, defectsActivity.isCreateMangel);
                        if (photoId != null && !photoId.equals("")) {
//                        intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_VIEW);
                            intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_UPDATE);
                        } else {
                            intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_UPDATE);

                        }
                        startActivity(intent);
                    } else
                        ((DefectsActivity) getActivity()).onListItemClick(adapter.getDefectsModels(), defectsModel);

                }
            }
        }

//        Intent intent = new Intent(getActivity(), DefectDetailsActivity.class);
//        intent.putExtra(ARG_PROJECT_ID, defectsModel.getProjectId());
//        intent.putExtra(ARG_PARAM2, defectsModel.getDefectLocalId() + "");
//        intent.putExtra("flaw_id", defectsModel.getDefectId());
//        intent.putExtra("photoId", photoId);
//        if(photoId!=null&&!photoId.equals("")) {
//            intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_VIEW);
//        }else{
//            intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_UPDATE);
//
//        }
//        startActivity(intent);
    }

    @Override
    public void onSyncIconClick(DefectsModel defectsModel) {
        defectsViewModel.isAutoSyncPhoto = false;
        if (defectsModel != null && defectsModel.getDefectName() != null && !defectsModel.getDefectName().equals("")) {

            if (ProjectDocuUtilities.isNetworkConnected(getActivity()) || ProjectNavigator.wlanIsConnected(getActivity())) {

                if (defectsModel != null && defectsModel.getUploadStatus().equals(DefectRepository.SYNCED_PHOTO) && !defectsModel.getDefectId().equalsIgnoreCase("")) {

                } else {
                    if (!defectsModel.getUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO)) {

                        if (defectsModel.getDefectName() != null && !defectsModel.getDefectName().equals("")) {
                            defectsModel.setUploadStatus(DefectRepository.UPLOADING_PHOTO);
                            if (defectsModel != null) {

                                startDefectSyncing(defectsModel);
                            }
                        } else {
//                        Toast.makeText(getActivity(), "Add Defect name before start syncing.", Toast.LENGTH_LONG).show();
                        }
                        //                    new UpdateAsyncTask().execute(defectsModel);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                           defectsViewModel. isStartUploadPhoto.postValue(true);
//                        }
//                    },100);
                    } else {
                    }
                }
            } else {
                // Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getActivity(), getString(R.string.project_defect_msg), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDeleteIconClick(DefectsModel defectsModel) {
        showCustomDialog(getActivity(), getResources().getString(R.string.custom_dialog_title), getResources().getString(R.string.defect_delete_msg), 2, 0, defectsModel);

    }

    public void startDefectSyncing(DefectsModel defectsModelList) {
        syncLocalDefetcsDataService.enqueueWork(getActivity(), mWorkerResultReceiver, defectsModelList);
    }

    @Override
    public void updateDefectResults(List<DefectsModel> defectsModelList) {
        updateUI(defectsModelList);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private void showProgressbar() {

        pbar.setVisibility(View.VISIBLE);
        progressBar.show();

    }

    private void hideProgressbar() {

        pbar.setVisibility(View.GONE);

    }

    private class ReteriveListAsyncTask extends AsyncTask<String, Void, List<DefectsModel>> {
        private DefectsDao mAsyncTaskDao;

        ReteriveListAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getActivity());
            mAsyncTaskDao = projectsDatabase.defectsDao();
        }

        @Override
        protected List<DefectsModel> doInBackground(final String... params) {

            List<DefectsModel> photoModelList = mAsyncTaskDao.getDefectsListtt(params[0]);

            return photoModelList;
        }

        @Override
        protected void onPostExecute(List<DefectsModel> photoModelList) {
            super.onPostExecute(photoModelList);
            if (photoModelList != null) {
                getAdapter().setData(photoModelList);
            }
        }
    }

    public void startBackgroundTask(Context context, WorkerResultReceiver mWorkerResultReceiver, String projectID, boolean isAutoSyncPhotos) {
        SyncLocalPhotosService.enqueueWork(context, mWorkerResultReceiver, projectID, isAutoSyncPhotos);
    }

    private class UpdateAsyncTask extends AsyncTask<DefectsModel, Void, Void> {
        private DefectsDao mAsyncTaskDao;
        private PhotoDao photoDao;

        UpdateAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getActivity());
            mAsyncTaskDao = projectsDatabase.defectsDao();
            photoDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final DefectsModel... params) {
            mAsyncTaskDao.update(params[0]);
            List<PhotoModel> photoModelList = photoDao.getDefectPhotosAndUnSyncLocalPhotosList(projectId, params[0].getDefectLocalId() + "");
            if (photoModelList != null && photoModelList.size() > 0) {
                for (int i = 0; i < photoModelList.size(); i++) {
                    PhotoModel photoModel = photoModelList.get(i);
                    photoModel.setUserSelectedStatus(false);
                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                    photoDao.update(photoModel);
                }
            }

            return null;
        }
    }


    private class UpdateDefectCellAsyncTask extends AsyncTask<String, Void, DefectsModel> {
        private DefectsDao mAsyncTaskDao;
        private PhotoDao photoDao;

        UpdateDefectCellAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getActivity());
            mAsyncTaskDao = projectsDatabase.defectsDao();
        }

        @Override
        protected DefectsModel doInBackground(final String... params) {
            DefectsModel defectsModel = mAsyncTaskDao.getDefectsObjectt(projectId, params[0]);

            return defectsModel;
        }

        @Override
        protected void onPostExecute(DefectsModel defectsModel) {
            super.onPostExecute(defectsModel);
            updateObject = defectsModel;

            if (flawIdBr != null && !flawIdBr.equals("")) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter != null && adapter.getDefectsModels() != null) {
                            for (int i = 0; i < adapter.getDefectsModels().size(); i++) {

                                if (String.valueOf(adapter.getDefectsModels().get(i).getDefectLocalId()).equals(flawIdBr)) {
                                    updateObject.setUploadStatus(uploadStatusBr);
                                    adapter.getDefectsModels().set(i, updateObject);
                                    break;
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                });
            }

            if (updateObject != null && isAutoUploadBr) {

                startDefectSyncing(updateObject);
            }
        }
    }

    public boolean syncAllDefects() {
        Utils.showLogger2("uploadingSynchAllProjects");
        boolean isAllDefectsSync = true;
        if (adapter != null && adapter.getDefectsModels() != null) {
            for (int i = 0; i < adapter.getDefectsModels().size(); i++) {

                if (adapter.getDefectsModels().get(i).getUploadStatus().equals(DefectRepository.UN_SYNC_PHOTO) && !adapter.getDefectsModels().get(i).getDefectName().equals("")) {
                    adapter.getDefectsModels().get(i).setUploadStatus(DefectRepository.UPLOADING_PHOTO);
                    unSyncdefectsQueue.add(adapter.getDefectsModels().get(i));
                }

            }
            adapter.notifyDataSetChanged();
            if (unSyncdefectsQueue.size() > 0) {
                startDefectSyncing(unSyncdefectsQueue.get(0));
                isAllDefectsSync = false;
            }

        }


        return isAllDefectsSync;

    }

    private class DeleteLocalDefectAsyncTask extends AsyncTask<DefectsModel, Void, Void> {
        private DefectsDao mAsyncTaskDao;
        private PhotoDao photoDao;

        DefectsModel defectsModel = null;

        DeleteLocalDefectAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getActivity());
            mAsyncTaskDao = projectsDatabase.defectsDao();
            photoDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(final DefectsModel... params) {
            defectsModel = params[0];
            mAsyncTaskDao.deleteUsingLocalDefectId(params[0].defectLocalId);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            int index=0;
//            if(defectsModel!=null&&adapter!=null){
//                index=adapter.getDefectsModels().indexOf(defectsModel);
//                adapter.getDefectsModels().remove(defectsModel);
//                adapter.notifyItemChanged(index);
////                adapter.notifyDataSetChanged();
//            }
            Utils.showLogger2("loadwhiledelete");
            loadDefects("loadwhiledelete");
        }
    }


    public void showCustomDialog(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag, DefectsModel defectsModel) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        Dialog customDialog = new Dialog(act, R.style.MyDialogTheme);
//        customDialog.setContentView(R.layout.custom_dialog_message_material_notification);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        customDialog.setContentView(R.layout.custom_dialog_message_material);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View productsView = inflater.inflate(
                R.layout.custom_dialog_message_material_notification,
                null);

        customDialog.setContentView(productsView);


        if (flag == 2) {
            customDialog.setCancelable(false);

        }
        customDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.y = 10;

        // set the custom dialog components - text, image and button
        TextView titleTxt = (TextView) customDialog.findViewById(R.id.customDialog_titleText);
        if (!title.equals(""))
            titleTxt.setText(title);
        TextView text = (TextView) customDialog.findViewById(R.id.movie_name);
        text.setText(msgToShow);
        //		if(Navigator.showCustomDialogType ==3)
        //		{
        //			text.setTextSize(getDipValue(8));
        //		}

        Button bt = (Button) customDialog.findViewById(R.id.customDialog_okBtn);
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

//                    Fragment fragment = defectDetailsPagerAdapter.getM1stFragment();
//                    if(fragment!=null){
//                        ((DefectDetailsDatesFragment) fragment).saveDefect();
//
//                    }
                new DeleteLocalDefectAsyncTask().execute(defectsModel);
                customDialog.dismiss();

            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    customDialog.dismiss();
                }
            });

        }
        customDialog.show();
    }


}
