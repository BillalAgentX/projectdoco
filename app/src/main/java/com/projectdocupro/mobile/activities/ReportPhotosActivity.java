package com.projectdocupro.mobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.fragments.OnlinePhotosFragment;
import com.projectdocupro.mobile.fragments.ReportPhotoDetailFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportPhotosActivity extends AppCompatActivity implements OnlinePhotosFragment.OnFragmentInteractionListener, ReportPhotoDetailFragment.OnFragmentInteractionListener {

    

    private Toolbar toolbar;


    private FrameLayout fl_layout;

    private LinearLayout ll_bottom_tabs;

    private ImageView iv_share_photo;

    private ImageView iv_report_photo;

    public PhotoDao photoDao;


    public String projectID;
    private String FragTAG;
    OnlinePhotosFragment onlinePhotosFragment = null;
    private boolean isReportDetailLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_photo_activity);
        bindView();
        Log.d("projectId", getIntent().getStringExtra("projectId"));
        projectID = getIntent().getStringExtra("projectId");

        photoDao = ProjectsDatabase.getDatabase(this).photoDao();
        setSupportActionBar(toolbar);

        toolbar.setTitle(getResources().getString(R.string.choose_photos_report_title));
        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        addEvent();
        openFragment();

        
    }

    private void openFragment() {


        onlinePhotosFragment = OnlinePhotosFragment.newInstance(projectID, "fromNextAction");

        FragTAG = "OnlinePhotosFragment";


        addFragment(R.id.fl_layout, onlinePhotosFragment, FragTAG, FragTAG);
    }

    public void addFragment(@IdRes int containerViewId,
                            @NonNull Fragment fragment,
                            @NonNull String fragmentTag,
                            String backStackStateName) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(containerViewId, fragment, fragmentTag)
                .setCustomAnimations(R.anim.left_to_right, R.anim.right_to_left)
                .addToBackStack(backStackStateName)
                .commit();
    }


    private void addEvent() {

        iv_share_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onlinePhotosFragment != null && onlinePhotosFragment.stringBuilderPhotoPath != null && !onlinePhotosFragment.stringBuilderPhotoPath.toString().equals("")) {

                    List<String> items = Arrays.asList(onlinePhotosFragment.stringBuilderPhotoPath.toString().split("\\s*,\\s*"));

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    intent.setType("image/jpeg"); /* This example is sharing jpeg images. */

                    ArrayList<Uri> files = new ArrayList<Uri>();

                    for(String path : items /* List of the files you want to send */) {
                        File file = new File(path);
                        Uri photoURI = FileProvider.getUriForFile(ReportPhotosActivity.this, ReportPhotosActivity.this.getApplicationContext().getPackageName() + ".fileprovider", file);
//                        Uri uri = Uri.fromFile(file);
                        files.add(photoURI);
                    }
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                    startActivity(Intent.createChooser(intent, "Share using?"));


                } else {
                    isReportDetailLoad = false;
                    Toast.makeText(ReportPhotosActivity.this, getResources().getString(R.string.report_choose_photo_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });

        iv_report_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onlinePhotosFragment != null && onlinePhotosFragment.stringBuilder != null && !onlinePhotosFragment.stringBuilder.toString().equals("")) {
                    toolbar.setTitle(getResources().getString(R.string.create_report_title));
                    fragment = ReportPhotoDetailFragment.newInstance(projectID, onlinePhotosFragment.stringBuilder.toString());
                    FragTAG = "ReportPhotoDetailFragment";
                    addFragment(R.id.fl_layout, fragment, FragTAG, FragTAG);
                    ll_bottom_tabs.setVisibility(View.GONE);
                } else {
                    isReportDetailLoad = false;
                    Toast.makeText(ReportPhotosActivity.this, getResources().getString(R.string.report_choose_photo_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.next_action_menu, menu);
        return true;
    }

    ReportPhotoDetailFragment fragment = null;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next_action:
                // Set the text color to red
                if (onlinePhotosFragment != null && onlinePhotosFragment.stringBuilder != null && !onlinePhotosFragment.stringBuilder.toString().equals("")) {
//                    isReportDetailLoad = true;
//                    if (fragment != null && fragment.isReportDetailOpened) {
//                        fragment.validate();
//                    } else {
                        toolbar.setTitle(getResources().getString(R.string.create_report_title));
                        fragment = ReportPhotoDetailFragment.newInstance(projectID, onlinePhotosFragment.stringBuilder.toString());
                        FragTAG = "ReportPhotoDetailFragment";
                        addFragment(R.id.fl_layout, fragment, FragTAG, FragTAG);
//                    }
                } else {
                    isReportDetailLoad = false;
                    Toast.makeText(this, getResources().getString(R.string.report_choose_photo_msg), Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
            toolbar.setTitle(getResources().getString(R.string.choose_photos_report_title));
            ll_bottom_tabs.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
    }


    @Override
    public void onOnlineFragmentInteraction(Uri uri) {

    }

    private void bindView() {
        toolbar =         findViewById(R.id.toolbar);
        fl_layout =         findViewById(R.id.fl_layout);
        ll_bottom_tabs =         findViewById(R.id.ll_bottom_tabs);
        iv_share_photo =         findViewById(R.id.iv_share_photo);
        iv_report_photo =         findViewById(R.id.iv_report_photo);
    }


//    private class GetAllPhotosProjectAsyncTask extends AsyncTask<String, Void, List<PhotoModel> > {
//
//
//        @Override
//        protected List<PhotoModel>  doInBackground(String... params) {
//            List<PhotoModel> photosList = photoDao.getPhotosList(params[0]);
//            return photosList;
//        }
//
//        @Override
//        protected void onPostExecute(List<PhotoModel> photoModels) {
//            super.onPostExecute(photoModels);
//
//            for (int i = 0; i <photoModels.size() ; i++) {
//
//            }
//
//        }
//    }
}
