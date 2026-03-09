package com.projectdocupro.mobile.adapters;


import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.fragments.AllWordsFragment;
import com.projectdocupro.mobile.fragments.FavoriteWordsFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

public class WordsPagerAdapter extends FragmentPagerAdapter {

    private String  projectId;
    private long    photoId;

    Context mContext;
        public WordsPagerAdapter(Context context,FragmentManager fm, String  projectId, long  photoId) {
            super(fm);
            this.projectId  =   projectId;
            this.photoId    =   photoId;
            mContext=context;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment=null;
            switch (position){
                case    0:
                    fragment= FavoriteWordsFragment.newInstance(projectId,photoId);
                    break;
                case    1:
                    fragment= AllWordsFragment.newInstance(projectId,photoId);
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
                    return mContext.getString(R.string.tab_favorite);
                case 1:
                    return mContext.getString(R.string.tab_all);
            }
            return null;
        }

}
