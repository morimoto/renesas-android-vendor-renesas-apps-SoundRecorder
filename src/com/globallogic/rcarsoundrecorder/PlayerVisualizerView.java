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

import static com.globallogic.rcarsoundrecorder.Constants.UPDATE_INTERVAL;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;

public class PlayerVisualizerView extends View {
    private static final String TAG = "PlayerVisualizerView";
    private static final int DELTA = 0;
    long currentTimestamp = UPDATE_INTERVAL;
    private float mLineScale;
    private HashMap<Long, Integer> mAmplitudes;
    private int mWidth;
    private int mHeight;
    private int mVisibleAmpsCount;
    private Paint mZeroPaint;
    private Paint mPastPaint;
    private Paint mFuturePaint;
    private MediaPlayer mMediaPlayer;

    public PlayerVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPastPaint = new Paint();
        mPastPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mPastPaint.setStrokeWidth(Constants.LINE_WIDTH);

        mFuturePaint = new Paint();
        mFuturePaint.setColor(getResources().getColor(R.color.colorPrimaryLight));
        mFuturePaint.setStrokeWidth(Constants.LINE_WIDTH);

        mZeroPaint = new Paint();
        mZeroPaint.setColor(getResources().getColor(R.color.colorAccent));
        mZeroPaint.setStrokeWidth(Constants.LINE_WIDTH * 2);
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mMediaPlayer = mediaPlayer;
    }

    public void setAmplitudes(HashMap<Long, Integer> list) {
        mAmplitudes = list;
    }

    public void setCurrentTimestamp(long currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        mLineScale = h / Constants.MAX_AMP;
        mVisibleAmpsCount = mWidth / Constants.LINE_WIDTH;
        mAmplitudes = new HashMap<>();
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            int mediaPlayerPosition = mMediaPlayer.getCurrentPosition();
            currentTimestamp = mediaPlayerPosition / UPDATE_INTERVAL * UPDATE_INTERVAL;
            if (currentTimestamp >= mMediaPlayer.getDuration()) {
                currentTimestamp = mMediaPlayer.getDuration() / UPDATE_INTERVAL * UPDATE_INTERVAL;
            }
        }
        if (mAmplitudes.size() > 0) {

            int middle = mHeight / 2;

            //draw future lines
            float curX = mWidth / 2; //starting from middle
            float scaledHeight = 0;
            long maxTimestamp = currentTimestamp + (mVisibleAmpsCount / 2
                    * UPDATE_INTERVAL); // not more than half width of the screen

            if (maxTimestamp > mAmplitudes.size() * UPDATE_INTERVAL - 1) {
                maxTimestamp = mAmplitudes.size() * UPDATE_INTERVAL;
            }
            for (long i = currentTimestamp + UPDATE_INTERVAL;
                    i < maxTimestamp; i += UPDATE_INTERVAL) {
                if (mAmplitudes.containsKey(i)) {
                    scaledHeight = mAmplitudes.get(i) * mLineScale + 2; // full length of the line
                }
                curX += Constants.LINE_WIDTH; // position of the line
                canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                        - scaledHeight / 2, mFuturePaint);
            }

            //draw past lines
            curX = mWidth / 2; //starting from middle
            scaledHeight = 0;

            long minTimestamp = currentTimestamp - (mVisibleAmpsCount / 2
                    * UPDATE_INTERVAL); // not less than half width of the screen
            if (minTimestamp < 0) minTimestamp = 0;

            for (long i = currentTimestamp - UPDATE_INTERVAL;
                    i > minTimestamp; i -= UPDATE_INTERVAL) {
                if (mAmplitudes.containsKey(i)) {
                    scaledHeight = mAmplitudes.get(i) * mLineScale + 2;

                }
                curX -= Constants.LINE_WIDTH;
                canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                        - scaledHeight / 2, mPastPaint);
            }

            //draw zero line
            curX = mWidth / 2;

            scaledHeight = Constants.MAX_AMP * mLineScale + 2; // max available length

            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle
                    - scaledHeight / 2, mZeroPaint);

        }

        invalidate();
    }


}
