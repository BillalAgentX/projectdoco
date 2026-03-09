package com.projectdocupro.mobile.adapters;


import android.content.Context;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.fragments.AllFragment;
import com.projectdocupro.mobile.fragments.FavoritesFragment;
import com.projectdocupro.mobile.fragments.LastUsedFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

public class ProjectsPagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    public FavoritesFragment m1stFragment;
    public AllFragment m2ndFragment;
    public LastUsedFragment m3rdFragment;

        public ProjectsPagerAdapter(Context context,    FragmentManager fm) {
            super(fm);
            this.context    =   context;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment=null;
            switch (position){
                case    0:
                    fragment= FavoritesFragment.newInstance("","");
                    break;
                case    1:
                    fragment= AllFragment.newInstance("","");
                    break;
                case    2:
                    fragment= LastUsedFragment.newInstance("","");
//                    fragment= new ScanBarcodeFragment();
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.tab_favorite);
                case 1:
                    return context.getString(R.string.tab_all);
                case 2:
                    return context.getString(R.string.tab_lastused_new);
            }
            return null;
        }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment)
                super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                m1stFragment = (FavoritesFragment) createdFragment;
                break;
            case 1:
                m2ndFragment = (AllFragment) createdFragment;
                break;
            case 2:
                m3rdFragment = (LastUsedFragment) createdFragment;
                break;
        }
        return createdFragment;
    }

}
