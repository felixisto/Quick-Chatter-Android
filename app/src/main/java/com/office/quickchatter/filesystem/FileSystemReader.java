package com.office.quickchatter.filesystem;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FileExtension;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import java.nio.Buffer;
import java.util.List;

public interface FileSystemReader {
    @NonNull DirectoryPath getRootDirectory() throws Exception;

    boolean isEntityDirectory(@NonNull Path path);
    boolean isEntityFile(@NonNull Path path);

    @NonNull List<Path> contentsOfDirectory(@NonNull DirectoryPath path, @NonNull List<FileExtension> filterOut) throws Exception;

    long sizeOfFile(@NonNull FilePath path) throws Exception;

    @NonNull Buffer readFromFile(@NonNull FilePath path) throws Exception;
}
