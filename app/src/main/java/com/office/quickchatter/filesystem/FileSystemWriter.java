package com.office.quickchatter.filesystem;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.FilePath;

import java.nio.Buffer;

public interface FileSystemWriter {
    void writeToFile(@NonNull FilePath path, @NonNull Buffer data) throws Exception;
}
