package com.office.quickchatter.filesystem.worker.loader;

import androidx.annotation.NonNull;
import com.office.quickchatter.filesystem.FileSystemReader;
import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FileExtension;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.filesystem.model.DirectoryContents;
import com.office.quickchatter.filesystem.model.DirectoryInfo;
import com.office.quickchatter.filesystem.model.EntityInfo;
import com.office.quickchatter.filesystem.model.FileInfo;
import com.office.quickchatter.utilities.DataSize;
import com.office.quickchatter.utilities.Errors;

import java.util.ArrayList;
import java.util.List;

// A file system loader for the local device disk.
public class FileSystemLoaderLocal implements FileSystemLoader {
    private final @NonNull FileSystemReader _reader;

    public FileSystemLoaderLocal(@NonNull FileSystemReader reader) {
        this._reader = reader;
    }

    @Override
    public @NonNull FileSystemReader getReader() {
        return _reader;
    }

    @Override
    public @NonNull EntityInfo.Directory readRootDirectoryInfo() throws Exception {
        return readDirectoryInfo(_reader.getRootDirectory());
    }

    @Override
    public @NonNull DirectoryContents readDirectoryContents(final @NonNull DirectoryPath path) throws Exception {
        List<Path> paths = _reader.contentsOfDirectory(path, new ArrayList<FileExtension>());
        ArrayList<EntityInfo> entities = new ArrayList<>();

        for (Path p : paths) {
            if (path.equals(p)) {
                Errors.throwIllegalStateError("Directory trying to load itself");
            }

            try {
                entities.add(readEntityInfo(p));
            } catch (Exception e) {

            }
        }

        return new DirectoryContents(entities);
    }

    @Override
    public @NonNull EntityInfo readEntityInfo(final @NonNull Path path) throws Exception {
        if (_reader.isEntityDirectory(path)) {
            return readDirectoryInfo(new DirectoryPath(path));
        }

        return readFileInfo(new FilePath(path));
    }

    @Override
    public @NonNull EntityInfo.File readFileInfo(final @NonNull FilePath path) throws Exception {
        DataSize size = DataSize.buildBytes(_reader.sizeOfFile(path));
        FileInfo info = new FileInfo(path, size);

        return info;
    }

    @Override
    public @NonNull EntityInfo.Directory readDirectoryInfo(final @NonNull DirectoryPath path) throws Exception {
        DirectoryContents contents = readDirectoryContents(path);
        boolean isRoot = path.equals(_reader.getRootDirectory());
        return new DirectoryInfo(path, computeDirectorySize(contents), contents, isRoot);
    }

    @Override
    public @NonNull DataSize readDirectorySize(@NonNull DirectoryPath path) throws Exception {
        DirectoryContents contents = readDirectoryContents(path);
        return computeDirectorySize(contents);
    }

    @Override
    public @NonNull DataSize computeDirectorySize(@NonNull DirectoryContents contents) {
        long totalSize = 0;

        for (EntityInfo info : contents.entitiesCopy()) {
            totalSize += info.getSize().inBytes();
        }

        return DataSize.buildBytes(totalSize);
    }
}
