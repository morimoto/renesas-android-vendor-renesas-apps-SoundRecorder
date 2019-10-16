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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import java.io.File;
import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    private final Context mContext;
    private List<File> mItems;
    private int lastCheckedPosition = 0;
    private OnSelectedItemChangedListener onSelectedItemChangedListener;

    MyListAdapter(List<File> items, Context context) {
        this.mItems = items;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.list_item, viewGroup, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListAdapter.ViewHolder viewHolder, final int position) {
        File item = mItems.get(position);
        viewHolder.textView.setText(item.getName());
        viewHolder.textView.setChecked(lastCheckedPosition == position);

        viewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCheckedPosition = position;
                if (onSelectedItemChangedListener != null) {
                    onSelectedItemChangedListener.onSelectedItemChanged(mItems.get(position));
                }
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    void setOnSelectedItemChangedListener(
            OnSelectedItemChangedListener onSelectedItemChangedListener) {
        this.onSelectedItemChangedListener = onSelectedItemChangedListener;
    }

    int setSelectedItem(String recordedFile) {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getAbsolutePath().contains(recordedFile)) {
                lastCheckedPosition = i;
                notifyDataSetChanged();
                return i;
            }
        }
        return lastCheckedPosition;
    }

    File getSelectedItem() {
        if(mItems.size()==0) return null;
        return mItems.get(lastCheckedPosition);
    }

    public interface OnSelectedItemChangedListener {
        void onSelectedItemChanged(File file);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CheckedTextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = (CheckedTextView) itemView.findViewById(R.id.textView);
        }
    }
}