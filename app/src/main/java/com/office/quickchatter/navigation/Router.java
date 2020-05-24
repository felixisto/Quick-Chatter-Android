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
        void navigateToConnectMenuScreen();
        void navigateToConnectScreen();
        void navigateToReconnectScreen();
        void navigateToConnectingAsServer(@NonNull BEClient client);
        void navigateToConnectingAsClient(@NonNull BEClient client);
        void navigateToChatScreen(@NonNull BEClient client,
                                  @NonNull BETransmitter.ReaderWriter transmitter,
                                  @NonNull BETransmitter.Service transmitterService);
    }

    interface FileSystem extends Router {

    }

    interface System extends Router {
        void closeAllPopupWindows();

        void pickFile(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String description);
        void pickDirectory(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String description);
        void pickFileDestination(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String name, @NonNull String description);
    }
}
