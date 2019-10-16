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

class Stopwatch {
    private long mStartTime = 0;
    private boolean mRunning = false;

    static String secondsToString(long pTime) {
        String result = String.format("%02d:%02d", pTime / 60, pTime % 60);
        if (pTime / 3600 > 0) {
            result = (pTime / 3600) + ":" + result;
        }
        return result;
    }

    void start() {
        this.mStartTime = System.currentTimeMillis();
        this.mRunning = true;
    }

    void stop() {
        this.mRunning = false;
    }

    //elaspsed time in seconds
    long getElapsedTimeSecs() {
        long elapsed = 0;
        if (mRunning) {
            elapsed = ((System.currentTimeMillis() - mStartTime) / 1000);
        }
        return elapsed;
    }

    //elaspsed time in milliseconds
    long getElapsedTimeMilliSecs() {
        long elapsed = 0;
        if (mRunning) {
            elapsed = System.currentTimeMillis() - mStartTime;
        }
        return elapsed;
    }

    boolean isRunning() {
        return mRunning;
    }
}
