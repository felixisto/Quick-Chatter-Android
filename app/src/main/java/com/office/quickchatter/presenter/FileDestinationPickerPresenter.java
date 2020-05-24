package com.office.quickchatter.presenter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.filesystem.model.EntityInfo;
import com.office.quickchatter.filesystem.worker.loader.FileSystemLoader;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.Parser;
import com.office.quickchatter.utilities.SimpleCallback;

import java.io.File;

public class FileDestinationPickerPresenter extends FilePickerPresenter implements BasePresenter.FileDestinationPicker {
    private BasePresenterDelegate.FilePicker _pickerDelegate;
    private FileDestinationHandler _destinationHandler;
    private Parser<EntityInfo, FileSystemEntityViewModel> _parser;
    private String _name = "file";

    public FileDestinationPickerPresenter(@NonNull Context context,
                                          @NonNull FileSystemLoader loader,
                                          @NonNull EntityInfo.Directory rootInfo,
                                          @NonNull Parser<EntityInfo, FileSystemEntityViewModel> parser,
                                          @NonNull FileDestinationHandler destinationHandler) {
        super(context, loader, rootInfo, parser);

        _destinationHandler = destinationHandler;
        _parser = parser;
    }

    // # Presenter.FileDestinationPicker

    @Override
    public void start(@NonNull BasePresenterDelegate.FilePicker filePickerDelegate,
                      @NonNull BasePresenterDelegate.FileSystemNavigation navigationDelegate,
                      @NonNull BasePresenterDelegate.FileSystemDirectory directoryDelegate) {
        _pickerDelegate = filePickerDelegate;

        super.start(filePickerDelegate, navigationDelegate, directoryDelegate);
    }

    @Override
    public void pickFile(@NonNull FileSystemEntityViewModel entity) {
        Path path = pathOfEntityInfoViewModel(entity);

        if (!(path instanceof FilePath) || entity.isDirectory) {
            Logger.error(this, "Cannot pick file, given model is invalid or corrupted");
            return;
        }

        pickFileWithPath((FilePath)path);
    }

    @Override
    public void pickDirectory(@NonNull FileSystemEntityViewModel entity) {
        final Path path = pathOfEntityInfoViewModel(entity);

        if (path == null || entity.isFile) {
            Logger.error(this, "Cannot pick directory, given model is invalid or corrupted");
            return;
        }

        Logger.message(this, "Picked destination directory " + path.toString() + ", please enter file name");

        String initialName = getName();

        _destinationHandler.onPickFileName(new Callback<String>() {
            @Override
            public void perform(String argument) {
                try {
                    setName(argument);
                    FilePath destinationPath = buildPath(path);

                    Logger.message(this, "Picked destination " + destinationPath);

                    // Now pick file, if chosen name overwrites something, the overwrite dialog
                    // will be handled properly
                    pickFileWithPath(destinationPath);
                } catch (Exception e) {
                    Logger.warning(this, "Invalid file name, cannot pick destination, error: " + e);
                }
            }
        }, initialName);
    }

    @Override
    public @NonNull FileDestinationHandler getDestinationHandler() {
        return _destinationHandler;
    }

    @Override
    public @NonNull FilePath buildPath(@NonNull Path base) {
        return new FilePath(base, getName());
    }

    @Override
    public boolean isPathAvailable(@NonNull FilePath path) {
        String pathString = path.getPath();

        if (pathString.isEmpty()) {
           return false;
        }

        File file = new File(pathString);
        return !file.exists();
    }

    @Override
    public @NonNull String getName() {
        return _name;
    }

    @Override
    public void setName(@NonNull String name) throws Exception {
        if (name.isEmpty()) {
            Errors.throwInvalidArgument("Name cannot be empty");
        }

        _name = name;
    }

    // # Internals

    private void pickFileWithPath(@NonNull final FilePath path) {
        if (isPathAvailable(path)) {
            chooseFile(path);
            return;
        }

        Logger.message(this, "Picked file " + path.toString() + " already exists, overwrite?");

        _destinationHandler.onFileOverwrite(new Callback<Boolean>() {
            @Override
            public void perform(Boolean confirm) {
                if (confirm) {
                    chooseFile(path);
                } else {
                    Logger.message(this, "Cancel overwrite file");
                }
            }
        });
    }

    private void chooseFile(final @NonNull FilePath path) {
        Logger.message(this, "Pick file " + path.toString());

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_pickerDelegate == null) {
                    return;
                }

                try {
                    _pickerDelegate.onPickFile(path);
                } catch (Exception e) {

                }
            }
        });
    }
}
