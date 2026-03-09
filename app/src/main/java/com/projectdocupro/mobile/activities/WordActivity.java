package com.projectdocupro.mobile.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.projectdocupro.mobile.ProjectsDatabase;
import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.WordsPagerAdapter;
import com.projectdocupro.mobile.fragments.AllWordsFragment;
import com.projectdocupro.mobile.fragments.FavoriteWordsFragment;



public class WordActivity extends AppCompatActivity implements FavoriteWordsFragment.OnFragmentInteractionListener, AllWordsFragment.OnFragmentInteractionListener {

    public static boolean isChanged=false;

    private WordsPagerAdapter wordsPagerAdapter;

    private ViewPager wordsViewPager;

    private TabLayout pagerTabStrip;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);

        bindView();
        setSupportActionBar(toolbar);

        wordsPagerAdapter =   new WordsPagerAdapter(this,getSupportFragmentManager(),getIntent().getStringExtra("projectId"),getIntent().getLongExtra("photoId",0));
        wordsViewPager.setAdapter(wordsPagerAdapter);

        pagerTabStrip.setupWithViewPager(wordsViewPager);
        new SelectTabAsyncTask(this).execute();
        toolbar.setNavigationOnClickListener(v  ->  onBackPressed());


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_project) {
            startActivity(new Intent(this,AddWordActivity.class).putExtra("projectId",getIntent().getStringExtra("projectId")));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void bindView() {
        wordsViewPager = findViewById(R.id.words_view_pager);
        pagerTabStrip = findViewById(R.id.words_tab_strip);
        toolbar = findViewById(R.id.toolbar);
    }

    private  class SelectTabAsyncTask extends AsyncTask<Void, Void, Integer> {
        private ProjectsDatabase database;

        SelectTabAsyncTask(Context context) {
            database = ProjectsDatabase.getDatabase(context);
        }

        @Override
        protected Integer doInBackground(final Void... params) {
            Integer favCount= database.wordDao().getFavoriteWordsCount();

            return favCount;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if(integer>0){
                wordsViewPager.setCurrentItem(0);
            }else {
                wordsViewPager.setCurrentItem(1);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        Intent  intent  =   new Intent();
        intent.putExtra("isChanged",isChanged);
        setResult(104,intent);
        super.onBackPressed();
    }

    @Override
    public void onAllWordsFragmentInteraction(Uri uri, boolean isChanged) {
    }

    @Override
    public void onFavoriteWordsFragmentInteraction(Uri uri, boolean isChanged) {
    }
}
