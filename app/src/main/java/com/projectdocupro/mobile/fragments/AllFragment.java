package com.projectdocupro.mobile.fragments;

import static android.content.Context.RECEIVER_EXPORTED;
import static com.projectdocupro.mobile.managers.AppConstantsManager.AUTO_LOAD_LAST_PROJECT_DEFAULT_VALUE;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.HomeActivity;
import com.projectdocupro.mobile.activities.ProjectDetailActivity;
import com.projectdocupro.mobile.adapters.ProjectsRecyclerAdapter;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.interfaces.ICacheImagesInDB;
import com.projectdocupro.mobile.interfaces.IDefectSyncTaskComplete;
import com.projectdocupro.mobile.interfaces.IGetProjectsList;
import com.projectdocupro.mobile.interfaces.IPhotosSyncTaskComplete;
import com.projectdocupro.mobile.interfaces.ProjectsListItemClickListener;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.ProjectRepository;
import com.projectdocupro.mobile.service.SyncLocalPhotosService;
import com.projectdocupro.mobile.service.syncLocalDefetcsDataService;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AllFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AllFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllFragment extends Fragment implements ProjectsListItemClickListener, IPhotosSyncTaskComplete, IDefectSyncTaskComplete, IGetProjectsList {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private RecyclerView recyclerView;

    private EditText search;

    private TextView closeSearch;

    private SwipeRefreshLayout swipeToRefresh;

    ProjectRepository projectRepository;
    public ProjectsRecyclerAdapter adapter;

    List<ProjectModel> projectModelsList;
    private View progress_bar_view;

    private String imagePath;
    ICacheImagesInDB iCacheImagesInDB;
    private Dialog customDialog;
    private SharedPrefsManager sharedPrefsManager;
    boolean isLoadProjectDetail;

    private BroadcastReceiver updateProjectData = null;
    private BroadcastReceiver AddNewProject = null;
    private boolean isAPIAlreadyCalled;
    public static final String BR_ACTION_ADD_NEW_PROJECT = "br_add_new_project";
    List<ProjectModel> projectModelAdapterList;
    private Bitmap planBitmap;
    boolean isAllPhotoSyncCalled;

    RelativeLayout pbar;

    ContentLoadingProgressBar progressBar;

    public AllFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllFragment newInstance(String param1, String param2) {
        AllFragment fragment = new AllFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.showLogger("AllFragment onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recyclerViewSetting();
        Utils.showLogger2("onConfigChange");
//        adapter = new ProjectsRecyclerAdapter(projectModelAdapterList, true, AllFragment.this);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

//        recyclerView.setAdapter(adapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all, container, false);

        bindView(view);

//        checkTheDeviceResolution();
        recyclerViewSetting();
        swipeToRefreshSetup();
        adapter = new ProjectsRecyclerAdapter(new ArrayList<>(), true, AllFragment.this);
        Utils.showLogger2("setadapter");
        recyclerView.setAdapter(adapter);

        sharedPrefsManager = new SharedPrefsManager(getActivity());

//        populateData();
//        projectRepository = new ProjectRepository(getContext(),false);
//        projectRepository.deleteAllPlans();
        projectRepository = new ProjectRepository(getContext(), this);

        projectRepository.getIsLoadingComplete().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    hideProgressbar();
                } else {
                    showProgressbar();
                }

            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(getActivity());
                return false;
            }
        });

        new LoadProjectsAsyncTask().execute();
//        new GetAllProjectsPhotosForSyncAsyncTask().execute();


       /* if (ProjectNavigator.wlanIsConnected(getActivity()) || ProjectNavigator.mobileNetworkIsConnected(getActivity())) {
            isAPIAlreadyCalled = true;
            projectRepository.callGetListAPI(getContext());
        } else {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.server_unreachable_message), Toast.LENGTH_SHORT).show();
        }

        Observer observer = new Observer<List<ProjectModel>>() {
            @Override
            public void onChanged(List<ProjectModel> projectModels) {
                if (projectModels == null || projectModels.size() == 0) {
                    if (ProjectNavigator.wlanIsConnected(getActivity()) || ProjectNavigator.mobileNetworkIsConnected(getActivity())) {
                        if (!isAPIAlreadyCalled)
                            projectRepository.callGetListAPI(getContext());

                    } else {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.server_unreachable_message), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    if (iCacheImagesInDB != null && projectRepository != null && projectModels.size() > 0) {
                        iCacheImagesInDB.cacheImagesInDB(projectModels);
                    }
                    Collections.sort(projectModels, (projectModel, t1) -> {
                        //2019-07-24 08:51:04 Date Format
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.GERMANY);
                        Date firstDate = new Date(), secondDate = new Date();
                        try {
                            firstDate = simpleDateFormat.parse(projectModel.getLastupdated());
                            secondDate = simpleDateFormat.parse(t1.getLastupdated());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return secondDate.compareTo(firstDate);
                    });
                    adapter.setData(projectModels);

                    if (sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PROJECT, false) && !sharedPrefsManager.getLastProjectId(getActivity()).equals("") && !isLoadProjectDetail) {

                        for (int i = 0; i < projectModels.size(); i++) {
                            if (projectModels.get(i).getProjectid().equals(sharedPrefsManager.getLastProjectId(getActivity()))) {

                                startActivity(new Intent(getContext(), ProjectDetailActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(getActivity())));
                                isLoadProjectDetail = true;
                                break;
                            }
                        }
                    }
                }
//                if(projectModels.size()>0)


            }

        };
        projectRepository.getAllProjects().observe(getActivity(), observer);*/
/*
        projectRepository.getAllProjects().observe(this, projectModels -> {
            if (projectModels == null || projectModels.size() == 0) {
                projectRepository.callGetListAPI(getContext());

            } else {
                if (iCacheImagesInDB != null && projectRepository != null && projectModels.size() > 0) {
                    iCacheImagesInDB.cacheImagesInDB(projectModels);
                }
                Collections.sort(projectModels, (projectModel, t1) -> {
                    //2019-07-24 08:51:04 Date Format
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.GERMANY);
                    Date firstDate = new Date(), secondDate = new Date();
                    try {
                        firstDate = simpleDateFormat.parse(projectModel.getLastupdated());
                        secondDate = simpleDateFormat.parse(t1.getLastupdated());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return secondDate.compareTo(firstDate);
                });
                adapter.setData(projectModels);

                if (sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PROJECT, false) && !sharedPrefsManager.getLastProjectId(getActivity()).equals("") && !isLoadProjectDetail) {

                    for (int i = 0; i < projectModels.size(); i++) {
                        if (projectModels.get(i).getProjectid().equals(sharedPrefsManager.getLastProjectId(getActivity()))) {

                            startActivity(new Intent(getContext(), ProjectDetailActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(getActivity())));
                            isLoadProjectDetail = true;
                            break;
                        }
                    }
                }
            }
//            projectRepository.getAllProjects().removeObservers(this);
        });
*/


//        search.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
////                applySearch(v.getText().toString());
//                hideKeyboard(getActivity());
//                return true;
//            }
//            return false;
//        });

        search.setSelected(false);

        search.setOnClickListener(v -> closeSearch.setVisibility(View.VISIBLE));

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null)
                    adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addEvent();

        return view;
    }

    private void swipeToRefreshSetup() {
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                projectRepository.callGetListAPI(getContext());
                swipeToRefresh.setRefreshing(false);
            }
        });
    }


    private void recyclerViewSetting() {

        ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
        int count = projectDocuUtilities.getColumnSpam(getActivity());
        if (count > 1) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), count));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }

    private void checkTheDeviceResolution() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
        } else {
            // In portrait
        }
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        String toastMsg;
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                toastMsg = "Extra Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                toastMsg = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                toastMsg = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                toastMsg = "Small screen";
                break;
            default:
                toastMsg = "Screen size is neither large, normal or small";
        }
        Toast.makeText(getContext(), toastMsg, Toast.LENGTH_LONG).show();

    }


    int position = 0;
    private View mCloseSearch;

    private void addEvent() {
        updateProjectData = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (getActivity() != null && adapter != null) {
                        String projectId = intent.getExtras().getString(SyncLocalPhotosService.PROJECT_ID);

                        if (intent.getAction().equals(SyncLocalPhotosService.BR_ACTION_UPDATE_PROJECT_LIST)) {
                            updateProjectLoadingStatus(projectId, false);
//                           new Handler().postDelayed(new Runnable() {
//                               @Override
//                               public void run() {

//                               }
//                           },100);

//                            if (intent.hasExtra(SyncLocalPhotosService.PROJECT_ID)) {
//                                String projectId = intent.getExtras().getString(SyncLocalPhotosService.PROJECT_ID);
//                                new UpdateProjectAsyncTask().execute(projectId);
//                            }
                        }
                    }
                } catch (Exception e) {
                }

            }
        };


        AddNewProject = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {

                    if (intent.getAction().equals(BR_ACTION_ADD_NEW_PROJECT)) {
                        new LoadProjectsAsyncTask().execute();
                    }
                } catch (Exception e) {
                }

            }
        };
    }

    public void updateProjectLoadingStatus(String projectId, boolean isLoading) {
        Utils.showLogger2("updateProjectLoadingStatus");

        if (projectId != null && !projectId.equals("") && adapter != null) {
            for (int i = 0; i < adapter.getProjectsData().size(); i++) {
                if (projectId.equals(adapter.getProjectsData().get(i).getProjectid())) {
                    if (isLoading) {
                        adapter.getProjectsData().get(i).setLastUpdatedProjectStatus(LocalPhotosRepository.UPLOADING_PHOTO);

                    } else {
                        adapter.getProjectsData().get(i).setLastUpdatedProjectStatus(LocalPhotosRepository.SYNCED_PHOTO);

                    }
                    position = i;
                    break;
                }
            }
            new UpdateProjectLoadingStatusAsyncTask(isLoading).execute(projectId);
            adapter.notifyItemChanged(position);
            Utils.showLogger2("onItemsUpdate");
        } else
            adapter.notifyDataSetChanged();

    }


    private void applySearch(String query) {
        if (!query.isEmpty()) {
            LiveData<List<ProjectModel>> searchResults = projectRepository.getSearchResults("%" + query + "%");
            searchResults.observe(AllFragment.this, new Observer<List<ProjectModel>>() {
                @Override
                public void onChanged(List<ProjectModel> projectModels) {
//                                adapter = new ProjectsRecyclerAdapter(projectModels, AllFragment.this);
//                                recyclerView.setAdapter(adapter);
                    adapter.setData(projectModels);
                    searchResults.removeObserver(this);
                }
            });
        } else {
            onCloseSearch();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAllFragmentInteraction(uri);
        }
    }

    private void onCloseSearch() {

        search.setText("");
        if (adapter != null)
            adapter.getFilter().filter("");

        hideKeyboard(getActivity());

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        iCacheImagesInDB = (HomeActivity) context;
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
    public void onListItemClick(ProjectModel projectModel, boolean isMarkFavourite, ProjectsRecyclerAdapter adapter) {


//        projectRepository.getAllProjects().removeObservers(getActivity());
        if (isMarkFavourite) {
            if (projectModel.isFavorite()) {
                updateProjectLoadingStatus(projectModel.getProjectid(), false);
            } else {
                updateProjectLoadingStatus(projectModel.getProjectid(), true);
            }
            if (getActivity() instanceof HomeActivity)
                ((HomeActivity) getActivity()).onListItemClick(projectModel, isMarkFavourite, adapter);
        } else {
            if (projectModel.isFavorite() && projectModel.getSyncStatus() != null && projectModel.getSyncStatus().equals(LocalPhotosRepository.SYNCED_PHOTO)) {
//                new updateProjectAsyncTask(getActivity()).execute(projectModel);
                startActivity(new Intent(getContext(), ProjectDetailActivity.class).putExtra("projectId", projectModel.getProjectid()));
            } else {
                if (getActivity() instanceof HomeActivity) {
                    Utils.showLogger("show warning not synch");

                    ((HomeActivity) getActivity()).onListItemClick(projectModel, isMarkFavourite, adapter);
                }
            }
        }

    }

    @Override
    public void onSyncActionClick(ProjectModel projectModel) {
            Utils.showLogger2("onSyncActionClick");
//        if (getActivity() != null && adapter != null) {
//            for (int i = 0; i < adapter.getProjectsData().size(); i++) {
//                if (adapter.getProjectsData().get(i).getProjectid().equals(projectModel.getProjectid())) {
//                    adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.UPLOADING_PHOTO);
//                    adapter.notifyDataSetChanged();
//                    break;
//                }
//            }
//        }
        if (adapter != null)
            adapter.notifyDataSetChanged();
        if (projectModel != null && projectModel.getProjectid() != null && !projectModel.getProjectid().equals("")) {
            new SyncAllPhotosOfProjectAsyncTask().execute(projectModel.getProjectid());//sync project with images
            new SyncAllDefectsWithoutPhotoAgainstProjectAsyncTask().execute(projectModel.getProjectid());//sync defects without images
        }


    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String projectId = resultData.getString(syncLocalDefetcsDataService.PROJECT_ID, "");
        new SyncAllPhotosOfProjectAsyncTask().execute(projectId);

    }

    @Override
    public void onReceiveDefectResult(int resultCode, Bundle resultData) {

    }

    @Override
    public void onLoadProjectList(List<ProjectModel> projectModels) {
        Utils.showLogger("onLoadProjectList>>>>>>>>>>>>>>>>>>>>");
        if (projectModels != null) {
            if (iCacheImagesInDB != null && projectRepository != null && projectModels.size() > 0) {
                iCacheImagesInDB.cacheImagesInDB(projectModels);
            }
        /*    Collections.sort(projectModels, (projectModel, t1) -> {
                //2019-07-24 08:51:04 Date Format
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.GERMANY);
                Date firstDate = new Date(), secondDate = new Date();
                try {
                    firstDate = simpleDateFormat.parse(projectModel.getLastupdated());
                    secondDate = simpleDateFormat.parse(t1.getLastupdated());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return secondDate.compareTo(firstDate);
            });*/
            adapter.setData(projectModels);

            HomeActivity homeActivity = (HomeActivity) getActivity();
            homeActivity.allProjectsUpdates.postValue(projectModels);

//            for (int i = 0; i < projectModels.size(); i++) {
//
//                if(!projectModels.get(i).getProjectid().equals("44"))
//                onListItemClick(projectModels.get(i),true,adapter);
//                if(i==5)
//                    break;
//            }
            if (sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PROJECT, AUTO_LOAD_LAST_PROJECT_DEFAULT_VALUE) && !sharedPrefsManager.getLastProjectId(getActivity()).equals("") && !isLoadProjectDetail) {

                for (int i = 0; i < projectModels.size(); i++) {
                    if (projectModels.get(i).getProjectid().equals(sharedPrefsManager.getLastProjectId(getActivity()))) {

                        startActivity(new Intent(getContext(), ProjectDetailActivity.class).putExtra("projectId", sharedPrefsManager.getLastProjectId(getActivity())));
                        isLoadProjectDetail = true;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onFailure() {

    }

    private void bindView(View bindSource) {
        pbar = bindSource.findViewById(R.id.rl_pb_parent);

        progressBar = bindSource.findViewById(R.id.progressBar);
        recyclerView = bindSource.findViewById(R.id.all_projects_rv);
        search = bindSource.findViewById(R.id.search_bar);
        closeSearch = bindSource.findViewById(R.id.close_search);
        swipeToRefresh = bindSource.findViewById(R.id.swipeToRefresh);
        progress_bar_view = bindSource.findViewById(R.id.progress_bar_view);
        mCloseSearch = bindSource.findViewById(R.id.close_search);
        mCloseSearch.setOnClickListener(v -> {
            onCloseSearch();
        });
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
        void onAllFragmentInteraction(Uri uri);
    }


    private static class updateProjectAsyncTask extends AsyncTask<ProjectModel, Void, Void> {
        private ProjectsDatabase database;

        updateProjectAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected Void doInBackground(final ProjectModel... params) {
            database.projectDao().update(params[0]);
            return null;
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void showProgressbar() {

        pbar.setVisibility(View.VISIBLE);
        progressBar.show();

    }

    private void hideProgressbar() {

        pbar.setVisibility(View.GONE);

    }

    private class SyncAllPhotosOfProjectAsyncTask extends AsyncTask<String, Void, Void> {
        private DefectsDao mAsyncTaskDao;
        private PhotoDao photoDao;
        PhotoModel photoModel;

        SyncAllPhotosOfProjectAsyncTask() {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getActivity()).defectsDao();
            photoDao = ProjectsDatabase.getDatabase(getActivity()).photoDao();
        }

        @Override
        protected Void doInBackground(final String... params) {
            //  mAsyncTaskDao.update(params[0]);

            Utils.showLogger("getUnSyncLocalPhotosList");

            List<PhotoModel> photoModelList = photoDao.getUnSyncLocalPhotosList(params[0]);
            if (photoModelList != null && photoModelList.size() > 0) {
//                for (int i = 0; i < photoModelList.size(); i++) {
//                    PhotoModel photoModel = photoModelList.get(i);
//                    photoModel.setUserSelectedStatus(false);
//                    photoModel.setPhotoSynced(false);
//                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
//                    photoDao.update(photoModel);
//                }
                photoModel = photoModelList.get(0);
                if (photoModel != null && photoModel.getFailedCount() < 6)
                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                else
                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UN_SYNC_PHOTO);


                photoModel.setUserSelectedStatus(false);
                photoModel.setPhotoSynced(false);
                photoDao.update(photoModel);


            } else {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Utils.showLogger2("onPostExecute");

            if (photoModel != null && photoModel.getFailedCount() < 6) {
                SyncLocalPhotosService.enqueueWork(getActivity(), AllFragment.this::onReceiveResult, photoModel.getProjectId(), true);
            } else {
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }


        }
    }

    private class UpdateProjectLoadingStatusAsyncTask extends AsyncTask<String, Void, Void> {
        private ProjectDao projectDao;
        String projectId = "";
        boolean isLoading;

        UpdateProjectLoadingStatusAsyncTask(boolean isLoading) {
            projectDao = ProjectsDatabase.getDatabase(getActivity()).projectDao();
            isLoading = isLoading;
        }

        @Override
        protected Void doInBackground(final String... params) {
            //  mAsyncTaskDao.update(params[0]);
            projectId = params[0];
            ProjectModel projectModel = projectDao.getProjectOBJ(params[0]);
            if (isLoading)
                projectModel.setLastUpdatedProjectStatus(LocalPhotosRepository.UPLOADING_PHOTO);
            else
                projectModel.setLastUpdatedProjectStatus(LocalPhotosRepository.SYNCED_PHOTO);

            projectDao.update(projectModel);

            return null;
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        if (!search.getText().toString().isEmpty()) {
            search.setText("");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(updateProjectData, new IntentFilter(SyncLocalPhotosService.BR_ACTION_UPDATE_PROJECT_LIST), Context.RECEIVER_EXPORTED);

            getActivity().registerReceiver(AddNewProject, new IntentFilter(BR_ACTION_ADD_NEW_PROJECT), Context.RECEIVER_EXPORTED);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (updateProjectData != null) {
            getActivity().unregisterReceiver(updateProjectData);
            updateProjectData = null;
        }
        if (AddNewProject != null) {
            getActivity().unregisterReceiver(AddNewProject);
            AddNewProject = null;
        }
    }


    private class SyncAllDefectsWithoutPhotoAgainstProjectAsyncTask extends AsyncTask<String, Void, Void> {
        private DefectsDao mAsyncTaskDao;
        private PhotoDao photoDao;


        DefectsModel photoModel = null;

        SyncAllDefectsWithoutPhotoAgainstProjectAsyncTask() {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getActivity()).defectsDao();
        }

        @Override
        protected Void doInBackground(final String... params) {
            //  mAsyncTaskDao.update(params[0]);
            List<DefectsModel> photoModelList = mAsyncTaskDao.getUnSyncedDefectList(params[0]);
            if (photoModelList != null && photoModelList.size() > 0) {
//                for (int i = 0; i < photoModelList.size(); i++) {
//                    PhotoModel photoModel = photoModelList.get(i);
//                    photoModel.setUserSelectedStatus(false);
//                    photoModel.setPhotoSynced(false);
//                    photoModel.setPhotoUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
//                    photoDao.update(photoModel);
//                }
                photoModel = photoModelList.get(0);
                if (photoModel != null) {
                    photoModel.setUserSelectedStatus(false);
                    photoModel.setSynced(false);
                    photoModel.setStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                    mAsyncTaskDao.update(photoModel);
                }


            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (photoModel != null && photoModel.getProjectId() != null)
                syncLocalDefetcsDataService.enqueueWork(getActivity(), AllFragment.this, photoModel);

        }
    }

    private class LoadProjectsAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProjectDao mAsyncTaskDao;
        List<ProjectModel> projectModelList = null;

        LoadProjectsAsyncTask() {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getActivity()).projectDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // projectModelList = mAsyncTaskDao.getAllProjectsList();


            projectModelList = new ArrayList<>();
            projectModelList.addAll(mAsyncTaskDao.getAllActive());

            projectModelList.addAll(mAsyncTaskDao.getAllInActive());

            if (projectModelList.size() == 0)
                projectModelList.addAll(mAsyncTaskDao.getAllItems());

            for (ProjectModel projectModel : projectModelList) {

                Utils.showLogger(projectModel.getProject_name() + "::" + projectModel.getExtra1());
            }


            projectModelAdapterList = projectModelList;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (ProjectNavigator.wlanIsConnected(getActivity()) || ProjectNavigator.mobileNetworkIsConnected(getActivity())) {

                projectRepository.callGetListAPI(getContext());
            } else {
                if (projectModelList != null && projectModelList.size() > 0)
                    onLoadProjectList(projectModelList);
            }

//            callGetPlanImageAPI(getActivity(),"89");
        }
    }


/*
    private void callGetPlanImageAPI(Context context, String fileId) {
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getPlanImageWithSize(authToken, Utils.DEVICE_ID, fileId, "xl");

        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        if (bmp != null)
                            writeResponseBodyToDisk(bmp, "44");
//                        File imgFile = new File(imagePath);
//
//                        if (imgFile.exists()) {
//
//                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                             Log.d("List", "Empty response");
//                        }
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
*/


    private void writeResponseBodyToDisk(Bitmap planBitmap, String projectId) {

        if (getActivity() == null)
            return;
        // todo change the file location/name according to your needs
        File dir = getActivity().getExternalFilesDir("/projectDocu/project_plans_" + projectId);
        if (dir == null) {
            dir = getActivity().getFilesDir();
        }
//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_plans_" + projectId);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
        File photo = new File(dir, "Download_" + new Date().getTime() + ".jpg");

        imagePath = photo.getAbsolutePath();
        FileOutputStream planFileOutputStream = null;

        try {
            planFileOutputStream = new FileOutputStream(photo);

            System.gc();

            Thread.yield();

            planBitmap.compress(Bitmap.CompressFormat.JPEG, 100, planFileOutputStream);
            //System.out.println("ProductDocuUpdatePlan:"+planFileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Exception ProductDocuUpdatePlan:"+e);
        } finally {
            try {
                if (planFileOutputStream != null) {
                    planFileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                //System.out.println("Exception ProductDocuUpdatePlan:"+e);
            }
        }
    }

    // check unsynced photos against project.

    private class GetAllProjectsPhotosForSyncAsyncTask extends AsyncTask<Void, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        List<ProjectModel> projectModelList = null;
        long unsyncPhotoCount = 0;

        GetAllProjectsPhotosForSyncAsyncTask() {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getActivity()).photoDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            unsyncPhotoCount = mAsyncTaskDao.getUnSyncedPhotoCountAllProject();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String msg = "";
            if (unsyncPhotoCount > 1) {
                msg = unsyncPhotoCount + " photos needs to get synced. Do you want to start syncing process now";
            } else {
                msg = unsyncPhotoCount + " photo needs to get synced. Do you want to start syncing process now";
            }
            if (unsyncPhotoCount > 0)
                showCustomDialogUnSyncPhotosData(getActivity(), getResources().getString(R.string.custom_dialog_title), msg, 2, 0);

        }
    }


    public void showCustomDialogUnSyncPhotosData(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        customDialog = new Dialog(act, R.style.MyDialogTheme);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);
        if (flag == 2) {
            customDialog.setCancelable(false);
        }
        customDialog.setCanceledOnTouchOutside(true);
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

                customDialog.dismiss();
                isAllPhotoSyncCalled = true;
            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (selectedProjectModel.isFavorite()) {
//                        selectedProjectModel.setFavorite(false);
////                        showCustomDialogUnSyncData(this, "Project Docu", "Are you sure you want to unSync project and delete all data against it.", 2, 0);
//                    } else {
//                        selectedProjectModel.setFavorite(true);
////                        new ProjectSyncManager(this, projectModel.getProjectid(), syncTaskComplete, isMarkFavourite);
//                    }
                    customDialog.dismiss();

                }
            });
        }
        customDialog.show();
    }

}
