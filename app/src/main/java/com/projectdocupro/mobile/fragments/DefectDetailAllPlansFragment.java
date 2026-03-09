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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.DefectDetailsActivity;
import com.projectdocupro.mobile.activities.DefectDetailsActivity2;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.adapters.PlansRecyclerAdapter;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.fragments.add_direction.SpecificDefectAddDirectionFragment;
import com.projectdocupro.mobile.interfaces.PlansListItemClickListener;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.repos.AllPlansRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DefectDetailAllPlansFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DefectDetailAllPlansFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DefectDetailAllPlansFragment extends Fragment implements PlansListItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PROJECT_ID = "projectId";
    private static final String ARG_FLAW_ID = "flaw_id";
    private static final String ARG_PHOTO_ID = "photoId";
    private static final String ARG_FROM_PHOTO = "param2";
    private static final String ARG_FROM_DEFECT = "is_from_defect";
    public static final String BR_UPDATE_PLAN_LOCATION = "update_plan_location";
    public static final String IS_UPDATE_LOCATION = "is_update_location";


    // TODO: Rename and change types of parameters
    private String projectId;
    private String flawId;
    private long photoId;
    private boolean fromPhoto;
    private boolean isMangelCreated;


    private OnFragmentInteractionListener mListener;

    private RecyclerView recyclerView;

    private LinearLayout cl_parent;

    AllPlansRepository plansRepository;
    PdFlawFlagRepository pdFlawFlagRepository;
    PlansRecyclerAdapter adapter;
    private boolean isFromDefect;
    private Pdflawflag flawFlagObj;
    private Pdflawflag pdflawflagOBJ = new Pdflawflag();
    private boolean is_plan_location_update;
    private boolean isFirstTimeAdded = false;

    public DefectDetailAllPlansFragment() {
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
    public static DefectDetailAllPlansFragment newInstance(String projectId, long photoId, boolean fromPhoto) {
        DefectDetailAllPlansFragment fragment = new DefectDetailAllPlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);

        args.putLong(ARG_PHOTO_ID, photoId);
        args.putBoolean(ARG_FROM_PHOTO, fromPhoto);
        fragment.setArguments(args);
        return fragment;
    }

    public static DefectDetailAllPlansFragment newInstance(String projectId, String flawId, long photoId, boolean fromPhoto, boolean isfromDefect) {
        DefectDetailAllPlansFragment fragment = new DefectDetailAllPlansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_FLAW_ID, flawId);
        args.putLong(ARG_PHOTO_ID, photoId);
        args.putBoolean(ARG_FROM_PHOTO, fromPhoto);
        args.putBoolean(ARG_FROM_DEFECT, isfromDefect);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            photoId = getArguments().getLong(ARG_PHOTO_ID);
            fromPhoto = getArguments().getBoolean(ARG_FROM_PHOTO);
            flawId = getArguments().getString(ARG_FLAW_ID);
            isFromDefect = getArguments().getBoolean(ARG_FROM_DEFECT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_plans_2, container, false);
        bindView(view);
        if (getActivity() instanceof DefectDetailsActivity) {
            isMangelCreated = ((DefectDetailsActivity) getActivity()).isMangelCreated;
        } else if (getActivity() instanceof DefectDetailsActivity2) {
            isMangelCreated = ((DefectDetailsActivity2) getActivity()).isMangelCreated;
        }

        plansRepository = new AllPlansRepository(getContext(), projectId);

        if (isFromDefect) {
//           ViewGroup.LayoutParams layoutParams= cl_parent.getLayoutParams();
//
//
//                    LinearLayout.LayoutParams lpp = (LinearLayout.LayoutParams) layoutParams;
//            lpp.setMargins(0, 0, 0, 0);
//            cl_parent.setLayoutParams(lpp);
            pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectId, flawId);


            plansRepository.getAllProjects().observe(getActivity(), plansModels -> {
                if (plansModels == null || plansModels.size() == 0) {
                    plansRepository.callGetListAPI(getContext(), projectId);

                } else {

                    adapter = new PlansRecyclerAdapter(plansModels, DefectDetailAllPlansFragment.this);
                    recyclerViewSetting();
//                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);

                }
                plansRepository.getAllProjects().removeObservers(getActivity());
            });

            new GetFlawFlagUsingFlawid().execute();

//            pdFlawFlagRepository.getListMutableLiveData().observe(this, pdflawflags -> {
//
//                if (pdflawflags.size() > 0) {
//
//                    if(pdflawflags!=null&&pdflawflags.size()==1){
////                        new GetFlawFlagUsingPhotoId().execute(pdflawflags.get(0).getPdplanid());
//
//                        if(pdflawflags.get(0)!=null){
//                            pdflawflagOBJ= pdflawflags.get(0);
//                            SpecificDefectAddDirectionFragment newFragment = new SpecificDefectAddDirectionFragment(pdflawflagOBJ,projectId,pdflawflagOBJ.getPdplanid(),flawId+"",fromPhoto);
//                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//                            ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();
//                        }
//                    }
//
//                }
//            });
            addEvent();
        }


        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recyclerViewSetting();
        adapter.notifyDataSetChanged();

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

    private void bindView(View bindSource) {
        recyclerView = bindSource.findViewById(R.id.all_projects_rv);
        cl_parent = bindSource.findViewById(R.id.linearLayout);
    }


    private class UpdateFlawFlagUsingFlawid extends AsyncTask<Void, Void, Pdflawflag> {
        private DefectsDao mAsyncTaskDao;

        UpdateFlawFlagUsingFlawid() {
        }

        @Override
        protected Pdflawflag doInBackground(final Void... params) {

            pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectId);

            flawFlagObj = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagOBJExist(projectId, flawId);

            return flawFlagObj;
        }

        @Override
        protected void onPostExecute(Pdflawflag pdflawflag) {
            super.onPostExecute(pdflawflag);


            if (pdflawflag != null) {

                SpecificDefectAddDirectionFragment newFragment = new SpecificDefectAddDirectionFragment(pdflawflag, projectId, pdflawflag.getPdplanid(), flawId + "", fromPhoto);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();

            }

        }
    }

    private class GetFlawFlagUsingFlawid extends AsyncTask<Void, Void, Pdflawflag> {
        private DefectsDao mAsyncTaskDao;

        GetFlawFlagUsingFlawid() {
        }

        @Override
        protected Pdflawflag doInBackground(final Void... params) {

            pdFlawFlagRepository = new PdFlawFlagRepository(getContext(), projectId);

            flawFlagObj = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagOBJExist(projectId, flawId);
//            flawFlagObj = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagOBJExist("44", "369");

            return flawFlagObj;
        }

        @Override
        protected void onPostExecute(Pdflawflag pdflawflag) {
            super.onPostExecute(pdflawflag);

           /* if(pdflawflag!=null){
                pdflawflagOBJ= pdflawflag;

//                startActivity(new Intent(getActivity(), PhotoAddDirectionMainActivity2Testing.class)
//                        .putExtra("projectId", projectId)
//                        .putExtra("photoId", flawId)
//                        .putExtra("planId", pdflawflagOBJ.getPdplanid())
//                        .putExtra("flawFlagObj", flawFlagObj)
//                        .putExtra("fromPhoto", true)
//                        .putExtra("fromPlanScreen", false));

//                PhotoAddDirectionFragment2Testing newFragment = new PhotoAddDirectionFragment2Testing(pdflawflagOBJ, projectId, pdflawflagOBJ.getPdplanid(), flawId, fromPhoto);
//                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//                ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("PhotoAddDirectionFragment2Testing").commit();


//                SpecificDefectAddDirectionFragment newFragment = new SpecificDefectAddDirectionFragment(pdflawflagOBJ,projectId,pdflawflagOBJ.getPdplanid(),flawId+"",fromPhoto,is_plan_location_update);
//                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//                ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();
            }*/

            if (pdflawflag != null) {
                SpecificDefectAddDirectionFragment newFragment = new SpecificDefectAddDirectionFragment(pdflawflag, projectId, pdflawflag.getPdplanid(), flawId + "", isFromDefect);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                if (!isFirstTimeAdded) {
                    ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();
                    isFirstTimeAdded = true;
                } else {
                    ft.replace(R.id.fl_layout_plans, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();

                }
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
//        if (fromPhoto) {
//            startActivity(new Intent(getContext(), AddPhotoDirectionActivity.class).putExtra("projectId", projectId).putExtra("photoId", photoId).putExtra("fileId", plansModel.getPlanId()));
//        } else {
//            startActivity(new Intent(getContext(), PlanDetailsActivity.class).putExtra("projectId", projectId).putExtra("fileId", plansModel.getPlanId()));
//        }

        if (!isMangelCreated && pdflawflagOBJ != null && pdflawflagOBJ.getPdplanid() != null && pdflawflagOBJ.getPdplanid().equals(plansModel.getPlanId())) {
            SpecificDefectAddDirectionFragment newFragment = new SpecificDefectAddDirectionFragment(pdflawflagOBJ, projectId, plansModel.getPlanId(), flawId + "", fromPhoto);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();
        } else {
//                           pdflawflagOBJ.setPdplanid(plansModel.getPlanId());

            if (isMangelCreated) {
                SpecificDefectAddDirectionFragment newFragment = new SpecificDefectAddDirectionFragment(null, projectId, plansModel.getPlanId(), flawId + "", false);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();
            } else {
                SpecificDefectAddDirectionFragment newFragment = new SpecificDefectAddDirectionFragment(null, projectId, plansModel.getPlanId(), flawId + "", fromPhoto);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.add(R.id.fl_layout_plans, newFragment).addToBackStack("SpecificDefectAddDirectionFragment").commit();
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
        if (updatePlanLocation != null) {
            getActivity().unregisterReceiver(updatePlanLocation);
            updatePlanLocation = null;
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(updateFlawFlag, new IntentFilter("updateDefectdetailFlawFlag"), Context.RECEIVER_EXPORTED);
            getActivity().registerReceiver(updatePlanLocation, new IntentFilter(BR_UPDATE_PLAN_LOCATION), Context.RECEIVER_EXPORTED);

        }
    }


    private BroadcastReceiver updateFlawFlag = null;
    private BroadcastReceiver updatePlanLocation = null;

    private void addEvent() {

        updateFlawFlag = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String planId = "";
//                String flawId = "";
                DefectsModel defectsModel = null;

                if (action.equals("updateDefectdetailFlawFlag")) {
                    planId = intent.getExtras().getString(SavePictureActivity.PLAN_ID_KEY);
                    flawId = intent.getExtras().getString("flawId");

                }
                new UpdateFlawFlagUsingFlawid().execute();
//                plansRepository.retrievePlanUsingFlawFlag();
//
//                if(flawId!=null&&!flawId.equals("")){
//                    for (int i = 0; i <defectsModelListt.size() ; i++) {
//                        if(flawId.equals(defectsModelListt.get(i).getDefectLocalId())){
//                            defectsModel=defectsModelListt.get(i);
//                            if(planId!=null&&!planId.equals("")){
//                                defectsModelListt.get(i).setpla
//                                defectsModel.setpla;
//                            }
//                        }
//                    }
//
//                }

            }
        };


        updatePlanLocation = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String planId = "";
//                String flawId = "";
                DefectsModel defectsModel = null;

                if (action.equals(BR_UPDATE_PLAN_LOCATION)) {
                    is_plan_location_update = intent.getExtras().getBoolean(IS_UPDATE_LOCATION, false);
                    new GetFlawFlagUsingFlawid().execute();

                }


            }
        };
    }

    private class RetrievePlansUsingFlawFlagAsyncTask extends AsyncTask<List<Pdflawflag>, Void, List<PlansModel>> {
        private PlansDao mAsyncTaskDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        boolean isFromDefectListing;

        RetrievePlansUsingFlawFlagAsyncTask(Context context, String project_id, boolean isFromFlawListing) {
            projectsDatabase = ProjectsDatabase.getDatabase(context);
            mAsyncTaskDao = projectsDatabase.plansDao();
            projectId = project_id;
            isFromDefectListing = isFromFlawListing;

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

        }
    }

}
