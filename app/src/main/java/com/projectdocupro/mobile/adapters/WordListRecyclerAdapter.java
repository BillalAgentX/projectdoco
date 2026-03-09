package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.WordsSelectListener;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WordListRecyclerAdapter extends RecyclerView.Adapter<WordListRecyclerAdapter.ProjectViewHolder> {

    Map<String, List<WordModel>> wordsHash;
    List<String> keys;
    WordsSelectListener listener;
    Context context;
    List<WordsSubRecyclerAdapter> wordsSubRecyclerAdapters;
    private long photoId;

    public WordListRecyclerAdapter() {
    }

    public WordListRecyclerAdapter(long photoId, List<String> keys, Map<String, List<WordModel>> wordsHash, WordsSelectListener listener) {
        this.photoId = photoId;
        this.keys = keys;
        this.wordsHash = wordsHash;
        this.listener = listener;
        wordsSubRecyclerAdapters = new ArrayList<>();
    }

    public void setData(List<WordModel> wordModels) {
        wordsHash.clear();
        keys.clear();
        for (WordModel wordModel : wordModels) {
            List<WordModel> list = wordsHash.get(wordModel.getGroup());
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(wordModel);
            wordsHash.put(wordModel.getGroup(), list);
            if (!keys.contains(wordModel.getGroup())) {
                keys.add(wordModel.getGroup());
            }
        }
        notifyDataSetChanged();
        for (WordsSubRecyclerAdapter wordsSubRecyclerAdapter : wordsSubRecyclerAdapters) {
            wordsSubRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.words_view, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.title.setText(keys.get(position));
        if (wordsSubRecyclerAdapters.size() < position + 1) {
            ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
            int count = projectDocuUtilities.getColumnSpam(context);
            if (count > 1) {
                holder.recyclerView.setLayoutManager(new GridLayoutManager(context, count));
            } else {
                holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            holder.recyclerView.setAdapter(new WordsSubRecyclerAdapter(context, photoId, wordsHash.get(keys.get(position)), listener));
            holder.recyclerView.setHasFixedSize(true);
        } else {
            wordsSubRecyclerAdapters.get(position).notifyDataSetChanged();
        }
        holder.dropdown.setOnClickListener(view -> {
            Log.d("Click", "dropdown");
            if (holder.recyclerView.getVisibility() == View.VISIBLE) {
                holder.recyclerView.setVisibility(View.GONE);
            } else {
                holder.recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordsHash.size();
    }


    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView title;
        AppCompatImageView dropdown;
        RecyclerView recyclerView;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            title = itemView.findViewById(R.id.key_title);
            recyclerView = itemView.findViewById(R.id.sublist);
            dropdown = itemView.findViewById(R.id.dropdown_arrow);
        }
    }

}
