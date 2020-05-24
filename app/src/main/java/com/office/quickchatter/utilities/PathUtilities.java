package com.office.quickchatter.utilities;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.office.quickchatter.BuildConfig;

import java.io.File;
import java.net.URL;

public class PathUtilities {
    public static @NonNull URL filePathWithoutLastComponent(@NonNull URL path) {
        // Uri always use / as separators
        Uri uri = Uri.parse(path.getPath());
        String last = uri.getLastPathSegment();

        if (last == null || last.isEmpty()) {
            return path;
        }

        File parent = new File(path.getPath()).getParentFile();

        if (parent == null) {
            return path;
        }

        try {
            return parent.toURI().toURL();
        } catch (Exception e) {
            return path;
        }
    }

    public static @NonNull Uri safeAndroidURLPath(@NonNull Context context, @NonNull URL url) {
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(url.getPath()));
    }
}
