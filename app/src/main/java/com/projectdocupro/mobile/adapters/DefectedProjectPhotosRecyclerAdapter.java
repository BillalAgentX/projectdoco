package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.SavePictureActivity;
import com.projectdocupro.mobile.interfaces.DefectProjectsPhotoGridItemClickListener;
import com.projectdocupro.mobile.models.PhotoModel;

import java.util.List;

public class DefectedProjectPhotosRecyclerAdapter extends RecyclerView.Adapter<DefectedProjectPhotosRecyclerAdapter.ProjectViewHolder> {

    private List<PhotoModel> photosData;
    private DefectProjectsPhotoGridItemClickListener listener;
    private Context context;
    String flaw_id = "";

    public List<PhotoModel> getPhotosData() {
        return photosData;
    }

    public void setPhotosData(List<PhotoModel> photosData) {
        this.photosData = photosData;
    }

    public DefectedProjectPhotosRecyclerAdapter(List<PhotoModel> photosData, String flaw_Id, DefectProjectsPhotoGridItemClickListener listener) {
        this.photosData = photosData;
        this.listener = listener;
        flaw_id = flaw_Id;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_view, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        PhotoModel photoModel = photosData.get(position);
        String path = photoModel.getPohotPath();

        if (photoModel.getPdphotoid() == null || photoModel.getPdphotoid().equals("")) {
            holder.iv_sync.setVisibility(View.VISIBLE);
        } else {
            holder.iv_sync.setVisibility(View.GONE);
        }

        if (photoModel.isCameraOpen()) {
            holder.photo.setImageResource(R.drawable.capture_image);
            holder.iv_sync.setVisibility(View.GONE);
        } else {
            if(path!=null&&!path.equals("")){
//                Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), AppConstantsManager.THUMBNAIL_WIDTH,  AppConstantsManager.THUMBNAIL_HEIGHT);
                Glide.with(context).load(path).into(holder.photo);
            }

        }

        holder.itemView.setOnClickListener(view -> {
            if (!photoModel.isCameraOpen()) {
                Intent intent = new Intent(context, SavePictureActivity.class);
                intent.putExtra("isBackCamera", true);
                intent.putExtra("projectId", photoModel.getProjectId());
                intent.putExtra("photoId", photoModel.getPdphotoid());
                intent.putExtra("path", photoModel.getPohotPath());
                intent.putExtra("photoModel", photoModel);
                intent.putExtra("isViewMode", true);
                context.startActivity(intent);
            } else {
                context.startActivity(new Intent(context, SavePictureActivity.class).putExtra("projectId", photoModel.getProjectId()).putExtra("flawId", flaw_id));

            }
        });
    }

    @Override
    public int getItemCount() {
        return photosData.size();
    }


    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView photo;
        ImageView iv_sync;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            photo = itemView.findViewById(R.id.photo);
            iv_sync = itemView.findViewById(R.id.iv_sync);

        }
    }

}
