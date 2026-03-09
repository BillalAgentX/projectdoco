package com.projectdocupro.mobile.activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.projectdocupro.mobile.fragments.DefectsListFragment.ARG_PROJECT_ID;
import static com.projectdocupro.mobile.fragments.DefectsListFragment.BR_ACTION_UPDATE_DEFECT_DATA;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.DefectsPagerAdapter;
import com.projectdocupro.mobile.adapters.ExpandableListAdapter;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.DefectsTradesDao;
import com.projectdocupro.mobile.dao.PdFlawFLagListDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.PlansDao;
import com.projectdocupro.mobile.dao.ProjectUsersDao;
import com.projectdocupro.mobile.fragments.AllPlansFragment;
import com.projectdocupro.mobile.fragments.DefectsListFragment;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.DefectsShortDetailListItemClickListener;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.DefectTradeModel;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.models.Pdflawflag;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.PlansPhotoModel;
import com.projectdocupro.mobile.models.mangel_filters.ChildRowModel;
import com.projectdocupro.mobile.models.mangel_filters.GroupheadingModel;
import com.projectdocupro.mobile.models.mangel_filters.ProjectUserModel;
import com.projectdocupro.mobile.repos.AllPlansRepository;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.repos.DefectTradesRepository;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.repos.PdFlawFlagRepository;
import com.projectdocupro.mobile.repos.ProjectDetailRepository;
import com.projectdocupro.mobile.utility.Utils;
import com.projectdocupro.mobile.viewModels.DefectsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefectsActivity extends AppCompatActivity implements DefectsListFragment.OnFragmentInteractionListener, AllPlansFragment.OnFragmentInteractionListener
        , DefectsShortDetailListItemClickListener {

    public static final int DEFECT_DETAIL_CODE = 9811;


    private DefectsPagerAdapter defectsPagerAdapter;

    private ViewPager defectsViewPager;

    private RelativeLayout toolbar;

    private TabLayout pagerTabStrip;

    private ExpandableListView simple_expandable_listview;

    private LinearLayout ll_hidenView;

    private LinearLayout ll_expand_view;

    private LinearLayout ll_filter_view;

    private RelativeLayout rl_short_detail_view;


    private TextView tv_reset_filter;

    private ImageView iv_next;
    private ImageView iv_previous;

    private LinearLayout ll_next;
    private LinearLayout ll_previous;
    public static final String BR_ACTION_UPDATE_PLAN_DEFECTS = "br_update_plan_defects";
    public static final String KEY_DEFECTS_ID_LIST = "defects_ids_list";

    public List<GroupheadingModel> groupheadingModelList = new ArrayList<>();
    ExpandableListAdapter expandableListAdapter;

    DefectRepository defectRepository;
    ProjectDetailRepository projectDetailRepository;
    DefectTradesRepository defectTradesRepository;

    String projectID;
    private List<DefectsModel> listOfMangels;
    private ArrayList<ChildRowModel> childRowModelList = new ArrayList<>();

    Set<DefectTradeModel> defectTradeModelHashSet = new HashSet<DefectTradeModel>();
    List<DefectTradeModel> defectTradeModelList = new ArrayList<DefectTradeModel>();
    int countTradeOff;
    private MediatorLiveData<List<DefectsModel>> mSectionLive = new MediatorLiveData<>();
    private boolean isFromDefect;
    private boolean isFromPhoto;

    public static final String TYPE_DEFECT_UPDATE = "defect_update";
    public static final String TYPE_DEFECT_ADD = "defect_add";
    public static final String TYPE_DEFECT_VIEW = "defect_view";
    public static final String TYPE_DEFECT_KEY = "defect_key";
    public static final String PHOTO_ID_KEY = "photoId";
    public static final String IS_CREATED_MANGEL_KEY = "isCreateMangel";
    private String photoId;
    public boolean isCreateMangel;
    private boolean isPlanAttach;

    // short detail resources

    private TextView tv_nr_no;


    private ImageView iv_status_red;

    private ImageView iv_status_orange;

    private ImageView iv_status_green;

    private RelativeLayout rl_art_view;

    private TextView tv_art_text;

    private TextView tv_gewerk;

    private TextView tv_end_date;

    private TextView tv_photo_date;

    private ImageView iv_photo;
    private ImageView iv_plans;

    private TextView tv_description_text;

    private TextView tv_res_user_name_text;


    private RelativeLayout rl_users_view;

    private TextView ll_mangel_detail_view;

    private TextView tv_defect_name_text;

    private LinearLayout ll_photos_view;

    private LinearLayout ll_plans_view;

    private LinearLayout ll_defect_bottom_tabs;

    public ImageView iv_sync_all;

    private ImageView iv_info;


    private DefectsModel defectsModelObj;

    private SharedPrefsManager sharedPrefsManager;
    private int currentObjectPos;
    private List<DefectsModel> defectsModelListt;

    AllPlansRepository plansRepository;
    PdFlawFlagRepository pdFlawFlagRepository;
    private SimpleDateFormat simpleDateFormat;
    private Pdflawflag flawFlagObj;


    private ImageView ivAdd;

    private ImageView ivFilter;

    private ImageView ivMenu;

    private View llCart;

    private TextView tv_count;

    private TextView tv_count_2;
    public String filterDefectsIds = "";

    public  ImageView img_Or_List;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defects);
        bindView();

        defectRepository = new DefectRepository(this, getIntent().getStringExtra("projectId"));
        plansRepository = new AllPlansRepository(this, getIntent().getStringExtra("projectId"));

//        defectRepository.deleteAllDefects();
        projectDetailRepository = new ProjectDetailRepository(getApplication(), getIntent().getStringExtra("projectId"));
        defectTradesRepository = new DefectTradesRepository(getApplication(), getIntent().getStringExtra("projectId"));
        projectID = getIntent().getStringExtra("projectId");
        photoId = getIntent().getStringExtra("photoId");
        isFromDefect = getIntent().getBooleanExtra(ProjectDetailActivity.IS_FROM_DEFECT_KEY, false);
        isCreateMangel = getIntent().getBooleanExtra("isCreateMangel", false);
        isPlanAttach = getIntent().getBooleanExtra("isPlanAttach", false);

        defectsPagerAdapter = new DefectsPagerAdapter(this, getSupportFragmentManager(), getIntent().getStringExtra("projectId"), photoId, isFromDefect);

        defectsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if(position==0)
                    img_Or_List.setVisibility(VISIBLE);
                else
                    img_Or_List.setVisibility(GONE);

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        defectsViewPager.setAdapter(defectsPagerAdapter);
        pagerTabStrip.setupWithViewPager(defectsViewPager);
        sharedPrefsManager = new SharedPrefsManager(this);
        loadFacetsData();


        addEvent();

        if (isCreateMangel) {
            new CreateLocalDefectAsyncTask().execute();
        }
        onDeviceRotate();


    }

    private void populateShortMangelDetail(DefectsModel defectsModel) {
        ll_hidenView.setVisibility(VISIBLE);
        ll_expand_view.setVisibility(VISIBLE);
        ll_filter_view.setVisibility(GONE);
        rl_short_detail_view.setVisibility(VISIBLE);

        ll_photos_view.setVisibility(GONE);
        ll_plans_view.setVisibility(GONE);


        if (defectsModel != null) {
            defectsModelObj = defectsModel;

            pdFlawFlagRepository = new PdFlawFlagRepository(DefectsActivity.this, defectsModelObj.getProjectId(), defectsModelObj.getDefectLocalId() + "");
            List<String> flawList = new ArrayList<>();
            flawList.add(defectsModel.defectLocalId + "");

            new RetrieveDefectFlagAsyncTask(pdFlawFlagRepository.getmDefectsPhotoDao(), flawList).execute(projectID);

            new RetrieveAsyncTask(ProjectsDatabase.getDatabase(this).photoDao()).execute(defectsModelObj.getProjectId(), defectsModelObj.getDefectLocalId() + "");

            if (defectsModel.getRunId() != null && !defectsModel.getRunId().equals("")) {
                tv_nr_no.setText("Nr. " + defectsModel.getRunId());
            } else {
                tv_nr_no.setText("Nr. ");
            }

            if (defectsModel.getDefectType() != null && !defectsModel.getDefectType().equals("")) {
                if (defectsModel.getDefectType().equals("1")) {
                    tv_art_text.setText(getResources().getString(R.string.mangel_art));
                } else if (defectsModel.getDefectType().equals("2")) {
                    tv_art_text.setText(getResources().getString(R.string.restleistung_art));
                }
            } else {
//                tv_art_text.setText(sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE,""));
                String flawType = sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_USED_FLAW_TYPE, "1");
                if (flawType.equals("1")) {
                    tv_art_text.setText(getResources().getString(R.string.mangel_art));
                } else if (flawType.equals("2")) {
                    tv_art_text.setText(getResources().getString(R.string.restleistung_art));
                }
            }

            if (defectsModel.getDescription() != null) {

                tv_description_text.setText(defectsModel.getDescription());
            }

            if (defectsModel.getDefectName() != null) {

                tv_defect_name_text.setText(defectsModel.getDefectName());
            }

            if (defectsModel.fristdate_df > 0) {
//                tv_end_date.setText(defectsModel.getFristDate());

                if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
                    simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

                } else {
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                }

                Date date = null;
                date = new Date(defectsModel.fristdate_df);
                tv_end_date.setText(simpleDateFormat.format(date));
            }
            if (defectsModel.getDiscipline() != null)
                tv_gewerk.setText(defectsModel.getDiscipline());


            if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("1")) {
                iv_status_orange.setImageResource(R.drawable.yellow_circle_selected);
                iv_status_red.setImageResource(R.drawable.red_circle_background);
                iv_status_green.setImageResource(R.drawable.green_circle_background);
            } else if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("2")) {
                iv_status_red.setImageResource(R.drawable.red_circle_selected);
                iv_status_orange.setImageResource(R.drawable.orange_circle_background);
                iv_status_green.setImageResource(R.drawable.green_circle_background);
            } else if (defectsModel.getStatus() != null && defectsModel.getStatus().equalsIgnoreCase("0")) {
                iv_status_green.setImageResource(R.drawable.green_circle_selected);
                iv_status_red.setImageResource(R.drawable.red_circle_background);
                iv_status_orange.setImageResource(R.drawable.orange_circle_background);
            }

            if (defectsModel.getResponsibleUser() != null && !defectsModel.getResponsibleUser().equals("")) {
                new RetrProjectUsersAsyncTask(projectDetailRepository.getWordDao()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, projectID, defectsModel.getResponsibleUser());
            }

        }
    }

    private void bindView() {
        defectsViewPager = findViewById(R.id.defects_view_pager);
        img_Or_List = findViewById(R.id.img_or_list);
        toolbar = findViewById(R.id.toolbar);
        pagerTabStrip = findViewById(R.id.defects_tab_strip);
        simple_expandable_listview = findViewById(R.id.simple_expandable_listview);
        ll_hidenView = findViewById(R.id.ll_hidenView);
        ll_expand_view = findViewById(R.id.ll_expand_view);
        ll_filter_view = findViewById(R.id.ll_filter_view);
        rl_short_detail_view = findViewById(R.id.rl_short_detail_view);
        tv_reset_filter = findViewById(R.id.tv_reset_filter);
        iv_next = findViewById(R.id.iv_next);
        iv_previous = findViewById(R.id.iv_previous);
        ll_next = findViewById(R.id.ll_next);
        ll_previous = findViewById(R.id.ll_previous);
        tv_nr_no = findViewById(R.id.tv_nr_no);
        iv_status_red = findViewById(R.id.iv_status_red);
        iv_status_orange = findViewById(R.id.iv_status_orange);
        iv_status_green = findViewById(R.id.iv_status_green);
        rl_art_view = findViewById(R.id.rl_art_view);
        tv_art_text = findViewById(R.id.art_text);
        tv_gewerk = findViewById(R.id.tv_gewerk);
        tv_end_date = findViewById(R.id.tv_end_date);
        tv_photo_date = findViewById(R.id.tv_photo_date);
        iv_photo = findViewById(R.id.iv_photo);
        iv_plans = findViewById(R.id.iv_plans);
        tv_description_text = findViewById(R.id.tv_description_text);
        tv_res_user_name_text = findViewById(R.id.name_text);
        rl_users_view = findViewById(R.id.rl_users_view);
        ll_mangel_detail_view = findViewById(R.id.ll_mangel_detail_view);
        tv_defect_name_text = findViewById(R.id.tv_defect_name_text);
        ll_photos_view = findViewById(R.id.ll_photos_view);
        ll_plans_view = findViewById(R.id.ll_plans_view);
        ll_defect_bottom_tabs = findViewById(R.id.ll_defect_bottom_tabs);
        iv_sync_all = findViewById(R.id.iv_sync_all);
        iv_info = findViewById(R.id.iv_info);
        ivAdd = findViewById(R.id.ivAdd);
        ivFilter = findViewById(R.id.ivFilter);
        ivMenu = findViewById(R.id.ivMenu);
        llCart = findViewById(R.id.ll_cart);
        tv_count = findViewById(R.id.tv_count);
        tv_count_2 = findViewById(R.id.tv_count_2);


    }


    private class RetrieveDefectFlagAsyncTask extends AsyncTask<String, Void, List<Pdflawflag>> {
        private PdFlawFLagListDao mAsyncTaskDao;
        List<String> defectlist;

        RetrieveDefectFlagAsyncTask(PdFlawFLagListDao dao, List<String> stringList) {
            mAsyncTaskDao = dao;
            defectlist = stringList;
        }

        @Override
        protected List<Pdflawflag> doInBackground(final String... params) {
            List<Pdflawflag> flawFlagList = mAsyncTaskDao.getFlawFlagList(params[0], defectlist);
            return flawFlagList;
        }

        @Override
        protected void onPostExecute(List<Pdflawflag> pdflawflags) {
            super.onPostExecute(pdflawflags);

            new RetrievePlansUsingFlawFlagAsyncTask(DefectsActivity.this, projectID).execute(pdflawflags);
        }
    }

    public void onDeviceRotate() {

        int currentOrientation = getResources().getConfiguration().orientation;

        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.v("TAG", "Landscape !!!");
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            try {
                display.getRealSize(size);
            } catch (NoSuchMethodError err) {
                display.getSize(size);
            }
            int width = size.x;
            int height = size.y;

//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)(width/3),
//                    LinearLayout.LayoutParams.MATCH_PARENT);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (width / 1.5),
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.BELOW, toolbar.getId());
            ll_expand_view.setLayoutParams(params);
//           Toast.makeText(this,"Land",Toast.LENGTH_SHORT).show();
        } else {
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMarginStart((int) getResources().getDimension(R.dimen.filter_width));
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.BELOW, toolbar.getId());

//            Toast.makeText(this,"Por",Toast.LENGTH_SHORT).show();

            ll_expand_view.setLayoutParams(params);
        }
    }


    private class RetrievePlansUsingFlawFlagAsyncTask extends AsyncTask<List<Pdflawflag>, Void, List<PlansModel>> {
        private PlansDao mAsyncTaskDao;
        List<String> stringList = new ArrayList<>();
        ProjectsDatabase projectsDatabase;
        boolean isFromDefectListing;
        String projectId = "";

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
            if (plansModelList != null && plansModelList.size() > 0)
                new loadPlansImagesAsyncTask(DefectsActivity.this).execute(plansModelList.get(0).getPlanId());
            return plansModelList;
        }

        @Override
        protected void onPostExecute(List<PlansModel> plansModelList) {
            super.onPostExecute(plansModelList);
        }
    }


    private class loadPlansImagesAsyncTask extends AsyncTask<String, Void, PlansPhotoModel> {
        private ProjectsDatabase database;
        PlansPhotoModel plansPhotoModel;
        boolean isOnpostCalled;

        loadPlansImagesAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected PlansPhotoModel doInBackground(final String... params) {
            PlansPhotoModel plansModel = database.planPhotosDao().getPlansPhotoObject(params[0]);

            return plansModel;
        }

        @Override
        protected void onPostExecute(PlansPhotoModel aVoid) {
            super.onPostExecute(aVoid);
            isOnpostCalled = true;
            plansPhotoModel = aVoid;

            if (plansPhotoModel != null) {
                ll_plans_view.setVisibility(VISIBLE);
                if (plansPhotoModel.getPohotPath() != null) {
                    Glide.with(DefectsActivity.this).load(plansPhotoModel.getPohotPath()).into(iv_plans);
                }
            }

        }

    }

    private void addEvent() {
        tv_count.setVisibility(GONE);
        tv_count_2.setVisibility(View.INVISIBLE);
        if ((photoId != null && !photoId.equals("")))
            ivAdd.setVisibility(View.INVISIBLE);
        else {
            ivAdd.setVisibility(VISIBLE);
        }

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateLocalDefectAsyncTask().execute();

            }
        });
        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_expand_view.getVisibility() != VISIBLE) {
                    showFilterView();
                } else {
                    hideFilterView();
                }

            }
        });
        defectsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    ll_defect_bottom_tabs.setVisibility(VISIBLE);
                } else {
                    ll_defect_bottom_tabs.setVisibility(GONE);

                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        iv_sync_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ProjectDocuUtilities.isNetworkConnected(DefectsActivity.this) || ProjectNavigator.wlanIsConnected(DefectsActivity.this)) {
                    {

                        Utils.showLogger2("AllDefectsSynchingStarted");
                        Fragment fragment = defectsPagerAdapter.m1stFragment;
                        if (fragment != null) {
                            DefectsListFragment defectsListFragment = (DefectsListFragment) fragment;

                            if (defectsListFragment.syncAllDefects())
                                defectsListFragment.defectsViewModel.getDefectsRepository().getDefectsAPIAndIsertIntoTheTable(DefectsActivity.this, getIntent().getStringExtra("projectId"));
                            else {
                                Utils.showLogger2("fetchingNewDefectsFromServer");
                                defectsListFragment.fetchDefectsFromServerAfterSynchingDefects = true;
                            }
                        }
                    }
                }
            }
        });

        iv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DefectsActivity.this, DefectSyncStatusActivity.class).putExtra("projectId", getIntent().getStringExtra("projectId")).putExtra("isPhotoFromGallary", true));
            }
        });

        ll_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (defectsModelListt != null && defectsModelListt.size() > 0 && currentObjectPos > 0) {
                    currentObjectPos--;
                    populateShortMangelDetail(defectsModelListt.get(currentObjectPos));
                }
            }
        });

        ll_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defectsModelListt != null && defectsModelListt.size() > 0 && currentObjectPos >= 0 && currentObjectPos < defectsModelListt.size() - 1) {
                    currentObjectPos++;
                    populateShortMangelDetail(defectsModelListt.get(currentObjectPos));
                }
            }
        });

        ll_mangel_detail_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defectsModelListt != null && defectsModelListt.size() > 0 && currentObjectPos >= 0 && currentObjectPos < defectsModelListt.size()) {

                    DefectsModel defectsModel = defectsModelListt.get(currentObjectPos);
                    Intent intent = new Intent(DefectsActivity.this, DefectDetailsActivity2.class);
                    intent.putExtra(ARG_PROJECT_ID, defectsModel.getProjectId());
                    intent.putExtra(DefectsListFragment.ARG_PARAM2, defectsModel.getDefectLocalId() + "");
                    intent.putExtra("flaw_id", defectsModel.getDefectId());
                    intent.putExtra("photoId", photoId);
                    intent.putExtra(IS_CREATED_MANGEL_KEY, isCreateMangel);
                    if (photoId != null && !photoId.equals("")) {
//                        intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_VIEW);
                        intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_UPDATE);
                    } else {
                        intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_UPDATE);

                    }
                    startActivity(intent);
                    ll_hidenView.performClick();
                }
            }
        });

        ll_hidenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_hidenView.setVisibility(GONE);
                ll_expand_view.setVisibility(GONE);
                rl_short_detail_view.setVisibility(GONE);

            }
        });

        tv_reset_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter(DefectsActivity.this);
            }
        });
        simple_expandable_listview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (groupheadingModelList.get(groupPosition).isMultiSelect()) {
                    ChildRowModel childRowModel = groupheadingModelList.get(groupPosition).getListChildData().get(groupheadingModelList.get(groupPosition).getType()).get(childPosition);
                    if (groupheadingModelList.get(groupPosition).getListChildDataSelected() != null) {
                        if (groupheadingModelList.get(groupPosition).getListChildDataSelected().contains(childRowModel.getId())) {
                            groupheadingModelList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());

                        } else {
                            groupheadingModelList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                        }
                    }
                } else {
                    ChildRowModel childRowModel = groupheadingModelList.get(groupPosition).getListChildData().get(groupheadingModelList.get(groupPosition).getType()).get(childPosition);
                    if (groupheadingModelList.get(groupPosition).getListChildDataSelected() != null) {

                        if (groupheadingModelList.get(groupPosition).getListChildDataSelected().size() > 0) {
                            if (groupheadingModelList.get(groupPosition).getListChildDataSelected().get(0).equals(childRowModel.getId())) {
                                groupheadingModelList.get(groupPosition).getListChildDataSelected().remove(childRowModel.getId());
                            } else {
                                groupheadingModelList.get(groupPosition).getListChildDataSelected().set(0, childRowModel.getId());

                            }
                        } else {
                            groupheadingModelList.get(groupPosition).getListChildDataSelected().add(childRowModel.getId());
                        }
                    }
                }
                if (expandableListAdapter != null)
                    expandableListAdapter.notifyDataSetChanged();

                applyFilter();
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_optoin_menu_filter, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                // Set the text color to red
                if (ll_expand_view.getVisibility() != VISIBLE) {
                    showFilterView();
                } else {
                    hideFilterView();
                }


                return true;
            case R.id.add:

                new CreateLocalDefectAsyncTask().execute();


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onListItemClick(List<DefectsModel> defectsModelList, DefectsModel defectsModel) {
        defectsModelListt = defectsModelList;
        currentObjectPos = defectsModelList.indexOf(defectsModel);
        populateShortMangelDetail(defectsModel);
    }

    private class CreateLocalDefectAsyncTask extends AsyncTask<Void, Void, DefectsModel> {
        long local_flaw_id = 0;

        @Override
        protected DefectsModel doInBackground(final Void... params) {

            ProjectsDatabase db = ProjectsDatabase.getDatabase(DefectsActivity.this);
            PdFlawFlagRepository pdFlawFlagRepository = new PdFlawFlagRepository(DefectsActivity.this);
            DefectTradesRepository mRepositoryDefecttrade = new DefectTradesRepository(DefectsActivity.this, projectID);
            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(DefectsActivity.this);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            final Calendar c = Calendar.getInstance();
            int yearCurrent = c.get(Calendar.YEAR);
            int monthCurrent = c.get(Calendar.MONTH);
            int dayCurrent = c.get(Calendar.DAY_OF_MONTH);
            String createdDate = yearCurrent + "-" + (monthCurrent + 1) + "-" + dayCurrent;
            String dateString1 = sharedPrefsManager.getStringValue(AppConstantsManager.PD_USER_ID, "");
            String creator = sharedPrefsManager.getStringValue(AppConstantsManager.USER_FIRST_NAME, "") + " " + sharedPrefsManager.getStringValue(AppConstantsManager.USER_LAST_NAME, "");
            DefectsModel defectsModel = new DefectsModel("", "", projectID, dateString1, "", "", dateFormat.format(date),
                    "2", creator, "", dateFormat.format(date), "", "", dateFormat.format(date), "0000-00-00 00:00:00", "0000-00-00 00:00:00", "0000-00-00 00:00:00", dateString1, "");
            defectsModel.setCreateDate_df(date.getTime());
            defectsModel.setUploadStatus(DefectRepository.UN_SYNC_PHOTO);
            defectsModel.setDeleted("0");
            local_flaw_id = db.defectsDao().insert(defectsModel);

            defectsModel.setDefectLocalId(local_flaw_id);


            defectTradesRepository.addLocalMangelGewerk(defectTradesRepository.getmDefectsTradeDao(), projectID, local_flaw_id + "");

            if (isPlanAttach) {

                pdFlawFlagRepository = new PdFlawFlagRepository(DefectsActivity.this, projectID);

                flawFlagObj = pdFlawFlagRepository.getmDefectsPhotoDao().getFlawFlagObjUsingPhotoID(projectID, sharedPrefsManager.getLastUsedPlanId(DefectsActivity.this), photoId + "");

                flawFlagObj.setFlaw_Id(local_flaw_id + "");
                flawFlagObj.setLocal_flaw_Id(local_flaw_id + "");
                flawFlagObj.setFlaw_Id(local_flaw_id + "");
                flawFlagObj.setLocal_photo_id(photoId + "");
//                flawFlagObj.setPdFlawFlagServerId("");
                pdFlawFlagRepository.getmDefectsPhotoDao().insert(flawFlagObj);
            }


            return defectsModel;
        }

        @Override
        protected void onPostExecute(DefectsModel defectsModel) {
            super.onPostExecute(defectsModel);

            Intent intent = new Intent(DefectsActivity.this, DefectDetailsActivity2.class);
            intent.putExtra(ARG_PROJECT_ID, projectID);
            intent.putExtra(DefectsListFragment.ARG_PARAM2, local_flaw_id + "");
            intent.putExtra(DefectsActivity.TYPE_DEFECT_KEY, DefectsActivity.TYPE_DEFECT_ADD);
            intent.putExtra("FROM_CREATE", true);
            intent.putExtra(DefectsActivity.PHOTO_ID_KEY, photoId);
            intent.putExtra(IS_CREATED_MANGEL_KEY, isCreateMangel);
            startActivityForResult(intent, DEFECT_DETAIL_CODE);
            if (defectsModel != null) {
                if (defectsPagerAdapter != null && defectsPagerAdapter.m1stFragment != null) {
//                    if (defectsPagerAdapter.m1stFragment.getAdapter() != null && defectsPagerAdapter.m1stFragment.getAdapter().getDefectsModels() != null) {
//                        defectsPagerAdapter.m1stFragment.getAdapter().getDefectsModels().add(defectsModel);
//                        defectsPagerAdapter.notifyDataSetChanged();
//                    }
                    Utils.showLogger2("loadFromParentBroadcast");
                    defectsPagerAdapter.m1stFragment.loadDefects("loadFromParentBroadcast");
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DEFECT_DETAIL_CODE:
                if (resultCode == Activity.RESULT_OK) {
//                    loadFacetsData();
                }
                break;
        }

    }

    private void showFilterView() {
//        loadFacetsData();
        ll_hidenView.setVisibility(VISIBLE);
        ll_expand_view.setVisibility(VISIBLE);
        ll_filter_view.setVisibility(VISIBLE);
        rl_short_detail_view.setVisibility(GONE);
    }

    private void hideFilterView() {
        ll_hidenView.setVisibility(GONE);
        ll_expand_view.setVisibility(GONE);
    }

    @Override
    public void onBackPressed() {
        setResult(101, new Intent());
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onAllFragmentInteraction(Uri uri) {

    }

    private void loadFacetsData() {
        countTradeOff = 0;
        groupheadingModelList.clear();
        defectTradeModelList.clear();
        childRowModelList.clear();

        List<ChildRowModel> childRowModelList3 = new ArrayList<>();
        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_photo_number), getResources().getString(R.string.heading_photo_number), false, new HashMap<String, List<ChildRowModel>>() {{
            put(getResources().getString(R.string.heading_photo_number), childRowModelList3);
        }}));

        new RetriveStatusAsyncTask(defectRepository.getmDefectDao()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, projectID);
        expandableListAdapter = new ExpandableListAdapter(DefectsActivity.this, groupheadingModelList);
        simple_expandable_listview.setAdapter(expandableListAdapter);


    }

    private void pupulateFristDate() {
        List<ChildRowModel> childRowModelList = new ArrayList<>();
        ChildRowModel childRowModel = new ChildRowModel();
        childRowModel.setId("1");
        childRowModel.setTitle(getResources().getString(R.string.open_status));
        childRowModelList.add(childRowModel);
        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_deadline), getResources().getString(R.string.heading_deadline), false, new HashMap<String, List<ChildRowModel>>() {{
            put(getResources().getString(R.string.heading_deadline), childRowModelList);
        }}));
    }

    private void pupulateDate() {
        List<ChildRowModel> childRowModelList = new ArrayList<>();
        ChildRowModel childRowModel = new ChildRowModel();
        childRowModel.setId("1");
        childRowModel.setTitle(getResources().getString(R.string.open_status));
        childRowModelList.add(childRowModel);
        groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_date), getResources().getString(R.string.heading_date), false, new HashMap<String, List<ChildRowModel>>() {{
            put(getResources().getString(R.string.heading_date), childRowModelList);
        }}));
    }


    private class RetrProjectUsersAsyncTask extends AsyncTask<String, Void, ProjectUserModel> {

        private ProjectUsersDao mAsyncTaskDao;

        RetrProjectUsersAsyncTask(ProjectUsersDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected ProjectUserModel doInBackground(final String... params) {
            ProjectUserModel stringList = mAsyncTaskDao.getProjectUserInfo(params[0], params[1]);
            return stringList;
        }

        @Override
        protected void onPostExecute(ProjectUserModel params) {
            super.onPostExecute(params);
            if (params != null) {

                if ((params.getFirstname() != null && !params.getFirstname().equals("")) || (params.getLastname() != null && !params.getLastname().equals(""))) {
                    SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(DefectsActivity.this);
                    String firstName = params.getFirstname();
                    String lastName = params.getLastname();

//                    if (params.getLastname() != null && !params.getLastname().equals("")) {
//                        tv_res_user_name_text.setText(params.getFirstname() + " " + params.getLastname());
//                    } else
//                        tv_res_user_name_text.setText(params.getFirstname());

                    if (tv_res_user_name_text != null && firstName != null && lastName != null) {
                        tv_res_user_name_text.setText(firstName + " " + lastName);
                    } else
                        tv_res_user_name_text.setText(lastName);

                }
            }
        }
    }


    private class RetriveStatusAsyncTask extends AsyncTask<String, Void, List<String>> {

        private DefectsDao mAsyncTaskDao;

        RetriveStatusAsyncTask(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<String> doInBackground(final String... params) {
            List<String> stringList = mAsyncTaskDao.getDefectsUniqueStatusObject(params[0]);
            return stringList;
        }

        @Override
        protected void onPostExecute(List<String> params) {
            super.onPostExecute(params);
            List<ChildRowModel> childRowModelList = new ArrayList<>();
            ChildRowModel childRowModel11 = new ChildRowModel();


            boolean isFirstTimeAdded = false;
            for (int i = 0; i < params.size(); i++) {
                ChildRowModel childRowModel = new ChildRowModel();
//                if (!isFirstTimeAdded) {
//                    isFirstTimeAdded = true;
//                    childRowModelList.add(0, childRowModel11);
//                }
//                if (params.get(i).equals("0") && params.size() > 0) {
//                    childRowModelList.add(1, childRowModel11);
//                    childRowModel.setId(params.get(i));
//                    if (params.get(i) != null && !params.get(i).equals(""))
//                        childRowModel.setId_sorting(Integer.valueOf(params.get(i)));
//                    childRowModel.setTitle(getResources().getString(R.string.close_status));
//                    childRowModelList.set(1, childRowModel);
//                } else if (params.get(i).equals("1")) {
//                    childRowModel.setId(params.get(i));
//                    if (params.get(i) != null && !params.get(i).equals(""))
//                        childRowModel.setId_sorting(Integer.valueOf(params.get(i)));
//                    childRowModel.setTitle(getResources().getString(R.string.progress_status));
//                    childRowModelList.add(childRowModel);
//                } else if (params.get(i).equals("2")) {
//
//                    childRowModel.setId(params.get(i));
//                    if (params.get(i) != null && !params.get(i).equals(""))
//                        childRowModel.setId_sorting(Integer.valueOf(params.get(i)));
//                    childRowModel.setTitle(getResources().getString(R.string.open_status));
//                    childRowModelList.set(0, childRowModel);
//                }

            }

//            if(params.size() > 0) {
//                childRowModelList.add(1, new ChildRowModel(getResources().getString(R.string.close_status), "0", Integer.valueOf("0")));
//                childRowModelList.add( 2, new ChildRowModel(getResources().getString(R.string.progress_status), "1", Integer.valueOf("1")));
//                childRowModelList.add( 3, new ChildRowModel(getResources().getString(R.string.open_status), "2", Integer.valueOf("2")));
//            } else {
//                childRowModelList.add(0, new ChildRowModel(getResources().getString(R.string.close_status), "0", Integer.valueOf("0")));
//                childRowModelList.add( 1, new ChildRowModel(getResources().getString(R.string.progress_status), "1", Integer.valueOf("1")));
//                childRowModelList.add( 2, new ChildRowModel(getResources().getString(R.string.open_status), "2", Integer.valueOf("2")));
//            }

            childRowModelList.add(0, new ChildRowModel(getResources().getString(R.string.close_status), "0", Integer.valueOf("0")));
            childRowModelList.add(1, new ChildRowModel(getResources().getString(R.string.progress_status), "1", Integer.valueOf("1")));
            childRowModelList.add(2, new ChildRowModel(getResources().getString(R.string.open_status), "2", Integer.valueOf("2")));


//            if(childRowModelList!=null&&childRowModelList.size()>0){
//                List<ChildRowModel> childRowModelListTemp = new ArrayList<>();
//                for (int i = 0; i < childRowModelList.size(); i++)
//                {
//                    for (int j = i + 1; j < childRowModelList.size(); j++) {
//                        if (childRowModelList.get(i).getId_sorting() > childRowModelList.get(j).getId_sorting())
//                        {
//                            childRowModelListTemp.add(childRowModelList.get(i)) ;
//                            childRowModelList.add(i,childRowModelList.get(j));
//                            num[i] = num[j];
//                            num[j] = temp;
//                        }
//                    }
//                }
//            }

//            Collections.sort(childRowModelList, new Comparator<ChildRowModel>() {
//                @Override
//                public int compare(ChildRowModel o1, ChildRowModel o2) {
//                    return o2.getId_sorting().compareTo(o1.getId_sorting());
//                }
//            });
            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_status), getResources().getString(R.string.heading_status), true, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_status), childRowModelList);
            }}));
            if (expandableListAdapter != null) {
                expandableListAdapter.notifyDataSetChanged();
            }
            new RetriveMangelAsyncTask(defectRepository.getmDefectDao()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, projectID);


        }


    }

    private class RetriveMangelsOfPorjectAsyncTask extends AsyncTask<String, Void, List<DefectsModel>> {

        private DefectsDao mAsyncTaskDao;
        String ProjectId = "";

        RetriveMangelsOfPorjectAsyncTask(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<DefectsModel> doInBackground(final String... params) {
            ProjectId = params[0];

            listOfMangels = mAsyncTaskDao.getDefectsListtt(params[0]);
            return listOfMangels;
        }

        @Override
        protected void onPostExecute(List<DefectsModel> params) {
            super.onPostExecute(params);

            if (params != null) {
                List<String> stringList = new ArrayList<>();
                for (int i = 0; i < params.size(); i++) {
                    stringList.add(params.get(i).getDefectLocalId() + "");
                }
                new RetriveGewerkAsyncTask(defectTradesRepository.getmDefectsTradeDao()).execute(stringList);

            }

        }


        private class RetriveGewerkAsyncTask extends AsyncTask<List<String>, Void, List<DefectTradeModel>> {

            private DefectsTradesDao mAsyncTaskDao;

            RetriveGewerkAsyncTask(DefectsTradesDao dao) {
                mAsyncTaskDao = dao;
            }

            @Override
            protected List<DefectTradeModel> doInBackground(final List<String>... params) {
                List<DefectTradeModel> listOfGewerk = mAsyncTaskDao.getAllDefectTradeWithStatusONModelUsingDefectList(projectID, params[0]);
                return listOfGewerk;
            }

            @Override
            protected void onPostExecute(List<DefectTradeModel> list) {
                // super.onPostExecute(list);
                boolean addUnique = false;
                countTradeOff++;
                if (list != null && list.size() > 0) {
                    ChildRowModel tempChildRowModel = new ChildRowModel();
                    for (int i = 0; i < list.size(); i++) {
                        DefectTradeModel defectTradeModel = new DefectTradeModel();
                        defectTradeModel = list.get(i);
                        defectTradeModelList.add(list.get(i));

                    }

//                if (countTradeOff == listOfMangels.size()) {

                    for (int i = 0; i < defectTradeModelList.size(); i++) {
                        for (int j = i + 1; j < defectTradeModelList.size(); j++) {
                            if (defectTradeModelList.get(i).getPdservicetitle().equals(defectTradeModelList.get(j).getPdservicetitle())) {
                                defectTradeModelList.remove(j);
                                j--;
                            }
                        }
                    }

                    for (int i = 0; i < defectTradeModelList.size(); i++) {
                        ChildRowModel childRowModel = new ChildRowModel();
                        DefectTradeModel defectTradeModel = defectTradeModelList.get(i);
                        childRowModel.setTitle(defectTradeModel.getPdservicetitle());
                        childRowModel.setId(defectTradeModel.getSelectvalue());
                        childRowModelList.add(childRowModel);


                    }

                    groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_gewerk), getResources().getString(R.string.heading_gewerk), true, new HashMap<String, List<ChildRowModel>>() {{
                        put(getResources().getString(R.string.heading_gewerk), childRowModelList);
                    }}));

//                }
                    if (expandableListAdapter != null)
                        expandableListAdapter.notifyDataSetChanged();
                    pupulateFristDate();
                    new RetriveProjectUsersAsyncTask(projectDetailRepository.getWordDao()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, projectID);

                }

            }

        }

    }

    private class RetriveMangelAsyncTask extends AsyncTask<String, Void, List<String>> {

        private DefectsDao mAsyncTaskDao;

        RetriveMangelAsyncTask(DefectsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<String> doInBackground(final String... params) {
            List<String> stringList = mAsyncTaskDao.getDefectsUniqueDefectObject(params[0]);
            return stringList;
        }

        @Override
        protected void onPostExecute(List<String> params) {
            super.onPostExecute(params);
            List<ChildRowModel> childRowModelList = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                ChildRowModel childRowModel = new ChildRowModel();

                if (params.get(i).equals("1")) {
                    childRowModel.setId(params.get(i));
                    childRowModel.setTitle(getResources().getString(R.string.mangel_art));
                    childRowModelList.add(childRowModel);
                } else if (params.get(i).equals("2")) {
                    childRowModel.setId(params.get(i));
                    childRowModel.setTitle(getResources().getString(R.string.restleistung_art));
                    childRowModelList.add(childRowModel);
                }

            }
            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_art), getResources().getString(R.string.heading_art), false, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_art), childRowModelList);
            }}));
            new RetriveMangelsOfPorjectAsyncTask(defectRepository.getmDefectDao()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, projectID);


            if (expandableListAdapter != null) {
                expandableListAdapter.notifyDataSetChanged();
            }
        }
    }


    private class RetriveProjectUsersAsyncTask extends AsyncTask<String, Void, List<ProjectUserModel>> {

        private ProjectUsersDao mAsyncTaskDao;

        RetriveProjectUsersAsyncTask(ProjectUsersDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<ProjectUserModel> doInBackground(final String... params) {
            List<ProjectUserModel> stringList = mAsyncTaskDao.getDistinctProjectUserListInfo();
            return stringList;
        }

        @Override
        protected void onPostExecute(List<ProjectUserModel> params) {
            super.onPostExecute(params);
            List<ChildRowModel> childRowModelList = new ArrayList<>();
            for (int i = 0; i < params.size(); i++) {
                ChildRowModel childRowModel = new ChildRowModel();
                childRowModel.setId(params.get(i).getPduserid());

                if ((params.get(i).getFirstname() != null && !params.get(i).getFirstname().equals(""))
                        || (params.get(i).getLastname() != null && !params.get(i).getLastname().equals(""))) {
                    childRowModel.setTitle(params.get(i).getFirstname() + " " + params.get(i).getLastname());
                } else if ((params.get(i).getFirstname() == null || params.get(i).getFirstname().equals(""))
                        && (params.get(i).getLastname() == null || params.get(i).getLastname().equals(""))) {
                    childRowModel.setTitle(params.get(i).getEmail());
                }


                childRowModelList.add(childRowModel);
            }


            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_responsible), getResources().getString(R.string.heading_responsible), true, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_responsible), childRowModelList);
            }}));
            groupheadingModelList.add(new GroupheadingModel(getResources().getString(R.string.heading_creator), getResources().getString(R.string.heading_creator), true, new HashMap<String, List<ChildRowModel>>() {{
                put(getResources().getString(R.string.heading_creator), childRowModelList);
            }}));
//            pupulateDate();
            if (expandableListAdapter != null) {
                expandableListAdapter.notifyDataSetChanged();
            }
        }


    }

    public void applyFilter() {

        new RetriveFilteredDataAsyncTask(this, defectRepository.getmDefectDao()).execute(groupheadingModelList);
        PlansActivity.hideKeyboard(DefectsActivity.this);
    }

    int filterCount = 0;

    class RetriveFilteredDataAsyncTask extends AsyncTask<List<GroupheadingModel>, Void, List<DefectsModel>> {

        private DefectsDao mAsyncTaskDao;
        Context mContext;

        RetriveFilteredDataAsyncTask(Context context, DefectsDao dao) {
            mAsyncTaskDao = dao;
            mContext = context;
        }

        @Override
        protected List<DefectsModel> doInBackground(final List<GroupheadingModel>... params) {


            // Single Selection
            // Art
            // Date
            // Deadline

            List<String> selectedStatus = new ArrayList<>();
            List<String> selectedArt = new ArrayList<>();
            List<String> selectedGewerk = new ArrayList<>();
            List<String> selectedDeadline = new ArrayList<>();
            List<String> selectedResponsible = new ArrayList<>();
            List<String> selectedCreator = new ArrayList<>();
            List<String> selectedDate = new ArrayList<>();

            String PREDICATE_STATUS = "";
            String PREDICATE_DESCRIPTION = "";
            String PREDICATE_ART = "";
            String PREDICATE_GEWERK = "";
            String PREDICATE_DEADLINE = "";
            String PREDICATE_RESPONSIBLE = "";
            String PREDICATE_CREATOR = "";
            String PREDICATE_DATE = "";
            String ORDER_BY = " ORDER BY runidInt DESC";

            long date = 0;
            long start_date = 0;
            long end_date = 0;

            String art = "";

            boolean isApplyGewerk = false;
            filterCount = 0;
            List<GroupheadingModel> groupadheadingModelList = params[0];
            if (groupheadingModelList != null) {

                for (int i = 0; i < groupheadingModelList.size(); i++) {

                    if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_number))) {
                        if (groupheadingModelList.get(i).getKeyword() != null && !groupheadingModelList.get(i).getKeyword().equals("")) {
                            art = groupheadingModelList.get(i).getKeyword();
//                            PREDICATE_DESCRIPTION = " AND description LIKE '%" + art+"%'" ;
//                            PREDICATE_DESCRIPTION = " AND ( defectName LIKE '%"+ art + "%')";
                            PREDICATE_DESCRIPTION = " AND (runId LIKE '%" + art + "%' OR " + "flawname LIKE '%" + art + "%' OR " + "description LIKE '%" + art + "%')";
                            filterCount++;
                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_status))) {

                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedStatus.addAll(groupheadingModelList.get(i).getListChildDataSelected());

                            String inClause = selectedStatus.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_STATUS = " AND status in " + inClause;
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();

                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_art))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedArt.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            art = groupheadingModelList.get(i).getListChildDataSelected().get(0);
                            PREDICATE_ART = " AND flawtype = " + art;
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();

                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_gewerk))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedGewerk.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            isApplyGewerk = true;
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();

                        }
                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_deadline))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedDeadline.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            date = groupheadingModelList.get(i).getStart_date();
                            PREDICATE_DEADLINE = " AND fristdate_df <= " + date + " AND status != 0";
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();
                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_responsible))) {
                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedResponsible.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            String inClause = selectedResponsible.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_RESPONSIBLE = " AND responsibleuser in " + inClause;
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();
                        }


                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_creator))) {

                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedCreator.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            String inClause = selectedCreator.toString();
                            inClause = inClause.replace("[", "(");
                            inClause = inClause.replace("]", ")");
                            PREDICATE_RESPONSIBLE = " AND creator_id in " + inClause;
                            filterCount = filterCount + groupheadingModelList.get(i).getListChildDataSelected().size();

                        }

                    } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_date))) {

                        if (groupheadingModelList.get(i).getListChildDataSelected().size() > 0) {
                            selectedDate.addAll(groupheadingModelList.get(i).getListChildDataSelected());
                            start_date = groupheadingModelList.get(i).getStart_date();
                            end_date = groupheadingModelList.get(i).getEnd_date();

                            if (groupheadingModelList.get(i).getListChildDataSelected().get(0).equalsIgnoreCase("1")) {
                                PREDICATE_DATE = " AND noticeDate_df >= " + start_date + " AND noticeDate_df <= " + end_date;

                            } else if (groupheadingModelList.get(i).getListChildDataSelected().get(0).equalsIgnoreCase("2")) {
                                PREDICATE_DATE = " AND notifiedate_df >= " + start_date + " AND notifiedate_df <= " + end_date;

                            } else if (groupheadingModelList.get(i).getListChildDataSelected().get(0).equalsIgnoreCase("3")) {
                                PREDICATE_DATE = " AND donedate_df >= " + start_date + " AND donedate_df <= " + end_date;
                            }

                        }

                    }
                }
            }
            if (filterCount == 0) {
                Intent intent = new Intent(BR_ACTION_UPDATE_DEFECT_DATA);
                sendBroadcast(intent);
                return null;
            }
            List<DefectsModel> defectsModelList = new ArrayList<>();
            SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM  DefectsModel WHERE projectId = ? " + PREDICATE_DESCRIPTION + PREDICATE_STATUS + PREDICATE_ART + PREDICATE_RESPONSIBLE + PREDICATE_CREATOR + PREDICATE_DEADLINE + PREDICATE_DATE + ORDER_BY,
                    new Object[]{projectID});

            SimpleSQLiteQuery query2 = new SimpleSQLiteQuery("SELECT * FROM  DefectsModel WHERE projectId = ? AND pdflawid = '' " + PREDICATE_DESCRIPTION + PREDICATE_STATUS + PREDICATE_ART + PREDICATE_RESPONSIBLE + PREDICATE_CREATOR + PREDICATE_DEADLINE + PREDICATE_DATE + ORDER_BY,
                    new Object[]{projectID});


            List<DefectsModel> defectsModels = mAsyncTaskDao.getFilterListViaQuery(query);
            List<DefectsModel> localDefectsModels = mAsyncTaskDao.getFilterListViaQuery(query2);

            for (DefectsModel defectsModel : localDefectsModels) {
                defectsModels.add(0, defectsModel);
            }

            if (isApplyGewerk && defectsModels != null) {
                List<DefectTradeModel> defectTradeModelList = defectTradesRepository.getmDefectsTradeDao().getAllDefectTradeWithStatusONModelWithProject(projectID, selectedGewerk);
                if (defectTradeModelList != null) {
                    for (int i = 0; i < defectsModels.size(); i++) {
                        for (int j = 0; j < defectTradeModelList.size(); j++) {
                            if (String.valueOf(defectsModels.get(i).getDefectLocalId()).equalsIgnoreCase(defectTradeModelList.get(j).getLocalpdflawid())) {
//                                if(defectsModelList.size()==0)
                                defectsModelList.add(defectsModels.get(i));
                                // break;
//                                if(!defectsModelList.contains(defectsModels.get(i))){
//                                    defectsModelList.add(defectsModels.get(i));
//                                }
                            }
                        }
                    }
                }
            } else {
                defectsModelList = defectsModels;
            }
            return defectsModelList;
        }

        @Override
        protected void onPostExecute(List<DefectsModel> defectsModels) {
            // super.onPostExecute(list);
            if (tv_count != null && tv_count_2 != null) {
                if (filterCount > 0) {
                    tv_count.setText(filterCount + "");
                    tv_count_2.setText(filterCount + "");
                    tv_count.setVisibility(VISIBLE);
                    tv_count_2.setVisibility(VISIBLE);
                } else {
                    tv_count.setText(filterCount + "");
                    tv_count_2.setText(filterCount + "");
                    tv_count.setVisibility(View.INVISIBLE);
                    tv_count_2.setVisibility(View.INVISIBLE);
                }

            }

            if (defectsModels != null) {
                StringBuilder builder = new StringBuilder();
                List<String> defectIds = new ArrayList<>();
                for (int i = 0; i < defectsModels.size(); i++) {
                    defectIds.add(defectsModels.get(i).defectLocalId + "");
                    if (i == defectsModels.size() - 1) {
                        builder.append(defectIds.get(i) + " ");
                    } else {
                        builder.append(defectIds.get(i) + ",");
                    }
                }
                if (defectIds != null) {
                    String delim = ",";
                    filterDefectsIds = builder.toString();
                    Intent intentt = new Intent(DefectsActivity.BR_ACTION_UPDATE_PLAN_DEFECTS);
                    if (filterDefectsIds != null && !filterDefectsIds.equals(""))
                        intentt.putExtra(DefectsActivity.BR_ACTION_UPDATE_PLAN_DEFECTS, filterDefectsIds);
                    sendBroadcast(intentt);
                    Fragment fragment = defectsPagerAdapter.m2ndFragment;
                    ((AllPlansFragment) fragment).applyPlanFilterAccordingToDefects(defectIds);
                }

                Fragment fragment = defectsPagerAdapter.m1stFragment;
                ((DefectsListFragment) fragment).updateDefectResults(defectsModels);
//                ((DefectsListFragment) fragment).defectsViewModel.getAllWords().getValue().addAll(defectsModels);
            }
        }

    }

    private void resetFilter(Context mContext) {
        Utils.showLogger2("resetFilters");
        if (groupheadingModelList != null) {

            for (int i = 0; i < groupheadingModelList.size(); i++) {
                if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_status))) {

                    groupheadingModelList.get(i).getListChildDataSelected().clear();


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_art))) {
                    groupheadingModelList.get(i).getListChildDataSelected().clear();


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_gewerk))) {
                    groupheadingModelList.get(i).getListChildDataSelected().clear();

                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_deadline))) {
                    groupheadingModelList.get(i).getListChildDataSelected().clear();


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_responsible))) {
                    groupheadingModelList.get(i).getListChildDataSelected().clear();


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_creator))) {

                    groupheadingModelList.get(i).getListChildDataSelected().clear();


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_date))) {

                    groupheadingModelList.get(i).getListChildDataSelected().clear();


                } else if (groupheadingModelList.get(i).getType().equalsIgnoreCase(mContext.getResources().getString(R.string.heading_photo_number))) {

                    groupheadingModelList.get(i).setKeyword("");


                }
            }

        }
        filterCount = 0;
        if (tv_count != null)
            tv_count.setVisibility(GONE);
        if (tv_count_2 != null)
            tv_count_2.setVisibility(View.INVISIBLE);
        if (expandableListAdapter != null)
            expandableListAdapter.notifyDataSetChanged();
        if (defectsPagerAdapter != null && defectsPagerAdapter.m1stFragment != null) {
            Utils.showLogger2("loadDefectsFromActivity");
            defectsPagerAdapter.m1stFragment.loadDefects("loadDefectsFromActivity");
        }

        if (defectsPagerAdapter != null && defectsPagerAdapter.m2ndFragment != null) {
            Fragment fragment = defectsPagerAdapter.m2ndFragment;
            ((AllPlansFragment) fragment).loadPlanRelatedWitDefect();
        }

//        applyFilter();
    }

    private class RetrieveAsyncTask extends AsyncTask<String, Void, PhotoModel> {
        private PhotoDao mAsyncTaskDao;

        RetrieveAsyncTask(PhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected PhotoModel doInBackground(final String... params) {
//            setmDefectedPhotos( mAsyncTaskDao.getAllDefectPhotoModel());
            // if( getmDefectedPhotos().getValue()!=null)

            Utils.showLogger2(params[0] + ":" + params[1]);
            PhotoModel photoModel = mAsyncTaskDao.getDefectPhotosAndLocalPhotosOBj(params[0], params[1]);


            return photoModel;
        }

        @Override
        protected void onPostExecute(PhotoModel photoModel) {
            super.onPostExecute(photoModel);
            if (photoModel != null) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (photoModel.getPath() != null && !photoModel.getPath().equals("")) {
                            Glide.with(DefectsActivity.this).load(photoModel.getPath()).into(iv_photo);
                            ll_photos_view.setVisibility(VISIBLE);
                        } else {
                            ll_photos_view.setVisibility(GONE);
                        }
                    }
                });


//                if (photoModel.getCreated() != null&&!photoModel.getCreated().equals("")){
//                    tv_photo_date.setText(photoModel.getCreated());
//                    tv_photo_date.setVisibility(View.VISIBLE);
//                }else{
//                    tv_photo_date.setVisibility(View.GONE);
//                }

            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    public void syncAllPhotos(boolean isAutoSync) {

        boolean isBreakOuterLoop = false;
        boolean isCalledBackgroundtask = false;
        Fragment fragment = defectsPagerAdapter.m1stFragment;
        if (fragment == null)
            return;
        DefectsListFragment localPhotosFragment = (DefectsListFragment) fragment;
        DefectsViewModel localPhotosViewModel = localPhotosFragment.getDefectsViewModel();
        if (localPhotosViewModel == null)
            return;
//        localPhotosViewModel.isAutoSyncPhoto = isAutoSync;
        if (localPhotosFragment != null && localPhotosFragment.getAdapter() != null && localPhotosFragment.getAdapter().getDefectsModels() != null) {

            List<DefectsModel> photoModelList = localPhotosFragment.getAdapter().getDefectsModels();
            if (photoModelList != null) {
                for (int i = 0; i < photoModelList.size(); i++) {
                    if (localPhotosViewModel != null && isAutoSync) {
                        DefectsModel photoModel = photoModelList.get(i);
                        if (ProjectNavigator.mobileNetworkIsConnected(DefectsActivity.this)) {

                            if ((photoModel.getUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UN_SYNC_PHOTO)
                                    || photoModel.getUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UPLOADING_PHOTO))) {
                                photoModel.setUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                                photoModel.setUserSelectedStatus(false);
                                new UpdateAsyncTask().execute(photoModel);
                                isCalledBackgroundtask = true;
                                isBreakOuterLoop = true;
                                break;
                            }
                        } else {
                            if (!photoModel.getUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO)) {
                                photoModel.setUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                                photoModel.setUserSelectedStatus(false);
                                new UpdateAsyncTask().execute(photoModel);
                                isCalledBackgroundtask = true;
                                isBreakOuterLoop = true;
                                break;
                            }
                        }
                    } else {
                        DefectsModel photoModel = photoModelList.get(i);
                        if (!photoModel.getUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO)) {
                            photoModel.setUploadStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                            photoModel.setUserSelectedStatus(true);
                            isCalledBackgroundtask = true;
                            new UpdateAsyncTask().execute(photoModel);
                        }
                    }

                }
            }
        }

        if (isCalledBackgroundtask) {
            Intent intent = new Intent("updateProfile");
            sendBroadcast(intent);

            if (localPhotosViewModel != null) {
                localPhotosViewModel.isStartUploadPhoto.postValue(true);
            }
        }

//        Intent msgIntent = new Intent(getApplication(), SyncLocalPhotosService.class);
//        msgIntent.putExtra(SyncLocalPhotosService.PROJECT_ID,projectID);
//        if(localPhotosViewModel!=null)
//        msgIntent.putExtra(SyncLocalPhotosService.IS_PHOTOS_AUTO_SYNC,localPhotosViewModel.isAutoSyncPhoto);
//        getApplication().startService(msgIntent);
    }

    private class UpdateAsyncTask extends AsyncTask<DefectsModel, Void, Void> {
        private DefectsDao mAsyncTaskDao;

        UpdateAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(getApplication());
            mAsyncTaskDao = projectsDatabase.defectsDao();
        }

        @Override
        protected Void doInBackground(final DefectsModel... params) {

            mAsyncTaskDao.update(params[0]);

            return null;
        }
    }


}
