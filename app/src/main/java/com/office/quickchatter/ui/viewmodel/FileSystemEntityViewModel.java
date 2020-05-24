package com.office.quickchatter.ui.viewmodel;

import androidx.annotation.NonNull;

public class FileSystemEntityViewModel {
    public final boolean isDirectory;
    public final boolean isFile;
    public final @NonNull String name;
    public final @NonNull String size;
    public final @NonNull String dateCreated;
    public final @NonNull String dateModified;

    public FileSystemEntityViewModel(boolean isDirectory,
                                     @NonNull String name,
                                     @NonNull String size,
                                     @NonNull String dateCreated,
                                     @NonNull String dateModified) {
        this.isDirectory = isDirectory;
        this.isFile = !this.isDirectory;
        this.name = name;
        this.size = size;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof FileSystemEntityViewModel) {
            FileSystemEntityViewModel otherModel = (FileSystemEntityViewModel)object;

            if (isDirectory != otherModel.isDirectory || isFile != otherModel.isFile || !name.equals(otherModel.name)) {
                return false;
            }

            if (!size.equals(otherModel.size) || !dateCreated.equals(otherModel.dateCreated) || !dateModified.equals(otherModel.dateModified)) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.isDirectory ? 1 : 0);
        hash = 23 * hash + (this.isFile ? 1 : 0);
        hash = 23 * hash + this.name.hashCode();
        hash = 23 * hash + this.size.hashCode();
        hash = 23 * hash + this.dateCreated.hashCode();
        hash = 23 * hash + this.dateModified.hashCode();
        return hash;
    }
}
