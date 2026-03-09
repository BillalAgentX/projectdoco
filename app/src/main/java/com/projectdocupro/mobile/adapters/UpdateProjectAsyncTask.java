package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.DefectsDao;
import com.projectdocupro.mobile.dao.PhotoDao;
import com.projectdocupro.mobile.dao.ProjectDao;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.repos.LocalPhotosRepository;

public class UpdateProjectAsyncTask extends AsyncTask<ProjectsRecyclerAdapter.ProjectViewHolder, Void, Void> {

    Context context;
    boolean isfavouriteShow;
    public UpdateProjectAsyncTask(int position,Context context,boolean isfavouriteShow){
        this.context = context;
        this.isfavouriteShow = isfavouriteShow;

        index = position;
        mAsyncTaskDao = ProjectsDatabase.getDatabase(context).defectsDao();
        photoDao = ProjectsDatabase.getDatabase(context).photoDao();
        projectDao = ProjectsDatabase.getDatabase(context).projectDao();
    }

    private DefectsDao mAsyncTaskDao;
    private PhotoDao photoDao;
    private ProjectDao projectDao;
    String projectId = "";

    ProjectsRecyclerAdapter.ProjectViewHolder holder;

    ProjectModel projectModel;

    boolean isUploading, isSynced, isUnSynced;
    boolean isUploadingDefect, isSyncedDefect, isUnSyncedDefect;

    long unSyncPhotoCountDefect = 0, syncPhotoCountDefect = 0, uploadingPhotoCountDefect = 0;
    long unSyncPhotoCount = 0, syncPhotoCount = 0, uploadingPhotoCount = 0;
    int index = 0;


    @Override
    protected Void doInBackground(final ProjectsRecyclerAdapter.ProjectViewHolder... params) {
        //  mAsyncTaskDao.update(params[0]);
        holder = params[0];
        projectModel = (ProjectModel) params[0].itemView.getTag();
        if (projectModel == null || projectModel.getProjectid() == null)
            return null;
        projectId = projectModel.getProjectid();
        projectModel = projectDao.getSpecificProject(projectId);
        unSyncPhotoCount = photoDao.getSpecificUnSyncPhotoCount(projectId);
        syncPhotoCount = photoDao.getSyncedPhotoCount(projectId);
        uploadingPhotoCount = photoDao.getUploadingPhotoCount(projectId);

        if (uploadingPhotoCount == 0 && unSyncPhotoCount == 0) {
            isSynced = true;
        } else if (uploadingPhotoCount > 0) {
            isUploading = true;
        } else {
            isUnSynced = true;
        }

        unSyncPhotoCountDefect = mAsyncTaskDao.getSpecificUnSyncedPhotoCount(projectId);
        syncPhotoCountDefect = mAsyncTaskDao.getSyncedPhotoCount(projectId);
        uploadingPhotoCountDefect = mAsyncTaskDao.getUploadingPhotoCount(projectId);

        if (unSyncPhotoCountDefect == 0 && uploadingPhotoCountDefect == 0) {
            isSyncedDefect = true;
        } else if (uploadingPhotoCountDefect > 0) {
            isUploadingDefect = true;
        } else {
            isUnSyncedDefect = true;
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (projectModel == null)
            return;
        if (isfavouriteShow) {
            if (projectModel.isFavorite()) {
                holder.iv_sync.setVisibility(View.VISIBLE);
                if (isUploading) {
//                    projectsData.get(index).setSyncStatus(LocalPhotosRepository.UPLOADING_PHOTO);
                    holder.iv_sync.setImageResource(R.drawable.sync_yellow_bg);
                    Animation rotation = AnimationUtils.loadAnimation(context, R.anim.rotation);
                    rotation.setFillAfter(true);
                    holder.iv_sync.startAnimation(rotation);
                    holder.iv_sync.setEnabled(true);

                } else if (isSyncedDefect && isSynced) {

//                    projectsData.get(index).setSyncStatus(LocalPhotosRepository.SYNCED_PHOTO);


                    holder.iv_sync.setImageResource(R.drawable.sync_green_bg);
                    if (holder.iv_sync.getAnimation() != null)
                        holder.iv_sync.getAnimation().cancel();
                    holder.iv_sync.setEnabled(false);

                } else {
//                    projectsData.get(index).setSyncStatus(LocalPhotosRepository.UN_SYNC_PHOTO);

                    holder.iv_sync.setImageResource(R.drawable.sync_yellow_bg);
                    if (holder.iv_sync.getAnimation() != null)
                        holder.iv_sync.getAnimation().cancel();
                    holder.iv_sync.setEnabled(true);
                }

                if (projectModel.getSyncStatus().equals(LocalPhotosRepository.UPLOADING_PHOTO)) {
                    holder.iv_sync.setImageResource(R.drawable.sync_yellow_bg);
                    holder.iv_sync.setVisibility(View.VISIBLE);
                    holder.iv_project_loading.setVisibility(View.GONE);
                } else {
//                    holder.iv_sync.setImageResource(R.drawable.sync_green_bg);
//                    holder.iv_sync.setVisibility(View.VISIBLE);
                    holder.iv_project_loading.setVisibility(View.GONE);

                }

//                notifyItemChanged(index);
            } else {
                holder.iv_sync.setImageResource(R.drawable.sync_red_bg);
                holder.iv_sync.setVisibility(View.VISIBLE);


            }
        } else {
            holder.iv_sync.setVisibility(View.INVISIBLE);
        }
//
//            if (projectModel.isFavorite()) {
//                holder.favoriteIcon.setImageResource(R.drawable.ic_favorite);
//
//            } else {
//                holder.favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
//                holder.iv_sync.setImageResource(R.drawable.sync_red_bg);
//                holder.iv_sync.setVisibility(View.VISIBLE);
//            }


//            if (photoModelList != null && photoModelList.size() == 0) {
//                if (getActivity() != null && adapter != null) {
//                    for (int i = 0; i < adapter.getProjectsData().size(); i++) {
//                        if (adapter.getProjectsData().get(i).getProjectid().equals(projectId)) {
//                            adapter.getProjectsData().get(i).setSyncStatus(LocalPhotosRepository.SYNCED_PHOTO);
//                            adapter.notifyDataSetChanged();
//                            break;
//                        }
//                    }
//                }
//            }
    }
}
