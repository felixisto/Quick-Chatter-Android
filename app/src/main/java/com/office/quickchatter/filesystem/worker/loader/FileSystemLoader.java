package com.office.quickchatter.filesystem.worker.loader;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.FileSystemReader;
import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.filesystem.model.DirectoryContents;
import com.office.quickchatter.filesystem.model.EntityInfo;
import com.office.quickchatter.utilities.DataSize;

public interface FileSystemLoader {
    @NonNull FileSystemReader getReader();

    @NonNull EntityInfo.Directory readRootDirectoryInfo() throws Exception;

    @NonNull DirectoryContents readDirectoryContents(final @NonNull DirectoryPath path) throws Exception;

    @NonNull EntityInfo readEntityInfo(final @NonNull Path path) throws Exception;
    @NonNull EntityInfo.File readFileInfo(final @NonNull FilePath path) throws Exception;
    @NonNull EntityInfo.Directory readDirectoryInfo(final @NonNull DirectoryPath path) throws Exception;

    @NonNull DataSize readDirectorySize(@NonNull DirectoryPath path) throws Exception;

    @NonNull DataSize computeDirectorySize(@NonNull DirectoryContents contents);
}
