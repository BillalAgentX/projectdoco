package com.projectdocupro.mobile.adapters;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.AudioRecordingListItemClickListener;
import com.projectdocupro.mobile.models.RecordAudioModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.IOException;
import java.util.List;

public class RecordingsRecyclerAdapter extends RecyclerView.Adapter<RecordingsRecyclerAdapter.ProjectViewHolder> {

    List<RecordAudioModel> plansModels;
    AudioRecordingListItemClickListener listener;
    MediaPlayer player;
    String lastPath = "";
    ImageView lastImage = null;

    public RecordingsRecyclerAdapter(List<RecordAudioModel> plansModels, AudioRecordingListItemClickListener listener) {
        this.plansModels = plansModels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recordingview_item, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        RecordAudioModel recordAudioModel = plansModels.get(position);


        holder.recordName.setText(recordAudioModel.getName());
        holder.recordDate.setText(recordAudioModel.getDate());
        holder.recordDuration.setText(recordAudioModel.getDuration().replace(",",":"));
        holder.iv_play.setTag(position);
        if (plansModels.get(position).isPlaying) {

            holder.iv_play.setImageResource(R.drawable.puasegrey);
//            holder.iv_play.setTag(false);
        } else {
            holder.iv_play.setImageResource(R.drawable.playgrey);
//            holder.iv_play.setTag(true);
        }


        if(recordAudioModel.getRecordServerId()==null||recordAudioModel.getRecordServerId().equals(""))
            holder.iv_status.setVisibility(View.INVISIBLE);
        else
            holder.iv_status.setImageResource(R.drawable.ic_done);
        holder.iv_play.setOnClickListener(v -> {
            lastImage = (ImageView) v;
            lastPath = recordAudioModel.getPath();
            int pos = (int) holder.iv_play.getTag();

            if (holder.iv_play != null/*&&(boolean)holder.iv_play.getTag()==true*/) {
//                stopPlaying(lastImage);
                //    startPlaying(recordAudioModel.getPath(), holder.iv_play);
//            }else{

                if (plansModels.get(pos).isPlaying) {
//                            plansModels.get(i).isPlaying=true;
//                        }else{
                    plansModels.get(pos).isPlaying = false;
                    if(player!=null)
                        player.reset();
                    notifyDataSetChanged();
                } else {
                    startPlaying(recordAudioModel.getPath(), plansModels.get(pos).getRecordId(), holder.iv_play);
                    Utils.showLogger("playing_audio_file"+recordAudioModel.getPath());
                }
            }

//            if (player!=null&&player.isPlaying()==true){
////                if(!lastPath.equalsIgnoreCase("")) {
////                    holder.iv_play.setImageResource(R.drawable.playgrey);
////                    pausePlaying(lastPath, lastImage);
//                    stopPlaying(holder.iv_delete);
//                    startPlaying(recordAudioModel.getPath(), holder.iv_play);
//
//               // }
//            }else{
//                holder.iv_play.setImageResource(R.drawable.puasegrey);
//                startPlaying(recordAudioModel.getPath(),holder.iv_play);
//
//            }
        });

        holder.iv_delete.setOnClickListener(v -> {
            if (player != null)
                player.release();
            player = null;
            listener.onListItemClick(recordAudioModel);
        });


    }

    @Override
    public int getItemCount() {
        return plansModels.size();
    }


    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView recordName, recordDate, recordDuration;
        ImageView iv_play, iv_delete,iv_status;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            recordName = itemView.findViewById(R.id.record_name);
            recordDate = itemView.findViewById(R.id.record_date);
            recordDuration = itemView.findViewById(R.id.record_duration);
            iv_play = itemView.findViewById(R.id.iv_play);
            iv_delete = itemView.findViewById(R.id.iv_delete);
            iv_status = itemView.findViewById(R.id.synch_staus);

        }
    }


    private void pausePlaying(String fileName, ImageView imageView) {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.pause();
            imageView.setImageResource(R.drawable.playgrey);

        } catch (IOException e) {
            Log.e("Play", "prepare() failed");
        }
    }


    private void startPlaying(String fileName, int id, ImageView imageView) {
        int recordingId = id;
        if (player == null)
            player = new MediaPlayer();
        if (player.isPlaying())
            player.reset();

        for (int i = 0; i < plansModels.size(); i++) {
            if (plansModels.get(i).getRecordId() == id) {
                plansModels.get(i).isPlaying = true;
            } else {
                plansModels.get(i).isPlaying = false;
            }
        }
        notifyDataSetChanged();

        try {
            if(fileName!=null&&!fileName.equals("")){
                player.reset();
            player.setDataSource(fileName);
            player.prepare();
            player.start();
            }
            imageView.setTag(true);
            imageView.setImageResource(R.drawable.puasegrey);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    for (int i = 0; i < plansModels.size(); i++) {
                        if (plansModels.get(i).getRecordId() == recordingId) {
//                            plansModels.get(i).isPlaying=true;
//                        }else{
                            plansModels.get(i).isPlaying = false;
                        }
                    }
                    notifyDataSetChanged();

                    /*if(player!=null&&player.isPlaying() == true){
                        imageView.setImageResource(R.drawable.puasegrey );
                        imageView.setTag(true);
                    }else{
                        imageView.setImageResource(R.drawable.playgrey);
                        imageView.setTag(false);
                    }*/


                }
            });

        } catch (IOException e) {
            Log.e("Play", "prepare() failed");
        }
    }

    private void stopPlaying(ImageView imageView) {
        if (player != null)
            player.release();
        if (imageView != null) {
            imageView.setTag(false);
            imageView.setImageResource(R.drawable.playgrey);
        }
        lastPath = "";
        lastImage = null;
        player = null;
    }


    public void appInBackgroundState(){
        if (player != null)
            player.reset();
        for (int i = 0; i < plansModels.size(); i++) {
                plansModels.get(i).isPlaying = false;
        }
        notifyDataSetChanged();
    }

}
