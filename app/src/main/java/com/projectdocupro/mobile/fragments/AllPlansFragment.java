package com.projectdocupro.mobile.fragments;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.PlansActivity;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.adapters.PlansRecyclerAdapter;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.fragments.add_direction.DefectPlanDirectionFragment;
import com.projectdocupro.mobile.fragments.add_direction.PhotoAddDirectionMainActivity;
import com.projectdocupro.mobile.fragments.add_direction.VIewPlanAddDirectionMainActivity;
import com.projectdocupro.mobile.interfaces.PlansListItemClickListener;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.repos.AllPlansRepository;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.repos.ProjectRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.util.ArrayList;
import java.util.List;


import okio.Utf8;

import static android.app.Activity.RESULT_OK;
import static com.projectdocupro.mobile.activities.PlansActivity.IGNORE_LOADING_LAST_PLAN;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AllPlansFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AllPlansFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllPlansFragment extends Fragment implements PlansListItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PROJECT_ID = "projectId";
    public static final String ARG_FLAW_ID = "flaw_id";
    private static final String ARG_PHOTO_ID = "photoId";
    private static final String ARG_FROM_PHOTO = "param2";
    public static final String ARG_PLAN_ID = "planId";
    public static final String ARG_FROM_DEFECT = "is_from_defect";

    // TODO: Rename and change types of parameters
    private String projectId;
    private String planId;
    private String flawId;
    private long photoId;
    private boolean fromPhoto;

    private OnFragmentInteractionListener mListener;


    private RecyclerView recyclerView;

    private FrameLayout fl_layout_plans;

    private EditText search;

    private TextView closeSearch;

    AllPlansRepository plansRepository;
    ProjectRepository allProjectRepository;
    PdFlawFlagRepository pdFlawFlagRepository;
    PlansRecyclerAdapter adapter;
    private boolean isFromDefect;
    private Pdflawflag flawFlagObj;
    private SharedPrefsManager sharedPrefsManager;
    private boolean isAutoLoadPlan;
    private boolean isAPICalled;
    private boolean ignoreLoadingLastPlan;

    public AllPlansFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param projectId Parameter 1.
     * @param photoId   Parameter 2.
     * @param fromPhoto Parameter 3.
     * @return A new instance of fragment AllFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllPlansFragment newInstance(String projectId, long photoId, boolean fromPhoto) {
        AllPlansFragment fragment = new AllPlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);

        args.putLong(ARG_PHOTO_ID, photoId);
        args.putBoolean(ARG_FROM_PHOTO, fromPhoto);
        fragment.setArguments(args);
        return fragment;
    }

    public static AllPlansFragment newInstance(String projectId, long photoId, boolean fromPhoto, boolean isfromDefect) {
        AllPlansFragment fragment = new AllPlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
//        args.putString(ARG_FLAW_ID, flawId);
        args.putLong(ARG_PHOTO_ID, photoId);
        args.putBoolean(ARG_FROM_PHOTO, fromPhoto);
        args.putBoolean(ARG_FROM_DEFECT, isfromDefect);
        fragment.setArguments(args);
        return fragment;
    }

    public static AllPlansFragment newInstance(String param1, String planID, long photoId, boolean fromPhoto, boolean ignoreLoadingLastPlan) {
        AllPlansFragment fragment = new AllPlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, param1);
        args.putString(ARG_PLAN_ID, planID);
        args.putLong(ARG_PHOTO_ID, photoId);
        args.putBoolean(ARG_FROM_PHOTO, fromPhoto);
        args.putBoolean(IGNORE_LOADING_LAST_PLAN, ignoreLoadingLastPlan);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            planId = getArguments().getString(ARG_PLAN_ID);
            photoId = getArguments().getLong(ARG_PHOTO_ID);
            fromPhoto = getArguments().getBoolean(ARG_FROM_PHOTO);
            flawId = getArguments().getString(ARG_FLAW_ID);
            isFromDefect = getArguments().getBoolean(ARG_FROM_DEFECT);
             ignoreLoadingLastPlan = getArguments().getBoolean(IGNORE_LOADING_LAST_PLAN,false);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_plans, container, false);
        bindView(view);

        sharedPrefsManager = new SharedPrefsManager(getActivity());

//        if (fromPhoto) {
//
//            new GetFlawFlagUsingPhotoId().execute();
//        }
        addEvent();
        recyclerViewSetting();
        plansRepository = new AllPlansRepository(getContext(), projectId);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (isFromDefect) {
            loadPlanRelatedWitDefect();
        } else {
            Observer observer = new Observer<List<PlansModel>>() {
                @Override
                public void onChanged(List<PlansModel> plansModels) {

                    if (plansModels == null || plansModels.size() == 0) {
                        plansRepository.callGetListAPI(getContext(), projectId);

                    } else {

                        adapter = new PlansRecyclerAdapter(plansModels, AllPlansFragment.this);
//                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(adapter);


                        if (!ignoreLoadingLastPlan) {
                            if (((photoId > 0
                                    && sharedPrefsManager.getBooleanValue(AppConstantsManager.AUTO_LOAD_LAST_PLAN, true)
                                    && !sharedPrefsManager.getLastUsedPlanId(getActivity()).equals("") && !isAutoLoadPlan)) || (planId != null && !planId.equals(""))) {

                                for (int i = 0; i < plansModels.size(); i++) {
                                    if (plansModels.get(i).getPlanId().equals(sharedPrefsManager.getLastUsedPlanId(getActivity()))) {
                                        sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);

                                        sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);

                                        startActivity(new Intent(getActivity(), PhotoAddDirectionMainActivity.class)//fixed--
                                                .putExtra("projectId", plansModels.get(i).getProjectId())
                                                .putExtra("photoId", photoId)
                                                .putExtra("planId", sharedPrefsManager.getLastUsedPlanId(getActivity()))
                                                .putExtra("flawFlagObj", flawFlagObj)
                                                .putExtra("planName", plansModels.get(i).getOrigName())
                                                .putExtra("fromPhoto", fromPhoto)
                                                .putExtra("fromPlanScreen", true));
                                        isAutoLoadPlan = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
//                    isAPICalled=true;
//                    if(isAPICalled)
                    plansRepository.getAllProjects().removeObservers(getActivity());
                }
            };
            plansRepository.getAllProjects().observe(getActivity(), observer);
        }

        return view;
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
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(glm);
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 3);
            recyclerView.setLayoutManager(glm);
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(glm);
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(glm);
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
                recyclerView.setLayoutManager(glm);
            }
        }


    }

    public void loadPlanRelatedWitDefect(){

        DefectRepository mDefectRepository = new DefectRepository(getActivity(), projectId);
        new GetAllDefects(mDefectRepository.getmDefectDao()).execute(projectId);

    }

    private void addEvent() {
        updateFlawFlag =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (fromPhoto) {
                    String action = intent.getAction();
                    String planIdd = "";
                    boolean isFinishPlanScreen = false;
                    boolean isPlanAttachToPhoto = false;
                    if (action.equals("updateFlawFlag")) {
                        planId = intent.getExtras().getString(SavePictureActivity.PLAN_ID_KEY);
                        isPlanAttachToPhoto = intent.getExtras().getBoolean(SavePictureActivity.PLAN_ATTACH_TO_PHOTO_KEY);

                    }

                    if (action.equals("updateFlawFlag")) {
                        isFinishPlanScreen = intent.getExtras().getBoolean(SavePictureActivity.FINISH_PLAN_SCREEN_KEY);
                    }
                    if (isFinishPlanScreen) {

                        getActivity().finish();

                    } else {

                        new GetFlawFlagUsingPhotoId().execute(planId);
//                    Toast.makeText(getActivity(),"Update",Toast.LENGTH_SHORT).show();
                        Intent data = new Intent();
                        data.putExtra(SavePictureActivity.PLAN_ATTACH_TO_PHOTO_KEY, isPlanAttachToPhoto);
                        data.putExtra(SavePictureActivity.PLAN_ID_KEY, planId);
                        getActivity().setResult(RESULT_OK, data);
                        getActivity().finish();
                    }
                }
            }
        };

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

    }

    private void onCloseSearch() {

        search.setText("");
        if (adapter != null)
            adapter.getFilter().filter("");

        PlansActivity.hideKeyboard(getActivity());

    }


    private BroadcastReceiver updateFlawFlag = null;
    private View mCloseSearch;

    private void bindView(View bindSource) {
        recyclerView = bindSource.findViewById(R.id.all_projects_rv);
        fl_layout_plans = bindSource.findViewById(R.id.fl_layout_plans);
        search = bindSource.findViewById(R.id.search_bar);
        closeSearch = bindSource.findViewById(R.id.close_search);
        mCloseSearch = bindSource.findViewById(R.id.close_search);
        mCloseSearch.setOnClickListener(v -> {
            onCloseSearch();
        });
    }

    private class GetFlawFlagUsingPhotoId extends AsyncTask<String, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        GetFlawFlagUsingPhotoId() {
        }

        @Override
        protected Void doInBackground(final String... params) {

            pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectId);



            flawFlagObj = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagObjUsingPhotoID(projectId, params[0], photoId + "");

            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(updateFlawFlag, new IntentFilter("updateFlawFlag"), Context.RECEIVER_EXPORTED);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }


    private class GetAllDefects extends AsyncTask<String, Void, List<Long>> {
        private DefectsDao mAsyncTaskDao;

        GetAllDefects(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<Long> doInBackground(final String... params) {

            List<Long> stringList = mAsyncTaskDao.getDefectsUniqueDefectIds(params[0]);

            return stringList;
        }

        @Override
        protected void onPostExecute(List<Long> longList) {
            super.onPostExecute(longList);
            List<String> stringList = new ArrayList<>();
            for (int i = 0; i < longList.size(); i++) {
                stringList.add(longList.get(i) + "");
            }

            pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectId, stringList);

            if (getActivity() != null) {
                pdFlawFlagRepository.getListMutableLiveData().observe(getActivity(), pdflawflags -> {

                    if (pdflawflags.size() > 0) {

                        new RetrievePlansUsingFlawFlagAsyncTask(getActivity(), projectId).execute(pdflawflags);
                    }
                });
            }
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAllFragmentInteraction(uri);
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
    public void onListItemClick(PlansModel plansModel) {
//        startActivity(new Intent(getContext(), ProjectDetailActivity.class));
        if (fromPhoto) {
//            startActivity(new Intent(getContext(), AddPhotoDirectionActivity.class).putExtra("projectId", projectId).putExtra("photoId", photoId).putExtra("fileId", plansModel.getPlanId()));
            new GetFlawFlagUsingPhotoId().execute(plansModel.getPlanId());

            if (plansModel.getPlanId() != null && !plansModel.getPlanId().equals("")) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);
                        sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);

                        startActivity(new Intent(getActivity(), PhotoAddDirectionMainActivity.class)//fixxe---
                                .putExtra("projectId", plansModel.getProjectId())
                                .putExtra("photoId", photoId)
                                .putExtra("planId", plansModel.getPlanId())
                                .putExtra("flawFlagObj", flawFlagObj)
                                .putExtra("planName", plansModel.getOrigName())
                                .putExtra("fromPhoto", fromPhoto)
                                 .putExtra("fromPlanScreen", true));
                    }
                }, 50);
            }
        } else if (isFromDefect) {
            // startActivity(new Intent(getContext(), PlanDetailsActivity.class).putExtra("projectId", projectId).putExtra("fileId", plansModel.getPlanId()));
            DefectPlanDirectionFragment newFragment = new DefectPlanDirectionFragment(null, projectId, plansModel.getPlanId(), isFromDefect);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("DefectPlanDirectionFragment").commit();

        } else {

            startActivity(new Intent(getActivity(), VIewPlanAddDirectionMainActivity.class)
                    .putExtra("projectId", plansModel.getProjectId())
                    .putExtra("photoId", photoId)
                    .putExtra("planName", plansModel.getOrigName())
                    .putExtra("planId", plansModel.getPlanId()));

        }
    }

    private class RetrievePlansUsingFlawFlagAsyncTask extends AsyncTask<List<Pdflawflag>, Void, List<PlansModel>> {
        private PlansDao mAsyncTaskDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        boolean isFromDefectListing;

        RetrievePlansUsingFlawFlagAsyncTask(Context context, String project_id) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.plansDao();
            projectId = project_id;

        }

        @Override
        protected List<PlansModel> doInBackground(final List<Pdflawflag>... params) {

            for (Pdflawflag plansModel : params[0]) {
                stringList.add(plansModel.getPdplanid());
            }
            List<PlansModel> plansModelList = mAsyncTaskDao.getPlansUsingDefect(projectId, stringList);


            return plansModelList;
        }

        @Override
        protected void onPostExecute(List<PlansModel> plansModelList) {
            super.onPostExecute(plansModelList);

            if (plansModelList != null) {
                adapter = new PlansRecyclerAdapter(plansModelList, AllPlansFragment.this);
                recyclerView.setAdapter(adapter);
            }
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (updateFlawFlag != null) {
            getActivity().unregisterReceiver(updateFlawFlag);
            updateFlawFlag = null;
        }
    }


    public void applyPlanFilterAccordingToDefects(List<String> stringList) {

        pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectId, stringList);


                if (stringList!=null&&stringList.size() > 0) {

                    new FilterPlansUsingFlawFlagAsyncTask(getActivity(), projectId).execute(stringList);
        }
    }

    private class FilterPlansUsingFlawFlagAsyncTask extends AsyncTask<List<String>, Void, Void> {
        private PlansDao mAsyncTaskDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        boolean isFromDefectListing;

        FilterPlansUsingFlawFlagAsyncTask(Context context, String project_id) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.plansDao();
            projectId = project_id;

        }

        @Override
        protected Void doInBackground(final List<String>... params) {


           stringList = params[0];




            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectId, stringList);

            if (getActivity() != null) {
                pdFlawFlagRepository.getListMutableLiveData().observe(getActivity(), pdflawflags -> {

                    if (pdflawflags.size() > 0) {

                        new RetrievePlansUsingFlawFlagAsyncTask(getActivity(), projectId).execute(pdflawflags);
                    }
                });
            }
        }
    }


}
