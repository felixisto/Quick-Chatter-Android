package com.office.quickchatter.filesystem.model;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.filesystem.repository.Repository;
import com.office.quickchatter.utilities.DataSize;

// Immutable model of a directory.
// Thread safe: yes (immutable)
public class DirectoryInfo implements EntityInfo.Directory {
    final @NonNull DirectoryPath path;
    final @NonNull DataSize size;
    final @NonNull DirectoryContents contents;
    final boolean isRoot;

    public DirectoryInfo(@NonNull DirectoryPath path, @NonNull DataSize size, @NonNull DirectoryContents contents) {
        this(path, size, contents, false);
    }

    public DirectoryInfo(@NonNull DirectoryPath path, @NonNull DataSize size, @NonNull DirectoryContents contents, boolean isRoot) {
        this.path = path;
        this.size = size;
        this.contents = contents;
        this.isRoot = isRoot;
    }

    public DirectoryInfo(@NonNull Repository.Directory prototype) {
        this(new DirectoryPath(prototype.getDirPath()), prototype.getSize(), prototype.getContents(), prototype.isRoot());
    }

    public DirectoryInfo(@NonNull EntityInfo.Directory info) {
        this(new DirectoryPath(info.getPath()), info.getSize(), info.getContents(), info.isRoot());
    }

    // # EntityInfo.Directory

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public @NonNull Path getPath() {
        return getDirPath();
    }

    @Override
    public @NonNull DirectoryPath getDirPath() {
        return path;
    }

    @Override
    public @NonNull DataSize getSize() {
        return size;
    }

    @Override
    public @NonNull DirectoryContents getContents() {
        return contents;
    }
}
