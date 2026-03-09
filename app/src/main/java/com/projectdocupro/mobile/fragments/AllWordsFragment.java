package com.projectdocupro.mobile.fragments;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.viewModels.AllWordsViewModel;

import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AllWordsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AllWordsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllWordsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String projectId;
    private long photoId;

    private OnFragmentInteractionListener mListener;


    AllWordsViewModel allWordsViewModel;

    private RecyclerView    recyclerView;

    public AllWordsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param projectId Parameter 1.
     * @param photoId Parameter 2.
     * @return A new instance of fragment AllWordsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllWordsFragment newInstance(String projectId, long photoId) {
        AllWordsFragment fragment = new AllWordsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, projectId);
        args.putLong(ARG_PARAM2, photoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PARAM1);
            photoId = getArguments().getLong(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View    view    = inflater.inflate(R.layout.fragment_all_words, container, false);
        bindView(view);

        allWordsViewModel =   ViewModelProviders.of(this).get(AllWordsViewModel.class);
        allWordsViewModel.setPhotoId(photoId);
        allWordsViewModel.InitRepo(projectId,photoId);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {


                return super.requestChildRectangleOnScreen(parent, child, rect, immediate, focusedChildVisible);
            }
        };
        linearLayoutManager.setReverseLayout(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(allWordsViewModel.getAdapter());

        allWordsViewModel.getWordsList().observe(this, new Observer<List<WordModel>>() {
            @Override
            public void onChanged(List<WordModel> wordModels) {
//                allWordsViewModel.getKeysList().clear();
//                allWordsViewModel.getWordsMap().clear();
                Log.d("wordM","size: "+wordModels.size());
                if (wordModels==null ||  wordModels.size()==0){
                    allWordsViewModel.callGetListAPI(getContext(),projectId);
                }else{
                    allWordsViewModel.getAdapter().setData(wordModels);
                }
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAllWordsFragmentInteraction(uri, allWordsViewModel.isChanged());
        }else{
            Log.d("null","null");
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

        super.onDestroy();
    }

    private void bindView(View bindSource) {
        recyclerView = bindSource.findViewById(R.id.words_rv);
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
        void onAllWordsFragmentInteraction(Uri uri,boolean  isChanged);
    }
}
