package com.office.quickchatter.filesystem.simple;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.FileSystemReader;
import com.office.quickchatter.filesystem.FileSystemWriter;
import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FileExtension;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;

import java.io.File;
import java.net.URL;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class SimpleFileSystem implements FileSystemReader, FileSystemWriter {
    public @NonNull DirectoryPath getAppDirectory() {
        String value = Environment.getRootDirectory().getAbsolutePath();
        return new DirectoryPath(Uri.parse(value));
    }

    public @NonNull DirectoryPath getDataDirectory() {
        String value = Environment.getDataDirectory().getAbsolutePath();
        return new DirectoryPath(Uri.parse(value));
    }

    public @NonNull DirectoryPath getExternalStorageDirectory() {
        String value = Environment.getExternalStorageDirectory().getAbsolutePath();
        return new DirectoryPath(Uri.parse(value));
    }

    // # FileSystemReader

    @Override
    public @NonNull DirectoryPath getRootDirectory() {
        File root = Environment.getExternalStorageDirectory();
        return new DirectoryPath(Uri.parse(root.getPath()));
    }

    @Override
    public boolean isEntityDirectory(@NonNull Path path) {
        String pathAsString = path.getPath();

        if (pathAsString == null) {
            return false;
        }

        return new File(pathAsString).isDirectory();
    }

    @Override
    public boolean isEntityFile(@NonNull Path path) {
        return !isEntityDirectory(path);
    }

    @Override
    public @NonNull List<Path> contentsOfDirectory(@NonNull DirectoryPath path, @NonNull List<FileExtension> filterOut) throws Exception {
        String pathAsString = path.getPath();

        if (pathAsString == null) {
            return new ArrayList<>();
        }

        File directory = new File(pathAsString);
        File[] files = directory.listFiles();

        ArrayList<Path> paths =  new ArrayList<>();

        if (files == null) {
            return paths;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                paths.add(new DirectoryPath(Uri.parse(file.getPath())));
            } else {
                paths.add(new FilePath(Uri.parse(file.getPath())));
            }
        }

        return paths;
    }

    @Override
    public long sizeOfFile(@NonNull FilePath path) throws Exception {
        String pathAsString = path.getPath();

        if (pathAsString == null) {
            return 0;
        }

        return new File(pathAsString).length();
    }

    public @NonNull List<Path> contentsOfDirectory(@NonNull DirectoryPath path) throws Exception {
        return contentsOfDirectory(path, new ArrayList<FileExtension>());
    }

    @Override
    public @NonNull Buffer readFromFile(@NonNull FilePath path) throws Exception
    {
        return CharBuffer.allocate(100);
    }

    // # FileSystemWriter

    @Override
    public void writeToFile(@NonNull FilePath path, @NonNull Buffer data) throws Exception {

    }
}
