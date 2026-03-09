package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.PlansListItemClickListener;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.PlansPhotoModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlansRecyclerAdapter extends RecyclerView.Adapter<PlansRecyclerAdapter.ProjectViewHolder> implements Filterable {

    List<PlansModel> plansModels;
    List<PlansModel> plansModelsOrignal;
    PlansListItemClickListener listener;
    private Context context;
    boolean isFavouHide=false;
    public PlansRecyclerAdapter(List<PlansModel> plansModels, PlansListItemClickListener listener) {
        this.plansModels = plansModels;
        this.listener = listener;
        plansModelsOrignal=plansModels;
    }

    public PlansRecyclerAdapter(List<PlansModel> plansModels, boolean isFavHide,PlansListItemClickListener listener) {
        this.plansModels = plansModels;
        plansModelsOrignal=plansModels;

        this.listener = listener;
        isFavouHide= isFavHide;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_view_item, parent, false);
        return new ProjectViewHolder(itemView);
    }

    public void updateData(List<PlansModel> plansModelList) {
        plansModels = new ArrayList<>(plansModelList);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {

        PlansModel plansModel = plansModels.get(position);

        holder.planName.setText(plansModel.getDescription());
        holder.planNumber.setText(plansModel.getPlanNumber());

        if(isFavouHide){
            holder.favoriteIcon.setVisibility(View.INVISIBLE);
        }else{
            holder.favoriteIcon.setVisibility(View.VISIBLE);

        }

        if (plansModel.isFavorite()) {
            holder.favoriteIcon.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
        }

        holder.itemView.setOnClickListener(view -> {
            listener.onListItemClick(plansModels.get(position));
        });

        holder.favoriteIcon.setOnClickListener(view -> {


            if (!plansModel.isFavorite()) {
                holder.favoriteIcon.setImageResource(R.drawable.ic_favorite);
                plansModel.setFavorite(true);

            } else {
                holder.favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
                plansModel.setFavorite(false);

            }
            new updatePlansAsyncTask(context).execute(plansModel);
        });




//        if(plansImagesAsyncTask!=null&&plansImagesAsyncTask.isOnpostCalled){
        if (plansModel.getPlanPhotoPath() != null && !plansModel.getPlanPhotoPath().equals("")) {

            File imgFile = new File(plansModel.getPlanPhotoPath());

            if (imgFile.exists()) {//File exists but not completely downloaded

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


                holder.projectImage.setImageBitmap(myBitmap);

            }
        } else {
            holder.projectImage.setImageBitmap(null);
            new loadPlansImagesAsyncTask(context).execute(plansModel.getPlanId());

        }

//        }


    }

    @Override
    public int getItemCount() {
        return plansModels.size();
    }


    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView projectImage, menuIcon, favoriteIcon;
        TextView planName, planNumber;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            projectImage = itemView.findViewById(R.id.project_image);
            menuIcon = itemView.findViewById(R.id.menu_icon);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            planName = itemView.findViewById(R.id.plan_name);
            planNumber = itemView.findViewById(R.id.plan_number);

        }
    }


    private static class updatePlansAsyncTask extends AsyncTask<PlansModel, Void, Void> {
        private ProjectsDatabase database;

        updatePlansAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected Void doInBackground(final PlansModel... params) {
            database.plansDao().update(params[0]);
            return null;
        }

        protected String doInBackground() {
            return null;
        }
    }

    private class loadPlansImagesAsyncTask extends AsyncTask<String, Void, PlansPhotoModel> {
        private ProjectsDatabase database;
        PlansPhotoModel plansPhotoModel;
        boolean isOnpostCalled;

        loadPlansImagesAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected PlansPhotoModel doInBackground(final String... params) {
            PlansPhotoModel plansModel = database.planPhotosDao().getPlansPhotoObject(params[0]);


            return plansModel;
        }

        @Override
        protected void onPostExecute(PlansPhotoModel aVoid) {
            super.onPostExecute(aVoid);
            isOnpostCalled = true;
            plansPhotoModel = aVoid;
            if(plansPhotoModel!=null) {
                //Utils.showLogger("plansAdapterPhotoModle=>not null"+plansModels.size());
                for (int i = 0; i < plansModels.size(); i++) {
                    if (aVoid.getPlanId().equalsIgnoreCase(plansModels.get(i).getPlanId()) && aVoid.getPohotPath() != null && !aVoid.getPohotPath().equals("")) {

                        plansModels.get(i).setPlanPhotoPath(aVoid.getPohotPath());
                        break;
                    }
                }
                notifyDataSetChanged();
            }
            //else
                //Utils.showLogger("plansAdapterPhotoModle=> null");
        }


    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {

                    plansModels =  plansModelsOrignal;
                } else {
                    List<PlansModel> filteredList = new ArrayList<PlansModel>();
                    for (PlansModel row :  plansModelsOrignal) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getDescription().toLowerCase().contains(charString.toLowerCase()) || row.getDescription().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    plansModels = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = plansModels;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                plansModels = (ArrayList<PlansModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
