package com.projectdocupro.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.OnlinePhotoDao;
import com.projectdocupro.mobile.interfaces.OnlinePhotosListItemClickListener;
import com.projectdocupro.mobile.interfaces.RetroApiInterface;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.RetrofitManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.OnlinePhotoModel;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDetailsPhotosRecyclerAdapter extends RecyclerView.Adapter<ReportDetailsPhotosRecyclerAdapter.ProjectViewHolder> {

    public List<OnlinePhotoModel> photosData;
    //    private ProjectsListItemClickListener   listener;
    private Context mContext;
    private String projectId;
    private String imagePath;
    OnlinePhotoDao mDefectsPhotoDao;
    boolean isSelectedPhoto;

    OnlinePhotosListItemClickListener photosListItemClickListener;

    public ReportDetailsPhotosRecyclerAdapter(String projectId, List<OnlinePhotoModel> photosData,  OnlinePhotosListItemClickListener listItemClickListener) {
        this.projectId = projectId;
        this.photosData = photosData;
        photosListItemClickListener=listItemClickListener;
//        this.listener   =   listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        mDefectsPhotoDao = ProjectsDatabase.getDatabase(mContext).onlinePhotoDao();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_view, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        OnlinePhotoModel photoModel = photosData.get(position);

//        Glide.with(context).load(photoModel.).into(holder.photo);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //if you need three fix imageview in width
        com.projectdocupro.mobile.utility.ProjectDocuUtilities projectDocuUtilities = new com.projectdocupro.mobile.utility.ProjectDocuUtilities();
        int val = projectDocuUtilities.calculateNoOfColumns(mContext, 130);
        int devicewidth = displaymetrics.widthPixels / val;

        holder.itemView.getLayoutParams().width = devicewidth;
//        holder.itemView.getLayoutParams().height = devicewidth;

         if(getItemCount()>1) {
             holder.iv_selected.setVisibility(View.VISIBLE);
             holder.iv_selected.setImageResource(R.drawable.minus_report_photo);
         }else{
             holder.iv_selected.setVisibility(View.GONE);
         }
//        else
//            holder. iv_selected.setVisibility(View.GONE);

        holder.iv_selected.setOnClickListener(view -> {


                 photosListItemClickListener.onListItemClick(photoModel);


        });

        if (photoModel.getPohotPath() != null && !photoModel.getPohotPath().equals("")) {

            File imgFile = new File(photoModel.getPohotPath());

            if (imgFile.exists()) {

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


                holder.photo.setImageBitmap(myBitmap);

            }

        } else {
//            holder.photo.setImageBitmap(null);
            callGetPlanImageAPI(mContext,photoModel,position);
        }
    }

    @Override
    public int getItemCount() {
        return photosData.size();
    }



    private static class updateProjectAsyncTask extends AsyncTask<ProjectModel, Void, Void> {
        private ProjectsDatabase database;

        updateProjectAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected Void doInBackground(final ProjectModel... params) {
            database.projectDao().update(params[0]);
            return null;
        }
    }


    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView photo;
        ImageView iv_selected;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            photo = itemView.findViewById(R.id.photo);
            iv_selected = itemView.findViewById(R.id.iv_selected);

        }
    }




    private static class UpdateAsyncTask extends AsyncTask<OnlinePhotoModel, Void, Void> {
        private OnlinePhotoDao mAsyncTaskDao;

        UpdateAsyncTask(OnlinePhotoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final OnlinePhotoModel... params) {

            mAsyncTaskDao.update(params[0]);

            return null;
        }
    }



    private void callGetPlanImageAPI(Context context, OnlinePhotoModel projectModel, int pos) {

        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        RetroApiInterface retroApiInterface = RetrofitManager.getInstance().create(RetroApiInterface.class);
        String authToken = sharedPrefsManager.getStringValue(AppConstantsManager.AUTH_API_TOKEN, "");

        if (authToken.length() > 2) {
            authToken = authToken.substring(1, authToken.length() - 1);
        }

        Call<ResponseBody> call = retroApiInterface.getDefectPhotosWithSize(authToken, Utils.DEVICE_ID, projectModel.getPdphotoid(), "sm");
        Log.d("call url", call.request().url().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d("List", "Success : " + response.body());
                        if (writeResponseBodyToDisk(response.body(), projectModel.getProjectId())) {
                            if (imagePath != null && !imagePath.equals("")) {
                                projectModel.setPohotPath(imagePath);
                                projectModel.setPath(imagePath);
                                projectModel.setPhotoCached(true);
                                photosData.get(pos).setPohotPath(imagePath);
                                photosData.get(pos).setPath(imagePath);
                                photosData.get(pos).setPhotoCached(true);
                                notifyItemChanged(pos);
                                new UpdateAsyncTask(mDefectsPhotoDao).execute(projectModel);

                            }else{
                                Toast.makeText(mContext, "Position "+pos, Toast.LENGTH_SHORT).show();
                            }
//                            Bitmap bitmap  =   BitmapFactory.decodeFile(imagePath);
//                            imageView.setImageBitmap(bitmap);
                        }
                    } else {
                        Log.d("List", "Empty response");
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            Log.d("List", "Not Success : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        Log.d("List", "Not Success : " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("List", "failed : " + t.getMessage());
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String projectId) {
        imagePath = "";
        try {
            // todo change the file location/name according to your needs

//            File dir = new File(Environment.getExternalStorageDirectory() + "/projectDocu/project_defects_" + projectId);

            File dir = mContext.getExternalFilesDir("/projectDocu/project_online_photos" + projectId);
            if (dir == null) {
                dir = mContext.getFilesDir();
            }
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File photo = new File(dir, "/Download_" + new Date().getTime() + ".jpg");

            imagePath = photo.getAbsolutePath();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[15000];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(photo);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("A TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }


                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

}
