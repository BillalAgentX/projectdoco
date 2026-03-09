package com.projectdocupro.mobile.adapters;


import android.content.Context;
import android.view.ViewGroup;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.fragments.DefectDetailsDatesFragment;
import com.projectdocupro.mobile.fragments.DefectDetailsPhotoFragment;
import com.projectdocupro.mobile.fragments.EmptyTestFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */

public class DefectDetailsPagerAdapter2 extends FragmentStatePagerAdapter {

    private  Context mContext;
    private String  projectId;
    private String  defectID;
    private long  photo_id;
    private boolean  is_from_photo;
    private boolean  is_from_defect;
    private boolean  is_from_Mangel_created;
    private DefectDetailsDatesFragment m1stFragment;
    private DefectDetailsPhotoFragment m2ndFragment;
    private EmptyTestFragment m3rdFragment;

    public DefectDetailsDatesFragment getM1stFragment() {
        return m1stFragment;
    }

    public void setM1stFragment(DefectDetailsDatesFragment m1stFragment) {
        this.m1stFragment = m1stFragment;
    }

    public DefectDetailsPhotoFragment getM2ndFragment() {
        return m2ndFragment;
    }

    public void setM2ndFragment(DefectDetailsPhotoFragment m2ndFragment) {
        this.m2ndFragment = m2ndFragment;
    }

    public EmptyTestFragment getM3rdFragment() {
        return m3rdFragment;
    }

    public void setM3rdFragment(EmptyTestFragment m3rdFragment) {
        this.m3rdFragment = m3rdFragment;
    }

    public DefectDetailsPagerAdapter2(Context context, FragmentManager fm, String  projectId, String  defect_id) {
            super(fm);
            this.projectId  =   projectId;
            defectID  =   defect_id;
            mContext=context;
        }

    public DefectDetailsPagerAdapter2(Context context, FragmentManager fm, String  projectId, String  defect_id, long photoID, boolean isFromPhoto, boolean isFromDefect, boolean isMangeLCreated) {
        super(fm);
        this.projectId  =   projectId;
        defectID  =   defect_id;
        photo_id  =  photoID;
        is_from_photo  =  isFromPhoto;
        is_from_defect  =   isFromDefect;
        is_from_Mangel_created  =   isMangeLCreated;
        mContext=context;
    }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment=null;
            switch (position){
                case    0:
                    fragment= DefectDetailsDatesFragment.newInstance(projectId,defectID,is_from_Mangel_created);
                    break;
                case    1:
                    fragment= DefectDetailsPhotoFragment.newInstance(projectId,defectID);
                    break;
                case    2:
                    fragment= EmptyTestFragment.newInstance();
                    break;
            }

            return fragment;
        }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                m1stFragment = (DefectDetailsDatesFragment) createdFragment;
                break;
            case 1:
                m2ndFragment = (DefectDetailsPhotoFragment) createdFragment;
              break;
                case 2:
                m3rdFragment = (EmptyTestFragment) createdFragment;
                break;
        }
        return createdFragment;
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
                    return mContext.getString(R.string.infos);
                case 1:
                    return mContext.getString(R.string.photos);
                case 2:
                    return mContext.getString(R.string.heading_photo_plans);
            }
            return null;
        }



}
