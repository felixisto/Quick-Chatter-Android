package com.office.quickchatter.navigation;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BETransmitter;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.SimpleCallback;

public interface Router {
    void navigateBack();

    interface Primary extends Router {
        void navigateToConnectMenuScreen() throws Exception;
        void navigateToConnectScreen() throws Exception;
        void navigateToReconnectScreen() throws Exception;
        void navigateToConnectingAsServer(@NonNull BEClient client) throws Exception;
        void navigateToConnectingAsClient(@NonNull BEClient client) throws Exception;
        void navigateToChatScreen(@NonNull BEClient client,
                                  @NonNull BETransmitter.ReaderWriter transmitter,
                                  @NonNull BETransmitter.Service transmitterService) throws Exception;
    }

    interface FileSystem extends Router {

    }

    interface System extends Router {
        void closeAllPopupWindows();

        void pickFile(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String description) throws Exception;
        void pickDirectory(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String description) throws Exception;
        void pickFileDestination(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String name, @NonNull String description) throws Exception;
    }
}
