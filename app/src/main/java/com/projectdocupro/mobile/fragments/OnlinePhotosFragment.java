package com.projectdocupro.mobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.PhotosActivity;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.adapters.OnlinePhotosRecyclerAdapter;
import com.projectdocupro.mobile.dao.OnlinePhotoDao;
import com.projectdocupro.mobile.interfaces.OnlinePhotosListItemClickListener;
import com.projectdocupro.mobile.interfaces.UpdateOnlinePhotosFilterResults;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.OnlinePhotoModel;
import com.projectdocupro.mobile.models.mangel_filters.GroupheadingModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.utility.PaginationScrollListener;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;
import com.projectdocupro.mobile.utility.SpaceItemDecoration;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.OnlinePhotosViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class OnlinePhotosFragment extends Fragment implements UpdateOnlinePhotosFilterResults {
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


    public OnlinePhotosViewModel onlinePhotosViewModel;
    private GridLayoutManager linearLayoutManager;
    private SpaceItemDecoration spaceItemDecoration;
    private boolean isSelected;
    public StringBuilder stringBuilder = new StringBuilder();
    public StringBuilder stringBuilderPhotoPath = new StringBuilder();

    String onlineCachedPhotoCount;

    public static int onlinePhotosDBCount = 0;
    private List<GroupheadingModel> filterDataList;
    private SharedPrefsManager sharedPrefsManager;
    public JSONObject jsonObject=null;
    public JSONArray jsonElements=null;


    private boolean isVisible;

    private String defaultStartDate = "";
    private String defaultEndDate = "";

    public OnlinePhotosFragment() {
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
    public static OnlinePhotosFragment newInstance(String param1, String param2) {
        OnlinePhotosFragment fragment = new OnlinePhotosFragment();
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


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setAdapterSetting();
        if (onlinePhotosViewModel.adapter != null) {
            onlinePhotosViewModel.adapter.notifyDataSetChanged();
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_online_photos, container, false);
        bindView(view);
        onlinePhotosViewModel = ViewModelProviders.of(getActivity()).get(OnlinePhotosViewModel.class);
        onlinePhotosViewModel.init(getActivity(), projectId);
        sharedPrefsManager = new SharedPrefsManager(getActivity());
        onlineCachedPhotoCount = sharedPrefsManager.getStringValue(AppConstantsManager.ONLINE_PHOTO_COUNT, "");
        setAdapterSetting();
//        linearLayoutManager = new GridLayoutManager(getActivity(), 3);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        addEvent();
        addObserverListener();

        if (mParam2.equalsIgnoreCase("fromNextAction"))
            isSelected = true;

        //region original code
//        onlinePhotosViewModel.photoPaths.observe(this, onlinePhotoModels -> {
//            if (mParam2.equalsIgnoreCase("fromNextAction")){
//                isSelected = true;
//            }
//            if (onlinePhotoModels == null || onlinePhotoModels.size() == 0) {
//                if (!mParam2.equalsIgnoreCase("fromNextAction"))
//                    if (!onlinePhotosViewModel.isFilterApplied)
//                        jsonElements= new JSONArray();
//                        jsonElements.put("photodate desc");
////                        onlinePhotosViewModel.callGetPhotosAPI(getActivity(), projectId, null, jsonElements);
//                        onlinePhotosViewModel.callGetPhotosAPI(getActivity(), projectId, createJsonObjectWithoutFilters(), jsonElements);
//            } else {
////                if (onlinePhotoModels.size() > 0) {
////                    if (getActivity() != null && getActivity() instanceof PhotosActivity) {
////                        ((PhotosActivity) getActivity()).isShowIconReport.setValue(true);
////                        ((PhotosActivity) getActivity()).isShowFilterIcon.setValue(true);
////                    }
////                } else {
////                    if (getActivity() != null && getActivity() instanceof PhotosActivity) {
////                        ((PhotosActivity) getActivity()).isShowIconReport.setValue(false);
////                    }
////                }
//                if(getActivity() instanceof PhotosActivity) {
//                    ((PhotosActivity) getActivity()).isShowFilterIcon.setValue(true);
//                    ((PhotosActivity) getActivity()).isShowIconReport.setValue(true);
//                }
//
//                recyclerView.setVisibility(View.VISIBLE);
//                if (onlinePhotosViewModel.adapter != null) {
//                    onlinePhotosViewModel.adapter.notifyDataSetChanged();
//                    addEvent();
//                }else {
//                    updateUI(onlinePhotoModels, false);
//                }
//            }
//
//        });
        //endregion

        onlinePhotosViewModel.getIsLoadPhotos().observe(this, aBoolean -> {
            if (aBoolean) {
                if (onlinePhotosViewModel.getAdapter() != null && onlinePhotosViewModel.getAdapter().getItemCount() < 1) {
                    recyclerView.setVisibility(View.INVISIBLE);
                }
                onlineCachedPhotoCount = onlinePhotosViewModel.totalCountString;
            } else {
                recyclerView.setVisibility(View.INVISIBLE);
            }
        });

        //region minMaxDateFromApiObserver
        onlinePhotosViewModel.getMinMaxDateMap().observe(this, new Observer<Map<String, String>>() {
            @Override
            public void onChanged(Map<String, String> dateStringMap) {
                if (dateStringMap != null) {
                    defaultStartDate = dateStringMap.get("min_date");
                    defaultEndDate = dateStringMap.get("max_date");

//                    callOnlinePhotosApi();
//                    Log.d("DATE", "minMaxDateFromApi: min_date=" +defaultStartDate);
//                    Log.d("DATE", "minMaxDateFromApi: max_date=" +defaultEndDate);
//                    loadFacetsData();
                }
            }
        });
        //endregion

        return view;
    }

    private void addObserverListener() {
        onlinePhotosViewModel.photoPaths.observe(this, new Observer<List<OnlinePhotoModel>>() {
            @Override
            public void onChanged(List<OnlinePhotoModel> onlinePhotoModels) {
                if(onlinePhotoModels != null) {
                    if(onlinePhotoModels.size() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        if (onlinePhotosViewModel.adapter != null) {
                            onlinePhotosViewModel.adapter.notifyDataSetChanged();
                            addEvent();
                        } else {
                            updateUI(onlinePhotoModels, false);
                        }
                    }
                }
            }
        });
    }

    private void addEvent() {


        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if(onlinePhotosViewModel.photoPaths.getValue().size()>= onlinePhotosViewModel.totalCount) {
                    Utils.showLogger("online scrolling ends");
                    return;
                }
                    //                if (!onlinePhotosViewModel.isCached || (onlineCachedPhotoCount != null && !onlineCachedPhotoCount.equals("") && Integer.valueOf(onlineCachedPhotoCount) != onlinePhotosDBCount)) {
                if ((onlineCachedPhotoCount != null && !onlineCachedPhotoCount.equals("") && Integer.valueOf(onlineCachedPhotoCount) >= onlinePhotosDBCount)) {
                    onlinePhotosViewModel.isLoading = true;
                    onlinePhotosViewModel.currentPage += 1;

                    if(getActivity() != null) {
                        onlinePhotosViewModel.callGetPhotosAPI(getActivity(), projectId, jsonObject, jsonElements, ((PhotosActivity) getActivity()).groupheadingModelList);
                    }
                }
            }

            @Override
            public int getTotalPageCount() {
                return onlinePhotosViewModel.TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return onlinePhotosViewModel.isLastPage;
            }

            @Override
            public boolean isLoading() {

                return onlinePhotosViewModel.isLoading;
            }
        });

    }

    //region callOnlinePhotosApi
    private void callOnlinePhotosApi() {
        jsonElements= new JSONArray();
        jsonElements.put("photodate desc");
//      onlinePhotosViewModel.callGetPhotosAPI(getActivity(), projectId, null, jsonElements);
        if(getActivity() != null) {
            new DeleteAsyncTask(ProjectsDatabase.getDatabase(getActivity()).onlinePhotoDao()).execute();
            onlinePhotosViewModel.callGetPhotosAPI(getActivity(), projectId, createJsonObjectWithoutFilters(), jsonElements, ((PhotosActivity) getActivity()).groupheadingModelList);
        }
    }
    //endregion

    private void setAdapterSetting() {
        ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
//        int valueInPixels = (int) getContext().getResources().getDimension(R.dimen.size130);
        if (linearLayoutManager == null) {

            linearLayoutManager = new GridLayoutManager(getContext(),
                    projectDocuUtilities.calculateNoOfColumns(getActivity(), 130));
        }else {
            linearLayoutManager.setSpanCount(projectDocuUtilities.calculateNoOfColumns(getActivity(), 130));

        }
        recyclerView.setLayoutManager(linearLayoutManager);

//        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    public void updateUI(List<OnlinePhotoModel> onlinePhotoModelList, boolean isClearData) {
        if (onlinePhotosViewModel.photoPaths.getValue() != null && isClearData) {
            onlinePhotosViewModel.photoPaths.getValue().clear();
            onlinePhotosViewModel.photoPaths.getValue().addAll(onlinePhotoModelList);

        }

//        if (!isClearData) {
//            Collections.sort(onlinePhotoModelList, new Comparator<OnlinePhotoModel>() {
//                @Override
//                public int compare(OnlinePhotoModel lhs, OnlinePhotoModel rhs) {
//                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
//                    return lhs.getCreated_df() > rhs.getCreated_df() ? -1 : (lhs.getCreated_df() < rhs.getCreated_df()) ? 1 : 0;
//                }
//            });
//        }

        onlinePhotosViewModel.adapter = null;
//        onlinePhotosViewModel.adapter = new OnlinePhotosRecyclerAdapter(projectId, onlinePhotosViewModel.photoPaths.getValue(), isSelected, new OnlinePhotosListItemClickListener() {
        onlinePhotosViewModel.adapter = new OnlinePhotosRecyclerAdapter(projectId, onlinePhotoModelList, isSelected, new OnlinePhotosListItemClickListener() {
            @Override
            public void onListItemClick(OnlinePhotoModel onlinePhotoModel) {

                if (!isSelected) {
                    Intent intent = new Intent(getActivity(), SavePictureActivity.class);
                    intent.putExtra("isBackCamera", true);
                    intent.putExtra("projectId", onlinePhotoModel.getProjectId());
                    intent.putExtra("photoId", Long.valueOf(onlinePhotoModel.getPdphotoid()));
                    intent.putExtra("path", onlinePhotoModel.getPohotPath());
                    intent.putExtra("screenTitle", getResources().getString(R.string.photos_label_2));

                    intent.putExtra("isViewMode", true);
                    intent.putExtra("isFromOnlinePhotos", true);
                    getActivity().startActivity(intent);
                } else {
                    if (onlinePhotoModel.isPhotoSelected()) {
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
                }
            }
        });
        recyclerView.setAdapter(onlinePhotosViewModel.getAdapter());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

              if(isVisibleToUser){
                  isVisible = isVisibleToUser;
                  callOnlinePhotosApi();
                if(getActivity()!=null&&((PhotosActivity)getActivity()).isOnlinePhotoFilterApplied) {
                    ((PhotosActivity) getActivity()).isOnlinePhotoFilterApplied = false;
                    applyFilterOnlinePhotos(((PhotosActivity) getActivity()).groupheadingModelList,true);
//                Toast.makeText(getActivity(), "Called", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void bindView(View bindSource) {
        recyclerView = bindSource.findViewById(R.id.online_photos_rv);
    }


    private class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {
        private OnlinePhotoDao mAsyncTaskDao;

        DeleteAsyncTask(OnlinePhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... params) {

            mAsyncTaskDao.deleteAll();

            return null;
        }
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

    @Override
    public void updatePhotosResults(List<OnlinePhotoModel> onlinePhotoModelList, boolean isClearPrevList) {
        updateUI(onlinePhotoModelList, isClearPrevList);
    }


    public void applyFilterOnlinePhotos(List<GroupheadingModel> onlinePhotoModelList, boolean isClearPrevList) {
        filterDataList = onlinePhotoModelList;

        if (onlinePhotosViewModel.photoPaths.getValue() != null && isClearPrevList) {
            onlinePhotosViewModel.photoPaths.getValue().clear();
            new DeleteAsyncTask(ProjectsDatabase.getDatabase(getActivity()).onlinePhotoDao()).execute();
//            onlinePhotosViewModel.adapter=null;
//            linearLayoutManager = new GridLayoutManager(getActivity(), 3);
//            recyclerView.setLayoutManager(linearLayoutManager);
//            recyclerView.setItemAnimator(new DefaultItemAnimator());
            sharedPrefsManager.setStringValue(AppConstantsManager.ONLINE_PHOTO_COUNT, "");
            jsonObject=null; jsonElements=null;
            onlinePhotosViewModel.currentPage = 0;
            onlinePhotosDBCount=0;

        }

//        applyFilter(getActivity(), filterDataList);
        new PrepareFilterDataAsyncTask(getActivity()).execute(filterDataList);
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

    private void  applyFilter(Context mContext, List<GroupheadingModel> groupheadingModelList) {

        Utils.showLogger("applyFilterOnline");
        Utils.showLogger("applyingFilterSize>>"+groupheadingModelList.size());

        List<String> selectedStatus = new ArrayList<>();
        List<String> selectedArt = new ArrayList<>();
        List<String> selectedGewerk = new ArrayList<>();
        List<String> selectedDeadline = new ArrayList<>();
        List<String> selectedResponsible = new ArrayList<>();
        List<String> selectedCreator = new ArrayList<>();
        List<String> selectedPlan = new ArrayList<>();
        List<String> selectedDate = new ArrayList<>();

        String PREDICATE_PHOTO_TYPE = LocalPhotosRepository.TYPE_ONLINE_PHOTO;
        String PREDICATE_SORTING = "";
        String PREDICATE_DESCRIPTION = "";
        String PREDICATE_DESCRIPTION_SWITCH = "";
        String PREDICATE_KEYWORD = "";
        String PREDICATE_KEYWORD_SWITCH = "";
        String PREDICATE_DEADLINE = "";
        String PREDICATE_KEYWORDS = "";
        String PREDICATE_CREATOR = "";
        String PREDICATE_CREATED_DATE = "";

        long date = 0;
        long start_date = 0;
        long end_date = 0;

        String art = "";
        String tag_keyword = "";

        boolean isApplyGewerk = false;
        JSONArray jsonElementsParentField = new JSONArray();


        if (groupheadingModelList != null) {

            for (int i = 0; i < groupheadingModelList.size(); i++) {
                if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_desc))) {
                    if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                        art = groupheadingModelList.get(i).getKeyword();
//                            PREDICATE_DESCRIPTION = " AND description LIKE '%" + art+"%'" ;
                        PREDICATE_DESCRIPTION = " AND description LIKE '%" + art + "%'";

//                       {"operand": "LIKE", "field": "description", "value": "%searchstring
//                               %
//                               "
//                       }

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("operand", "LIKE");

                            jsonObject.put("field", "description");
                            jsonObject.put("value", "%" + art + "%");

                            jsonElementsParentField.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_number))) {
                    if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                        tag_keyword = groupheadingModelList.get(i).getKeyword();
                        PREDICATE_KEYWORD = " AND pdphotolocalId  =" + tag_keyword;
                        try {
//                       {"operand": „EQ", "field": "runid", "value": "56" }
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("operand", "EQ");
                            jsonObject.put("field", "runid");

                            jsonObject.put("value", tag_keyword);

                            jsonElementsParentField.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_deadline))) {
                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedDeadline.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            date = groupheadingModelList.get(i).getStart_date();
                        PREDICATE_DEADLINE = " AND fristdate_df <= " + date;
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_creator))) {
                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedResponsible.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        String inClause = selectedResponsible.toString();
                        inClause = inClause.replace("[", "(");
                        inClause = inClause.replace("]", ")");
                        PREDICATE_CREATOR = " AND pdUserId in " + inClause;


                        JSONObject jsonObjectMain = new JSONObject();
                        JSONArray jsonElementss = new JSONArray();
                        for (int j = 0; j < selectedResponsible.size(); j++) {
                            JSONObject jsonObject = new JSONObject();

                            try {
                                jsonObject.put("operand", "EQ");

                                jsonObject.put("field", "pduserid");
                                jsonObject.put("value", selectedResponsible.get(j));
                                jsonElementss.put(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (jsonElementss != null && jsonElementss.length() > 0) {
                            try {
                                jsonObjectMain.put("operator", "OR");

                                jsonObjectMain.put("fields", jsonElementss);
                                jsonElementsParentField.put(jsonObjectMain);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_decs))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {

                        PREDICATE_DESCRIPTION_SWITCH = " AND description !='' ";
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("operand", "NE");

                            jsonObject.put("field", "description");
                            jsonObject.put("value", "");

                            jsonElementsParentField.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

//                   {"operand": „NE", "field": „description", "value": "" }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_switch_keyword))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {

                        PREDICATE_DESCRIPTION_SWITCH = " AND params != '' ";

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("operand", "NE");
                            jsonObject.put("field", "params");

                            jsonObject.put("value", "||");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonElementsParentField.put(jsonObject);
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_localized_photo))) {
                    if (groupheadingModelList.get(i).isSwitchOn()) {

                        PREDICATE_DESCRIPTION_SWITCH = " AND params != '' ";

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("operand", "EQ");
                            jsonObject.put("field", "inplan");

                            jsonObject.put("value", "1");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonElementsParentField.put(jsonObject);
                    }


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_keyword))) {

                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedCreator.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        String inClause = selectedCreator.toString();
                        inClause = inClause.replace("[", "(");
                        inClause = inClause.replace("]", ")");
                        PREDICATE_KEYWORDS = " AND pdphotoid in " + inClause;

                        JSONObject jsonObjectMain = new JSONObject();
                        JSONArray jsonElementss = new JSONArray();
                        for (int j = 0; j < groupheadingModelList.get(i).getWordModelArrayList().size(); j++) {
                            JSONObject jsonObject = new JSONObject();

                            try {
                                jsonObject.put("operand", "LIKE");

                                jsonObject.put("field", "params");
                                if (groupheadingModelList.get(i).getWordModelArrayList().get(j).getType().equals("0"))
                                    jsonObject.put("value", "%|" + groupheadingModelList.get(i).getWordModelArrayList().get(j).getProjectParamId() + "=1|%");
                                else {

                                    if (groupheadingModelList.get(i).getWordModelArrayList().get(j).getValue() != null) {
                                        jsonObject.put("value", "%|" + groupheadingModelList.get(i).getWordModelArrayList().get(j).getProjectParamId() + "=" + groupheadingModelList.get(i).getWordModelArrayList().get(j).getValue() + "|%");
                                        Utils.showLogger(groupheadingModelList.get(i).getWordModelArrayList().get(j).getProjectParamId() + "=" + groupheadingModelList.get(i).getWordModelArrayList().get(j).getValue() + "|%");
                                    }
                                    }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Utils.showLogger("filter exception");
                            }
                            jsonElementss.put(jsonObject);
                        }

                        if (jsonElementss != null && jsonElementss.length() > 0) {
                            try {
                                jsonObjectMain.put("operator", "AND");

                                jsonObjectMain.put("fields", jsonElementss);
                                jsonElementsParentField.put(jsonObjectMain);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_plans))) {
                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedPlan.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                        String inClause = selectedPlan.toString();
                        inClause = inClause.replace("[", "(");
                        inClause = inClause.replace("]", ")");
                        PREDICATE_CREATOR = " AND pdplanid in " + inClause;
//                            "operand": „EQ", "field": "pdplanid", "value": „26"

                        JSONObject jsonObjectMain = new JSONObject();
                        JSONArray jsonElementss = new JSONArray();
                        for (int j = 0; j < selectedPlan.size(); j++) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("operand", "EQ");
                                jsonObject.put("field", "pdplanid");
                                jsonObject.put("value", selectedPlan.get(j).toString());
                                jsonElementss.put(jsonObject);
                            } catch (JSONException e) {

                            }
                        }


                        if (jsonElementss != null && jsonElementss.length() > 0) {

                            try {
                                jsonObjectMain.put("operator", "OR");

                                jsonObjectMain.put("fields", jsonElementss);
                            } catch (JSONException e) {

                            }
                            jsonElementsParentField.put(jsonObjectMain);
                        }
                    }

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_created_date))) {

//                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
//                            selectedDate.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                    start_date = groupheadingModelList.get(i).getStart_date();
                    end_date = groupheadingModelList.get(i).getEnd_date();

                    Date startDate = new Date(start_date);
                    Date endDate = new Date(end_date);
                    DateFormat f = new SimpleDateFormat("yyyy-MM-dd");


                    if (start_date > 0 && end_date > 0) {
                        PREDICATE_CREATED_DATE = " AND created_df >= " + start_date + " AND created_df <= " + end_date;

                        JSONObject jsonObjectMain = new JSONObject();
                        JSONArray jsonElementss = new JSONArray();

                        JSONObject jsonObject = new JSONObject();

                        try {
                            jsonObject.put("operand", "GTE");

                            jsonObject.put("field", "photodate");
                            jsonObject.put("value", f.format(startDate));
                            jsonElementss.put(jsonObject);

                            JSONObject jsonObject1 = new JSONObject();

                            jsonObject1.put("operand", "LTE");
                            jsonObject1.put("field", "photodate");
                            jsonObject1.put("value", f.format(endDate));
                            jsonElementss.put(jsonObject1);


                            if (jsonElementss != null && jsonElementss.length() > 0) {
                                jsonObjectMain.put("operator", "AND");
                                jsonObjectMain.put("fields", jsonElementss);
                                jsonElementsParentField.put(jsonObjectMain);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
//                        }
                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_sorting))) {
                    jsonElements = new JSONArray();
                    if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                        selectedStatus.addAll(groupheadingModelList.get(i).getListChildDataSelected());

//                            String inClause = selectedStatus.toString();
//                            inClause = inClause.replace("[", "(");
//                            inClause = inClause.replace("]", ")");
//                            PREDICATE_SORTING = " AND status in " + inClause;
                        if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("1")) {
                            PREDICATE_SORTING = "  ORDER BY created_df ASC ";
                            jsonElements.put("photodate");

                        } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("2")) {
                            PREDICATE_SORTING = "  ORDER BY created_df DESC ";
                            jsonElements.put("photodate desc");
                        } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("3")) {
                            PREDICATE_SORTING = "  ORDER BY runid ASC ";
                            jsonElements.put("runid");

                        } else if (selectedStatus.size() > 0 && selectedStatus.get(0).equals("4")) {
                            PREDICATE_SORTING = "  ORDER BY runid DESC ";

                            jsonElements.put("runid desc");
                        }
                    } else {
                        PREDICATE_SORTING = "  ORDER BY created_df DESC ";
                    }

                }
            }

            if (jsonElementsParentField != null && jsonElementsParentField.length() > 0) {


//                       "operator": "operator", "operator
                jsonObject = new JSONObject();
                try {
                    jsonObject.put("operator", "AND");

                    jsonObject.put("fields", jsonElementsParentField);
                } catch (JSONException e ) {
                    e.printStackTrace();
                }
             //   jsonElementsParentField.put(jsonObject);
                onlinePhotosViewModel.callGetPhotosAPI(getActivity(), projectId, jsonObject, jsonElements, groupheadingModelList);


            } else {
                onlinePhotosViewModel.callGetPhotosAPI(getActivity(), projectId, null, jsonElements, groupheadingModelList);

            }
            if (jsonObject != null) {
//                Log.d("online_photo", jsonObject + " /n " + jsonElements);

            }

        }


    }


    private class PrepareFilterDataAsyncTask extends AsyncTask<List<GroupheadingModel> , Void, Void> {
        Context mContext;
        PrepareFilterDataAsyncTask(Context context) {
            mContext=context;
        }

        @Override
        protected Void  doInBackground(final List<GroupheadingModel> ... params) {



            applyFilter(mContext,params[0]);

            return null;
        }
    }

    private JSONObject createJsonObjectWithoutFilters() {
        JSONObject mainJsonObject = new JSONObject();
        JSONObject filterJsonObject = new JSONObject();
        try{


            JSONArray fieldsOneJsonArray = new JSONArray();
            JSONArray fieldsTwoJsonArray = new JSONArray();

            JSONObject startJsonObject = new JSONObject();

            final Calendar c2 = Calendar.getInstance();
            c2.add(Calendar.YEAR, -5);
            int year2 = c2.get(Calendar.YEAR);
            int month2 = c2.get(Calendar.MONTH);
            int day2 = c2.get(Calendar.DAY_OF_MONTH);

            String dateOf5YearsBack = year2 + "-" + month2 + "-" + day2;

            startJsonObject.put("operand", "GTE");
            startJsonObject.put("field", "photodate");
//            startJsonObject.put("value", dateOf5YearsBack);
//            startJsonObject.put("value", "2017-03-29");
            startJsonObject.put("value", defaultStartDate);


            final Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, +1);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            String currentDate = year + "-" + month + "-" + day;

            JSONObject endJsonObject = new JSONObject();

            endJsonObject.put("operand", "LTE");
            endJsonObject.put("field", "photodate");
//            endJsonObject.put("value", currentDate);
//            endJsonObject.put("value", "2022-05-10");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendarNew = Calendar.getInstance();
            c.setTime(sdf.parse(defaultEndDate));
            c.add(Calendar.DATE, 1);
            defaultEndDate = sdf.format(c.getTime());
            endJsonObject.put("value", defaultEndDate);



            fieldsTwoJsonArray.put(startJsonObject);
            fieldsTwoJsonArray.put(endJsonObject);

            JSONObject fieldsOneJsonObject = new JSONObject();

            fieldsOneJsonObject.put("fields", fieldsTwoJsonArray);
            fieldsOneJsonObject.put("operator", "AND");

            fieldsOneJsonArray.put(fieldsOneJsonObject);

            filterJsonObject.put("fields", fieldsOneJsonArray);
            filterJsonObject.put("operator", "AND");

//            mainJsonObject.put("filter",fieldsOneJsonArray);

        }catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return filterJsonObject;
    }

    private JSONObject getMinMaxDates() {
        JSONObject mainJsonObject = new JSONObject();

        try{
            mainJsonObject.put("page", "0");
            mainJsonObject.put("limit", "1");
            mainJsonObject.put("filter", "");
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return mainJsonObject;
    }

    private JSONObject createJsonObjectWithoutFilters(String maxDate, String minDate) {
        JSONObject mainJsonObject = new JSONObject();
        JSONObject filterJsonObject = new JSONObject();
        try {


            JSONArray fieldsOneJsonArray = new JSONArray();
            JSONArray fieldsTwoJsonArray = new JSONArray();

            JSONObject startJsonObject = new JSONObject();

            startJsonObject.put("operand", "GTE");
            startJsonObject.put("field", "photodate");
            startJsonObject.put("value", minDate);

            JSONObject endJsonObject = new JSONObject();

            endJsonObject.put("operand", "LTE");
            endJsonObject.put("field", "photodate");
            endJsonObject.put("value", maxDate);

            fieldsTwoJsonArray.put(startJsonObject);
            fieldsTwoJsonArray.put(endJsonObject);

            JSONObject fieldsOneJsonObject = new JSONObject();

            fieldsOneJsonObject.put("fields", fieldsTwoJsonArray);
            fieldsOneJsonObject.put("operator", "AND");

            fieldsOneJsonArray.put(fieldsOneJsonObject);

            filterJsonObject.put("fields", fieldsOneJsonArray);
            filterJsonObject.put("operator", "AND");

//            mainJsonObject.put("filter",fieldsOneJsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return filterJsonObject;
    }




}
