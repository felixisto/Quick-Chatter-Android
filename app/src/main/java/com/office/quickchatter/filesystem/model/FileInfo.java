package com.office.quickchatter.filesystem.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.utilities.DataSize;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

// Mutable information source of a file.
// Thread safe: yes
public class FileInfo implements EntityInfo.File {
    public @NonNull AtomicReference<FilePath> path = new AtomicReference<>();
    public @NonNull AtomicReference<DataSize> size = new AtomicReference<>();
    public @NonNull AtomicReference<Date> dateCreated = new AtomicReference<>();
    public @NonNull AtomicReference<Date> dateModified = new AtomicReference<>();

    public FileInfo(@NonNull FilePath path, @NonNull DataSize size) {
        this.path.set(path);
        this.size.set(size);
    }

    @Override
    public @NonNull Path getPath() {
        return path.get();
    }

    @Override
    public @NonNull FilePath getFilePath() {
        return path.get();
    }

    @Override
    public @NonNull DataSize getSize() {
        return size.get();
    }

    @Override
    public @Nullable Date getDateCreated() {
        return dateCreated.get();
    }

    @Override
    public @Nullable Date getDateModified() {
        return dateModified.get();
    }
}