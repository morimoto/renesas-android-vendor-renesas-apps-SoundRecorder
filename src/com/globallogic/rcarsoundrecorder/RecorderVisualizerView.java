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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;

public class RecorderVisualizerView extends View {
    private static final String TAG = "RecorderVisualizerView";
    private float mLineScale = 50;
    private HashMap<Long, Integer> mAmplitudes;
    private int mHeight;
    private Paint mPaint;
    private MediaRecorder mAudioRecorder;
    private boolean mIsRecording = false;
    private Stopwatch mStopwatch;
    private long mStartKey = 0;
    private int mVisibleAmpsCount;

    // constructor
    public RecorderVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(Constants.LINE_WIDTH);
    }

    public void setStopwatch(Stopwatch stopwatch) {
        this.mStopwatch = stopwatch;
    }

    public void setAudioRecorder(MediaRecorder audioRecorder) {
        this.mAudioRecorder = audioRecorder;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void setRecording(boolean recording) {
        mIsRecording = recording;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mHeight = h;
        mLineScale = h / Constants.MAX_AMP;
        mAmplitudes = new HashMap<>();
        mVisibleAmpsCount = w / Constants.LINE_WIDTH;
    }

    public void addAmplitude(int amplitude) {
        if (mStopwatch.isRunning()) {
            long timestamp = mStopwatch.getElapsedTimeMilliSecs() / Constants.UPDATE_INTERVAL
                    * Constants.UPDATE_INTERVAL;
            mAmplitudes.put(timestamp, amplitude);
            if (mAmplitudes.size() >= mVisibleAmpsCount) {
                mStartKey = (mAmplitudes.size() - mVisibleAmpsCount) * Constants.UPDATE_INTERVAL;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mIsRecording) {
            addAmplitude(mAudioRecorder.getMaxAmplitude());

            int middle = mHeight / 2;
            float curX = 0;
            float scaledHeight = 0;

            for (long i = mStartKey; i < mAmplitudes.size() * Constants.UPDATE_INTERVAL;
                    i += Constants.UPDATE_INTERVAL) {
                if (mAmplitudes.containsKey(i)) {
                    scaledHeight = mAmplitudes.get(i) * mLineScale + 2;
                }
                curX += Constants.LINE_WIDTH;

                canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                        - scaledHeight / 2, mPaint);
            }
            invalidate();
        }
    }

    public HashMap<Long, Integer> getAmplitudes() {
        return mAmplitudes;
    }

}
