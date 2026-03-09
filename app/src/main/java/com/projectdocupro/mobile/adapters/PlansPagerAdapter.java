package com.projectdocupro.mobile.adapters;


import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.fragments.AllPlansFragment;
import com.projectdocupro.mobile.fragments.FavoritePlansFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

public class PlansPagerAdapter extends FragmentPagerAdapter {

    private  boolean ignoreLoadingLastPlan;
    private String projectId;
    private String planId;
    private boolean fromPhoto;
    private long photoId;
    private Context context;

    public PlansPagerAdapter(Context context, FragmentManager fm, String projectId, long photoId, boolean fromPhoto) {
        super(fm);
        this.projectId = projectId;
        this.photoId = photoId;
        this.fromPhoto = fromPhoto;
        this.context = context;
    }


    public PlansPagerAdapter(Context context, FragmentManager fm, String projectId, String planId, long photoId, boolean fromPhoto,boolean isIgnoreLostPlan) {
        super(fm);
        this.projectId = projectId;
        this.photoId = photoId;
        this.planId = planId;
        this.fromPhoto = fromPhoto;
        this.context = context;
        this.ignoreLoadingLastPlan = isIgnoreLostPlan;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = FavoritePlansFragment.newInstance(projectId, planId, photoId, fromPhoto);
                break;
            case 1:
                fragment = AllPlansFragment.newInstance(projectId, planId, photoId, fromPhoto,ignoreLoadingLastPlan);
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.tab_favorite);
            case 1:
                return context.getString(R.string.tab_all);
        }
        return null;
    }

}
