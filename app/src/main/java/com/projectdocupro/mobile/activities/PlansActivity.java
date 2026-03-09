package com.projectdocupro.mobile.activities;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.PlansPagerAdapter;
import com.projectdocupro.mobile.fragments.AllPlansFragment;
import com.projectdocupro.mobile.fragments.FavoritePlansFragment;



public class PlansActivity extends AppCompatActivity implements FavoritePlansFragment.OnFragmentInteractionListener, AllPlansFragment.OnFragmentInteractionListener {


    public  boolean isIgnoreLastPlan =false;//if true do not automatically open last plan because user changing it from Last Loaded Plan
    public static final String IGNORE_LOADING_LAST_PLAN = "ignore_loading_last_plna";


    private PlansPagerAdapter plansPagerAdapter;

    private ViewPager plansViewPager;

    private TabLayout pagerTabStrip;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        bindView();

        isIgnoreLastPlan  =getIntent().getBooleanExtra(IGNORE_LOADING_LAST_PLAN,false);

        plansPagerAdapter = new PlansPagerAdapter(this, getSupportFragmentManager(), getIntent().getStringExtra("projectId"), getIntent().getStringExtra(AllPlansFragment.ARG_PLAN_ID), getIntent().getLongExtra("photoId", 0), getIntent().getBooleanExtra("fromPhoto", false),isIgnoreLastPlan);
        plansViewPager.setAdapter(plansPagerAdapter);

        pagerTabStrip.setupWithViewPager(plansViewPager);
        new SelectTabAsyncTask(this).execute();
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

    }

    private void bindView() {
        plansViewPager = findViewById(R.id.plans_view_pager);
        pagerTabStrip = findViewById(R.id.plans_tab_layout);
        toolbar = findViewById(R.id.toolbar);
    }

    private class SelectTabAsyncTask extends AsyncTask<Void, Void, Integer> {
        private ProjectsDatabase database;

        SelectTabAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected Integer doInBackground(final Void... params) {
            Integer favCount = database.plansDao().getFavouritePlansCount();

            return favCount;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (plansViewPager != null) {
                if (integer > 0) {
                    plansViewPager.setCurrentItem(0);
                } else {
                    plansViewPager.setCurrentItem(1);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
//        setResult(101,new Intent());
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onAllFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFavoritesFragmentInteraction(Uri uri) {

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
}
