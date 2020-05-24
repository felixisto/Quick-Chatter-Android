package com.office.quickchatter.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Callback;

public interface BasePresenter {
    interface Connect extends BasePresenter {
        void start(@NonNull BasePresenterDelegate.Connect delegate);

        void stop();

        boolean isScanning();

        void startScan();
        void stopScan();

        void pickItem(int index);
    }

    interface Reconnect extends BasePresenter {
        void start(@NonNull BasePresenterDelegate.Reconnect delegate);

        void pickItem(int index);
    }

    interface ConnectingServer extends BasePresenter {
        void start(@NonNull BasePresenterDelegate.ConnectingServer delegate);

        void stop();
    }

    interface ConnectingClient extends BasePresenter {
        void start(@NonNull BasePresenterDelegate.ConnectingClient delegate);

        void stop();
    }

    interface Chat extends BasePresenter {
        void start(@NonNull BasePresenterDelegate.Chat delegate);
        void stop();

        void sendMessage(@NonNull String message);

        boolean canSendFile();
        void sendFile(@NonNull Path path);
    }

    interface FileSystem extends BasePresenter {
        @NonNull DirectoryPath getDirectoryPath();

        void navigateBack();
    }

    interface FileSystemNavigation extends FileSystem {
        void start(@NonNull BasePresenterDelegate.FileSystemNavigation delegate);

        @NonNull DirectoryPath getRootDirectoryPath();
    }

    interface FileSystemDirectory extends FileSystem {
        void start(@NonNull BasePresenterDelegate.FileSystemDirectory delegate);

        @NonNull FileSystemEntityViewModel getCurrentDirectory();
        @Nullable FileSystemEntityViewModel getEntityInfoAt(int index);

        void navigateToEntityAt(int index);

        void scrollBy(double scrollValue);
    }

    // Pick file or directory.
    interface FilePicker extends FileSystem {
        void start(@NonNull BasePresenterDelegate.FilePicker filePickerDelegate,
                   @NonNull BasePresenterDelegate.FileSystemNavigation navigationDelegate,
                   @NonNull BasePresenterDelegate.FileSystemDirectory directoryDelegate);

        void closeWithoutPick();

        void pickFile(@NonNull FileSystemEntityViewModel entity);
        void pickDirectory(@NonNull FileSystemEntityViewModel entity);

        @NonNull FileSystemNavigation getNavigationSubpresenter();
        @NonNull FileSystemDirectory getSystemDirectorySubpresenter();
    }

    // Pick file or directory as a destination path.
    interface FileDestinationPicker extends FilePicker {
        @NonNull FileDestinationHandler getDestinationHandler();

        @NonNull FilePath buildPath(@NonNull Path base);

        boolean isPathAvailable(@NonNull FilePath path);

        @NonNull String getName();
        void setName(@NonNull String name) throws Exception;
    }

    // Handles file destination specific cases.
    interface FileDestinationHandler {
        void onFileOverwrite(@NonNull Callback<Boolean> handler);
        void onPickFileName(@NonNull Callback<String> handler, @NonNull String initialName);
    }
}
