package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.fragments.add_direction.ProjectDocuUtilities;
import com.projectdocupro.mobile.interfaces.ProjectsListItemClickListener;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectsRecyclerAdapter extends RecyclerView.Adapter<ProjectsRecyclerAdapter.ProjectViewHolder> implements Filterable {

    private List<ProjectModel> projectsData;
    public ProjectModel selectedProject;
    public int lastSelectedPosition = -1;

    public List<ProjectModel> getProjectsData() {
        return projectsData;
    }

    public void setProjectsData(List<ProjectModel> projectsData) {
        this.projectsData = projectsData;
    }

    private List<ProjectModel> projectsData_orignal;
    private ProjectsListItemClickListener listener;
    private Context context;
    boolean isfavouriteShow;

    public ProjectsRecyclerAdapter(List<ProjectModel> projectsData, boolean isFavouriteShow, ProjectsListItemClickListener listener) {
        this.projectsData = projectsData;
        this.projectsData_orignal = projectsData;
        this.listener = listener;
        isfavouriteShow = isFavouriteShow;
    }

    public ProjectsRecyclerAdapter(List<ProjectModel> projectsData, ProjectsListItemClickListener listener) {
        this.projectsData = projectsData;
        this.projectsData_orignal = projectsData;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_view_item, parent, false);
        return new ProjectViewHolder(itemView);
    }

    public void setData(List<ProjectModel> newData) {


        projectsData = newData;
        projectsData_orignal = newData;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder,int  position) {
        ProjectModel project = projectsData.get(position);

        holder.projectName.setText(project.getProject_name());
        holder.place.setText(project.getCity());
        holder.company.setText(project.getCompany_name());
        holder.numberPhotos.setText(project.getPhotocount());

        if (project.getExtra1() != null) {
            if (project.getExtra1().equals("1"))
                holder.cardParent.setBackgroundResource(R.drawable.dialog_background);
            else
                holder.cardParent.setBackgroundResource(R.drawable.dialog_background_inactive);
        } else
            holder.cardParent.setBackgroundResource(R.drawable.dialog_background);


        if (isfavouriteShow) {
            holder.favoriteIcon.setVisibility(View.VISIBLE);
            if (project.isFavorite()) {
                holder.iv_sync.setVisibility(View.VISIBLE);
                if (project.getLastUpdatedProjectStatus() != null && project.getLastUpdatedProjectStatus().equals(LocalPhotosRepository.UPLOADING_PHOTO)) {
                    holder.iv_project_loading.setVisibility(View.VISIBLE);
                    holder.favoriteIcon.setEnabled(false);
                } else {
                    holder.iv_project_loading.setVisibility(View.INVISIBLE);
                    holder.favoriteIcon.setEnabled(true);
                }

            } else {
                if (project.getLastUpdatedProjectStatus() != null && project.getLastUpdatedProjectStatus().equals(LocalPhotosRepository.UPLOADING_PHOTO)) {
                    holder.iv_project_loading.setVisibility(View.VISIBLE);
                    holder.favoriteIcon.setEnabled(false);
                } else {
                    holder.iv_project_loading.setVisibility(View.INVISIBLE);
                    holder.favoriteIcon.setEnabled(true);
                }
                holder.favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
                holder.iv_sync.setImageResource(R.drawable.sync_red_bg);
                holder.iv_sync.setVisibility(View.VISIBLE);
                // holder.iv_sync.setVisibility(View.INVISIBLE);
            }
            if (project.isFavorite()) {
                holder.iv_sync.setImageResource(R.drawable.sync_green_bg);
                holder.favoriteIcon.setImageResource(R.drawable.ic_favorite);
            } else {
                holder.favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
                holder.iv_sync.setImageResource(R.drawable.sync_red_bg);
                holder.iv_sync.setVisibility(View.VISIBLE);
            }
        } else {
            holder.favoriteIcon.setVisibility(View.INVISIBLE);

        }


        holder.itemView.setTag(projectsData.get(position));
//        if (!isfavouriteShow) {
        new UpdateProjectAsyncTask(position,context,isfavouriteShow).execute(holder);
//        }


//        if (project.getSyncStatus() != null && project.getSyncStatus().equalsIgnoreCase(LocalPhotosRepository.SYNCED_PHOTO)) {
//            holder.iv_sync.setImageResource(R.drawable.sync_green_bg);
//            if (holder.iv_sync.getAnimation() != null)
//                holder.iv_sync.getAnimation().cancel();
//            holder.iv_sync.setEnabled(false);
//
//        } else if (project.getSyncStatus() != null && project.getSyncStatus().equalsIgnoreCase(LocalPhotosRepository.UN_SYNC_PHOTO)) {
//            holder.iv_sync.setImageResource(R.drawable.sync_red_bg);
//            if (holder.iv_sync.getAnimation() != null)
//                holder.iv_sync.getAnimation().cancel();
//            holder.iv_sync.setEnabled(true);
//
//        } else if (project.getSyncStatus() != null && project.getSyncStatus().equalsIgnoreCase(LocalPhotosRepository.UPLOADING_PHOTO)) {
//            holder.iv_sync.setImageResource(R.drawable.sync_yellow_bg);
//            Animation rotation = AnimationUtils.loadAnimation(context, R.anim.rotation);
//            rotation.setFillAfter(true);
//            holder.iv_sync.startAnimation(rotation);
//            holder.iv_sync.setEnabled(true);
//
//        }


        holder.itemView.setOnClickListener(view -> {
            selectedProject = projectsData.get(position);
            lastSelectedPosition = position;
            listener.onListItemClick(projectsData.get(position), false, this);
        });

        Log.d("lastUpdated", project.getLastupdated());

        holder.iv_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSyncActionClick(projectsData.get(position));
            }
        });

        holder.favoriteIcon.setOnClickListener(view -> {
            selectedProject = projectsData.get(position);
            lastSelectedPosition = position;

//            SharedPrefsManager sharedPrefsManager  =   new SharedPrefsManager(context);
//
//            project.setFavorite(!project.isFavorite());
//            if (project.isFavorite()){
//                holder.favoriteIcon.setImageResource(R.drawable.ic_favorite);
//                sharedPrefsManager.setFavouriteProjetBooleanValue(false);
//            }else{
//                sharedPrefsManager.setFavouriteProjetBooleanValue(true);
//                holder.favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
//            }
            if (ProjectDocuUtilities.isNetworkConnected(context)) {
                listener.onListItemClick(projectsData.get(position), true, this);

            } else {
                Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_LONG).show();
            }
//            Toast.makeText(context,"Fav call",Toast.LENGTH_SHORT).show();
        });

        if (project != null && project.getCacheImagePath() != null && !project.getCacheImagePath().equals("")) {

            File imgFile = new File(project.getCacheImagePath());

            if (imgFile.exists()) {

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                holder.projectImage.setImageBitmap(myBitmap);

            }
            else
                Utils.showLogger2("folder not exists");
        } else {
            holder.projectImage.setImageBitmap(null);
            Utils.showLogger2("set image null");
        }
    }

    @Override
    public int getItemCount() {
        return projectsData.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {

                    projectsData = projectsData_orignal;
                } else {
                    List<ProjectModel> filteredList = new ArrayList<ProjectModel>();
                    for (ProjectModel row : projectsData_orignal) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getProject_name().toLowerCase().contains(charString.toLowerCase()) || row.getProject_name().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    projectsData = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = projectsData;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                projectsData = (ArrayList<ProjectModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }




    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView projectImage, menuIcon, favoriteIcon, iv_sync;
        TextView projectName, place, company, numberPhotos;
        ProgressBar iv_project_loading;

        View cardParent;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            projectImage = itemView.findViewById(R.id.project_image);
            menuIcon = itemView.findViewById(R.id.menu_icon);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            projectName = itemView.findViewById(R.id.project_name);
            place = itemView.findViewById(R.id.plan_number);
            company = itemView.findViewById(R.id.company_name);
            numberPhotos = itemView.findViewById(R.id.number_photos);
            iv_sync = itemView.findViewById(R.id.iv_sync);
            iv_project_loading = itemView.findViewById(R.id.iv_project_loading);
            cardParent = itemView.findViewById(R.id.card_parent);

        }
    }



}
