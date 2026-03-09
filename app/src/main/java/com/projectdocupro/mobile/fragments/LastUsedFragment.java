package com.projectdocupro.mobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.HomeActivity;
import com.projectdocupro.mobile.activities.ProjectDetailActivity;
import com.projectdocupro.mobile.adapters.ProjectsRecyclerAdapter;
import com.projectdocupro.mobile.interfaces.ProjectsListItemClickListener;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.repos.LastUsedProjectsRepository;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;

import java.util.ArrayList;
import java.util.List;



import static com.projectdocupro.mobile.fragments.AllFragment.hideKeyboard;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LastUsedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LastUsedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LastUsedFragment extends Fragment implements ProjectsListItemClickListener {
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

    LastUsedProjectsRepository lastUsedProjectsRepository;
    ProjectsRecyclerAdapter adapter;
    private View mCloseSearch;

    public LastUsedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LastUsedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LastUsedFragment newInstance(String param1, String param2) {
        LastUsedFragment fragment = new LastUsedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recyclerViewSetting();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_last_used, container, false);
        bindView(view);

        recyclerViewSetting();
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
        adapter = new ProjectsRecyclerAdapter(new ArrayList<>(), false, this);
        recyclerView.setAdapter(adapter);

        lastUsedProjectsRepository = new LastUsedProjectsRepository(getContext());

        lastUsedProjectsRepository.getLastUsedProjects().observe(this, lastUsedProjects -> adapter.setData(lastUsedProjects));

        search.setSelected(false);

        search.setOnClickListener(v -> closeSearch.setVisibility(View.VISIBLE));

        search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(getActivity());
                return true;
            }
            return false;
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(getActivity());
                return false;
            }
        });
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


        return view;
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

    @Override
    public void onResume() {
        super.onResume();
        if (!search.getText().toString().isEmpty()) {
            search.setText("");
        }
    }

    private void applySearch(String query) {
        if (!query.isEmpty()) {
            LiveData<List<ProjectModel>> searchResults = lastUsedProjectsRepository.getSearchResults("%" + query + "%");
            searchResults.observe(LastUsedFragment.this, new Observer<List<ProjectModel>>() {
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

    private void onCloseSearch() {

        search.setText("");
        if (adapter != null)
            adapter.getFilter().filter("");
        hideKeyboard(getActivity());
//        search.setText("");
//        search.setSelected(false);
//        closeSearch.setVisibility(View.GONE);
//        hideKeyboard(getActivity());
//        lastUsedProjectsRepository.getLastUsedProjects().observe(LastUsedFragment.this, new Observer<List<ProjectModel>>() {
//            @Override
//            public void onChanged(List<ProjectModel> projectModels) {
//                adapter.setData(projectModels);
//                lastUsedProjectsRepository.getLastUsedProjects().removeObserver(this);
//            }
//        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onLastUsedFragmentInteraction(uri);
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
    public void onListItemClick(ProjectModel projectModel, boolean isMarkFavourite, ProjectsRecyclerAdapter adapter) {

        if (isMarkFavourite) {
            if (getActivity() instanceof HomeActivity)
                ((HomeActivity) getActivity()).onListItemClick(projectModel, isMarkFavourite, adapter);
        } else {
            if (projectModel.isFavorite()) {
                startActivity(new Intent(getContext(), ProjectDetailActivity.class).putExtra("projectId", projectModel.getProjectid()));
                new updateProjectAsyncTask(getActivity()).execute(projectModel);
            } else {
                if (getActivity() instanceof HomeActivity)
                    ((HomeActivity) getActivity()).onListItemClick(projectModel, isMarkFavourite, adapter);
            }
        }

    }

    @Override
    public void onSyncActionClick(ProjectModel projectModel) {

    }

    private void bindView(View bindSource) {
        recyclerView = bindSource.findViewById(R.id.last_used_projects_rv);
        search = bindSource.findViewById(R.id.search_bar);
        closeSearch = bindSource.findViewById(R.id.close_search);
        mCloseSearch = bindSource.findViewById(R.id.close_search);
        mCloseSearch.setOnClickListener(v -> {
            onCloseSearch();
        });
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
        void onLastUsedFragmentInteraction(Uri uri);
    }
}
