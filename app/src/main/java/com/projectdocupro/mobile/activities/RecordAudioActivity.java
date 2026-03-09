package com.projectdocupro.mobile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.adapters.RecordingsRecyclerAdapter;
import com.projectdocupro.mobile.interfaces.AudioRecordingListItemClickListener;
import com.projectdocupro.mobile.models.RecordAudioModel;
import com.projectdocupro.mobile.viewModels.RecordAudioViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import static android.media.MediaRecorder.AudioSource.MIC;

public class RecordAudioActivity extends AppCompatActivity {

    private static final int RECORD_RQUEST_CODE = 243;
    
    MediaRecorder mediaRecorder;
    RecordAudioViewModel recordAudioViewModel;
    private RecordingsRecyclerAdapter recordingsRecyclerAdapter;
    CountDownTimer timeCounter;

    private Toolbar toolbar;

    private TextView currentTimer;

    private TextView date;

    private ImageView recordAudio;

    private ImageView iv_play;

    private RecyclerView recordingsRV;
    private boolean isRecorderRunning;
    final long countDownStartingMilliSec=93600000;
     long countDownRemainingMilliSec=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        bindView();
        recordAudioViewModel = ViewModelProviders.of(this).get(RecordAudioViewModel.class);
        recordAudioViewModel.setProjectId(getIntent().getStringExtra("projectId"));
        recordAudioViewModel.setPhotoId(getIntent().getLongExtra("photoId", 0L));
        recordAudioViewModel.InitRepo(getIntent().getLongExtra("photoId", 0L));

        Log.d("photoId", recordAudioViewModel.getPhotoId() + "");

        recordAudioViewModel.getRecordModel().observe(this, recordAudioModels -> {
            recordingsRecyclerAdapter = new RecordingsRecyclerAdapter(recordAudioModels, new AudioRecordingListItemClickListener() {
                @Override
                public void onListItemClick(RecordAudioModel recordAudioModel) {
                    recordAudioViewModel.getmRepository().deleteUsingRecordingId(recordAudioModel);
                    if (recordingsRecyclerAdapter != null)
                        recordingsRecyclerAdapter.notifyDataSetChanged();
                }
            });
            recordingsRV.setLayoutManager(new LinearLayoutManager(this));
            recordingsRV.setAdapter(recordingsRecyclerAdapter);
        });

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        date.setText(simpleDateFormat.format(new Date()));
        addEvent();


    }

    boolean isPauseState = false;
    private View mRecordAudio;

    private void addEvent() {

        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaRecorder != null) {
                    if (isRecorderRunning) {
                        if (!isPauseState) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                mediaRecorder.pause();
                            }
                            isPauseState = true;
                            pauseTimer();
                            iv_play.setImageResource(R.drawable.play_icon);
                        } else {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                mediaRecorder.resume();
                            }
                            startTimer(countDownRemainingMilliSec);
                            iv_play.setImageResource(R.drawable.pause_icon);
                            isPauseState = false;
                        }
                    }
                }


            }
        });

    }

    private void startRecordingClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("peprmission", "M");
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.d("peprmission", "not granted");
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_RQUEST_CODE);
                return;
            }
        }

        if (mediaRecorder != null) {
            stopRecording();
        } else {
            startRecording();
        }


    }


    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        if (mediaRecorder != null) {
            mediaRecorder.setAudioSource(MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            File downDiretory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File dir = new File(downDiretory.getPath() + "/projectDocu/audio_" + recordAudioViewModel.getProjectId());
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File audio = new File(dir, "/Audio_" + new Date().getTime() + ".3gp");

            recordAudioViewModel.setAudioPath(audio.getAbsolutePath());
            mediaRecorder.setOutputFile(audio.getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            mediaRecorder.start();

            startTimer(countDownStartingMilliSec);
            recordAudio.setImageResource(R.drawable.ic_stop_recording_audio);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                iv_play.setImageResource(R.drawable.pause_icon);
                iv_play.setVisibility(View.VISIBLE);
            }else{
                iv_play.setVisibility(View.INVISIBLE);
            }
            isRecorderRunning = true;
        }
    }

    private void stopRecording() {
        if(mediaRecorder != null) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);
            recordAudioViewModel.insert(new RecordAudioModel(recordAudioViewModel.getProjectId(), recordAudioViewModel.getPhotoId(), "", recordAudioViewModel.getAudioPath(), simpleDateFormat.format(new Date()), currentTimer.getText().toString(),new Date().getTime()));
            stopTimer();

            try {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;

            } catch (IllegalStateException e) {
                e.printStackTrace();
            }


            recordAudio.setImageResource(R.drawable.ic_record_audio);
            iv_play.setVisibility(View.GONE);
            isRecorderRunning = false;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (recordingsRecyclerAdapter != null)
            recordingsRecyclerAdapter.appInBackgroundState();
    }

    private void startTimer(long startCountDown) {

        timeCounter = new CountDownTimer( startCountDown, 1000) {
            public void onTick(long millisUntilFinished) {
                // recodeing code
                countDownRemainingMilliSec=millisUntilFinished;
                long sec = (93600000 - millisUntilFinished) / 1000;
                if (sec < 60) {
                    currentTimer.setText("00:00:" + String.format("%02d", sec));
                } else if (sec < 3600) {
                    int m = (int) (sec / 60);
                    int s = (int) (sec % 60);
                    currentTimer.setText("00:" + String.format("%02d", m) + ":" + String.format("%02d", s));
                } else {
                    int h = (int) (sec / 3600);
                    int m = (int) (sec % 3600);
                    int s = (int) (sec % 60);
                    currentTimer.setText(String.format("%02d", h) + ":" + String.format("%02d", m) + ":" + String.format("%02d", s));
                }
            }

            public void onFinish() {
                //finish action
            }
        };
        timeCounter.start();
    }

    private void stopTimer() {
        if (timeCounter != null) {
            timeCounter.cancel();
            currentTimer.setText("00:00:00");
        }
    }

    private void pauseTimer() {
        if (timeCounter != null) {
            timeCounter.cancel();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RECORD_RQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        

        if (mediaRecorder != null)
            mediaRecorder.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(SavePictureActivity.AUDIO_ATTACH_TO_PHOTO_KEY, recordAudioViewModel.isAdded());
        setResult(105, intent);
        super.onBackPressed();
    }

    private void bindView() {
        toolbar =      findViewById(R.id.toolbar);
        currentTimer =      findViewById(R.id.current_timer);
        date =      findViewById(R.id.record_date);
        recordAudio =      findViewById(R.id.record_audio);
        iv_play =      findViewById(R.id.iv_play);
        recordingsRV =      findViewById(R.id.recordings_rv);
        mRecordAudio =      findViewById(R.id.record_audio);
        mRecordAudio.setOnClickListener(v -> {
            startRecordingClick();
        });
    }
}
