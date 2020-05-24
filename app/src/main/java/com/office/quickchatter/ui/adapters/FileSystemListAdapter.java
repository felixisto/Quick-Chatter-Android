package com.office.quickchatter.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;

import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;

public class FileSystemListAdapter extends BaseAdapter {
    private final @NonNull LayoutInflater inflater;
    private final @NonNull AdapterData<FileSystemEntityViewModel> data;

    public FileSystemListAdapter(@NonNull LayoutInflater inflater, @NonNull AdapterData<FileSystemEntityViewModel> data) {
        this.inflater = inflater;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.count();
    }

    @Override
    public Object getItem(int position) {
        return data.getValue(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileSystemEntityViewBuilder builder = new FileSystemEntityViewBuilder(inflater.getContext().getResources(), data.getValue(position));
        return builder.buildView(convertView, inflater, parent);
    }
}
