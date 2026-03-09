package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.LocalPhotosListItemClickListener;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.PhotoModel;
import com.projectdocupro.mobile.models.ProjectModel;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;
import com.projectdocupro.mobile.utility.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class PhotosGroupRecyclerAdapter extends RecyclerView.Adapter<PhotosGroupRecyclerAdapter.ProjectViewHolder> {

    private SharedPrefsManager sharedPrefsManager;
    private Map<Date, List<PhotoModel>> photosGroups;
    public  List<Date> groupNames;
    SimpleDateFormat simpleDateFormat;

    public Map<Date, List<PhotoModel>> getPhotosGroups() {
        return photosGroups;
    }

    public void setPhotosGroups(Map<Date, List<PhotoModel>> photosGroups) {
        this.photosGroups = photosGroups;
    }

    private Context context;
    private String projectId;
    List<PhotoModel> photoModelsList;

    public List<PhotoModel> getPhotoModelsList() {
        return photoModelsList;
    }

    public void setPhotoModelsList(List<PhotoModel> photoModelsList) {
        this.photoModelsList = photoModelsList;
    }

    LocalPhotosListItemClickListener itemClickListener;

    public PhotosGroupRecyclerAdapter(String projectId, List<PhotoModel> photosData, LocalPhotosListItemClickListener listener) {
        this.projectId = projectId;
         itemClickListener = listener;
        Map<Date, List<PhotoModel>> photosGroupsLocal = new HashMap<>();
        sharedPrefsManager = new SharedPrefsManager(ProjectNavigator.context);
        groupNames = new ArrayList<>();
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");
        }
        Date date = null;
        for (PhotoModel photoModel : photosData) {
            date = new Date(photoModel.created_df);
            date = trim(date);

            photoModelsList = photosGroupsLocal.get(date);
            if (photoModelsList == null) {
                photoModelsList = new ArrayList<>();

                groupNames.add(date);
            }
            photoModelsList.add(photoModel);
            if(photoModelsList.size()>0)
            photosGroupsLocal.put(date, photoModelsList);
//            Collections.sort(groupNames, new Comparator<Date>(){
//
//                @Override
//                public int compare(Date o1, Date o2) {
//                    return o1.compareTo(o2);
//                }
//            });

            if (photosGroupsLocal != null && photosGroupsLocal.entrySet().size() > 0)

                photosGroups = new TreeMap<Date, List<PhotoModel>>(photosGroupsLocal);
        }

    }

    public Date trim(Date date) {
        Date dateWithoutTime = null;
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");
        }
        try {
            dateWithoutTime = simpleDateFormat.parse(simpleDateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateWithoutTime);
//        calendar.set(Calendar.MILLISECOND, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.HOUR, 0);

        return calendar.getTime();
    }

    public void PreparePhotosGroupData(List<PhotoModel> photosData) {

        groupNames = new ArrayList<>();
        Map<Date, List<PhotoModel>> photosGroupsLocal = new HashMap<>();
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");

        } else {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");
        }
        Date date = null;
        for (PhotoModel photoModel : photosData) {
            date = new Date(photoModel.created_df);
            date = trim(date);

            photoModelsList = photosGroupsLocal.get(date);
            if (photoModelsList == null) {
                photoModelsList = new ArrayList<>();

                groupNames.add(date);
            }
            photoModelsList.add(photoModel);
            photosGroupsLocal.put(date, photoModelsList);
//            Collections.sort(groupNames, new Comparator<Date>(){
//
//                @Override
//                public int compare(Date o1, Date o2) {
//                    return o1.compareTo(o2);
//                }
//            });
            if (photosGroupsLocal != null && photosGroupsLocal.entrySet().size() > 0)
                photosGroups = new TreeMap<Date, List<PhotoModel>>(photosGroupsLocal);
        }
    }

    public PhotoModel getSelectedOBJ(PhotoModel photoModel) {
        return photoModel;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        sharedPrefsManager = new SharedPrefsManager(context);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_group_list_item, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {

        if (groupNames.get(position) == null)
            return;

        List<PhotoModel> photoModel = photosGroups.get(groupNames.get(position));
        ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
        GridLayoutManager linearLayoutManager;
        if (sharedPrefsManager.getStringValue(AppConstantsManager.APP_LANGUAGE, "de").equals("de")) {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy", Locale.GERMANY);
        } else {
            simpleDateFormat = new SimpleDateFormat("dd.MMMM yyyy");
        }
        //            Date date = simpleDateFormat.parse(groupNames.get(position));
        holder.groupTitle.setText(simpleDateFormat.format(groupNames.get(position)));

//        holder.photosRV.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        linearLayoutManager = new GridLayoutManager(context,
                projectDocuUtilities.calculateNoOfColumns(context, 130));
        if(photoModel.size()>0) {
            holder.photosRV.setLayoutManager(linearLayoutManager);
            holder.photosRV.setAdapter(new PhotosRecyclerAdapter(projectId, photoModel, itemClickListener));
            holder.photosRV.setHasFixedSize(true);
        }else{
            holder.photosRV.setVisibility(View.GONE);
            holder.groupTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return groupNames.size();
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

        TextView groupTitle;
        RecyclerView photosRV;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);

            this.groupTitle = itemView.findViewById(R.id.group_title);
            photosRV = itemView.findViewById(R.id.photos_rv);

        }
    }

}
