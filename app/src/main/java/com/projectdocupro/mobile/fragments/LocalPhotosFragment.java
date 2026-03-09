package com.projectdocupro.mobile.fragments;

import static com.projectdocupro.mobile.service.SyncLocalPhotosService.SHOW_RESULT;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.PhotosActivity;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.UpdatePhotosFilterResults;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.receivers.WorkerResultReceiver;
import com.projectdocupro.mobile.service.SyncLocalPhotosService;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.LocalPhotosViewModel;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalPhotosFragment.OnFragmentInteractionListener} interface
 * to handle interaction events..
 * Use the {@link LocalPhotosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalPhotosFragment extends Fragment implements UpdatePhotosFilterResults, WorkerResultReceiver.Receiver {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String DELETE_PHOTO = "delete_photo";
    public static final String PHOTO_MODEL = "photo_model";
    public static final String START_SYNC_PHOTOS_BACK_FROM_PHOTO_SCREEN = "syncPhotoFromTakePhotoScreen";

    // TODO: Rename and change types of parameters
    private String projectId;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private RecyclerView recyclerView;
    private BroadcastReceiver updateProfile = null;
    private BroadcastReceiver syncPhotoFromTakePhotoScreen = null;

    static public Activity context;

    LocalPhotosViewModel localPhotosViewModel;
    WorkerResultReceiver mWorkerResultReceiver;
    private boolean isFirstTimeCalled;
    private BroadcastReceiver deletePhoto = null;
    private SharedPrefsManager sharedPrefsManager;
    private boolean listenerApplied = false;
    public List<PhotoModel> lastData;


    public LocalPhotosViewModel getLocalPhotosViewModel() {
        return localPhotosViewModel;
    }

    public void setLocalPhotosViewModel(LocalPhotosViewModel localPhotosViewModel) {
        this.localPhotosViewModel = localPhotosViewModel;
    }

    public LocalPhotosFragment() {
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
    public static LocalPhotosFragment newInstance(String param1, String param2) {
        LocalPhotosFragment fragment = new LocalPhotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (localPhotosViewModel.getAdapter() != null) {
            localPhotosViewModel.getAdapter().notifyDataSetChanged();
        }

//        onlinePhotosViewModel.adapter.notifyDataSetChanged();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        context = getActivity();


    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(updateProfile, new IntentFilter("updateProfile"), Context.RECEIVER_EXPORTED);
            getActivity().registerReceiver(deletePhoto, new IntentFilter(DELETE_PHOTO), Context.RECEIVER_EXPORTED);
            getActivity().registerReceiver(syncPhotoFromTakePhotoScreen, new IntentFilter(START_SYNC_PHOTOS_BACK_FROM_PHOTO_SCREEN), Context.RECEIVER_EXPORTED);
        }


        if (!listenerApplied) {
            //  listenDataChange();
            listenerApplied = true;
        }
    }

    private void listenDataChange() {
        localPhotosViewModel.getAllPhotos().observe(this, new Observer<List<PhotoModel>>() {
            @Override
            public void onChanged(List<PhotoModel> photoModels) {
                Log.d("observer", "callback");

                Utils.showLogger("listenDataChange");

                lastData = photoModels;


//                localPhotosViewModel.initAdapter(projectId, photoModels);
//                recyclerView.setAdapter(localPhotosViewModel.getAdapter());
                if (photoModels.size() > 0 && !isFirstTimeCalled) {


                    localPhotosViewModel.initAdapter(projectId, photoModels);
                    recyclerView.setAdapter(localPhotosViewModel.getAdapter());
                    isFirstTimeCalled = true;


                    PhotosActivity photosActivity = (PhotosActivity) getActivity();
                    photosActivity.iv_sync_all.callOnClick();
                    // startSynchingAllPhotoes();//from first time
                } else {

                    localPhotosViewModel.initAdapter(projectId, photoModels);
                    recyclerView.setAdapter(localPhotosViewModel.getAdapter());
                }

                if (photoModels.size() > 0) {
                    if (getActivity() != null && (PhotosActivity) getActivity() instanceof PhotosActivity) {
                        ((PhotosActivity) getActivity()).isShowFilterIcon.setValue(true);
                        ((PhotosActivity) getActivity()).isShowIconReport.setValue(true);
                    }
                } else {
                    if (getActivity() != null && (PhotosActivity) getActivity() instanceof PhotosActivity) {
                        ((PhotosActivity) getActivity()).isShowFilterIcon.setValue(false);
                        ((PhotosActivity) getActivity()).isShowIconReport.setValue(false);
                    }
                }
            }
        });

    }

    private void startSynchingAllPhotoes() {
        Utils.showLogger("syncAllPhotos LogalPhotosFragment 195");
        if (ProjectNavigator.wlanIsConnected(getActivity()) || ProjectNavigator.mobileNetworkIsConnected(getActivity())) //If connected to internet
            ((PhotosActivity) getActivity()).syncAllPhotos(localPhotosViewModel.isAutoSyncPhoto);//startSynchingAllPhotoes first loading pics

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_photos, container, false);
        bindView(view);
        sharedPrefsManager = new SharedPrefsManager(getActivity());

        localPhotosViewModel = ViewModelProviders.of(this).get(LocalPhotosViewModel.class);
        localPhotosViewModel.init(projectId);
        localPhotosViewModel.refreshData(projectId);

        listenDataChange();
        mWorkerResultReceiver = new WorkerResultReceiver(new Handler());
        mWorkerResultReceiver.setReceiver(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {


                return super.requestChildRectangleOnScreen(parent, child, rect, immediate, focusedChildVisible);
            }
        };
        linearLayoutManager.setReverseLayout(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        addEvent();
        return view;
    }


    private void addEvent() {


        localPhotosViewModel.isStartUploadPhoto.observe(this, aBoolean -> {
            if (aBoolean) {
                Utils.showLogger("localPhotosViewModel.isStartUploadPhoto");
                startBackgroundTask(getContext(), mWorkerResultReceiver, projectId, localPhotosViewModel.isAutoSyncPhoto);
            }

        });

        deletePhoto = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                PhotoModel photoModel = (PhotoModel) intent.getSerializableExtra(PHOTO_MODEL);

                new DeletePhotoAsyncTask().execute(photoModel);
                localPhotosViewModel.deletePhotoFromList(photoModel);

            }
        };

        syncPhotoFromTakePhotoScreen = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Utils.showLogger("syncPhotoFromTakePhotoScreen");
//                new ReterivePhotoListAsyncTask().execute(projectId);
                localPhotosViewModel.isAutoSyncPhoto = false;
            /*    Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new ReterivePhotoListANDStartUploadAsyncTask().execute(projectId);
                    }
                },500);*/

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("up_serv_called", "true");

                        ///  checkCount();

                        PhotosActivity photosActivity = (PhotosActivity) getActivity();
                        photosActivity.iv_sync_all.callOnClick();

                        //  startBackgroundTask(getContext(), mWorkerResultReceiver, projectId, localPhotosViewModel.isAutoSyncPhoto);
                    }
                }, 400);

            }
        };

        updateProfile = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (getActivity() != null && localPhotosViewModel != null) {
                        Utils.showLogger("updateProfileBroadcast");

                        new ReterivePhotoListAsyncTask().execute(projectId);
/*
                        localPhotosViewModel.getAllPhotos().observe(getActivity(), new Observer<List<PhotoModel>>() {
                            @Override
                            public void onChanged(List<PhotoModel> photoModels) {
                                Log.d("observer", "callback");

                                localPhotosViewModel.initAdapter(projectId, photoModels);
                                recyclerView.setAdapter(localPhotosViewModel.getAdapter());
                                if (photoModels.size() > 0) {
                                    if (getActivity() != null && (PhotosActivity) getActivity() instanceof PhotosActivity) {
                                        ((PhotosActivity) getActivity()).isShowFilterIcon.setValue(true);
                                    }
                                } else {
                                    if (getActivity() != null && (PhotosActivity) getActivity() instanceof PhotosActivity) {
                                        ((PhotosActivity) getActivity()).isShowFilterIcon.setValue(false);
                                    }
                                }
                            }
                        });
*/

                    }
                } catch (Exception e) {
                }

            }
        };
    }

    private void bindView(View bindSource) {
        recyclerView = bindSource.findViewById(R.id.local_photos_rv);
    }


    private class DeletePhotoAsyncTask extends AsyncTask<PhotoModel, Void, Void> {
        private PhotoDao photoDao;

        DeletePhotoAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getContext());
            this.photoDao = projectsDatabase.photoDao();
        }

        @Override
        protected Void doInBackground(PhotoModel... params) {
            photoDao.deleteUsingPhotoId(params[0].getProjectId(), String.valueOf(params[0].getPdphotolocalId()));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private void updateUI(List<PhotoModel> photoModelList) {

        localPhotosViewModel.initAdapter(projectId, photoModelList);
        recyclerView.setAdapter(localPhotosViewModel.getAdapter());
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onLcoalPhotoFragmentInteraction(uri);
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

        if (updateProfile != null) {
            getActivity().unregisterReceiver(updateProfile);
            updateProfile = null;
        }
        if (deletePhoto != null) {
            getActivity().unregisterReceiver(deletePhoto);
            deletePhoto = null;
        }
        if (syncPhotoFromTakePhotoScreen != null) {
            getActivity().unregisterReceiver(syncPhotoFromTakePhotoScreen);
            syncPhotoFromTakePhotoScreen = null;
        }
    }

    public void startBackgroundTask(Context context, WorkerResultReceiver mWorkerResultReceiver, String projectID, boolean isAutoSyncPhotos) {
        Utils.showLogger("startBackgroundTask");
        SyncLocalPhotosService.enqueueWork(context, mWorkerResultReceiver, projectID, isAutoSyncPhotos);
    }


    @Override
    public void updatePhotosResults(List<PhotoModel> photoModelList) {
        updateUI(photoModelList);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case SHOW_RESULT:
                boolean isAutoSyncPhoto = false;
                new ReterivePhotoListAsyncTask().execute(projectId);
                if (resultData != null && resultData.getBoolean(SyncLocalPhotosService.IS_PHOTOS_AUTO_SYNC)) {
                    if (getActivity() != null && localPhotosViewModel != null) {
                        localPhotosViewModel.isAutoSyncPhoto = resultData.getBoolean(SyncLocalPhotosService.IS_PHOTOS_AUTO_SYNC);
//                        if (!ProjectNavigator.mobileNetworkIsConnected(getActivity()) ) {
//                            new Handler().postDelayed(() -> {
//                                //your code here
//                                ((PhotosActivity) getActivity()).syncAllPhotos(localPhotosViewModel.isAutoSyncPhoto);
//                            }, 200);
//                        }
                        if (ProjectDocuUtilities.isNetworkConnected(context) || ProjectNavigator.wlanIsConnected(context)) {
                            new Handler().postDelayed(() -> {
                                //your code here Utils.showLogger("syncAllPhotos> From on result receive");
                                ((PhotosActivity) getActivity()).syncAllPhotos(localPhotosViewModel.isAutoSyncPhoto);//On receive results
                            }, 200);
                        }
                    }
                }
                break;
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
        void onLcoalPhotoFragmentInteraction(Uri uri);
    }

    private class ReterivePhotoListAsyncTask extends AsyncTask<String, Void, List<PhotoModel>> {
        private PhotoDao mAsyncTaskDao;

        ReterivePhotoListAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getActivity());
            mAsyncTaskDao = projectsDatabase.photoDao();
        }

        @Override
        protected List<PhotoModel> doInBackground(final String... params) {

            List<PhotoModel> photoModelList = mAsyncTaskDao.getPhotoLocalList(params[0]);

            return photoModelList;
        }

        @Override
        protected void onPostExecute(List<PhotoModel> photoModelList) {
            super.onPostExecute(photoModelList);
            if (photoModelList != null && localPhotosViewModel.getAdapter() != null) {
                localPhotosViewModel.getAdapter().PreparePhotosGroupData(photoModelList);
                localPhotosViewModel.getAdapter().notifyDataSetChanged();
            }
        }
    }

}
