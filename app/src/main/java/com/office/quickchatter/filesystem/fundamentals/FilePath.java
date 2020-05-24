package com.office.quickchatter.filesystem.fundamentals;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class FilePath implements Path {
    private final @NonNull Uri _url;

    public FilePath(@NonNull Uri url) {
        this._url = url;
    }

    public FilePath(@NonNull Path path) {
        this._url = path.getURL();
    }

    public FilePath(@NonNull Path base, @NonNull String name) {
        _url = Uri.withAppendedPath(base.getURL(), name);
    }

    @Override
    public @NonNull String toString() {
        return getURL().toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Path) {
            return ((Path)other).getURL().equals(_url);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this._url.hashCode();
        return hash;
    }

    @Override
    public @NonNull Uri getURL() {
        return _url;
    }

    @Override
    public @NonNull String getPath() {
        return _url.getPath();
    }

    @Override
    public @NonNull String getLastComponent() {
        String last = Uri.parse(_url.getPath()).getLastPathSegment();
        return last != null ? last : "";
    }
}
