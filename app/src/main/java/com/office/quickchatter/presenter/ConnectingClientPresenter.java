package com.office.quickchatter.presenter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEConnector;
import com.office.quickchatter.network.bluetooth.basic.BESocket;
import com.office.quickchatter.network.bluetooth.bluedroid.transmission.BDTransmitter;
import com.office.quickchatter.presenter.worker.ConnectingPresenter;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SimpleCallback;

public class ConnectingClientPresenter implements BasePresenter.ConnectingClient {
    private final @NonNull Context _context;
    private @Nullable BasePresenterDelegate.ConnectingClient _delegate;

    private final @NonNull BEConnector.Client _connector;
    private final @NonNull BEClient _client;
    private @Nullable BDTransmitter _transmitter;

    public ConnectingClientPresenter(@NonNull Context context, @NonNull BEClient client, @NonNull BEConnector.Client connector) {
        _context = context;
        _client = client;
        _connector = connector;
    }

    // # Presenter.Connect

    @Override
    public void start(@NonNull final BasePresenterDelegate.ConnectingClient delegate) {
        if (_delegate != null) {
            return;
        }

        Logger.message(this, "Start.");

        _delegate = delegate;

        final Callback<BESocket> success = new Callback<BESocket>() {
            @Override
            public void perform(BESocket argument) {
                BDTransmitter transmitter = startServerConnection(argument);

                delegate.updateClientInfo("Paired!");
                delegate.navigateToChatScreen(_client, transmitter, transmitter);
            }
        };

        final Callback<Exception> failure = new Callback<Exception>() {
            @Override
            public void perform(Exception argument) {
                delegate.updateClientInfo("Failed!");
            }
        };

        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    _connector.connect(success, failure);
                } catch (Exception e) {
                    Logger.error(this, "Cannot runDiscoveryScan server, error: " + e);
                }
            }
        });

        _delegate.updateClientInfo(_connector.getServer().getName());
    }

    @Override
    public void stop() {
        _connector.stop();
    }

    private @NonNull BDTransmitter startServerConnection(@NonNull BESocket socket) {
        try {
            _transmitter = ConnectingPresenter.startServer(socket);
        } catch (Exception e) {
            Logger.error(this, "Cannot start transmitter, error: " + e);
        }

        return _transmitter;
    }
}
