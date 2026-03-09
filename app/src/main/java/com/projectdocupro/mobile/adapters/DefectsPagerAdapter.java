package com.projectdocupro.mobile.adapters;


import android.content.Context;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.fragments.AllPlansFragment;
import com.projectdocupro.mobile.fragments.DefectsListFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

public class DefectsPagerAdapter extends FragmentStatePagerAdapter{

    private  String photoID="";
    String  projectId;
    boolean  isfromDefect;
    private Context context;
    public DefectsListFragment m1stFragment;
    public AllPlansFragment m2ndFragment;

    public DefectsPagerAdapter(Context context, FragmentManager fm,String    projectId,String photoId,boolean isFromDefect) {
            super(fm);
            this.projectId  =   projectId;
            photoID  =   photoId;
            this.context=context;
            isfromDefect=isFromDefect;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment=null;
            switch (position){
                case    0:
                    fragment= DefectsListFragment.newInstance(projectId,"",photoID);
                    break;
                case    1:
                    fragment= AllPlansFragment.newInstance(projectId,0,false,isfromDefect);
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.list);
                case 1:
                    return context.getString(R.string.menu_plans);
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
                m1stFragment = (DefectsListFragment) createdFragment;
                break;
            case 1:
                m2ndFragment = (AllPlansFragment) createdFragment;
                break;
        }
        return createdFragment;
    }
}
