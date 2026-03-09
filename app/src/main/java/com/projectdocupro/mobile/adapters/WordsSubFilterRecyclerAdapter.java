package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.WordActivity;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.interfaces.WordsSelectListener;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.utility.Utils;

import java.util.Date;
import java.util.List;

public class WordsSubFilterRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<WordModel> wordModels;
    private WordsSelectListener listener;
    final static int TYPE_OPEN_FIELD = 1;
    final static int TYPE_CHECK_BOX = 0;
//    private long  photoId;
     Context context;
    public WordsSubFilterRecyclerAdapter(Context mContext,  long photoId, List<WordModel> wordModels, WordsSelectListener listener) {
//        this.photoId    =   photoId;
        this.wordModels = wordModels;
        this.listener = listener;
        context=mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView;
        if (viewType == TYPE_OPEN_FIELD) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_edittext_row_item, parent, false);
            return new OpenFieldViewHolder(itemView);

        } else {

            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_filter_sublist_item_view, parent, false);
            return new ProjectViewHolder(itemView);
        }
    }


    private class updateAsyncTask extends AsyncTask<WordModel, Void, Void> {
        private WordDao mAsyncTaskDao;
        updateAsyncTask(WordDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WordModel... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
         WordModel wordModel = wordModels.get(position);
        if (getItemViewType(position) == TYPE_OPEN_FIELD) {

            ((OpenFieldViewHolder) holder).txtName.setText(wordModel.getName());
//            ((OpenFieldViewHolder) holder).et_search_number.setText(wordModel.getValue());
            ((OpenFieldViewHolder) holder).et_search_number.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {


                    Utils.showLogger("WordsSubFilterRecyclerAdapter>>onEditorAction");

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // do your stuff here
                        wordModel.setValue(((OpenFieldViewHolder) holder).et_search_number.getText()
                                .toString());
//                    wordModels.set(position,wordModel);
                        wordModel.setFavorite(true);
                        listener.onListItemClick(wordModel);
                    //    new updateAsyncTask(ProjectsDatabase.getDatabase(context).wordDao()).execute(wordModel);
                    }
                    return false;
                }
            });
            ((OpenFieldViewHolder) holder).et_search_number.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {


                }
            });

        } else {

            ((ProjectViewHolder) holder).subTitle.setText(wordModel.getName());

            if (wordModel.isFavorite()) {
                ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_selected_word);
            } else {
                ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_unselected_word);
            }

            if (wordModel.isClocked()) {
                ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
            } else {
                ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);
            }


            ((ProjectViewHolder) holder).selectedWord.setOnClickListener(v -> {
                WordActivity.isChanged = true;
                listener.onListItemClick(wordModel);

            });

            ((ProjectViewHolder) holder).clockWord.setOnClickListener(v -> {
                WordActivity.isChanged = true;
                if (wordModel.isClocked()) {
                    wordModel.setClocked(false);
                    wordModel.setUseCount(wordModel.getUseCount() - 1);
                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);
                } else {
                    wordModel.setClocked(true);
                    wordModel.setUseCount(wordModel.getUseCount() + 1);
                    wordModel.setLastUsed(new Date().getTime());
                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
                }
            });

            ((ProjectViewHolder) holder).favoriteIcon.setOnClickListener(v -> {
                if (wordModel.isFavorite()) {
                    wordModel.setFavorite(!wordModel.isFavorite());
                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_unselected_filter);
                } else {
                    wordModel.setFavorite(!wordModel.isFavorite());
                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_selected_word);
                }
            });

        }


    }


    @Override
    public int getItemCount() {
        return wordModels.size();
    }


    class OpenFieldViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        private TextView txtName;
        private EditText et_search_number;

        OpenFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            txtName = itemView.findViewById(R.id.sub_title);
            et_search_number = itemView.findViewById(R.id.et_search_number);
        }
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView selectedWord, clockWord, favoriteIcon;
        TextView subTitle;
        LinearLayout mainLayout;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            selectedWord = itemView.findViewById(R.id.selected_word);
            clockWord = itemView.findViewById(R.id.clock_word);
            favoriteIcon = itemView.findViewById(R.id.favorite_word_icon);
            subTitle = itemView.findViewById(R.id.sub_title);
            mainLayout = itemView.findViewById(R.id.main_layout);

        }
    }

    @Override
    public int getItemViewType(int position) {

        if (wordModels.get(position).getType() != null && wordModels.get(position).getType().equals("1"))
            return TYPE_OPEN_FIELD;
        else
            return TYPE_CHECK_BOX;
    }
}
