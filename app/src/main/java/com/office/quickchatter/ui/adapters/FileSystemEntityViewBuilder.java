package com.office.quickchatter.ui.adapters;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.office.quickchatter.R;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;

public class FileSystemEntityViewBuilder {
    public final @NonNull Resources resources;
    public final @NonNull FileSystemEntityViewModel model;

    public FileSystemEntityViewBuilder(@NonNull Resources resources, @NonNull FileSystemEntityViewModel model) {
        this.resources = resources;
        this.model = model;
    }

    public @NonNull View buildView(View convertView, @NonNull LayoutInflater inflater, ViewGroup parent) {
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.item_filesystementity, parent, false);
        }

        ImageView thumbnail = convertView.findViewById(R.id.thumbnail);

        if (model.isDirectory) {
            thumbnail.setImageDrawable(resources.getDrawable(R.drawable.ic_folder_standard));
        } else {
            thumbnail.setImageDrawable(resources.getDrawable(R.drawable.ic_file_unknown));
        }

        TextView titleView = convertView.findViewById(R.id.title);
        titleView.setText(model.name);

        TextView descriptionView = convertView.findViewById(R.id.description);
        descriptionView.setText(model.size);

        return convertView;
    }
}
