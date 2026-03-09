package com.projectdocupro.mobile.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.projectdocupro.mobile.ProjectNavigator;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.interfaces.FinishCallback;
import com.projectdocupro.mobile.models.WordModel;
import com.projectdocupro.mobile.viewModels.AddWordViewModel;



public class AddWordActivity extends AppCompatActivity {

    AddWordViewModel    addWordViewModel;

    private Toolbar toolbar;

    private AppCompatSpinner    groupSpinner;

    private TextInputEditText   wordsEditText;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);
        bindView();
        setSupportActionBar(toolbar);
        addWordViewModel  =   ViewModelProviders.of(this).get(AddWordViewModel.class);
        addWordViewModel.InitRepo(getIntent().getStringExtra("projectId"),getIntent().getLongExtra("photoId",0L));

        addWordViewModel.getWordsList(getIntent().getStringExtra("projectId")).observe(this, list -> {
                if (list!=null) {
                    for (WordModel  wordModel:list){
                        if (!addWordViewModel.getKeysList().contains(wordModel.getGroup())){
                            addWordViewModel.getKeysList().add(wordModel.getGroup());
                        }
                    }
                    addWordViewModel.getKeysList().add(0,getString(R.string.select_group));
                    groupSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(),R.layout.text_view,addWordViewModel.getKeysList()));
                }
        });

        toolbar.setNavigationOnClickListener(v->onBackPressed());


        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int postion, long arg3) {
                // TODO Auto-generated method stub
                ((TextView) v).setTextColor(Color.parseColor("#000000"));
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                Log.d("item selected","nothing");
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save_action) {
            if (!wordsEditText.getText().toString().isEmpty())
                onSaveClick();
            else Toast.makeText(this, getString(R.string.word_name_empty), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void onSaveClick(){
        hideKeyboard();
        if (groupSpinner.getSelectedItemPosition()==0){
            Toast.makeText(this, getString(R.string.select_group), Toast.LENGTH_SHORT).show();
            return;
        }
        dialog= ProjectNavigator.showCustomProgress(this,"",false);

        addWordViewModel.saveWord(this, groupSpinner.getSelectedItem().toString(), wordsEditText.getText().toString(), new FinishCallback() {
            @Override
            public void onFinishSuccess() {
                if(dialog!=null)
                    dialog.dismiss();
                AddWordActivity.this.finish();
            }

            @Override
            public void onFinishFailure() {
                if(dialog!=null)
                    dialog.dismiss();
                Toast.makeText(AddWordActivity.this, getString(R.string.unable_create_tag), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideKeyboard();
        return true;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus()!=null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void bindView() {
        toolbar =     findViewById(R.id.toolbar);
        groupSpinner =     findViewById(R.id.groups_spinner);
        wordsEditText =     findViewById(R.id.word_edit_text);
    }
}
