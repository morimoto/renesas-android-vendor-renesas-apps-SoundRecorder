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

import static android.os.Environment.DIRECTORY_MUSIC;

import static com.globallogic.rcarsoundrecorder.Constants.UPDATE_INTERVAL;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListActivity extends AppCompatActivity implements
        MyListAdapter.OnSelectedItemChangedListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "ListActivity";
    private static final int MY_PERMISSIONS_REQUEST = 2523;
    private static final int RECORD_SOUND_REQUEST = 2433;
    private RecyclerView mRecyclerView;
    private MyListAdapter mAdapter;
    private FloatingActionButton mFabRecord, mFabPlay;
    private MediaPlayer mMediaPlayer;
    private SeekBar mSeekBar;
    private Runnable mSeekbarRunnable;
    private Handler mHandler;
    private TextView mTextPassedTime, mTextLeftTime;
    private File mCurrentFile;
    private PlayerVisualizerView mVisualizerView;
    private FileSaver mFileSaver;
    private AudioFocusRequestHelper mAudioFocusRequestHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mFabRecord = findViewById(R.id.fabRecord);
        mFabPlay = findViewById(R.id.fabPlay);
        mRecyclerView = findViewById(R.id.list);
        mSeekBar = findViewById(R.id.seekBar);
        mTextPassedTime = findViewById(R.id.textPassedTime);
        mTextLeftTime = findViewById(R.id.textLeftTime);
        mHandler = new Handler();
        mVisualizerView = findViewById(R.id.visualizer);
        mFileSaver = new FileSaver(this);
        mAudioFocusRequestHelper = new AudioFocusRequestHelper(
                (AudioManager) getSystemService(AUDIO_SERVICE), this);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        } else {
            initComponents();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initComponents() {
        initFab();
        initList();
        initMediaPlayer();
        initSeekBar();
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mFabPlay.setImageResource(android.R.drawable.ic_media_play);
                stopSeekBar();
                mVisualizerView.setCurrentTimestamp(
                        mMediaPlayer.getDuration() / UPDATE_INTERVAL * UPDATE_INTERVAL);
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                startSeekbar();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_SOUND_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                initList();
                if (data != null) {
                    String recordedFile = data.getStringExtra(RecordActivity.KEY_OUTPUT_FILE);
                    if (recordedFile != null) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onActivityResult: selectItem: " + recordedFile);
                        }
                        selectItem(new File(recordedFile));
                        initVisualizer(true, new File(recordedFile));
                    }
                }
                mSeekBar.setProgress(0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initComponents();
            } else {
                Toast.makeText(this, getString(R.string.no_permissions),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initFab() {
        mFabRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying()) {
                    pausePlayback();
                }
                Intent intent = new Intent(getApplicationContext(), RecordActivity.class);
                startActivityForResult(intent, RECORD_SOUND_REQUEST);
            }
        });

        mFabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying()) {
                    if (mAdapter.getSelectedItem() != null) {
                        playFile(mAdapter.getSelectedItem());
                    } else {
                        Toast.makeText(ListActivity.this, getString(R.string.error_no_items),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    pausePlayback();

                }
            }
        });
    }

    private boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    private void initList() {
        mAdapter = new MyListAdapter(getTracks(), this);
        mAdapter.setOnSelectedItemChangedListener(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


    private List<File> getTracks() {
        File dir = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            dir = getExternalFilesDir(DIRECTORY_MUSIC);
        }
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.isDirectory();
            }
        };
        if (dir != null && dir.listFiles(fileFilter) != null) {
            List<File> fileList = Arrays.asList(dir.listFiles(fileFilter));
            Collections.sort(fileList);
            return fileList;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void onSelectedItemChanged(File file) {
        selectItem(file);
        playFile(file);
    }

    private void playFile(File file) {
        if (mAudioFocusRequestHelper.requestAudioFocus()) {
            mFabPlay.setImageResource(android.R.drawable.ic_media_pause);
            stopSeekBar();
            if (mCurrentFile != null && mCurrentFile.getAbsolutePath().contains(
                    file.getAbsolutePath())) {
                if (mMediaPlayer.getDuration() == mMediaPlayer.getCurrentPosition()) {
                    initVisualizer(true, mCurrentFile);
                } else {
                    initVisualizer(false, mCurrentFile);
                }
                updateAudioStats();
                startSeekbar();
                mMediaPlayer.start();
            } else {
                mCurrentFile = file;
                updateAudioStats();
                initVisualizer(true, mCurrentFile);
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(file.getAbsolutePath());
                    mMediaPlayer.prepareAsync();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: error" + e.getMessage());
                }

            }
        } else {
            Toast.makeText(this, "Audio focus not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void pausePlayback() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        mFabPlay.setImageResource(android.R.drawable.ic_media_play);
        stopSeekBar();
        mAudioFocusRequestHelper.abandonAudioFocus();
    }

    private void stopSeekBar() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mSeekbarRunnable);
        }
        if (mMediaPlayer != null) {
            mSeekBar.setProgress(mMediaPlayer.getCurrentPosition() / Constants.SEEKBAR_STEP);
        }
    }


    private void selectItem(File recordingFile) {
        mRecyclerView.smoothScrollToPosition(
                mAdapter.setSelectedItem(recordingFile.getAbsolutePath()));
    }


    private void initVisualizer(boolean shouldRestart, File recordingFile) {
        mVisualizerView.setMediaPlayer(mMediaPlayer);
        mVisualizerView.setAmplitudes(mFileSaver.readVisualDataFromFile(recordingFile));
        if (shouldRestart) {
            mVisualizerView.setCurrentTimestamp(0);
        }
    }

    protected void initSeekBar() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(seekBar.getProgress() * Constants.SEEKBAR_STEP);
                }
            }
        });
    }

    private void startSeekbar() {
        if (mMediaPlayer != null) {
            mSeekBar.setMax(mMediaPlayer.getDuration() / Constants.SEEKBAR_STEP);
            mSeekbarRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mMediaPlayer != null) {
                        mSeekBar.setProgress(
                                mMediaPlayer.getCurrentPosition() / Constants.SEEKBAR_STEP);
                        updateAudioStats();
                    }
                    mHandler.postDelayed(mSeekbarRunnable, Constants.SEEKBAR_DELAY);
                }
            };
            mHandler.post(mSeekbarRunnable);
        }
    }

    protected void updateAudioStats() {
        int duration = mMediaPlayer.getDuration() / 1000; // In milliseconds
        int due = (mMediaPlayer.getDuration() - mMediaPlayer.getCurrentPosition()) / 1000;
        int pass = duration - due;
        mTextLeftTime.setText(Stopwatch.secondsToString(due));
        mTextPassedTime.setText(Stopwatch.secondsToString(pass));
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                pausePlayback();
                break;
        }
    }
}
