package com.projectdocupro.mobile.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.ReportDetailsPhotosRecyclerAdapter;
import com.projectdocupro.mobile.interfaces.OnlinePhotosListItemClickListener;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.OnlinePhotoModel;
import com.projectdocupro.mobile.repos.ONlinePhotoRepository;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReportPhotoDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReportPhotoDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportPhotoDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    // TODO: Rename and change types of parameters
    private String projectId;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private EditText et_title;

    private EditText et_description;

    private Switch sw_report_detail;

    private RecyclerView recyclerView;


    private LinearLayout ll_parent;

    private LinearLayout ll_content_view;

    private LinearLayout ll_photos_view;


    private GridLayoutManager linearLayoutManager;
    private boolean isSelected;
    StringBuilder stringBuilder = new StringBuilder();
    private ONlinePhotoRepository oNlinePhotoRepository;
    ReportDetailsPhotosRecyclerAdapter adapter;
    public boolean isReportDetailOpened;
    private String pdReportID;
    private View progress_bar_view;
    private MenuItem menuItem;
    private View itemViewTemp;
    private int itemWidth;

    RelativeLayout pbar;

    ContentLoadingProgressBar progressBar;

    public ReportPhotoDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OnlinePhotosFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportPhotoDetailFragment newInstance(String param1, String param2) {
        ReportPhotoDetailFragment fragment = new ReportPhotoDetailFragment();
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
            projectId = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);



    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        menu.clear();
        inflater.inflate(R.menu.report_action_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.report_action:
                menuItem = item;
                validate();

                break;

        }
        return true;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        bindView(view);
        pbar = view.findViewById(R.id.rl_pb_parent);

        progressBar = view.findViewById(R.id.progressBar);
        isReportDetailOpened = true;
        ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
        int currentOrientation = getActivity().getResources().getConfiguration().orientation;
        linearLayoutManager = new GridLayoutManager(getActivity(), projectDocuUtilities.calculateNoOfColumns(getActivity(), 130));

        if (itemViewTemp == null) {
            itemViewTemp = LayoutInflater.from(getActivity()).inflate(R.layout.photo_list_view, container, false);
            itemViewTemp.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            itemWidth = itemViewTemp.getMeasuredWidth();
        }
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            landScapeMode();
        } else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            portraitMode();
        }
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());

        addEvent();
        if (mParam2 != null && !mParam2.equalsIgnoreCase("")) {
            List<String> list = Arrays.asList(mParam2.split(","));
            oNlinePhotoRepository = new ONlinePhotoRepository(getActivity());
            oNlinePhotoRepository.retrievePhotosUsingIds(oNlinePhotoRepository.getmDefectsPhotoDao(), projectId, list);
            oNlinePhotoRepository.getmSectionLive().observe(this, onlinePhotoModels -> {

                if (onlinePhotoModels.size() > 0) {

                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                    else {
                        adapter = new ReportDetailsPhotosRecyclerAdapter(projectId, onlinePhotoModels, new OnlinePhotosListItemClickListener() {
                            @Override
                            public void onListItemClick(OnlinePhotoModel onlinePhotoModel) {

                                oNlinePhotoRepository.getmSectionLive().getValue().remove(onlinePhotoModel);
                                if (adapter != null)
                                    adapter.notifyDataSetChanged();
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    }
                }

            });
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }

        return view;
    }




    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landScapeMode();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            portraitMode();
        }

//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//        }
    }

    private void portraitMode() {
        ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
        LinearLayout.LayoutParams lpParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll_parent.setLayoutParams(lpParent);
        ll_parent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_content_view.setLayoutParams(lpSection1);
        ll_content_view.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll_photos_view.setLayoutParams(lpSection2);
        ll_photos_view.setOrientation(LinearLayout.VERTICAL);


        linearLayoutManager = new GridLayoutManager(getActivity(), projectDocuUtilities.calculateNoOfColumns(getActivity(), 130));
        recyclerView.setLayoutManager(linearLayoutManager);

        if (adapter != null)
            adapter.notifyDataSetChanged();

    }


    private void landScapeMode() {

        LinearLayout.LayoutParams lpParent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        ll_parent.setLayoutParams(lpParent);
        ll_parent.setWeightSum(2);
        ll_parent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams lpSection1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        ll_content_view.setLayoutParams(lpSection1);
        ll_content_view.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lpSection2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpSection2.setMargins(5, 5, 5, 10);
        ll_photos_view.setLayoutParams(lpSection2);
        ll_photos_view.setOrientation(LinearLayout.VERTICAL);

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float ScrDensity = metrics.density;
        int metricHeight = metrics.heightPixels;//
        int metricWidth = (metrics.widthPixels/2);
//            int width=455;
        int width = 410;
        if (itemWidth > 0) {
            width = metricWidth / itemWidth;
        }
        linearLayoutManager = new GridLayoutManager(getActivity(), width);
        recyclerView.setLayoutManager(linearLayoutManager);
        if (adapter != null)
            adapter.notifyDataSetChanged();


    }

    private void addEvent() {


    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onOnlineFragmentInteraction(uri);
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

    private void bindView(View bindSource) {
        et_title = bindSource.findViewById(R.id.et_title);
        et_description = bindSource.findViewById(R.id.et_description);
        sw_report_detail = bindSource.findViewById(R.id.sw_report_detail);
        recyclerView = bindSource.findViewById(R.id.online_photos_rv);
        ll_parent = bindSource.findViewById(R.id.ll_parent_view);
        ll_content_view = bindSource.findViewById(R.id.ll_content_view);
        ll_photos_view = bindSource.findViewById(R.id.ll_photos_view);
        progress_bar_view = bindSource.findViewById(R.id.progress_bar_view);
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
        void onOnlineFragmentInteraction(Uri uri);
    }

    public void validate() {

        if (et_title.getText().toString().equals("")) {
            Toast.makeText(getActivity(), getResources().getString(R.string.enter_title_msg), Toast.LENGTH_SHORT).show();
            return;
        }
        if (et_description.getText().toString().equals("")) {
            Toast.makeText(getActivity(), getResources().getString(R.string.enter_description_msg), Toast.LENGTH_SHORT).show();

            return;
        }
        if (menuItem != null)
            menuItem.setEnabled(false);
        if (getActivity() != null) {
            hideKeyboard(getActivity());
            callCreatereportAPI();
        }


    }

    private void callCreatereportAPI() {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(getActivity());
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }
        SimpleDateFormat yyyMMddHHmmssFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
        String photoDate = yyyMMddHHmmssFormat.format(new Date());
        JsonObject params = new JsonObject();
        params.addProperty("projectid", Integer.valueOf(projectId));
        params.addProperty("pduserid", Integer.valueOf(sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, "")));
        params.addProperty("reporttype", "1");
        params.addProperty("reportname", et_title.getText().toString());
        params.addProperty("description", et_description.getText().toString());
        params.addProperty("reportdate", photoDate);
        params.addProperty("status", "1");
        params.addProperty("deleted", "0");
        params.addProperty("creator", sharedPrefsManager.getStringValue(AppConstantsManager.USER_FIRST_NAME, "") + " " + sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_NAME, ""));
        if (sw_report_detail.isChecked()) {
            params.addProperty("incplan", "1");
        } else {
            params.addProperty("incplan", "0");

        }
        JsonArray jsonElements = new JsonArray();
        for (int i = 0; i < adapter.photosData.size(); i++) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", adapter.photosData.get(i).getPdphotoid());
            jsonObject.addProperty("txt", adapter.photosData.get(i).getDescription());
            jsonElements.add(jsonObject);
        }
        params.add("reportitems", jsonElements);

        Call<JsonObject> call = retroApiInterface.getCreatereportAPI(authToken, Utils.DEVICE_ID, projectId, params);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        String dateString = response.body().getAsJsonObject("data").get("pdreportid").toString();
                        pdReportID = dateString.replace("\"", "");

                        callGetReportAPI(getActivity(), projectId, pdReportID);

                    } else {
                        Log.d("Login", "Empty response");
                        Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("Login", "Not Success : " + response.toString());
                    Toast.makeText(getActivity(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                Log.d("Login", "failed : " + t.getMessage());
//                if(menuItem!=null)
//                    menuItem.setEnabled(true);
            }
        });
    }


    private boolean writeResponseBodyToDisk(ResponseBody body, String projectId) {
        String imagePath = "";
        try {
            // todo change the file location/name according to your needs

//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_defects_" + projectId);

            File dir = getActivity().getExternalFilesDir("/projectDocu/project_reports" + projectId);
            if (dir == null) {
                dir = getActivity().getFilesDir();
            }
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".pdf");

            imagePath = photo.getAbsolutePath();


//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ filename);


            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[15000];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(photo);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("A TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }


                outputStream.flush();
                hideProgressbar();
                Intent target = new Intent(Intent.ACTION_VIEW);

                Uri photoURI = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".fileprovider", photo);

//                Uri photoURI = FileProvider.getUriForFile(getActivity(),
//                        getActivity().getPackageName() + ".provider",
//                        photo);
                target.setDataAndType(photoURI, "application/pdf");
                target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                Intent intent = Intent.createChooser(target, "Open File");
                try {
                    startActivity(intent);
                    getActivity().finish();
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                }

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }


    private void callGetReportAPI(Context context, String projectId, String reportId) {
        showProgressbar();
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getCreatedReportFile(authToken, Utils.DEVICE_ID, projectId, reportId);
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        writeResponseBodyToDisk(response.body(), projectId);
//                            Bitmap bitmap  =   BitmapFactory.decodeFile(imagePath);
//                            imageView.setImageBitmap(bitmap);

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
                if (menuItem != null)
                    menuItem.setEnabled(true);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
                if (menuItem != null)
                    menuItem.setEnabled(true);
            }
        });
    }



    private void showProgressbar() {

        pbar.setVisibility(View.VISIBLE);
        progressBar.show();

    }

    private void hideProgressbar() {

        pbar.setVisibility(View.GONE);

    }
}
