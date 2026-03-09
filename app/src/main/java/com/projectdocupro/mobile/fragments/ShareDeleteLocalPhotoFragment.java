package com.projectdocupro.mobile.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.ShareDeleteLocalPhotosRecyclerAdapter;
import com.projectdocupro.mobile.interfaces.LocalPhotosListItemClickListener;
import com.projectdocupro.mobile.models.DefectPhotoModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.LocalPhotosViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ShareDeleteLocalPhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareDeleteLocalPhotoFragment extends Fragment implements LocalPhotosListItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    // TODO: Rename and change types of parameters
    private String projectId;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private RecyclerView recyclerView;

    LocalPhotosViewModel localPhotosViewModel;
    List<DefectPhotoModel> defectPhotoModelList;
    private String server_flaw_id = "";
    private String photoId = "";
    private String defect_type = "";
    boolean isDummyItemLoaded;
    public static String BR_ACTION_UPDATE_DEFECT_PHOTOS = "updateDefectPhotos";
    public static String BR_KEY_IS_UPLOAD_PHOTOS_AUTO = "isUploadPhotoAuto";
    public ShareDeleteLocalPhotosRecyclerAdapter adapter;
    private Observer<List<PhotoModel>> observer;

    public List<PhotoModel> selectedPhotosList = new ArrayList<>();
    public StringBuilder stringBuilder = new StringBuilder();
    public StringBuilder stringBuilderPhotoPath = new StringBuilder();


    public ShareDeleteLocalPhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocalPhotosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShareDeleteLocalPhotoFragment newInstance(String param1, String param2) {
        ShareDeleteLocalPhotoFragment fragment = new ShareDeleteLocalPhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static ShareDeleteLocalPhotoFragment newInstance(String param1, String param2, String PhotoId) {
        ShareDeleteLocalPhotoFragment fragment = new ShareDeleteLocalPhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, PhotoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            photoId = getArguments().getString(ARG_PARAM3);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_photos, container, false);
        bindView(view);
        localPhotosViewModel = ViewModelProviders.of(this).get(LocalPhotosViewModel.class);
        localPhotosViewModel.init(projectId);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));


        if(localPhotosViewModel.getAllPhotos()!=null) {
            localPhotosViewModel.getAllPhotos().observe(getActivity(), new Observer<List<PhotoModel>>() {
                @Override
                public void onChanged(List<PhotoModel> photoModels) {
//                PhotoModel photoModel = new PhotoModel();
//                photoModel.setPdphotolocalId(-100);
//                photoModel.setCameraOpen(true);
//                photoModel.setProjectId(projectId);
//                photoModels.add( photoModel);

                    for (int i = 0; i < photoModels.size(); i++) {
                        photoModels.get(i).setUserSelectedStatus(false);
                    }

                    adapter = new ShareDeleteLocalPhotosRecyclerAdapter(photoModels, ShareDeleteLocalPhotoFragment.this::onListItemClick);

                    recyclerView.setAdapter(adapter);
                }
            });

        }
        else
            Utils.showLogger("photoesAreNull");
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onLcoalPhotoFragmentInteraction(uri);
//        }
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
    }

    @Override
    public void onDestroy() {

        mListener = null;


        super.onDestroy();
    }

    @Override
    public void onListItemClick(PhotoModel onlinePhotoModel) {


        if (onlinePhotoModel.isUserSelectedStatus()) {
            stringBuilder.append(onlinePhotoModel.getPdphotoid() + ",");
            stringBuilderPhotoPath.append(onlinePhotoModel.getPath() + ",");
        } else {
            StringBuilder stringBuilderr = new StringBuilder();
            String strTemp = stringBuilder.toString().replace(onlinePhotoModel.getPdphotoid() + ",", "");
            stringBuilderr.append(strTemp);
            stringBuilder = new StringBuilder();
            stringBuilder.append(stringBuilderr);

            StringBuilder stringBuilderrPath = new StringBuilder();
            String strTempPath = stringBuilderPhotoPath.toString().replace(onlinePhotoModel.getPath() + ",", "");
            stringBuilderrPath.append(strTempPath);
            stringBuilderPhotoPath = new StringBuilder();
            stringBuilderPhotoPath.append(stringBuilderr);
        }

        if(selectedPhotosList.size()==0)
            selectedPhotosList.add(onlinePhotoModel);
        else {

            for (int i = 0; i < selectedPhotosList.size(); i++) {

                if (selectedPhotosList.get(i).getPdphotolocalId()!=onlinePhotoModel.getPdphotolocalId()&&onlinePhotoModel.isUserSelectedStatus()) {
                    selectedPhotosList.add(onlinePhotoModel);
                    break;
                }else {
                    selectedPhotosList.remove(onlinePhotoModel);
                    break;
                }
            }
        }
    }

    private void bindView(View bindSource) {
        recyclerView = bindSource.findViewById(R.id.local_photos_rv);
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
        void onLcoalPhotoFragmentInteraction(Uri uri);
    }


}
