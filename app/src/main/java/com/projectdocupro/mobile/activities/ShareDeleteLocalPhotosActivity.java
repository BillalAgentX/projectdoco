package com.projectdocupro.mobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.projectdocupro.mobile.fragments.ShareDeleteLocalPhotoFragment;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class ShareDeleteLocalPhotosActivity extends AppCompatActivity implements ShareDeleteLocalPhotoFragment.OnFragmentInteractionListener {



    private Toolbar toolbar;


    private FrameLayout fl_layout;

    private LinearLayout ll_bottom_tabs;

    private ImageView iv_share_photo;

    private ImageView iv_report_photo;

    public PhotoDao photoDao;


    public String projectID;
    private String FragTAG;
    ShareDeleteLocalPhotoFragment onlinePhotosFragment = null;
    private boolean isReportDetailLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_photo_activity);
        bindView();

        Utils.showLogger("ShareDeleteLocalPhotosActivity");

        Log.d("projectId", getIntent().getStringExtra("projectId"));
        projectID = getIntent().getStringExtra("projectId");

        photoDao = ProjectsDatabase.getDatabase(this).photoDao();
        setSupportActionBar(toolbar);
        iv_report_photo.setImageResource(R.drawable.delete_icon_direction_screen);

        toolbar.setTitle(getResources().getString(R.string.choose_photos_report_title));
        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        addEvent();
        openFragment();

    }

    private void openFragment() {


        onlinePhotosFragment = ShareDeleteLocalPhotoFragment.newInstance(projectID, "fromNextAction");

        FragTAG = "ShareDeleteLocalPhotoFragment";


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

                    for (String path : items /* List of the files you want to send */) {
                        File file = new File(path);
                        Uri photoURI = FileProvider.getUriForFile(ShareDeleteLocalPhotosActivity.this, ShareDeleteLocalPhotosActivity.this.getApplicationContext().getPackageName() + ".fileprovider", file);
//                        Uri uri = Uri.fromFile(file);
                        files.add(photoURI);
                    }
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                    startActivity(Intent.createChooser(intent, "Share using?"));


                } else {
                    isReportDetailLoad = false;
                    Toast.makeText(ShareDeleteLocalPhotosActivity.this, getResources().getString(R.string.report_choose_photo_msg), Toast.LENGTH_SHORT).show();
                }
            }
        });

        iv_report_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onlinePhotosFragment != null && onlinePhotosFragment.selectedPhotosList != null ) {
                    new DeletePhotosAsyncTask(onlinePhotosFragment.selectedPhotosList).execute();
                }
//                else {
//                    isReportDetailLoad = false;
//                    Toast.makeText(ShareDeleteLocalPhotosActivity.this, getResources().getString(R.string.report_choose_photo_msg), Toast.LENGTH_SHORT).show();
//                }
            }
        });
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
    public void onLcoalPhotoFragmentInteraction(Uri uri) {

    }

    private void bindView() {
        toolbar =         findViewById(R.id.toolbar);
        fl_layout =         findViewById(R.id.fl_layout);
        ll_bottom_tabs =         findViewById(R.id.ll_bottom_tabs);
        iv_share_photo =         findViewById(R.id.iv_share_photo);
        iv_report_photo =         findViewById(R.id.iv_report_photo);
    }


    private class DeletePhotosAsyncTask extends AsyncTask<Void, Void, Void> {

        List<PhotoModel> photosList;

        DeletePhotosAsyncTask(List<PhotoModel> photoModels) {
            photosList = photoModels;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (photosList != null) {
                for (int i = 0; i < photosList.size(); i++) {
                    if (onlinePhotosFragment != null && onlinePhotosFragment.adapter != null) {
                        if (onlinePhotosFragment.adapter.getPhotosData() != null) {
                            onlinePhotosFragment.adapter.getPhotosData().remove(photosList.get(i));
                            ProjectsDatabase.getDatabase(ShareDeleteLocalPhotosActivity.this).photoDao().deleteUsingPhotoId(photosList.get(i).getPdphotolocalId());
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void photoModels) {
            super.onPostExecute(photoModels);
            if (onlinePhotosFragment != null && onlinePhotosFragment.adapter != null) {
                onlinePhotosFragment.adapter.notifyDataSetChanged();
            }
            Intent intent = new Intent("updateProfile");
            sendBroadcast(intent);

        }
    }
}
