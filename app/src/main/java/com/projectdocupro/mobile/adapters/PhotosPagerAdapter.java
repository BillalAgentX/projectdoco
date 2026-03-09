package com.projectdocupro.mobile.adapters;


import android.content.Context;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.fragments.LocalPhotosFragment;
import com.projectdocupro.mobile.fragments.OnlinePhotosFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

public class PhotosPagerAdapter extends FragmentPagerAdapter {

    private String  projectId;
    private Context context;
    private LocalPhotosFragment m1stFragment;
    private OnlinePhotosFragment m2ndFragment;

    public LocalPhotosFragment getM1stFragment() {
        return m1stFragment;
    }

    public void setM1stFragment(LocalPhotosFragment m1stFragment) {
        this.m1stFragment = m1stFragment;
    }

    public OnlinePhotosFragment getM2ndFragment() {
        return m2ndFragment;
    }

    public void setM2ndFragment(OnlinePhotosFragment m2ndFragment) {
        this.m2ndFragment = m2ndFragment;
    }

    public PhotosPagerAdapter(Context   context, FragmentManager fm, String  projectId) {
            super(fm);
            this.context    =   context;
            this.projectId  =   projectId;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment=null;
            switch (position){
                case    0:
                    fragment= LocalPhotosFragment.newInstance(projectId,"");
                    break;
                case    1:
                    fragment= OnlinePhotosFragment.newInstance(projectId,"");
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
                    return context.getString(R.string.local);
                case 1:
                    return context.getString(R.string.online);
            }
            return null;
        }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                m1stFragment = (LocalPhotosFragment) createdFragment;
                break;
            case 1:
                m2ndFragment = (OnlinePhotosFragment) createdFragment;
                break;
        }
        return createdFragment;
    }
}
