package com.office.quickchatter.presenter;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BETransmitter;
import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.ui.adapters.AdapterData;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.SimpleCallback;

import java.util.List;

public interface BasePresenterDelegate {
    interface BEPresenterDelegate {
        void displayBluetoothIsOffAlert();
    }

    interface GenericConnect extends BEPresenterDelegate {
        void updateClientsListData(@NonNull AdapterData<BEClient> data);

        void navigateToConnectingScreen(@NonNull BEClient client);
    }

    interface Connect extends GenericConnect {
        void onScanStarted();
        void onScanEnded();
    }

    interface Reconnect extends GenericConnect {

    }

    interface Connecting extends BEPresenterDelegate {
        void updateClientInfo(@NonNull String name);
        void navigateToChatScreen(@NonNull BEClient client,
                                  @NonNull BETransmitter.ReaderWriter transmitter,
                                  @NonNull BETransmitter.Service transmitterService);
    }

    interface ConnectingServer extends Connecting {

    }

    interface ConnectingClient extends Connecting {

    }

    interface Chat extends BEPresenterDelegate {
        void updateClientInfo(@NonNull String name);
        void updateChat(@NonNull String newLine, @NonNull String fullChat);
        void clearChatTextField();

        void onAskToAcceptTransferFile(@NonNull Callback<Path> accept,
                                       @NonNull SimpleCallback deny,
                                       @NonNull String name,
                                       @NonNull String description);

        void onConnectionRestored();
        void onConnectionTimeout(boolean isWarning);

        void showError(@NonNull String title, @NonNull String message);
    }

    interface FileSystem extends BasePresenterDelegate {
        void setCurrentPath(@NonNull DirectoryPath path);
    }

    interface FileSystemNavigation extends FileSystem {
        void setEntityData(@NonNull FileSystemEntityViewModel entity);
    }

    interface FileSystemDirectory extends FileSystem {
        void setEntityData(@NonNull List<FileSystemEntityViewModel> entities);
    }

    interface FilePicker extends FileSystem {
        void onCloseWithoutPicking();

        void onPickFile(@NonNull FilePath path) throws Exception;
        void onPickDirectory(@NonNull DirectoryPath path) throws Exception;
    }
}
