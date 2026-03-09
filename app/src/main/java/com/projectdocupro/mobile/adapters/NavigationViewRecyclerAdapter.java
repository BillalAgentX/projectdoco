package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.NavigationItemClickListener;
import com.projectdocupro.mobile.managers.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;

public class NavigationViewRecyclerAdapter extends RecyclerView.Adapter<NavigationViewRecyclerAdapter.ProjectViewHolder> {

    private List<Integer> itemTitles;
    private List<Integer>   itemIcons;
    private List<Integer>   itemIds;
    private NavigationItemClickListener listener;
    private Context context;

    public NavigationViewRecyclerAdapter(Context    context,NavigationItemClickListener listener){
        this.itemTitles = new ArrayList<>();
        this.itemIcons=new ArrayList<>();
        this.itemIds=new ArrayList<>();

        SharedPrefsManager  sharedPrefsManager  =   new SharedPrefsManager(context);
        if (!sharedPrefsManager.getLastProjectId(context).isEmpty()){
            itemTitles.add(R.string.project);
            itemTitles.add(R.string.menu_camera);
            itemTitles.add(R.string.menu_plans);
            itemTitles.add(R.string.menu_photos);
            itemTitles.add(R.string.menu_defects_managment);

            itemIcons.add(R.drawable.ic_menu_project);
            itemIcons.add(R.drawable.ic_menu_camera);
            itemIcons.add(R.drawable.ic_menu_plans);
            itemIcons.add(R.drawable.ic_menu_photos);
            itemIcons.add(R.drawable.ic_menu_flaws);

            itemIds.add(R.id.project_item);
            itemIds.add(R.id.nav_camera);
            itemIds.add(R.id.nav_plans);
            itemIds.add(R.id.nav_photos);
            itemIds.add(R.id.nav_defects_management);
        }

        itemTitles.add(R.string.menu_settings);
        itemTitles.add(R.string.menu_about);
        itemTitles.add(R.string.menu_manual);

        itemIcons.add(R.drawable.ic_menu_settings);
        itemIcons.add(R.drawable.ic_menu_info);
        itemIcons.add(R.drawable.ic_menu_manual);

        itemIds.add(R.id.nav_settings);
        itemIds.add(R.id.nav_about);
        itemIds.add(R.id.nav_manual);

        this.listener   =   listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.navigation_view_item, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {


        holder.itemTitle.setText(context.getString(itemTitles.get(position)));
        holder.itemIcon.setImageResource(itemIcons.get(position));

        SharedPrefsManager  sharedPrefsManager  =   new SharedPrefsManager(context);
        if (!sharedPrefsManager.getLastProjectId(context).isEmpty()){
            if (position>0 &&  position<=4){
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                Resources r = context.getResources();
                int px = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        35,
                        r.getDisplayMetrics());
                params.setMargins(px, 0, 0, 0);
                holder.layout.setLayoutParams(params);
            }
            if (position==0){
                holder.itemTitle.setText(context.getString(itemTitles.get(position)));
            }
        }

        holder.itemView.setId(itemIds.get(position));

        holder.itemView.setOnClickListener(v -> {
            listener.onListItemClick(holder.itemView,position);
        });

    }

    @Override
    public int getItemCount() {
        return itemTitles.size();
    }


    public class ProjectViewHolder  extends RecyclerView.ViewHolder{

        View    itemView;
        LinearLayout    layout;
        ImageView itemIcon;
        TextView itemTitle;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
            itemIcon =   itemView.findViewById(R.id.item_icon);
            itemTitle    =   itemView.findViewById(R.id.item_title);
            layout  =   itemView.findViewById(R.id.item_view);

        }
    }

}
