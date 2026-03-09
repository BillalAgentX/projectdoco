package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.interfaces.WordsSelectListener;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.localFilters.ImageId_VS_Input;
import com.projectdocupro.mobile.models.localFilters.WordContentModel;
import com.projectdocupro.mobile.utility.Utils;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RecentUsedWordsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<WordModel> wordModels;
    private WordsSelectListener listener;
    public long photoIds;
    final static int TYPE_OPEN_FIELD = 1;
    final static int TYPE_CHECK_BOX = 0;

    public long getPhotoId() {
        return photoIds;
    }

    public void setPhotoId(long photoId) {
        this.photoIds = photoId;
    }

    Context mContext;

    public RecentUsedWordsRecyclerAdapter(long photoId, List<WordModel> wordModels, WordsSelectListener listener) {
        this.photoIds = photoId;
        this.wordModels = wordModels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View itemView;
        if (viewType == TYPE_OPEN_FIELD) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_edittext_row_item_fav_photo_screen, parent, false);
            return new OpenFieldViewHolder(itemView);

        } else {

            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_word_sublist_item, parent, false);
            return new ProjectViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Utils.showLogger("RecentlyUsedWordsRecyclerAdapter>>"+photoIds);

        WordModel wordModel = wordModels.get(position);


        if (getItemViewType(position) == TYPE_OPEN_FIELD) {
            if (wordModel.getPhotoIds() == null)
                wordModel.setPhotoIds("");

            if (wordModel.getOpen_field_content() == null)
                wordModel.setOpen_field_content("");
            ((OpenFieldViewHolder) holder).rl_mainView.setAlpha(0.7f);

            ((OpenFieldViewHolder) holder).txtName.setText(wordModel.getName());

            if (photoIds == 0)
                return;

            if (wordModel.getType() != null && wordModel.getType().equals("1")) {
                Utils.showLogger("wrong text");
                if (wordModel.getOpen_field_content() != null && wordModel.getOpen_field_content().contains(String.valueOf(photoIds))) {
//                            wordModel.setFavorite(true);
                    wordModel.setPhotoIds("," + photoIds + "");
                    List<String> items = new LinkedList<String>(Arrays.asList(wordModel.getOpen_field_content().split("\\s*,\\s*")));
                    if (items != null && items.size() > 0) {
                        for (int i = 0; i < items.size(); i++) {
                            if (items.get(i).contains(photoIds + "")) {

                                if (items.get(i).split("##").length > 1) {
                                    String strCon = items.get(i).split("##")[1];
//
                                    ((OpenFieldViewHolder) holder).et_search_number.setText(strCon);
                                    ((OpenFieldViewHolder) holder).et_search_number.setTextColor(mContext.getResources().getColor(R.color.white));
                                    ((OpenFieldViewHolder) holder).rl_mainView.setBackgroundColor(mContext.getResources().getColor(R.color.green_color));
                                    ((OpenFieldViewHolder) holder).txtName.setTextColor(mContext.getResources().getColor(R.color.white));
                                }
                                break;
                            } else {
                                ((OpenFieldViewHolder) holder).rl_mainView.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_bg));
                                ((OpenFieldViewHolder) holder).txtName.setTextColor(mContext.getResources().getColor(R.color.black));
                                ((OpenFieldViewHolder) holder).et_search_number.setTextColor(mContext.getResources().getColor(R.color.black));

                            }
                        }
                    }

                }
            }

            if (wordModel.isFavorite()) {
                ((OpenFieldViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_favorite);
            } else {
                ((OpenFieldViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
            }

            ((OpenFieldViewHolder) holder).favoriteIcon.setOnClickListener(v -> {
                if (wordModel.isFavorite()) {

                    if (wordModel.getPhotoIds().contains(String.valueOf(photoIds))) {
                        wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoIds, ""));
                        wordModel.setUseCount(wordModel.getUseCount() - 1);
                        listener.onListItemClick(wordModel);
//                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                    } else {
                        wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoIds);
                        wordModel.setUseCount(wordModel.getUseCount() + 1);
                        wordModel.setLastUsed(new Date().getTime());
                        listener.onListItemClick(wordModel);
//                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                    }
                    wordModel.setFavorite(!wordModel.isFavorite());
                    listener.onListItemClick(wordModel);
                    ((WordsSubRecyclerAdapter.OpenFieldViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_not_favorite);

                } else {

                    if (wordModel.getPhotoIds().contains(String.valueOf(photoIds))) {
                        wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoIds, ""));
                        wordModel.setUseCount(wordModel.getUseCount() - 1);
                        listener.onListItemClick(wordModel);
//                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                    } else {
                        wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoIds);
                        wordModel.setUseCount(wordModel.getUseCount() + 1);
                        wordModel.setLastUsed(new Date().getTime());
                        listener.onListItemClick(wordModel);
//                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                    }


                    wordModel.setFavorite(!wordModel.isFavorite());
                    listener.onListItemClick(wordModel);
                    ((WordsSubRecyclerAdapter.OpenFieldViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_favorite);
                }

                if (wordModel.getPhotoIds().contains(String.valueOf(photoIds))) {
                    wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoIds, ""));
                    wordModel.setUseCount(wordModel.getUseCount() - 1);
                    listener.onListItemClick(wordModel);
//                    ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                } else {
                    wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoIds);
                    wordModel.setUseCount(wordModel.getUseCount() + 1);
                    wordModel.setLastUsed(new Date().getTime());
                    listener.onListItemClick(wordModel);
//                    ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                }

            });
            ((OpenFieldViewHolder) holder).et_search_number.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        boolean isEmptyCase = false;
                        if (((OpenFieldViewHolder) holder).et_search_number.getText()
                                .toString().equals("")) {
                            isEmptyCase = true;
                            ((OpenFieldViewHolder) holder).rl_mainView.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_bg));
                            ((OpenFieldViewHolder) holder).txtName.setTextColor(mContext.getResources().getColor(R.color.black));
                            ((OpenFieldViewHolder) holder).et_search_number.setTextColor(mContext.getResources().getColor(R.color.black));

//                            return false;
                        }
                        // do your stuff here
                        wordModel.setValue(((OpenFieldViewHolder) holder).et_search_number.getText()
                                .toString());
//                    wordModels.set(position,wordModel);
//                        wordModel.setOpen_field_content(wordModel.getOpen_field_content() + "," + photoId + "##" + ((OpenFieldViewHolder) holder).et_search_number.getText()
//                                .toString());
                        if (wordModel.getOpen_field_content() != null && wordModel.getOpen_field_content().contains(String.valueOf(photoIds))) {
//                            wordModel.setFavorite(true);
                            wordModel.setPhotoIds("," + photoIds + "");
                            if (!isEmptyCase) {
                                if (!wordModel.getOpen_field_content().equals(""))
                                    wordModel.setOpen_field_content(wordModel.getOpen_field_content() + "," + photoIds + "##" + ((OpenFieldViewHolder) holder).et_search_number.getText()
                                            .toString());
                                else
                                    wordModel.setOpen_field_content(photoIds + "##" + ((OpenFieldViewHolder) holder).et_search_number.getText()
                                            .toString());
                            }
                            List<String> items = new LinkedList<String>(Arrays.asList(wordModel.getOpen_field_content().split("\\s*,\\s*")));
                            boolean isAlreadyFound = false;
                            if (items != null && items.size() > 0) {
                                for (int i = 0; i < items.size(); i++) {
                                    if (items.get(i).contains(photoIds + "") && !isAlreadyFound) {
                                        if (items.get(i).split("##").length > 1) {
                                            String strCon = items.get(i).split("##")[1];
                                            if (strCon != null) {
                                                if (isEmptyCase) {
                                                    items.remove(i);
                                                    ((OpenFieldViewHolder) holder).rl_mainView.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_bg));
                                                    ((OpenFieldViewHolder) holder).txtName.setTextColor(mContext.getResources().getColor(R.color.black));
                                                    ((OpenFieldViewHolder) holder).et_search_number.setTextColor(mContext.getResources().getColor(R.color.black));
                                                    wordModel.setUseCount(wordModel.getUseCount() - 1);
                                                    wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoIds, ""));


                                                    break;
                                                } else {
                                                    wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoIds);
                                                    strCon = +photoIds + "##" + ((OpenFieldViewHolder) holder).et_search_number.getText()
                                                            .toString();
                                                    items.set(i, strCon);
                                                    ((OpenFieldViewHolder) holder).et_search_number.setTextColor(mContext.getResources().getColor(R.color.white));
                                                    ((OpenFieldViewHolder) holder).rl_mainView.setBackgroundColor(mContext.getResources().getColor(R.color.green_color));
                                                    ((OpenFieldViewHolder) holder).txtName.setTextColor(mContext.getResources().getColor(R.color.white));
                                                    wordModel.setUseCount(wordModel.getUseCount() + 1);

                                                }
                                                isAlreadyFound = true;
                                            }
                                        } else {
                                            items.remove(i);
                                        }
                                        if (isAlreadyFound)
                                            items.remove(i);

                                    }
                                }

                                StringBuilder sb = new StringBuilder();
                                for (String s : items) {
                                    sb.append(s);
                                    sb.append(",");
                                }

                                String Something = sb.toString();
                                wordModel.setOpen_field_content(Something);
                            }

                            listener.onListItemClick(wordModel);
                            new updateAsyncTask(ProjectsDatabase.getDatabase(mContext).wordDao()).execute(wordModel);
                        } else {
//                            wordModel.setFavorite(false);
                            wordModel.setPhotoIds("," + photoIds + "");
                            if (!wordModel.getOpen_field_content().equals(""))
                                wordModel.setOpen_field_content(wordModel.getOpen_field_content() + "," + photoIds + "##" + ((OpenFieldViewHolder) holder).et_search_number.getText()
                                        .toString());
                            else
                                wordModel.setOpen_field_content(photoIds + "##" + ((OpenFieldViewHolder) holder).et_search_number.getText()
                                        .toString());

                            ((OpenFieldViewHolder) holder).et_search_number.setTextColor(mContext.getResources().getColor(R.color.white));
                            ((OpenFieldViewHolder) holder).rl_mainView.setBackgroundColor(mContext.getResources().getColor(R.color.green_color));
                            ((OpenFieldViewHolder) holder).txtName.setTextColor(mContext.getResources().getColor(R.color.white));
                            wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoIds);
                            listener.onListItemClick(wordModel);
                            wordModel.setUseCount(wordModel.getUseCount() + 1);
                            new updateAsyncTask(ProjectsDatabase.getDatabase(mContext).wordDao()).execute(wordModel);
                        }
                    }
                    return false;
                }
            });


        } else {






            if (wordModel.getType() != null && wordModel.getType().equals("1")) {
                Gson gson = new Gson();

                Utils.showLogger("rightCode");

                WordContentModel wordContentModel=null;

                try {
                     wordContentModel = gson.fromJson(wordModel.getOpen_field_content(), WordContentModel.class);

                }
                catch (Exception e){
                    e.printStackTrace();
                }

                if(wordContentModel!=null){

                    ImageId_VS_Input imgVSInput = wordContentModel.findByImageId(photoIds + "",wordModel.getName());
                    if(imgVSInput!=null&&!imgVSInput.getInputFields().isEmpty()) {
                        ((ProjectViewHolder) holder).cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.green_color));
                        ((ProjectViewHolder) holder).title.setTextColor(mContext.getResources().getColor(R.color.white));

                        ((ProjectViewHolder) holder).title.setText(wordModel.getName() + " ( " + imgVSInput.getInputFields() + " )");
                    }
                    else{
                        ((ProjectViewHolder) holder).cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.gray_light_bg));
                        ((ProjectViewHolder) holder).title.setTextColor(mContext.getResources().getColor(R.color.black));
                        ((ProjectViewHolder) holder).title.setText(wordModel.getName());
                        Utils.showLogger("word model search failed");
                    }
                }else
                {
                    Utils.showLogger("word model null");
                    ((ProjectViewHolder) holder).cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.gray_light_bg));
                    ((ProjectViewHolder) holder).title.setTextColor(mContext.getResources().getColor(R.color.black));
                    ((ProjectViewHolder) holder).title.setText(wordModel.getName());

                }



            } else {

                ((ProjectViewHolder) holder).title.setText(wordModel.getName());

//        if (wordModel.isClocked()){
//            holder.cardView.setCardBackgroundColor(Color.parseColor("#22d179"));
//        }else {
//            holder.cardView.setCardBackgroundColor(Color.parseColor("#c0c0c0"));
//        }
                ((ProjectViewHolder) holder).cardView.setAlpha(0.7f);

                if (wordModel.getPhotoIds() != null) {
                    if (wordModel.getPhotoIds().contains("," + photoIds)) {
                        ((ProjectViewHolder) holder).cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.green_color));
                        ((ProjectViewHolder) holder).title.setTextColor(mContext.getResources().getColor(R.color.white));
                    } else {
                        ((ProjectViewHolder) holder).cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.gray_light_bg));
                        ((ProjectViewHolder) holder).title.setTextColor(mContext.getResources().getColor(R.color.black));
                    }
                } else {
                    wordModel.setPhotoIds("");
                }
            }

            holder.itemView.setOnClickListener(v -> {


                Utils.showLogger("onItemViewClic>>>");

                if (wordModel.getType() != null && wordModel.getType().equals("1")) {
                    listener.onListItemClick(wordModel);
                } else {
                    if (wordModel.getPhotoIds().contains(String.valueOf(photoIds))) {
                        wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoIds, ""));
                        wordModel.setUseCount(wordModel.getUseCount() - 1);
                        listener.onListItemClick(wordModel);

                        ((ProjectViewHolder) holder).cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.gray_light_bg));
                        ((ProjectViewHolder) holder).title.setTextColor(mContext.getResources().getColor(R.color.black));
                    } else {
                        wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoIds);
                        wordModel.setUseCount(wordModel.getUseCount() + 1);
                        wordModel.setLastUsed(new Date().getTime());
                        listener.onListItemClick(wordModel);
                        ((ProjectViewHolder) holder).cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.green_color));
                        ((ProjectViewHolder) holder).title.setTextColor(mContext.getResources().getColor(R.color.white));
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return wordModels.size();
    }


    public class ProjectViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView title;
        CardView cardView;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;

            cardView = itemView.findViewById(R.id.recent_words_card);
            title = itemView.findViewById(R.id.recent_word_text);

        }
    }

    class OpenFieldViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        private TextView txtName;
        private EditText et_search_number;
        ImageView favoriteIcon;
        RelativeLayout rl_mainView;


        OpenFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            txtName = itemView.findViewById(R.id.sub_title);
            et_search_number = itemView.findViewById(R.id.et_search_number);
            favoriteIcon = itemView.findViewById(R.id.favorite_word_icon);
            rl_mainView = itemView.findViewById(R.id.main_layout);

        }
    }

    @Override
    public int getItemViewType(int position) {

        if (wordModels.get(position).getType() != null && wordModels.get(position).getType().equals("1"))
//            return TYPE_OPEN_FIELD;
            return TYPE_CHECK_BOX;
        else
            return TYPE_CHECK_BOX;
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


}
