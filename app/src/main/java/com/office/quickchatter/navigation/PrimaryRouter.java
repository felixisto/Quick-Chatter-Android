package com.office.quickchatter.navigation;

import android.bluetooth.BluetoothAdapter;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.office.quickchatter.R;
import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEEmitter;
import com.office.quickchatter.network.bluetooth.basic.BETransmitter;
import com.office.quickchatter.network.bluetooth.bluedroid.BDAdapter;
import com.office.quickchatter.network.bluetooth.bluedroid.discovery.BDClientScanner;
import com.office.quickchatter.network.bluetooth.bluedroid.discovery.BDDiscovery;
import com.office.quickchatter.network.bluetooth.bluedroid.discovery.BDEmitter;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.filesystem.model.EntityInfo;
import com.office.quickchatter.filesystem.simple.SimpleFileSystem;
import com.office.quickchatter.filesystem.worker.loader.FileSystemLoader;
import com.office.quickchatter.filesystem.worker.loader.FileSystemLoaderLocal;
import com.office.quickchatter.ui.FragmentBuilder;
import com.office.quickchatter.ui.fragment.FragmentFilePicker;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SimpleCallback;

import java.util.concurrent.atomic.AtomicReference;

public class PrimaryRouter implements Router.Primary, Router.System {
    private final @NonNull AtomicReference<State> _state = new AtomicReference<>();

    private final @NonNull FragmentActivity _context;
    private final @NonNull BDAdapter _adapter;

    private final @NonNull BDClientScanner _scanner;
    private final @NonNull BEEmitter _emitter;

    private final @NonNull SimpleFileSystem _fileSystem = new SimpleFileSystem();
    private final @NonNull FileSystemLoaderLocal _fileSystemLoader;
    private @Nullable EntityInfo.Directory _rootDirectoryRepo;

    private @Nullable Fragment _currentFragment;
    private @Nullable Fragment _currentPopOverFragment;
    private final @NonNull FragmentBuilder _fragmentBuilder;

    public PrimaryRouter(@NonNull FragmentActivity context) {
        _state.set(State.connectMenu);
        _context = context;
        _adapter = new BDAdapter(BluetoothAdapter.getDefaultAdapter());
        _scanner = new BDClientScanner(new BDDiscovery(_context, _adapter));
        _emitter = new BDEmitter(_context, _adapter, BDEmitter.DEFAULT_EMIT_TIME);
        _fileSystemLoader = new FileSystemLoaderLocal(_fileSystem);
        _fragmentBuilder = new FragmentBuilder(_context, this, this, _fileSystemLoader);

        Logger.message(this, "Initialized");

        goToConnectMenuScreen();
    }

    // # Properties

    @NonNull State getState() {
        return _state.get();
    }

    @Nullable View getCurrentContentView() {
        return _currentFragment != null ? _currentFragment.getView() : null;
    }

    // # Router.Primary

    @Override
    public void navigateBack() {
        Logger.message(this, "Navigate back...");

        // File picker screen navigation
        if (isFilePickerScreenVisible()) {
            exitFilePickerScreen();
            return;
        }

        // Cannot navigate away from root
        if (getState() == State.connectMenu) {
            Logger.message(this, "Cannot navigate back, already at root screen");
            return;
        }

        // Go back to root screen
        if (getState() == State.connect) {
            goToConnectMenuScreen();
            return;
        }

        // Go back to root screen
        if (getState() == State.reconnect) {
            goToConnectMenuScreen();
            return;
        }

        if (getState() == State.connecting) {
            goToConnectScreen();
            return;
        }

        if (getState() == State.chat) {
            goToConnectMenuScreen();
            return;
        }

        Logger.error(this, "Unknown screen, cannot navigate back, internal error.");
    }

    @Override
    public void navigateToConnectMenuScreen() throws Exception {
        if (getState() == State.connectMenu) {
            String message = "Already at connect menu screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        closeAllPopupWindows();

        goToConnectMenuScreen();
    }

    @Override
    public void navigateToConnectScreen() throws Exception {
        if (getState() == State.connect) {
            String message = "Already at connect screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        if (getState() != State.connectMenu && getState() != State.chat) {
            String message = "Can navigate to connect screen only from either chat OR connect menu screens!";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }

        closeAllPopupWindows();

        goToConnectScreen();
    }

    @Override
    public void navigateToReconnectScreen() throws Exception {
        if (getState() == State.reconnect) {
            String message = "Already at connect screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        if (getState() != State.connectMenu) {
            String message = "Can navigate to reconnect screen only from connect menu screen!";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }

        closeAllPopupWindows();

        goToReconnectScreen();
    }

    @Override
    public void navigateToConnectingAsServer(@NonNull BEClient client) throws Exception {
        closeAllPopupWindows();

        navigateToConnecting(client, true);
    }

    @Override
    public void navigateToConnectingAsClient(@NonNull BEClient client) throws Exception {
        closeAllPopupWindows();

        navigateToConnecting(client, false);
    }

    void navigateToConnecting(@NonNull BEClient client, boolean isServer) throws Exception {
        if (getState() == State.connecting) {
            String message = "Already at connecting screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        if (getState() != State.connect && getState() != State.reconnect) {
            String message = "Can navigate to connecting screen from either reconnect OR connect screen!";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }

        closeAllPopupWindows();

        if (isServer) {
            goToConnectingServerScreen(client);
        } else {
            goToConnectingClientScreen(client);
        }
    }

    @Override
    public void navigateToChatScreen(@NonNull BEClient client,
                                     @NonNull BETransmitter.ReaderWriter transmitter,
                                     @NonNull BETransmitter.Service transmitterService) throws Exception {
        if (getState() == State.chat) {
            String message = "Already at chat screen, no need for navigation.";
            Logger.warning(this, message);
            Errors.throwIllegalStateError(message);
        }

        if (getState() != State.connecting) {
            String message = "Can navigate to chat screen only from connecting screen!";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }

        closeAllPopupWindows();

        goToChatScreen(client, transmitter, transmitterService);
    }

    // # Router.System

    @Override
    public void closeAllPopupWindows() {
        if (isFilePickerScreenVisible()) {
            exitFilePickerScreen();
        }
    }

    @Override
    public void pickFile(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String description) throws Exception {
        if (isFilePickerScreenVisible()) {
            String message = "Cannot open pick file screen again, its already open";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }

        openPickFileScreen(false, success, failure, description);
    }

    @Override
    public void pickDirectory(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String description) throws Exception {
        if (isFilePickerScreenVisible()) {
            String message = "Cannot open pick file screen again, its already open";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }

        openPickFileScreen(true, success, failure, description);
    }


    @Override
    public void pickFileDestination(@NonNull Callback<Path> success, @NonNull SimpleCallback failure, @NonNull String name, @NonNull String description) throws Exception {
        if (isFilePickerScreenVisible()) {
            String message = "Cannot open pick file screen again, its already open";
            Logger.error(this, message);
            Errors.throwIllegalStateError(message);
        }

        openPickFileDestinationScreen(success, failure, name, description);
    }

    // # Go to

    private void goToConnectMenuScreen() {
        Logger.message(this, "Go to connect menu screen");

        _state.set(State.connectMenu);

        final Fragment fragment = _fragmentBuilder.buildConnectMenuScreen();

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _currentFragment = fragment;

                FragmentTransaction transaction = _context.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.contentArea, fragment);
                transaction.commit();
            }
        });
    }

    private void goToConnectScreen() {
        Logger.message(this, "Go to connect screen");

        _state.set(State.connect);

        final Fragment fragment = _fragmentBuilder.buildConnectScreen(_adapter, _scanner, _emitter);

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _currentFragment = fragment;

                FragmentTransaction transaction = _context.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.contentArea, fragment);
                transaction.commit();
            }
        });
    }

    private void goToReconnectScreen() {
        Logger.message(this, "Go to reconnect screen");

        _state.set(State.reconnect);

        final Fragment fragment = _fragmentBuilder.buildReconnectScreen(_adapter, _emitter);

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _currentFragment = fragment;

                FragmentTransaction transaction = _context.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.contentArea, fragment);
                transaction.commit();
            }
        });
    }

    private void goToConnectingServerScreen(@NonNull BEClient client) {
        Logger.message(this, "Go to connecting screen (server)");

        _state.set(State.connecting);

        final Fragment fragment = _fragmentBuilder.buildConnectingServerScreen(client, _adapter);

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _currentFragment = fragment;

                FragmentTransaction transaction = _context.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.contentArea, fragment);
                transaction.commit();
            }
        });
    }

    private void goToConnectingClientScreen(@NonNull BEClient client) {
        Logger.message(this, "Go to connecting screen (client)");

        _state.set(State.connecting);

        final Fragment fragment = _fragmentBuilder.buildConnectingClientScreen(client, _adapter);

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _currentFragment = fragment;

                FragmentTransaction transaction = _context.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.contentArea, fragment);
                transaction.commit();
            }
        });
    }

    private void goToChatScreen(@NonNull BEClient client,
                                @NonNull BETransmitter.ReaderWriter transmitter,
                                @NonNull BETransmitter.Service transmitterService) {
        Logger.message(this, "Go to chat screen");

        _state.set(State.chat);

        final Fragment fragment = _fragmentBuilder.buildChatScreen(client, transmitter, transmitterService);

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _currentFragment = fragment;

                FragmentTransaction transaction = _context.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.contentArea, fragment);
                transaction.commit();
            }
        });
    }

    private void openPickFileScreen(boolean pickDirectory,
                                    @NonNull Callback<Path> success,
                                    @NonNull SimpleCallback failure,
                                    @NonNull String description) {
        Logger.message(this, "Open pick file screen");

        final Fragment fragment = _fragmentBuilder.buildPickFileScreen(getRootDirectoryRepo(_fileSystemLoader), success, failure, pickDirectory, description);

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _currentPopOverFragment = fragment;

                FragmentTransaction transaction = _context.getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.add(R.id.contentArea, fragment);
                transaction.commit();
            }
        });
    }

    private void openPickFileDestinationScreen(@NonNull Callback<Path> success,
                                               @NonNull SimpleCallback failure,
                                               @NonNull String name,
                                               @NonNull String description) {
        Logger.message(this, "Open pick file destination screen");

        final Fragment fragment = _fragmentBuilder.buildPickFileDestinationScreen(getRootDirectoryRepo(_fileSystemLoader), success, failure, name, description);

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _currentPopOverFragment = fragment;

                FragmentTransaction transaction = _context.getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.add(R.id.contentArea, fragment);
                transaction.commit();
            }
        });
    }

    // # File system

    private synchronized EntityInfo.Directory getRootDirectoryRepo(@NonNull FileSystemLoader loader) {
        if (_rootDirectoryRepo != null) {
            return _rootDirectoryRepo;
        }

        EntityInfo.Directory rootDirectoryRepo;

        try {
            rootDirectoryRepo = loader.readRootDirectoryInfo();
        } catch (Exception e) {
            rootDirectoryRepo = null;
        }

        _rootDirectoryRepo = rootDirectoryRepo;

        return rootDirectoryRepo;
    }

    // # File picker screen

    private boolean isFilePickerScreenVisible() {
        return _currentPopOverFragment instanceof FragmentFilePicker;
    }

    private void exitFilePickerScreen() {
        Logger.message(this, "Exit file picker screen.");

        _context.getSupportFragmentManager().popBackStack();

        _currentPopOverFragment = null;
    }

    // # State

    enum State {
        connectMenu, connect, reconnect, connecting, chat;
    }
}
