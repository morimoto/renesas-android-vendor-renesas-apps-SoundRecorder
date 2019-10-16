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
 * limitations under the License
 */

package com.globallogic.rcarsoundrecorder;

import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

class AudioFocusRequestHelper {
    private AudioManager mAudioManager;
    private AudioFocusRequest mFocusRequest;

    AudioFocusRequestHelper(AudioManager audioManager,
            AudioManager.OnAudioFocusChangeListener focusChangeListener) {
        mAudioManager = audioManager;
        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build();
    }

    boolean requestAudioFocus() {
        int result = mAudioManager.requestAudioFocus(mFocusRequest);
        return result == AUDIOFOCUS_REQUEST_GRANTED;
    }

    void abandonAudioFocus() {
        mAudioManager.abandonAudioFocusRequest(mFocusRequest);
    }

}
