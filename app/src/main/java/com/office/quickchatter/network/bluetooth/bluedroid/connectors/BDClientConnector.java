package com.office.quickchatter.network.bluetooth.bluedroid.connectors;

import android.bluetooth.BluetoothSocket;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEClientDevice;
import com.office.quickchatter.network.bluetooth.basic.BEConnector;
import com.office.quickchatter.network.bluetooth.basic.BEError;
import com.office.quickchatter.network.bluetooth.basic.BESocket;
import com.office.quickchatter.network.bluetooth.bluedroid.BDAdapter;
import com.office.quickchatter.network.bluetooth.bluedroid.BDUUID;
import com.office.quickchatter.network.bluetooth.bluedroid.BDClientDevice;
import com.office.quickchatter.network.bluetooth.bluedroid.BDSocket;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SimpleCallback;
import com.office.quickchatter.utilities.TimeValue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BDClientConnector implements BEConnector.Client {
    private final @NonNull BEClient _server;
    private final @NonNull BDAdapter _adapter;

    private final @NonNull AtomicBoolean _running = new AtomicBoolean(false);

    private final int _tryCountOriginal;
    private final @NonNull AtomicInteger _tryCount = new AtomicInteger();
    private final @NonNull TimeValue _retryDelay;

    public BDClientConnector(@NonNull BEClient server, @NonNull BDAdapter adapter, int tryCount, @NonNull TimeValue retryDelay) {
        _server = server;
        _adapter = adapter;
        _tryCountOriginal = tryCount;
        _tryCount.set(_tryCountOriginal);
        _retryDelay = retryDelay;
    }

    // # Properties

    public boolean isTryExhausted() {
        return _tryCount.get() <= 0;
    }

    // # BEConnector.Client

    public @NonNull UUID getUUID() {
        return BDUUID.get();
    }

    @Override
    public @NonNull BEClient getServer() {
        return _server;
    }

    @Override
    public void connect(@NonNull Callback<BESocket> success, @NonNull Callback<Exception> failure) throws Exception, BEError {
        if (_running.getAndSet(true)) {
            Errors.throwCannotStartTwice("Already running");
        }

        Logger.message(this, "Connect start");

        _tryCount.set(_tryCountOriginal);

        start(success, failure);
    }

    @Override
    public void stop() {
        // Connector cannot stop, does not support fresh restart
    }

    // # Internals

    private void start(final @NonNull Callback<BESocket> success, final @NonNull Callback<Exception> failure) throws Exception, BEError {
        final BDClientConnector self = this;

        Logger.message(self, "Connecting to server...");

        SimpleCallback completion = new SimpleCallback() {
            @Override
            public void perform() {
                Logger.message(self, "Opening client socket...");

                try {
                    BEClientDevice clientDevice = _server.getDevice();
                    BluetoothSocket socket = null;

                    if (clientDevice instanceof BDClientDevice) {
                        socket = ((BDClientDevice)clientDevice).asBluetoothDevice().createRfcommSocketToServiceRecord(getUUID());
                    }

                    if (socket == null) {
                        Errors.throwTimeoutError("Timeout");
                    }

                    socket.connect();

                    Logger.message(self, "Successfully paired with server '" + _server.getName() + "'!");

                    success.perform(new BDSocket(socket));
                } catch (Exception e) {
                    retry(success, failure, e);
                }
            }
        };

        LooperService.getShared().performInBackground(completion);
    }

    private void retry(final @NonNull Callback<BESocket> success, final @NonNull Callback<Exception> failure, @NonNull Exception originalError) {
        if (isTryExhausted()) {
            Logger.error(this, "Timer is exhausted, ending connection.");
            completeFailure(failure, originalError);
            return;
        }

        _tryCount.decrementAndGet();

        LooperService.getShared().asyncInBackgroundAfterDelay(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    start(success, failure);
                } catch (Exception e) {
                    completeFailure(failure, e);
                }
            }
        }, _retryDelay);
    }

    private void completeFailure(final @NonNull Callback<Exception> failure, @NonNull Exception error) {
        Logger.error(this, "Failed to connect, error: " + error);

        _running.set(false);

        failure.perform(error);
    }
}
