package com.projectdocupro.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.utility.Utils;

import androidx.fragment.app.Fragment;


public class EmptyTestFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_all_plans_2, container, false);



        return view;
    }

    public static EmptyTestFragment newInstance() {
        EmptyTestFragment fragment = new EmptyTestFragment();
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Utils.showLogger("Defect Detail Photoes>>onCreateOptionsMenu");
        ///inflater.inflate(R.menu.save_action_menu, menu);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_action) {
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);

    }


}


