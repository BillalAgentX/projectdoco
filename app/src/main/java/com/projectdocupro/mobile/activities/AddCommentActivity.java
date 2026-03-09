package com.projectdocupro.mobile.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.utility.Utils;



public class AddCommentActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private EditText    commentsBox;

    private TextView tv_cancel;

    private TextView tv_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        bindView();
        commentsBox.setText(getIntent().getStringExtra("comments"));
        commentsBox.setSelection(commentsBox.getText().length());

        setSupportActionBar(toolbar);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                commentsBox.requestFocus();
            }
        },50);


        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_action_menu, menu);
        menu.getItem(0).setVisible(false);
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
            onSaveClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void onSaveClick(){

        Intent  intent  =   new Intent();
        intent.putExtra("comments",commentsBox.getText().toString());
        setResult(101,intent);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Utils.hideSoftKeyboard(AddCommentActivity.this);
        super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus()!=null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void bindView() {
        toolbar =    findViewById(R.id.toolbar);
        commentsBox = findViewById(R.id.comments);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_save = findViewById(R.id.tv_save);
    }
}

