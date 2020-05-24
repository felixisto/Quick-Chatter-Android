package com.office.quickchatter.filesystem.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.utilities.DataSize;

import java.util.Date;

// A directory or a file.
// Thread safe: yes
public interface EntityInfo {
    @NonNull Path getPath();
    @NonNull DataSize getSize();

    interface Directory extends EntityInfo {
        boolean isRoot();
        @NonNull DirectoryPath getDirPath();
        @NonNull DirectoryContents getContents();
    }
    interface File extends EntityInfo {
        @NonNull FilePath getFilePath();
        @Nullable Date getDateCreated();
        @Nullable Date getDateModified();
    }

    class Helper {
        public static boolean isDirectory(@NonNull EntityInfo info) {
            return info instanceof Directory;
        }

        public static boolean isFile(@NonNull EntityInfo info) {
            return info instanceof File;
        }
    }
}
