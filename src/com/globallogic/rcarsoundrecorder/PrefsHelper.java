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
import android.preference.PreferenceManager;

class PrefsHelper {
    static final String AUDIO_FORMAT_3GP = "3gp";
    static final String AUDIO_FORMAT_AAC = "aac";
    static final String AUDIO_FORMAT_MP4 = "mp4";
    static final String AUDIO_FORMAT_OGG = "ogg";
    static final String AUDIO_FORMAT_AMR = "amr";
    private static final String AUDIO_FORMAT_DEFAULT = "3gp";
    private static final String TAG = "PrefsHelper";
    private static final String PREF_AUDIO_FORMAT = "pref_audio_format";

    static String[] getAudioFormats(Context context) {
        return context.getResources().getStringArray(R.array.audio_formats);
    }

    static String getSavedAudioFormat(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_AUDIO_FORMAT, AUDIO_FORMAT_DEFAULT);
    }

    static void saveAudioFormat(Context context, String audioFormat) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREF_AUDIO_FORMAT, audioFormat)
                .apply();
    }

}
