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
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.projectdocupro.mobile.repos.FavoritePlansRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;

import java.util.ArrayList;
import java.util.List;



import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FavoritePlansFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FavoritePlansFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritePlansFragment extends Fragment implements PlansListItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PROJECT_ID = "param1";
    private static final String ARG_PHOTO_ID = "param2";
    private static final String ARG_FROM_PHOTO = "param3";
    private static final String ARG_PLAN_ID = "planId";

    // TODO: Rename and change types of parameters
    private String projectId;
    private String planId;
    private long photoId;
    private boolean fromPhoto;
    private boolean isFromDefect;
    private OnFragmentInteractionListener mListener;



    private RecyclerView recyclerView;

    private EditText search;

    private TextView closeSearch;

    FavoritePlansRepository favoritePlansRepository;
    PlansRecyclerAdapter adapter;
    private Pdflawflag flawFlagObj;
    private PdFlawFlagRepository pdFlawFlagRepository;
    private String flawId;

    public FavoritePlansFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters


    public static FavoritePlansFragment newInstance(String projectId, long photoId, boolean fromPhoto) {
        FavoritePlansFragment fragment = new FavoritePlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);

        args.putLong(ARG_PHOTO_ID, photoId);
        args.putBoolean(ARG_FROM_PHOTO, fromPhoto);
        fragment.setArguments(args);
        return fragment;
    }

    public static FavoritePlansFragment newInstance(String projectId, long photoId, boolean fromPhoto, boolean isfromDefect) {
        FavoritePlansFragment fragment = new FavoritePlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
//        args.putString(ARG_FLAW_ID, flawId);
        args.putLong(ARG_PHOTO_ID, photoId);
        args.putBoolean(ARG_FROM_PHOTO, fromPhoto);
        args.putBoolean(AllPlansFragment.ARG_FROM_DEFECT, isfromDefect);
        fragment.setArguments(args);
        return fragment;
    }

    public static FavoritePlansFragment newInstance(String param1, String planID, long photoId, boolean fromPhoto) {
        FavoritePlansFragment fragment = new FavoritePlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, param1);
        args.putString(ARG_PLAN_ID, planID);
        args.putLong(ARG_PHOTO_ID, photoId);
        args.putBoolean(ARG_FROM_PHOTO, fromPhoto);
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
            flawId = getArguments().getString(AllPlansFragment.ARG_FLAW_ID);
            isFromDefect = getArguments().getBoolean(AllPlansFragment.ARG_FROM_DEFECT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite_plans, container, false);
        bindView(view);

        recyclerViewSetting();
        //Get data from db
        favoritePlansRepository = new FavoritePlansRepository(getContext(), projectId);
        favoritePlansRepository.getFavoritePlans().observe(this, plansModels -> {
            adapter = new PlansRecyclerAdapter(plansModels, true, this);
            recyclerView.setAdapter(adapter);
        });
        addEvent();
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFavoritesFragmentInteraction(uri);
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
        if (updateFlawFlag != null)
            getActivity().unregisterReceiver(updateFlawFlag);
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
                        try {
                            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getActivity());

                            sharedPrefsManager.setBooleanValue(AppConstantsManager.VIEW_DIRECTION_THROUGH_COMPASS_MANUAL_STATE, false);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        //sharedPrefsManager.setBooleanValue(AppConstantsManager.ACTIVATE_GPS_MANUAL_STATE, false);

                        startActivity(new Intent(getActivity(), PhotoAddDirectionMainActivity.class)//fixed----
                                .putExtra("projectId", plansModel.getProjectId())
                                .putExtra("photoId", photoId)
                                .putExtra("planId", plansModel.getPlanId())
                                .putExtra("flawFlagObj", flawFlagObj)
                                .putExtra("fromPhoto", fromPhoto));
                    }
                }, 500);
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
                    .putExtra("planId", plansModel.getPlanId()));

        }
    }

    private void bindView(View bindSource) {
        recyclerView = bindSource.findViewById(R.id.favorite_projects_rv);
        search = bindSource.findViewById(R.id.search_bar);
        closeSearch = bindSource.findViewById(R.id.close_search);
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
        void onFavoritesFragmentInteraction(Uri uri);
    }

    private BroadcastReceiver updateFlawFlag = null;
    private View mCloseSearch;

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
                adapter = new PlansRecyclerAdapter(plansModelList, FavoritePlansFragment.this);
                recyclerView.setAdapter(adapter);
            }
        }
    }

    private void addEvent() {
        updateFlawFlag = new BroadcastReceiver() {
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

}
