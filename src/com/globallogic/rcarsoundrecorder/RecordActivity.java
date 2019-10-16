/*
 * Copyright (C) 2019 GlobalLogic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globallogic.rcarsoundrecorder;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class RecordActivity extends AppCompatActivity implements
        AudioManager.OnAudioFocusChangeListener {
    public static final String KEY_OUTPUT_FILE = "key_out_file";
    private static final String TAG = "RecordActivity";
    private MediaRecorder mAudioRecorder;
    private String mOutputFilePath;
    private FloatingActionButton mFabRecord;
    private RecorderVisualizerView mVisualizerView;
    private Handler handler = new Handler();
    private Stopwatch mStopwatch;
    private TextView mTextCurrentTime;
    private FileSaver mFileSaver;
    private Runnable mStopwatchRunnable;
    private AudioFocusRequestHelper mAudioFocusRequestHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mFabRecord = findViewById(R.id.fabRecord);
        mVisualizerView = findViewById(R.id.visualizer);
        mTextCurrentTime = findViewById(R.id.textCurrentTime);
        mFabRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVisualizerView.isRecording()) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });
        mFileSaver = new FileSaver(this);
        mAudioFocusRequestHelper = new AudioFocusRequestHelper(
                (AudioManager) getSystemService(AUDIO_SERVICE), this);
        startRecording();
        initVisualizer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVisualizerView.isRecording()) {
            stopRecording();
        }
    }

    private void initVisualizer() {
        mVisualizerView.setAudioRecorder(mAudioRecorder);
        mVisualizerView.setStopwatch(mStopwatch);
    }

    private void startRecording() {
        if (mAudioFocusRequestHelper.requestAudioFocus()) {
            try {
                mOutputFilePath = mFileSaver.getOutputFilePath();
                mAudioRecorder = new MediaRecorder();
                mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                setRecordingFormat(mAudioRecorder);
                mAudioRecorder.setOutputFile(mOutputFilePath);
                mAudioRecorder.prepare();
                mAudioRecorder.start();
                initStopWatch();
                mVisualizerView.setRecording(true);
                mFabRecord.setImageResource(R.drawable.ic_stop);
            } catch (Exception ise) {
                Log.e(TAG, "error: " + ise.getMessage());
                ise.printStackTrace();
            }
        }
    }

    private void setRecordingFormat(MediaRecorder audioRecorder) {
        String format = PrefsHelper.getSavedAudioFormat(this);
        switch (format) {
            case PrefsHelper.AUDIO_FORMAT_AAC:
                audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                break;
            case PrefsHelper.AUDIO_FORMAT_AMR:
                audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
                audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                break;
            case PrefsHelper.AUDIO_FORMAT_OGG:
                audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
                audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS);
                break;
            case PrefsHelper.AUDIO_FORMAT_MP4:
                audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                break;
            case PrefsHelper.AUDIO_FORMAT_3GP:
            default:
                audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                break;
        }
    }

    private void stopRecording() {
        mAudioFocusRequestHelper.abandonAudioFocus();
        mAudioRecorder.stop();
        mAudioRecorder.release();
        mStopwatch.stop();
        handler.removeCallbacks(mStopwatchRunnable);
        mVisualizerView.setRecording(false);
        mAudioRecorder = null;
        mFabRecord.setImageResource(R.drawable.ic_mic);
        saveResult();
    }

    private void saveResult() {
        mFileSaver.writeVisualDataToFile(mOutputFilePath, mVisualizerView.getAmplitudes());
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_OUTPUT_FILE, mOutputFilePath);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


    private void initStopWatch() {
        mStopwatch = new Stopwatch();
        mStopwatch.start();
        mStopwatchRunnable = new Runnable() {
            @Override
            public void run() {
                mTextCurrentTime.setText(
                        Stopwatch.secondsToString(mStopwatch.getElapsedTimeSecs()));
                handler.postDelayed(this, Constants.STOPWATCH_DELAY);
            }
        };
        handler.post(mStopwatchRunnable);
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                stopRecording();
                break;
        }
    }
}
