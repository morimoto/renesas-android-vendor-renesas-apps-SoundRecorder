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

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class FileSaver {
    private static final String TAG = "FileSaver";
    private static final String VISUAL_DATA_DIR = "visual_data";
    private Context context;

    FileSaver(Context context) {
        this.context = context;
    }

    private void writeVisualDataToFile(File recordingFile, HashMap<Long, Integer> data) {
        File dataFile = matchRecordingToDataFile(recordingFile);
        try {
            dataFile.createNewFile();
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter(dataFile));
            for (Long key : data.keySet()) {
                outputWriter.write(key + " " + data.get(key));
                outputWriter.newLine();
            }
            outputWriter.flush();
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "writeVisualDataToFile: ERROR ", e);
        }
    }

    void writeVisualDataToFile(String recordingFilePath, HashMap<Long, Integer> data) {
        writeVisualDataToFile(new File(recordingFilePath), data);
    }

    HashMap<Long, Integer> readVisualDataFromFile(File recordingFile) {
        HashMap<Long, Integer> data = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(matchRecordingToDataFile(recordingFile)));
            String line = reader.readLine();
            while (line != null) {
                data.put(parseTimestampFromLine(line), parseValueFromLine(line));
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "readVisualDataFromFile: ERROR", e);
        }
        return data;
    }

    private Long parseTimestampFromLine(String line) {
        return Long.parseLong(line.substring(0, line.indexOf(" ")));
    }

    private Integer parseValueFromLine(String line) {
        return Integer.parseInt(line.substring(line.indexOf(" ") + 1));
    }

    private File matchRecordingToDataFile(File recordingFile) {
        String recordingFileName = recordingFile.getName();
        String dataFileName = recordingFileName.substring(0, recordingFileName.lastIndexOf("."))
                .replace(context.getString(R.string.recording_filename_part),
                        context.getString(R.string.data_filename_part));
        return new File(getVisualDataDir(), dataFileName);
    }

    private File getVisualDataDir() {
        File visualDataDir = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            visualDataDir = new File(context.getExternalFilesDir(DIRECTORY_MUSIC), VISUAL_DATA_DIR);
        }
        if (!visualDataDir.exists()) {
            visualDataDir.mkdirs();
        }
        return visualDataDir;
    }


    String getOutputFilePath() {
        String format = PrefsHelper.getSavedAudioFormat(context);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        File file = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            file = new File(context.getExternalFilesDir(DIRECTORY_MUSIC).getAbsolutePath()
                    + "/recording" + currentDateTime + "." + format);
        }

        return file.getAbsolutePath();
    }
}
