package com.projectdocupro.mobile.activities;

import static com.projectdocupro.mobile.fragments.DefectsListFragment.BR_ACTION_UPDATE_DEFECT_DATA;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.DefectDetailsPagerAdapter2;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.fragments.DefectDetailAllPlansFragment;
import com.projectdocupro.mobile.fragments.DefectDetailsPhotoFragment;
import com.projectdocupro.mobile.fragments.DefectsListFragment;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.repos.DefectRepository;
import com.projectdocupro.mobile.utility.Utils;


public class DefectDetailsActivity2 extends AppCompatActivity implements DefectDetailAllPlansFragment.OnFragmentInteractionListener, DefectDetailsPhotoFragment.OnFragmentInteractionListener {


    private DefectDetailsPagerAdapter2 defectDetailsPagerAdapter;

    private ViewPager plansViewPager;

    private TabLayout pagerTabStrip;

    private Toolbar toolbar;
    private FrameLayout content_frame;

    public String defect_type;
    boolean isFromPhoto;
    public String photoId = "";
    public long longPhotoId = 0;
    public String flaw_id = "";
    public static boolean isDataUpdated = false;

    public boolean isMangelCreated;

    public boolean isfromCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_defect_details_2);
        bindView();
        setSupportActionBar(toolbar);

        Utils.showLogger("DefectDetailsActivity2");

        defect_type = getIntent().getStringExtra(DefectsActivity.TYPE_DEFECT_KEY);
        photoId = getIntent().getStringExtra(DefectsActivity.PHOTO_ID_KEY);
        flaw_id = getIntent().getStringExtra("flaw_id");
        Utils.showLogger2("DetailFlawId::"+flaw_id);
        isMangelCreated = getIntent().getBooleanExtra(DefectsActivity.IS_CREATED_MANGEL_KEY, false);
        isfromCreate = getIntent().getBooleanExtra("FROM_CREATE", false);
        if (photoId == null || photoId.equals(""))
            longPhotoId = 0;
        else
            isFromPhoto = true;

        if (!isMangelCreated) {

            toolbar.setTitle(getString(R.string.mangel_detail));
        }

        DefectDetailAllPlansFragment newFragment = DefectDetailAllPlansFragment.newInstance(getIntent().getStringExtra(DefectsListFragment.ARG_PROJECT_ID), getIntent().getStringExtra(DefectsListFragment.ARG_PARAM2), longPhotoId, isFromPhoto, true);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(content_frame.getId(), newFragment).addToBackStack(DefectDetailAllPlansFragment.class.getName()).commit();

        defectDetailsPagerAdapter = new DefectDetailsPagerAdapter2(this, getSupportFragmentManager(), getIntent().getStringExtra(DefectsListFragment.ARG_PROJECT_ID), getIntent().getStringExtra(DefectsListFragment.ARG_PARAM2), longPhotoId, isFromPhoto, true, isMangelCreated);
        plansViewPager.setAdapter(defectDetailsPagerAdapter);
        plansViewPager.setOffscreenPageLimit(3);

        pagerTabStrip.setupWithViewPager(plansViewPager);
        plansViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

//                if (defectDetailsPagerAdapter.getM1stFragment() != null && !defectDetailsPagerAdapter.getM1stFragment().isDefectNameAdded) {
//                    Toast.makeText(DefectDetailsActivity2.this, getResources().getString(R.string.defect_screen_flaw_name_empty_msg), Toast.LENGTH_SHORT).show();
//                    plansViewPager.setCurrentItem(0);
//                    return;
//                }
//                if (position == 2) {
//                    content_frame.setVisibility(View.VISIBLE);
//                    plansViewPager.setVisibility(View.GONE);
//
//                } else {
//                    content_frame.setVisibility(View.GONE);
//                    plansViewPager.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onPageSelected(int position) {
                pagerTabStrip.setScrollPosition(position, 0f, true);
                if (defectDetailsPagerAdapter.getM1stFragment() != null && !defectDetailsPagerAdapter.getM1stFragment().isDefectNameAdded) {
                    Toast.makeText(DefectDetailsActivity2.this, getResources().getString(R.string.defect_screen_flaw_name_empty_msg), Toast.LENGTH_SHORT).show();
                    plansViewPager.setCurrentItem(0);

                    return;
                } else {

                    Utils.hideSoftKeyboard(DefectDetailsActivity2.this);

                    Boolean oldValueIsDataUpdate = isDataUpdated;

                    defectDetailsPagerAdapter.getM1stFragment().afterSaveKillActivity = false;
                   // defectDetailsPagerAdapter.getM1stFragment().saveDefect();

                    isDataUpdated = oldValueIsDataUpdate;
                }
                if (position == 2) {
                    content_frame.setVisibility(View.VISIBLE);
                    plansViewPager.setVisibility(View.GONE);

                } else {
                    content_frame.setVisibility(View.GONE);
                    plansViewPager.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

    }

    @Override
    public void onBackPressed() {
        if (photoId == null)
            photoId = "";


        Utils.showLogger("isOnBackPressDataChange>>" + isDataUpdated);

        if (isDataUpdated) {
            showCustomDialog(this, getResources().getString(R.string.custom_dialog_title), getResources().getString(R.string.dircard_changes_defect_msg), 2, 0,getString(R.string.yes),getString(R.string.no));
        } else
            finish();
    }

    public void showCustomDialog(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);

        Utils.showLogger("customeDiaglogshown");
        Dialog customDialog = new Dialog(act, R.style.MyDialogTheme);

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);

        if (flag == 2) {
            customDialog.setCancelable(false);

        }
        customDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.y = 10;

        // set the custom dialog components - text, image and button
        TextView titleTxt = (TextView) customDialog.findViewById(R.id.customDialog_titleText);
        if (!title.equals(""))
            titleTxt.setText(title);
        TextView text = (TextView) customDialog.findViewById(R.id.movie_name);
        text.setText(msgToShow);
        //		if(Navigator.showCustomDialogType ==3)
        //		{
        //			text.setTextSize(getDipValue(8));
        //		}

        Button bt = (Button) customDialog.findViewById(R.id.customDialog_okBtn);
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

//                    Fragment fragment = defectDetailsPagerAdapter.getM1stFragment();
//                    if(fragment!=null){
//                        ((DefectDetailsDatesFragment) fragment).saveDefect();
//
//                    }


                boolean emptyEffectDelete = defectDetailsPagerAdapter.getM1stFragment().saveDefect();
                if(emptyEffectDelete){

                    Utils.showLogger("delete the mangel1");
                    if (isfromCreate) {
                        Utils.showLogger("delete the mangel");
                        new DeleteDefectAsyncTask().execute();
                        customDialog.dismiss();
                    }
                }else {
                    Utils.showLogger("noNeedToDelete");
                }
                Utils.showLogger("isDataUpdated is false on finish");
                isDataUpdated = false;//on finish
                customDialog.dismiss();
                finish();


            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (isfromCreate) {
                        new DeleteDefectAsyncTask().execute();
                        customDialog.dismiss();
                    } else {
                        customDialog.dismiss();
                        finish();
                    }
                }
            });

        }
        customDialog.show();
    }


    public void showCustomDialog(final Activity act, String title, String msgToShow, Integer noOfButtons, Integer flag,String yes, String no) {
        //customDialog = new Dialog(act, R.style.customDialogTheme);
        Dialog customDialog = new Dialog(act, R.style.MyDialogTheme);

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.custom_dialog_message_material);

        if (flag == 2) {
            customDialog.setCancelable(false);

        }
        customDialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.y = 10;

        // set the custom dialog components - text, image and button
        TextView titleTxt = (TextView) customDialog.findViewById(R.id.customDialog_titleText);
        if (!title.equals(""))
            titleTxt.setText(title);
        TextView text = (TextView) customDialog.findViewById(R.id.movie_name);
        text.setText(msgToShow);
        //		if(Navigator.showCustomDialogType ==3)
        //		{
        //			text.setTextSize(getDipValue(8));
        //		}

        Button bt = (Button) customDialog.findViewById(R.id.customDialog_okBtn);
        bt.setText(yes);
        bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

//                    Fragment fragment = defectDetailsPagerAdapter.getM1stFragment();
//                    if(fragment!=null){
//                        ((DefectDetailsDatesFragment) fragment).saveDefect();
//
//                    }


                boolean isDeleteDefect = defectDetailsPagerAdapter.getM1stFragment().saveDefect();

                if(isDeleteDefect){
                    if (isfromCreate) {
                        new DeleteDefectAsyncTask().execute();
                        customDialog.dismiss();
                    }
                }

                Utils.showLogger("isDataUpdated is false on finish");
                isDataUpdated = false;//on finish
                customDialog.dismiss();
                finish();


            }
        });
        if (noOfButtons == 2) {
            Button bt1 = (Button) customDialog.findViewById(R.id.customDialog_cancel);
            bt1.setVisibility(View.VISIBLE);
            bt1.setText(no);
            bt1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (isfromCreate) {
                        new DeleteDefectAsyncTask().execute();
                        customDialog.dismiss();
                    } else {
                        customDialog.dismiss();
                        finish();
                    }
                }
            });

        }
        customDialog.show();
    }

    private void bindView() {
        plansViewPager =     findViewById(R.id.defects_details_view_pager);
        pagerTabStrip =     findViewById(R.id.defects_details_tab_strip);
        toolbar =     findViewById(R.id.toolbar);
        content_frame =     findViewById(R.id.content_frame);
    }


    public class DeleteDefectAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            ProjectsDatabase db = ProjectsDatabase.getDatabase(DefectDetailsActivity2.this);
            if (getIntent() != null) {
                if (getIntent().hasExtra(DefectsListFragment.ARG_PARAM2)) {
                    String flawId = getIntent().getStringExtra(DefectsListFragment.ARG_PARAM2);
                    if (!flawId.equals("")) {
                        long flawIdInLong = Long.parseLong(flawId);
                        db.defectsDao().deleteUsingLocalDefectId(flawIdInLong);
                        Intent intent = new Intent(BR_ACTION_UPDATE_DEFECT_DATA);
                        intent.putExtra("flawId", flawId);
                        sendBroadcast(intent);
                        finish();
                    }
                }
            }

            return null;
        }
    }


    @Override
    public void onAllFragmentInteraction(Uri uri) {

    }

    @Override
    public void onLcoalPhotoFragmentInteraction(Uri uri) {

    }

    boolean isAutoSYNC;
    String uploadStatus;

    public void updateDefectSyncStatus(String local_flaw_id, String uploadSTatus, boolean isAutoSync) {
        Utils.showLogger("updateDefectSyncStatus");
        isAutoSYNC = isAutoSync;
        uploadStatus = uploadSTatus;
        new UpdateAsyncTask().execute(local_flaw_id);
    }


    private class UpdateAsyncTask extends AsyncTask<String, Void, Void> {
        private PhotoDao mAsyncTaskDao;
        private DefectsDao defectsDao;

        UpdateAsyncTask() {
            ProjectsDatabase projectsDatabase = ProjectsDatabase.getDatabase(DefectDetailsActivity2.this);
            defectsDao = projectsDatabase.defectsDao();
        }

        @Override
        protected Void doInBackground(final String... params) {

            Utils.showLogger("updatingstatus");

            if (params[0] != null && !params[0].equals("")) {

                DefectsModel defectsModel = defectsDao.getDefectsOBJ(getIntent().getStringExtra(DefectsListFragment.ARG_PROJECT_ID), params[0]);
                if (defectsModel != null) {
                    if (ProjectNavigator.wlanIsConnected(DefectDetailsActivity2.this) || ProjectNavigator.mobileNetworkIsConnected(DefectDetailsActivity2.this)) {
                        defectsModel.setUploadStatus(uploadStatus);
                    } else {
                        defectsModel.setUploadStatus(DefectRepository.UN_SYNC_PHOTO);
                    }
                    defectsDao.update(defectsModel);
                }

                Intent intent = new Intent(BR_ACTION_UPDATE_DEFECT_DATA);
                intent.putExtra("flawId", params[0]);

                if (ProjectNavigator.wlanIsConnected(DefectDetailsActivity2.this) || ProjectNavigator.mobileNetworkIsConnected(DefectDetailsActivity2.this)) {
                    intent.putExtra("uploadStatus", uploadStatus);
                } else {
                    intent.putExtra("uploadStatus", DefectRepository.UN_SYNC_PHOTO);
                }
                intent.putExtra("isAutoUpload", isAutoSYNC);
                sendBroadcast(intent);


            }

            return null;
        }
    }






}
