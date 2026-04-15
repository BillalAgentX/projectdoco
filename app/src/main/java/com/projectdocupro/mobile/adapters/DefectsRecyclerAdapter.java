package com.projectdocupro.mobile.adapters;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.DefectsListItemClickListener;
import com.projectdocupro.mobile.models.DefectsModel;
import com.projectdocupro.mobile.repos.DefectRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DefectsRecyclerAdapter extends RecyclerView.Adapter<DefectsRecyclerAdapter.ProjectViewHolder> {


    HashMap<String, String> defectVsPhoto;
    List<DefectsModel> defectsModels;
    DefectsListItemClickListener listener;
    private Context context;

    public boolean showImages = true;

    public List<DefectsModel> getDefectsModels() {
        return defectsModels;
    }

    public void setDefectsModels(List<DefectsModel> defectsModels) {
        this.defectsModels = defectsModels;
    }

    public DefectsRecyclerAdapter(List<DefectsModel> defectsModels, DefectsListItemClickListener listener, HashMap<String, String> map) {
        this.defectsModels = defectsModels;
        this.listener = listener;
        this.defectVsPhoto = map;
    }

    public void setShowImages(boolean show) {

        this.showImages = show;
    }

    public void setData(List<DefectsModel> newData) {
        defectsModels = newData;
//        projectsData_orignal=newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.defect_view_item, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        DefectsModel defectsModel = defectsModels.get(position);
        holder.title.setText(defectsModel.getDefectName());
        holder.description.setText(defectsModel.getDescription());
        if (showImages) {
            holder.title.setVisibility(VISIBLE);
            if (defectVsPhoto.get(defectsModel.getDefectId()) != null) {
                holder.imgCard.setVisibility(VISIBLE);
                if (!defectVsPhoto.get(defectsModel.getDefectId()).isEmpty())
                    Glide.with(context).load(defectVsPhoto.get(defectsModel.getDefectId())).into(holder.defectImg);


            } else
                holder.imgCard.setVisibility(GONE);
            holder.bottomRow.setVisibility(VISIBLE);
            holder.bottomSeparator.setVisibility(VISIBLE);
        } else {
            holder.description.setText(defectsModel.getDefectName());
            holder.title.setVisibility(GONE);
            holder.imgCard.setVisibility(GONE);
            holder.bottomRow.setVisibility(GONE);
            holder.bottomSeparator.setVisibility(GONE);
        }
/*        if (defectsModel.getDiscipline() != null && !defectsModel.getDiscipline().equals("")) {

            holder.tv_decipline_label.setVisibility(VISIBLE);
            holder.title.setVisibility(VISIBLE);
            holder.title.setText(defectsModel.getDiscipline());
        } else {
            holder.title.setVisibility(View.GONE);
        }*/


        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");
            holder.tv_date.setText(simpleDateFormat.format(new Date(defectsModel.getFristdate_df())));
        } catch (Exception e) {
        }
        holder.nrNumber.setText(defectsModel.getRunId());
        if (!defectsModel.getDefectType().equals("") && defectsModel.getDefectType().equals("1")) {
            holder.art.setText("Mangel");
        } else if (!defectsModel.getDefectType().equals("") && defectsModel.getDefectType().equals("2")) {
            holder.art.setText("Restleistung");
        } else {
            holder.art.setText("");
        }

        holder.tv_creator.setText(defectsModel.getCreator());

        if (defectsModel.isPhotoAttach()) {
            holder.iv_photo_attach.setVisibility(VISIBLE);
        } else {
            holder.iv_photo_attach.setVisibility(GONE);
        }

        if (defectsModel.getRunId().equals("")) {
            holder.iv_delete.setVisibility(VISIBLE);
        } else {
            holder.iv_delete.setVisibility(INVISIBLE);
        }

        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteIconClick(defectsModel);
            }
        });


        StringBuilder creator = new StringBuilder();
        if (defectsModel.defectTradeModelList != null) {
            for (var model : defectsModel.defectTradeModelList) {
                creator.append(model.getPdservicetitle()).append(" / ");
            }
        }


        holder.tv_trador.setText(creator.toString());

        if (defectsModel.getUploadStatus().equals(DefectRepository.SYNCED_PHOTO)) {

            holder.iv_sync.setImageResource(R.drawable.sync_green_bg);
            if (holder.iv_sync.getAnimation() != null)
                holder.iv_sync.getAnimation().cancel();

            holder.iv_sync.setEnabled(false);

        } else if (defectsModel.getUploadStatus().equals(DefectRepository.UN_SYNC_PHOTO)) {
            holder.iv_sync.setImageResource(R.drawable.sync_red_bg);
            if (holder.iv_sync.getAnimation() != null)
                holder.iv_sync.getAnimation().cancel();
            holder.iv_sync.setEnabled(true);
        } else if (defectsModel.getUploadStatus().equals(DefectRepository.UPLOADING_PHOTO)) {
            holder.iv_sync.setImageResource(R.drawable.sync_yellow_bg);
            Animation rotation = AnimationUtils.loadAnimation(context, R.anim.rotation);
            rotation.setFillAfter(true);
            holder.iv_sync.startAnimation(rotation);
            holder.iv_sync.setEnabled(true);
        }
        holder.iv_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //listener.onSyncIconClick(defectsModels.get(position));
            }
        });

        if (!defectsModel.getStatus().equals("") && defectsModel.getStatus().equals("1")) {
            holder.status.setCardBackgroundColor(Color.parseColor("#fa9917"));
            holder.tv_status.setText(context.getString(R.string.progress_status));
        } else if (!defectsModel.getStatus().equals("") && defectsModel.getStatus().equals("2")) {
            holder.status.setCardBackgroundColor(Color.parseColor("#FF4747"));
            holder.tv_status.setText(context.getString(R.string.open_status));
        } else if (!defectsModel.getStatus().equals("") && defectsModel.getStatus().equals("0")) {
            holder.status.setCardBackgroundColor(Color.parseColor("#2ac940"));
            holder.tv_status.setText(context.getString(R.string.close_status));
        } else
            holder.status.setVisibility(INVISIBLE);

        holder.nrNumber.setText(defectsModel.getRunId());

        holder.itemView.setOnClickListener(view -> {
            listener.onListItemClick(defectsModels.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return defectsModels.size();
    }


    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView, bottomRow, bottomSeparator;
        TextView title, description, nrNumber, art, tv_decipline_label, tv_date, tv_status, tv_creator, tv_trador;
        ImageView iv_sync;
        CardView status, imgCard;
        ImageView iv_photo_attach, iv_delete, defectImg;


        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            title = itemView.findViewById(R.id.title);
            tv_decipline_label = itemView.findViewById(R.id.tv_decipline_label);
            description = itemView.findViewById(R.id.description);
            nrNumber = itemView.findViewById(R.id.nr_number);
            art = itemView.findViewById(R.id.art);
            status = itemView.findViewById(R.id.status);
            imgCard = itemView.findViewById(R.id.img_card);
            imgCard = itemView.findViewById(R.id.img_card);
            iv_sync = itemView.findViewById(R.id.iv_sync);
            iv_photo_attach = itemView.findViewById(R.id.iv_photo_attach);
            defectImg = itemView.findViewById(R.id.defectimg);
            iv_delete = itemView.findViewById(R.id.iv_delete);
            tv_date = itemView.findViewById(R.id.date);
            tv_creator = itemView.findViewById(R.id.creator);
            tv_status = itemView.findViewById(R.id.status_text);
            tv_trador = itemView.findViewById(R.id.abc);
            bottomRow = itemView.findViewById(R.id.bottom_row);
            bottomSeparator = itemView.findViewById(R.id.bottom_separator);
        }
    }

}
