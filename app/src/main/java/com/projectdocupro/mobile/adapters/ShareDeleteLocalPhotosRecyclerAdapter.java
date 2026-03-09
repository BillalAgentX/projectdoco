package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.LocalPhotosListItemClickListener;
import com.projectdocupro.mobile.models.PhotoModel;

import java.util.List;

public class ShareDeleteLocalPhotosRecyclerAdapter extends RecyclerView.Adapter<ShareDeleteLocalPhotosRecyclerAdapter.ProjectViewHolder> {

    private List<PhotoModel> photosData;
    private LocalPhotosListItemClickListener listener;
    private Context context;

    public List<PhotoModel> getPhotosData() {
        return photosData;
    }

    public void setPhotosData(List<PhotoModel> photosData) {
        this.photosData = photosData;
    }

    public ShareDeleteLocalPhotosRecyclerAdapter(List<PhotoModel> photosData, LocalPhotosListItemClickListener listener) {
        this.photosData = photosData;
        this.listener = listener;
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

        if (photoModel.isUserSelectedStatus())
            holder.iv_selected.setVisibility(View.VISIBLE);
        else
            holder.iv_selected.setVisibility(View.GONE);


        if (path != null && !path.equals("")) {
//                Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), AppConstantsManager.THUMBNAIL_WIDTH,  AppConstantsManager.THUMBNAIL_HEIGHT);
            Glide.with(context).load(path).into(holder.photo);

        }

        holder.itemView.setOnClickListener(view -> {

            if (photosData.get(position).isUserSelectedStatus()) {
                photosData.get(position).setUserSelectedStatus(false);
            } else {
                photosData.get(position).setUserSelectedStatus(true);
            }
            if (listener != null)
                listener.onListItemClick(photosData.get(position));
            notifyItemChanged(position);

        });
    }

    @Override
    public int getItemCount() {
        return photosData.size();
    }


    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView photo;
        ImageView iv_sync, iv_selected;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            photo = itemView.findViewById(R.id.photo);
            iv_sync = itemView.findViewById(R.id.iv_sync);
            iv_selected = itemView.findViewById(R.id.iv_selected);


        }
    }

}
