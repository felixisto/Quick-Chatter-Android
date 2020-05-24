package com.office.quickchatter.filesystem.fundamentals;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.StringUtilities;

import java.net.URL;

public class DirectoryPath implements Path {
    private final @NonNull Uri _url;

    public DirectoryPath(@NonNull Uri url) {
        this._url = url;
    }

    public DirectoryPath(@NonNull Path path) {
        this._url = path.getURL();
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
        String last = _url.getLastPathSegment();
        return last != null ? last : "";
    }

    public @NonNull DirectoryPath pathWithoutLastComponent() {
        String lastComponent = _url.getLastPathSegment();

        if (lastComponent == null || lastComponent.isEmpty()) {
            return this;
        }

        String path = _url.getPath();

        if (path == null || path.isEmpty()) {
            return this;
        }

        path = StringUtilities.replaceLast(path, lastComponent, "");

        return new DirectoryPath(Uri.parse(path));
    }
}
