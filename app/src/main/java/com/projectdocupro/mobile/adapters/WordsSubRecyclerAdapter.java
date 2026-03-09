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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.activities.WordActivity;
import com.projectdocupro.mobile.dao.WordDao;
import com.projectdocupro.mobile.interfaces.WordsSelectListener;
import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.models.localFilters.ImageId_VS_Input;
import com.projectdocupro.mobile.models.localFilters.WordContentModel;
import com.projectdocupro.mobile.utility.ProjectDocuUtilities;
import com.projectdocupro.mobile.utility.Utils;

import java.util.Date;
import java.util.List;

public class WordsSubRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Gson gson = new Gson();
    boolean isColorEven = false;
    boolean isColorOdd = false;
    private List<WordModel> wordModels;
    private WordsSelectListener listener;
    final static int TYPE_OPEN_FIELD = 1;
    final static int TYPE_CHECK_BOX = 0;
    private long photoId;
    Context context;
    SharedPrefsManager sharedPrefsManager;

    public WordsSubRecyclerAdapter(Context mContext, long photoId, List<WordModel> wordModels, WordsSelectListener listener) {
        Utils.showLogger("WordsSubRecyclerAdapter");
        this.photoId = photoId;
        this.wordModels = wordModels;
        this.listener = listener;
        context = mContext;
        sharedPrefsManager = new SharedPrefsManager(context);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView;
        if (viewType == TYPE_OPEN_FIELD) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_edittext_row_item_fav, parent, false);
            return new OpenFieldViewHolder(itemView);

        } else {

            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_sublist_item, parent, false);
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

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 0, 10, 0);
            ((OpenFieldViewHolder) holder).mainLayout.setLayoutParams(params);

            ((OpenFieldViewHolder) holder).txtName.setText(wordModel.getName());
            if (wordModel.getPhotoIds() == null)
                wordModel.setPhotoIds("");

            if (wordModel.getOpen_field_content() == null)
                wordModel.setOpen_field_content("");

            if (wordModel.isClocked()) {
                ((OpenFieldViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
            } else {
                ((OpenFieldViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);
            }


            if (wordModel.getType() != null && wordModel.getType().equals("1")) {
                Utils.showLogger("subAdapterBillalCode");
                // if (wordModel.getOpen_field_content() != null && wordModel.getOpen_field_content().contains(String.valueOf(photoId))) {
//                            wordModel.setFavorite(true);
                // wordModel.setPhotoIds("," + photoId + "");

                Utils.showLogger("myOldContent>>" + wordModel.getOpen_field_content());


                WordContentModel wordContentModel =null;
                try {
                    wordContentModel = gson.fromJson(wordModel.getOpen_field_content(), WordContentModel.class);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                if(wordContentModel==null)
                    wordContentModel = new WordContentModel();
                ImageId_VS_Input lastSavedObj = wordContentModel.findByImageId(photoId + "", wordModel.getName());

                if (lastSavedObj != null)
                    ((OpenFieldViewHolder) holder).et_search_number.setText(lastSavedObj.getInputFields());
                else
                    Utils.showLogger("search failed");


            }

            if (wordModel.isFavorite()) {
                ((OpenFieldViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_favorite);
            } else {
                ((OpenFieldViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
            }

            ((OpenFieldViewHolder) holder).favoriteIcon.setOnClickListener(v -> {
                if (wordModel.isFavorite()) {

                    if (wordModel.getPhotoIds() != null && wordModel.getPhotoIds().contains(String.valueOf(photoId))) {
                        wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoId, ""));
                        wordModel.setUseCount(wordModel.getUseCount() - 1);
                        listener.onListItemClick(wordModel);
//                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                    } else {
                        wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                        wordModel.setUseCount(wordModel.getUseCount() + 1);
                        wordModel.setLastUsed(new Date().getTime());
                        listener.onListItemClick(wordModel);
//                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                    }
                    wordModel.setFavorite(!wordModel.isFavorite());
                    listener.onListItemClick(wordModel);
                    ((OpenFieldViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_not_favorite);

                } else {

                    if (wordModel.getPhotoIds().contains(String.valueOf(photoId))) {
                        wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoId, ""));
                        wordModel.setUseCount(wordModel.getUseCount() - 1);
                        listener.onListItemClick(wordModel);
//                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                    } else {
                        wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                        wordModel.setUseCount(wordModel.getUseCount() + 1);
                        wordModel.setLastUsed(new Date().getTime());
                        listener.onListItemClick(wordModel);
//                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                    }


                    wordModel.setFavorite(!wordModel.isFavorite());
                    listener.onListItemClick(wordModel);
                    ((OpenFieldViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_favorite);
                }

                if (wordModel.getPhotoIds().contains(String.valueOf(photoId))) {
                    wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoId, ""));
                    wordModel.setUseCount(wordModel.getUseCount() - 1);
                    listener.onListItemClick(wordModel);
//                    ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                } else {
                    wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                    wordModel.setUseCount(wordModel.getUseCount() + 1);
                    wordModel.setLastUsed(new Date().getTime());
                    listener.onListItemClick(wordModel);
//                    ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                }

            });
            ((OpenFieldViewHolder) holder).clockWord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    {
                        boolean isEmptyCase = false;
                        if (((OpenFieldViewHolder) holder).et_search_number.getText()
                                .toString().equals("")) {
                            isEmptyCase = true;
                            wordModel.setClocked(false);
                            ((OpenFieldViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);
                            wordModel.setValue("");
                            Toast.makeText(context, context.getResources().getString(R.string.enter_keyword_msg), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String currentText = ((OpenFieldViewHolder) holder).et_search_number.getText()
                                .toString();
                        if (wordModel.isClocked()) {
                            wordModel.setClocked(false);
                            ((OpenFieldViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);
                            sharedPrefsManager.setBooleanValue(AppConstantsManager.IS_OPEN_FIELD_KEYWORD_CLOCKED, false);
                            wordModel.setValue("");

                            wordModel.addOrUpdateInputField(photoId + "", currentText);
                           // listener.onListItemClick(wordModel);

                        } else {

                            ((OpenFieldViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
                            wordModel.setClocked(true);
                            sharedPrefsManager.setBooleanValue(AppConstantsManager.IS_OPEN_FIELD_KEYWORD_CLOCKED, true);




                            wordModel.setValue(currentText);

                            wordModel.addOrUpdateInputField(photoId + "", currentText);


                            wordModel.setUseCount(wordModel.getUseCount() + 1);
                            //wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                            //listener.onListItemClick(wordModel);



//                            if (!isEmptyCase) {
//                                wordModel.setClocked(true);
//                                ((OpenFieldViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
//                            }
                        }
                        new updateAsyncTask(ProjectsDatabase.getDatabase(context).wordDao()).execute(wordModel);


                    }

                }
            });
            ((OpenFieldViewHolder) holder).et_search_number.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        boolean isEmptyCase = false;
                        String newInputText = ((OpenFieldViewHolder) holder).et_search_number.getText().toString();

                        WordContentModel oldWordModel = null;
                        try {
                            oldWordModel = gson.fromJson(wordModel.getOpen_field_content(), WordContentModel.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (oldWordModel == null)
                            oldWordModel = new WordContentModel();
                        ImageId_VS_Input oldKeyPair = oldWordModel.findByImageId(photoId + "", wordModel.getName());

                        if (oldKeyPair != null) {
                            oldKeyPair.setInputFields(newInputText);
                        } else {
                            oldKeyPair = new ImageId_VS_Input(photoId + "", newInputText, wordModel.getName());
                            oldWordModel.getInputsList().add(oldKeyPair);
                            wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                        }
                        wordModel.setOpen_field_content(gson.toJson(oldWordModel));
                        wordModel.setUseCount(wordModel.getUseCount() + 1);


                        listener.onListItemClick(wordModel);
                        new updateAsyncTask(ProjectsDatabase.getDatabase(context).wordDao()).execute(wordModel);


                    }
                    return false;
                }
            });


        } else {

            ((ProjectViewHolder) holder).subTitle.setText(wordModel.getName());

//            if (wordModel.isFavorite()) {
//                ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_selected_word);
//            } else {
//                ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_unselected_word);
//            }
//
//            if (wordModel.isClocked()) {
//                ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
//            } else {
//                ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);
//            }


//            ((ProjectViewHolder) holder).selectedWord.setOnClickListener(v -> {
//                WordActivity.isChanged = true;
//                listener.onListItemClick(wordModel);
//
//            });

//            ((ProjectViewHolder) holder).clockWord.setOnClickListener(v -> {
//                WordActivity.isChanged = true;
//                if (wordModel.isClocked()) {
//                    wordModel.setClocked(false);
//                    wordModel.setUseCount(wordModel.getUseCount() - 1);
//                    listener.onListItemClick(wordModel);
//                    ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);
//                } else {
//                    wordModel.setClocked(true);
//                    wordModel.setUseCount(wordModel.getUseCount() + 1);
//                    wordModel.setLastUsed(new Date().getTime());
//                    listener.onListItemClick(wordModel);
//                    ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
//                }
//            });

//            ((ProjectViewHolder) holder).favoriteIcon.setOnClickListener(v -> {
//                if (wordModel.isFavorite()) {
//                    wordModel.setFavorite(!wordModel.isFavorite());
//                    listener.onListItemClick(wordModel);
//                    ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_unselected_filter);
//                } else {
//                    wordModel.setFavorite(!wordModel.isFavorite());
//                    listener.onListItemClick(wordModel);
//                    ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_selected_word);
//                }
//            });


            if (wordModel.isFavorite()) {
                ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_favorite);
            } else {
                ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
            }

            if (wordModel.isClocked()) {
                ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
            } else {
                ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);
            }

            if (wordModel.getPhotoIds() != null) {
                if (wordModel.getPhotoIds().contains("," + String.valueOf(photoId))) {
                    ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                } else {
                    ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                }
            } else {
                wordModel.setPhotoIds("");
            }
            int currentOrientation = context.getResources().getConfiguration().orientation;
            ProjectDocuUtilities projectDocuUtilities = new ProjectDocuUtilities();
            int count = projectDocuUtilities.getColumnSpam(context);
            if (count == 1) {
//                count = 2;
            }
//            boolean shownInTwoColumns = false;
//            int orientation = context.getResources().getConfiguration().orientation;
//            int screenSize = context.getResources().getConfiguration().screenLayout &
//                    Configuration.SCREENLAYOUT_SIZE_MASK;
//            if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE && orientation == Configuration.ORIENTATION_PORTRAIT){
//                shownInTwoColumns = true;
//            }
//
            if (position % count == 0) {

                if (count >= 2) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(10, 0, 10, 0);
                    ((ProjectViewHolder) holder).mainLayout.setLayoutParams(params);

                    if (isColorEven) {
                        ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.light_gray_bg));
                        isColorEven = false;
                    } else {
                        ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.gray_light_bg));
                        isColorEven = true;
                    }
                } else {
                    ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.light_gray_bg));

                }
//                ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(Color.parseColor("#efefef"));
            } else if (position % count == 1) {
                if (count >= 2) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(10, 0, 10, 0);
                    ((ProjectViewHolder) holder).mainLayout.setLayoutParams(params);

                    if (isColorOdd) {
                        ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.light_gray_bg));
                        isColorOdd = false;
                    } else {
                        ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.gray_light_bg));
                        isColorOdd = true;
                    }
                } else {
                    ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.gray_light_bg));

                }
            } else {
                if (count >= 2) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(10, 0, 10, 0);
                    ((ProjectViewHolder) holder).mainLayout.setLayoutParams(params);

                    if (!isColorOdd && !isColorEven) {
                        ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.light_gray_bg));
//                        isColorOdd = false;
//                        isColorEven = false;

                    } else {
                        ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.gray_light_bg));
//                        isColorOdd = true;
                    }
                } else {
                    ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(context.getResources().getColor(R.color.gray_light_bg));

                }
//                ((ProjectViewHolder) holder).mainLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            }

            ((ProjectViewHolder) holder).selectedWord.setOnClickListener(v -> {
                WordActivity.isChanged = true;
                if (wordModel.getPhotoIds().contains(String.valueOf(photoId))) {
                    wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoId, ""));
                    wordModel.setUseCount(wordModel.getUseCount() - 1);
                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                } else {
                    wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                    wordModel.setUseCount(wordModel.getUseCount() + 1);
                    wordModel.setLastUsed(new Date().getTime());
                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                }
            });

            ((ProjectViewHolder) holder).clockWord.setOnClickListener(v -> {
                WordActivity.isChanged = true;
                if (wordModel.isClocked()) {
                    wordModel.setClocked(false);
                    wordModel.setUseCount(wordModel.getUseCount() - 1);

                    if (wordModel.getPhotoIds().contains(String.valueOf(photoId))) {
                        wordModel.setPhotoIds(wordModel.getPhotoIds().replace("," + photoId, ""));
                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_unselected_word);
                    }


                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_grey);

                } else {
                    wordModel.setClocked(true);
                    wordModel.setUseCount(wordModel.getUseCount() + 1);
                    wordModel.setLastUsed(new Date().getTime());

                    {
                        wordModel.setPhotoIds(wordModel.getPhotoIds() + "," + photoId);
                        wordModel.setLastUsed(new Date().getTime());
                        ((ProjectViewHolder) holder).selectedWord.setBackgroundResource(R.drawable.ic_selected_word);
                    }

                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).clockWord.setImageResource(R.drawable.ic_clock_green);
                }
            });

            ((ProjectViewHolder) holder).favoriteIcon.setOnClickListener(v -> {
                if (wordModel.isFavorite()) {
                    wordModel.setFavorite(!wordModel.isFavorite());
                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_not_favorite);
                } else {
                    wordModel.setFavorite(!wordModel.isFavorite());
                    listener.onListItemClick(wordModel);
                    ((ProjectViewHolder) holder).favoriteIcon.setImageResource(R.drawable.ic_favorite);
                }
            });
//            new updateAsyncTask(ProjectsDatabase.getDatabase(context).wordDao()).execute(wordModel);

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
        ImageView favoriteIcon, clockWord;
        RelativeLayout mainLayout;


        OpenFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            txtName = itemView.findViewById(R.id.sub_title);
            et_search_number = itemView.findViewById(R.id.et_search_number);
            favoriteIcon = itemView.findViewById(R.id.favorite_word_icon);
            clockWord = itemView.findViewById(R.id.clock_word);
            mainLayout = itemView.findViewById(R.id.main_layout);

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

