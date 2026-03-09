package com.projectdocupro.mobile.fragments;

import android.content.Context;
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
import com.projectdocupro.mobile.viewModels.FavoriteWordsViewModel;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FavoriteWordsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FavoriteWordsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteWordsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String projectId;
    private long photoId;

    private OnFragmentInteractionListener mListener;


    FavoriteWordsViewModel allWordsViewModel;

    private RecyclerView recyclerView;

    public FavoriteWordsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param projectId Parameter 1.
     * @param photoId Parameter 2.
     * @return A new instance of fragment FavoriteWordsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteWordsFragment newInstance(String projectId, long photoId) {
        FavoriteWordsFragment fragment = new FavoriteWordsFragment();
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
        View    view    = inflater.inflate(R.layout.fragment_favorite_words, container, false);
        bindView(view);
        allWordsViewModel =   ViewModelProviders.of(this).get(FavoriteWordsViewModel.class);
        allWordsViewModel.setPhotoId(photoId);
        allWordsViewModel.InitRepo(photoId,projectId);
        Log.d("pnp",projectId+":"+photoId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(allWordsViewModel.getAdapter());

        allWordsViewModel.getFavoriteWordsList().observe(this, new Observer<List<WordModel>>() {
            @Override
            public void onChanged(List<WordModel> wordModels) {
//                allWordsViewModel.getKeysList().clear();
//                allWordsViewModel.getWordsMap().clear();
                if (wordModels!=null ||  wordModels.size()>0){
                    Log.d("wordM","size: "+wordModels.size());
                    allWordsViewModel.getAdapter().setData(wordModels);
                }
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFavoriteWordsFragmentInteraction(uri, allWordsViewModel.isChanged());
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
        void onFavoriteWordsFragmentInteraction(Uri uri,boolean isChanged);
    }
}
