package com.projectdocupro.mobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.interfaces.LocalPhotosListItemClickListener;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.util.List;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ProjectViewHolder> {

    private  Animation rotation=null;
    private List<PhotoModel> photosData;
    private LocalPhotosListItemClickListener listener;
    private Context context;
    private String projectId;

    public PhotosRecyclerAdapter(String projectId, List<PhotoModel> photosData, LocalPhotosListItemClickListener listener) {
        this.projectId = projectId;
        this.photosData = photosData;
        this.listener = listener;

    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        rotation = AnimationUtils.loadAnimation(context, R.anim.rotation);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_view, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        PhotoModel photoModel = photosData.get(position);
        String path = photoModel.getPath();



        if (path != null && !path.equals("")) {
//            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), AppConstantsManager.THUMBNAIL_WIDTH,  AppConstantsManager.THUMBNAIL_HEIGHT);
//            Glide.with(context).asBitmap().load(ThumbImage).diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .dontAnimate().into(holder.photo);
            Glide.with(context).load(path).placeholder(R.drawable.border_photos).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate().into(holder.photo);

        }


        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //if you need three fix imageview in width
//        int devicewidth = displaymetrics.widthPixels / 3;
//
//        holder.itemView.getLayoutParams().width = devicewidth;
//        holder.itemView.getLayoutParams().height = devicewidth;
        holder.iv_sync.setVisibility(View.VISIBLE);

        if (photoModel.getPhotoTime() != null && !photoModel.getPhotoTime().equalsIgnoreCase("")) {
            holder.tv_time.setVisibility(View.VISIBLE);
            try {

                holder.tv_time.setText(photoModel.getPhotoTime()/*+" "+photoModel.getPdphotolocalId()+""*/);
            }
            catch (Exception e){
                e.printStackTrace();
                //holder.tv_time.setText(photoModel.getPhotoTime()/*+" "+photoModel.getPdphotolocalId()+""*/);

            }
        } else {
            holder.tv_time.setVisibility(View.GONE);
        }
        if (!photoModel.isPhotoSynced()) {

            if (photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UN_SYNC_PHOTO) && photoModel.getFailedCount() > 5) {
//                holder.iv_sync.setImageResource(R.drawable.fatal_error);
                holder.iv_sync.setImageResource(R.drawable.sync_red_bg);
                if (holder.iv_sync.getAnimation() != null)
                    holder.iv_sync.getAnimation().cancel();
                holder.iv_sync.setEnabled(true);

            } else if (photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UN_SYNC_PHOTO)) {
                holder.iv_sync.setImageResource(R.drawable.sync_red_bg);
                if (holder.iv_sync.getAnimation() != null)
                    holder.iv_sync.getAnimation().cancel();
                holder.iv_sync.setEnabled(true);

            } else if (photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.UPLOADING_PHOTO)) {
                holder.iv_sync.setImageResource(R.drawable.sync_yellow_bg);

                rotation.setFillAfter(true);
                holder.iv_sync.startAnimation(rotation);
                holder.iv_sync.setEnabled(true);

            } else if (photoModel.getPdphotoid() != null && !photoModel.getPdphotoid().equals("") && photoModel.getPhotoUploadStatus().equalsIgnoreCase(LocalPhotosRepository.SHORTLY_SYNCED_PHOTO)) {
                holder.iv_sync.setImageResource(R.drawable.sync_blue_bg);
//                Animation rotation = AnimationUtils.loadAnimation(context, R.anim.rotation);
//                rotation.setFillAfter(true);
//                holder.iv_sync.startAnimation(rotation);
                holder.iv_sync.setEnabled(true);
            }

        } else {


            holder.iv_sync.setImageResource(R.drawable.sync_green_bg);
            if (holder.iv_sync.getAnimation() != null)
                holder.iv_sync.getAnimation().cancel();

            holder.iv_sync.setEnabled(false);

        }
        holder.iv_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                photoModel.setClickedPosition(position);
                listener.onListItemClick(photoModel);
            }
        });
        holder.itemView.setOnClickListener(view -> {

            Intent intent = new Intent(context, SavePictureActivity.class);
            intent.putExtra("isBackCamera", true);
            intent.putExtra("projectId", projectId);
            intent.putExtra("photoId", photoModel.getPdphotolocalId());
            intent.putExtra("path", photoModel.getPath());
            intent.putExtra("photoModel", photoModel);
            intent.putExtra("position", position);
            intent.putExtra("isFromLocalPhotos", true);
            ((Activity) context).startActivityForResult(intent, 7890);
        });
    }

    Drawable getRotateDrawable(final Bitmap b, final float angle) {
        final BitmapDrawable drawable = new BitmapDrawable(context.getResources(), b) {
            @Override
            public void draw(final Canvas canvas) {
                canvas.save();
                canvas.rotate(angle, b.getWidth() / 2, b.getHeight() / 2);
                super.draw(canvas);
                canvas.restore();
            }
        };
        return drawable;
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
        ImageView iv_sync;
        TextView tv_time;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            photo = itemView.findViewById(R.id.photo);
            iv_sync = itemView.findViewById(R.id.iv_sync);
            tv_time = itemView.findViewById(R.id.tv_time);


        }
    }

}
