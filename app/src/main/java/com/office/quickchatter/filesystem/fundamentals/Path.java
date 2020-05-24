package com.office.quickchatter.filesystem.fundamentals;

import android.net.Uri;

import androidx.annotation.NonNull;

public interface Path {
    @NonNull Uri getURL();
    @NonNull String getPath();
    @NonNull String getLastComponent();
}
