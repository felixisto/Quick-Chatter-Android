package com.office.quickchatter.filesystem.repository;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.model.DirectoryContents;
import com.office.quickchatter.filesystem.model.DirectoryInfo;
import com.office.quickchatter.filesystem.model.EntityInfo;
import com.office.quickchatter.utilities.DataSize;

// Mutable model of a directory.
// Essentially wraps a EntityInfo.Directory and allows to modify it safely.
// Thread safe: yes (lock)
public class RepoDirectory implements Repository.MutableDirectory {
    private final @NonNull Object lock = new Object();

    private @NonNull EntityInfo.Directory _info;

    public RepoDirectory(@NonNull EntityInfo.Directory info) {
        _info = info;
    }

    public RepoDirectory(@NonNull Repository.MutableDirectory prototype) {
        this(getInfoFrom(prototype));
    }

    public @NonNull EntityInfo.Directory getInfo() {
        return _info;
    }

    // # Repository.MutableDirectory

    @Override
    public @NonNull Repository.MutableDirectory copy() {
        EntityInfo.Directory info;

        synchronized (lock) {
            info = this._info;
        }

        return new RepoDirectory(info);
    }

    @Override
    public boolean isRoot() {
        synchronized (lock) {
            return _info.isRoot();
        }
    }

    @Override
    public @NonNull DirectoryPath getDirPath() {
        synchronized (lock) {
            return _info.getDirPath();
        }
    }

    @Override
    public @NonNull String getAbsolutePath() {
        return getDirPath().getPath();
    }

    @Override
    public @NonNull String getName() {
        return getDirPath().getLastComponent();
    }

    @NonNull
    public @Override DataSize getSize() {
        synchronized (lock) {
            return _info.getSize();
        }
    }

    @Override
    public @NonNull DirectoryContents getContents() {
        synchronized (lock) {
            return _info.getContents();
        }
    }

    @Override
    public void assign(@NonNull Repository.Directory prototype) {
        DirectoryInfo info = getInfoFrom(prototype);

        synchronized (lock) {
            this._info = info;
        }
    }

    // # Utilities

    private static @NonNull DirectoryInfo getInfoFrom(@NonNull Repository.Directory prototype) {
        return new DirectoryInfo(prototype.getDirPath(), prototype.getSize(), prototype.getContents(), prototype.isRoot());
    }
}
