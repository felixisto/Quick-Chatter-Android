package com.office.quickchatter.navigation;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;

public class FileSystemRouter implements Router.FileSystem {
    public final @NonNull DirectoryPath rootDirectory;

    public FileSystemRouter(@NonNull DirectoryPath rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public void navigateBack() {

    }
}
