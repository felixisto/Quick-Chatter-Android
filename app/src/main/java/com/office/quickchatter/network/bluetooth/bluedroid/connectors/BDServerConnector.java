package com.office.quickchatter.network.bluetooth.bluedroid.connectors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BEAdapter;
import com.office.quickchatter.network.bluetooth.basic.BEConnector;
import com.office.quickchatter.network.bluetooth.basic.BEError;
import com.office.quickchatter.network.bluetooth.basic.BESocket;
import com.office.quickchatter.network.bluetooth.bluedroid.BDAdapter;
import com.office.quickchatter.network.bluetooth.bluedroid.BDUUID;
import com.office.quickchatter.network.bluetooth.bluedroid.BDSocket;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SimpleCallback;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BDServerConnector implements BEConnector.Server {
    private final @NonNull String _serverName;
    private final @NonNull BDAdapter _adapter;

    private final @NonNull AtomicBoolean _running = new AtomicBoolean();

    private final @NonNull AtomicReference<BluetoothServerSocket> _serverSocket = new AtomicReference<>();

    public BDServerConnector(@NonNull String serverName, @NonNull BDAdapter adapter) {
        _serverName = serverName;
        _adapter = adapter;
    }

    // # Properties

    private @Nullable BluetoothServerSocket getOpenedServerSocket() {
        return _serverSocket.get();
    }

    // # BEConnector.Server

    public @NonNull UUID getUUID() {
        return BDUUID.get();
    }

    @Override
    public void start(final @NonNull Callback<BESocket> success, final @NonNull Callback<Exception> failure) throws Exception, BEError {
        if (_running.getAndSet(true)) {
            return;
        }

        final BDServerConnector self = this;

        Logger.message(self, "Starting server...");

        SimpleCallback completion = new SimpleCallback() {
            @Override
            public void perform() {
                startServer(success, failure);
            }
        };

        LooperService.getShared().performInBackground(completion);
    }

    @Override
    public synchronized void stop() {
        if (!_running.get()) {
            return;
        }

        resetServerSocket(true);

        // Does not support fresh restart
    }

    // # Internals

    private void startServer(final @NonNull Callback<BESocket> success, final @NonNull Callback<Exception> failure) {
        Logger.message(this, "Opening server socket...");

        BluetoothServerSocket serverSocket = getOpenedServerSocket();

        try {
            if (serverSocket == null) {
                serverSocket = openServerSocket();
                _serverSocket.set(serverSocket);
            }

            Logger.message(this, "Server searching for clients...");

            BluetoothSocket clientSocket = serverSocket.accept();

            if (clientSocket == null) {
                Errors.throwTimeoutError("Timeout");
            }

            Logger.message(this, "Successfully paired with client!");

            resetServerSocket(false);

            success.perform(new BDSocket(clientSocket));
        } catch (Exception e) {
            resetServerSocket(true);

            Logger.error(this, "Failed to open server socket, error: " + e);

            failure.perform(e);
        }
    }

    private @NonNull BluetoothServerSocket openServerSocket() throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth adapter is not available");
        }

        final BluetoothServerSocket socket = _adapter.getAdapter().listenUsingRfcommWithServiceRecord(_serverName, BDUUID.get());

        if (socket == null) {
            Errors.throwTimeoutError("Timeout");
        }

        return socket;
    }

    private void resetServerSocket(boolean close) {
        try {
            if (close) {
                _serverSocket.get().close();
            }

            _serverSocket.set(null);
        } catch (Exception e) {

        }
    }
}
