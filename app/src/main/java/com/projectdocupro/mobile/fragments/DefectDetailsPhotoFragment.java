package com.projectdocupro.mobile.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.DefectDetailsActivity;
import com.projectdocupro.mobile.activities.DefectDetailsActivity2;
import com.projectdocupro.mobile.adapters.DefectedProjectPhotosRecyclerAdapter;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.models.DefectPhotoModel;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.DefectDetailsPhotosViewModel;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DefectDetailsPhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DefectDetailsPhotoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_DEFECT_ID = "param2";
    private static final String ARG_PARAM3 = "param3";

    // TODO: Rename and change types of parameters
    private String projectId;
    private String currentDefectID;

    private OnFragmentInteractionListener mListener;


    private RecyclerView recyclerView;

    private BroadcastReceiver updateProfile = null;
    DefectDetailsPhotosViewModel localPhotosViewModel;
    List<DefectPhotoModel> defectPhotoModelList;
    private String server_flaw_id = "";
    private String photoId = "";
    private String defect_type = "";
    boolean isDummyItemLoaded;
    public static String BR_ACTION_UPDATE_DEFECT_PHOTOS = "updateDefectPhotos";
    public static String BR_KEY_IS_UPLOAD_PHOTOS_AUTO = "isUploadPhotoAuto";
    DefectedProjectPhotosRecyclerAdapter adapter;
    private Observer<List<PhotoModel>> observer;

    public DefectDetailsPhotoFragment() {
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
    public static DefectDetailsPhotoFragment newInstance(String param1, String param2) {
        DefectDetailsPhotoFragment fragment = new DefectDetailsPhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_DEFECT_ID, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static DefectDetailsPhotoFragment newInstance(String param1, String param2, String PhotoId) {
        DefectDetailsPhotoFragment fragment = new DefectDetailsPhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_DEFECT_ID, param2);
        args.putString(ARG_PARAM3, PhotoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PARAM1);
            currentDefectID = getArguments().getString(ARG_DEFECT_ID);
            photoId = getArguments().getString(ARG_PARAM3);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //setHasOptionsMenu(true);
        Utils.showLogger("reqFragmentCreated");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_photos, container, false);
        bindView(view);
        localPhotosViewModel = ViewModelProviders.of(this).get(DefectDetailsPhotosViewModel.class);
        localPhotosViewModel.init(projectId, currentDefectID);
        ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), projectDocuUtilities.calculateNoOfColumns(getActivity(), 130)));

        if (getActivity() != null && getActivity() instanceof DefectDetailsActivity) {
            Utils.showLogger("DefectDetailsAc");
            server_flaw_id = ((DefectDetailsActivity) getActivity()).flaw_id;
            defect_type = ((DefectDetailsActivity) getActivity()).defect_type;
            photoId = ((DefectDetailsActivity) getActivity()).photoId;
            if (server_flaw_id != null && !server_flaw_id.equals(""))
                localPhotosViewModel.getLocalPhotosRepository().cacheImages(projectId, server_flaw_id);
        } else if (getActivity() != null && getActivity() instanceof DefectDetailsActivity2) {
            server_flaw_id = ((DefectDetailsActivity2) getActivity()).flaw_id;
            defect_type = ((DefectDetailsActivity2) getActivity()).defect_type;
            photoId = ((DefectDetailsActivity2) getActivity()).photoId;
            if (server_flaw_id != null && !server_flaw_id.equals(""))
                localPhotosViewModel.getLocalPhotosRepository().cacheImages(projectId, server_flaw_id);
        }
//        new ReterivePhotoDesAsyncTask(getActivity()).execute();
        localPhotosViewModel.getLocalPhotosRepository().getmDefectedPhotos().observe(getActivity(), new Observer<List<PhotoModel>>() {
            @Override
            public void onChanged(List<PhotoModel> photoModels) {
                PhotoModel photoModel = new PhotoModel();
                photoModel.setPdphotolocalId(-100);
                photoModel.setCameraOpen(true);
                photoModel.setProjectId(projectId);
                photoModels.add(photoModel);

                adapter = new DefectedProjectPhotosRecyclerAdapter(photoModels, currentDefectID, null);
                recyclerView.setAdapter(adapter);

            }
        });

        /*if (defect_type != null && defect_type.equalsIgnoreCase(DefectsActivity.TYPE_DEFECT_ADD) && photoId != null && !photoId.equalsIgnoreCase("")) {
//            localPhotosViewModel.initAdapter(projectId, mParam2);
            new RetrievePhotoObjAsyncTask().execute(photoId);
        } else {
            localPhotosViewModel.initAdapter(projectId, mParam2);

             observer= new Observer<List<PhotoModel>>() {
                @Override
                public void onChanged(List<PhotoModel> photoModels) {
                    if (getActivity() != null && getActivity() instanceof DefectDetailsActivity) {
                        photoId = ((DefectDetailsActivity) getActivity()).photoId;
                        if (photoId != null && !photoId.equals("")) {
                            if (localPhotosViewModel.getAdapter() != null && localPhotosViewModel.getAdapter().getItemCount() > 0) {
                                localPhotosViewModel.getAdapter().notifyDataSetChanged();
                            } else {
                                recyclerView.setAdapter(localPhotosViewModel.getAdapter());
                            }
                        } else {

//                            if (localPhotosViewModel.getAdapter().getPhotosData().size() == 0) {
//                                PhotoModel photoModel = new PhotoModel();
//                                photoModel.setPdphotolocalId(-100);
//                                photoModel.setCameraOpen(true);
//                                photoModel.setProjectId(projectId);
//                                localPhotosViewModel.getAdapter().getPhotosData().add(photoModel);
//
//                            } else {

                                PhotoModel photoModel = new PhotoModel();
                                photoModel.setPdphotolocalId(-100);
                                photoModel.setCameraOpen(true);
                                photoModel.setProjectId(projectId);
                                photoModels.add( photoModel);

//                            }
//                            if (localPhotosViewModel.getAdapter() != null&&localPhotosViewModel.getAdapter().getItemCount()>0) {
//                                localPhotosViewModel.getAdapter().notifyDataSetChanged();
//                            } else {

                            //if(adapter==null) {
                                adapter = new DefectedProjectPhotosRecyclerAdapter(photoModels, mParam2, null);
                                recyclerView.setAdapter(adapter);
//                            }else{
//                                adapter.notifyDataSetChanged();
//                            }
//                            }
                        }
                        //localPhotosViewModel.getAllPhotos().removeObservers(getActivity());
                    }
                }
            };
            localPhotosViewModel.getAllPhotos().observe(this,observer);
*//*
            localPhotosViewModel.getAllPhotos().observe(this, new Observer<List<PhotoModel>>() {
                @Override
                public void onChanged(List<PhotoModel> photoModels) {
                    Log.d("observer", "callback");
                    //localPhotosViewModel.getAdapter().getPhotosData().addAll(photoModels);
                    if (getActivity() != null && getActivity() instanceof DefectDetailsActivity) {
                        photoId = ((DefectDetailsActivity) getActivity()).photoId;
                        if (photoId != null && !photoId.equals("")) {
                            if (localPhotosViewModel.getAdapter() != null && localPhotosViewModel.getAdapter().getItemCount() > 0) {
                                localPhotosViewModel.getAdapter().notifyDataSetChanged();
                            } else {
                                recyclerView.setAdapter(localPhotosViewModel.getAdapter());
                            }
                        } else {

                            if (localPhotosViewModel.getAdapter().getPhotosData().size() == 0) {
                                PhotoModel photoModel = new PhotoModel();
                                photoModel.setPdphotolocalId(-100);
                                photoModel.setCameraOpen(true);
                                photoModel.setProjectId(projectId);
                                localPhotosViewModel.getAdapter().getPhotosData().add(photoModel);

                            } else {

                                PhotoModel photoModel = new PhotoModel();
                                photoModel.setPdphotolocalId(-100);
                                photoModel.setCameraOpen(true);
                                photoModel.setProjectId(projectId);
                                localPhotosViewModel.getAdapter().getPhotosData().set(localPhotosViewModel.getAdapter().getPhotosData().size() - 1, photoModel);

                            }
//                            if (localPhotosViewModel.getAdapter() != null&&localPhotosViewModel.getAdapter().getItemCount()>0) {
//                                localPhotosViewModel.getAdapter().notifyDataSetChanged();
//                            } else {
                            recyclerView.setAdapter(localPhotosViewModel.getAdapter());
//                            }
                        }
                    }
                }
            });
*//*
        }*/
        updateProfile = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                try {
                localPhotosViewModel.loadPhotosData();
                isDummyItemLoaded = false;
                String action = intent.getAction();
                if (action.equals(BR_ACTION_UPDATE_DEFECT_PHOTOS)) {
                    if (intent.hasExtra(BR_KEY_IS_UPLOAD_PHOTOS_AUTO)) {
                        boolean isUploadPhotoAuto = intent.getExtras().getBoolean(BR_KEY_IS_UPLOAD_PHOTOS_AUTO);
                        if (isUploadPhotoAuto) {
                            if (getActivity() != null && getActivity() instanceof DefectDetailsActivity) {
                                ((DefectDetailsActivity) getActivity()).updateDefectSyncStatus(currentDefectID, DefectRepository.UPLOADING_PHOTO, true);
                            } else if (getActivity() != null && getActivity() instanceof DefectDetailsActivity2) {
                                ((DefectDetailsActivity2) getActivity()).updateDefectSyncStatus(currentDefectID, DefectRepository.UPLOADING_PHOTO, true);
                            }
                        }
                    }
                }
//                    if (getActivity() != null && localPhotosViewModel != null) {
//                        //     Toast.makeText(getActivity(), "called", Toast.LENGTH_SHORT).show();
//                        localPhotosViewModel.getAllPhotos().observe(getActivity(), new Observer<List<PhotoModel>>() {
//                            @Override
//                            public void onChanged(List<PhotoModel> photoModels) {
//                                Log.d("observer", "callback");
//                                //localPhotosViewModel.getAdapter().getPhotosData().addAll(photoModels);
//                                if (getActivity() != null && getActivity() instanceof DefectDetailsActivity) {
//                                    photoId = ((DefectDetailsActivity) getActivity()).photoId;
//                                    if (photoId != null && !photoId.equals("")) {
////                                        recyclerView.setAdapter(localPhotosViewModel.getAdapter());
//                                        if (localPhotosViewModel.getAdapter() != null)
//                                            localPhotosViewModel.getAdapter().notifyDataSetChanged();
//                                    } else {
//                                        PhotoModel photoModel = new PhotoModel();
//                                        photoModel.setPdphotolocalId(-100);
//                                        photoModel.setCameraOpen(true);
//                                        photoModel.setProjectId(projectId);
//                                        localPhotosViewModel.getAdapter().getPhotosData().add(photoModel);
//                                        if (localPhotosViewModel.getAdapter() != null)
//                                            localPhotosViewModel.getAdapter().notifyDataSetChanged();
//                                    }
//                                    recyclerView.setAdapter(localPhotosViewModel.getAdapter());
//
//                                }
//                            }
//                        });
//
//                    }
//                } catch (Exception e) {
//                }

            }
        };

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(updateProfile, new IntentFilter("updateDefectPhotos"), Context.RECEIVER_EXPORTED);
        }
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
        if (updateProfile != null) {
            getActivity().unregisterReceiver(updateProfile);
            updateProfile = null;
        }

        super.onDestroy();
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

    private class RetrievePhotoObjAsyncTask extends AsyncTask<String, Void, List<PhotoModel>> {
        private PhotoDao mAsyncTaskDao;

        RetrievePhotoObjAsyncTask() {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(getActivity()).photoDao();
        }

        @Override
        protected List<PhotoModel> doInBackground(final String... params) {

            List<PhotoModel> photoModelList = new ArrayList<>();
            photoModelList.add(mAsyncTaskDao.getPhotosOBJ(params[0]));

            return photoModelList;
        }

        @Override
        protected void onPostExecute(List<PhotoModel> photoModels) {
            super.onPostExecute(photoModels);

//            PhotoModel photoModel = new PhotoModel();
//            photoModel.setPdphotolocalId(-100);
//            photoModel.setCameraOpen(true);
//            photoModel.setProjectId(projectId);
//            photoModels.add( photoModel);
//
//            adapter = new DefectedProjectPhotosRecyclerAdapter(photoModels, mParam2, null);
//            recyclerView.setAdapter(adapter);

            localPhotosViewModel.getAdapter().getPhotosData().addAll(photoModels);
            recyclerView.setAdapter(localPhotosViewModel.getAdapter());
        }
    }


/*
    private class ReterivePhotoDesAsyncTask extends AsyncTask<String, Void, List<PhotoModel>> {
        private PhotoDao mAsyncTaskDao;

        ReterivePhotoDesAsyncTask(Context context) {
            mAsyncTaskDao = ProjectsDatabase.getDatabase(context).photoDao();
        }

        @Override
        protected List<PhotoModel> doInBackground(final String... params) {

            List<PhotoModel> defectTradeModelListt = mAsyncTaskDao.getDefectPhotosAndLocalPhotosList(projectId, currentDefectID);


            return defectTradeModelListt;
        }

        @Override
        protected void onPostExecute(List<PhotoModel> photoModels) {
            super.onPostExecute(photoModels);

//            if (getActivity() != null) {
//
//                PhotoModel photoModel = new PhotoModel();
//                photoModel.setPdphotolocalId(-100);
//                photoModel.setCameraOpen(true);
//                photoModel.setProjectId(projectId);
//                photoModels.add(photoModel);
//
//                adapter = new DefectedProjectPhotosRecyclerAdapter(photoModels, mParam2, null);
//                recyclerView.setAdapter(adapter);
//            }

        }
    }
*/


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Utils.showLogger("Defect Detail Photoes>>onCreateOptionsMenu");
      //  inflater.inflate(R.menu.save_action_menu, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_action) {
        getActivity().finish();
        }
        return super.onOptionsItemSelected(item);

    }


}
