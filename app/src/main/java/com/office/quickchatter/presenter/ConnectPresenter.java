package com.office.quickchatter.presenter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEClientScanner;
import com.office.quickchatter.network.bluetooth.basic.BEClientScannerListener;
import com.office.quickchatter.network.bluetooth.basic.BEEmitter;
import com.office.quickchatter.ui.adapters.BEClientsAdapterData;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperClient;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SafeMutableArray;
import com.office.quickchatter.utilities.SimpleCallback;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectPresenter implements BasePresenter.Connect, LooperClient, BEClientScannerListener {
    private @NonNull Context _context;
    private @Nullable
    BasePresenterDelegate.Connect _delegate;

    private final @NonNull AtomicBoolean _scanning = new AtomicBoolean();

    private final @NonNull BEClientScanner _scanner;
    private final @NonNull BEEmitter _emitter;

    private final @NonNull SafeMutableArray<BEClient> _clients = new SafeMutableArray<>();
    private @NonNull BEClientsAdapterData _data = new BEClientsAdapterData(_clients.copyData());

    public ConnectPresenter(@NonNull Context context, @NonNull BEClientScanner scanner, @NonNull BEEmitter emitter) {
        _context = context;
        _scanner = scanner;
        _emitter = emitter;
    }

    // # Presenter.Connect

    @Override
    public boolean isScanning() {
        return _scanning.get();
    }

    @Override
    public void start(@NonNull BasePresenterDelegate.Connect delegate) {
        if (_delegate != null) {
            return;
        }

        Logger.message(this, "Start.");

        _delegate = delegate;

        if (!_emitter.isBluetoothOn()) {
            delegate.displayBluetoothIsOffAlert();
            return;
        }
    }

    @Override
    public void stop() {
        stopScan();
    }

    @Override
    public void startScan() {
        if (isScanning()) {
            return;
        }

        Logger.message(this, "Start scanning...");

        startScanningNow();
        startEmittingPresence();
    }

    @Override
    public void stopScan() {
        Logger.message(this, "Stop scanning.");

        stopCurrentScan();
        stopEmittingPresence();
    }

    @Override
    public void pickItem(int index) {
        if (index < 0 || index >= _clients.size()) {
            return;
        }

        try {
            _scanner.stop();
            stopScan();
        } catch (Exception e) {

        }

        if (_delegate == null) {
            return;
        }

        BEClient client = _clients.get(index);

        _delegate.navigateToConnectingScreen(client);
    }

    // # LooperClient

    @Override
    public void loop() {
        if (!isScanning()) {
            return;
        }

        if (_delegate == null) {
            return;
        }

        try {
            if (!_scanner.isRunning()) {
                _scanner.start();
            }
        } catch (Exception e) {

        }
    }

    // # BEClientScannerListener

    @Override
    public void onScanStart() {
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate != null) {
                    _delegate.onScanStarted();
                }
            }
        });
    }

    @Override
    public void onScanRestart() {

    }

    @Override
    public void onScanEnd() {
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate != null) {
                    _delegate.onScanEnded();
                }
            }
        });
    }

    @Override
    public void onClientFound(final @NonNull BEClient client) {
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                addClient(client);
                updateClientsData();
            }
        });
    }

    @Override
    public void onClientUpdate(final @NonNull BEClient client) {
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                addClient(client);
                updateClientsData();
            }
        });
    }

    @Override
    public void onClientLost(final @NonNull BEClient client) {
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                removeClient(client);
                updateClientsData();
            }
        });
    }

    // # Internals - Scan

    private void startScanningNow() {
        _scanning.set(true);
        _scanner.subscribe(this);
        LooperService.getShared().subscribe(this);

        // No need to start scan, will be started by loop()
    }

    private void stopCurrentScan() {
        _scanning.set(false);
        _scanner.unsubscribe(this);
        LooperService.getShared().unsubscribe(this);

        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    _scanner.stop();
                } catch (Exception e) {

                }
            }
        });
    }

    // # Internals - Emit presence

    private void startEmittingPresence() {
        LooperService.getShared().asyncInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                startEmittingPresenceNow();
            }
        });
    }

    private void startEmittingPresenceNow() {
        _emitter.addEndCompletion(new SimpleCallback() {
            @Override
            public void perform() {
                restartEmittingPresenceIfNecessary();
            }
        });

        try {
            _emitter.start();
        } catch (Exception e) {

        }
    }

    private void stopEmittingPresence() {
        try {
            _emitter.stop();
        } catch (Exception e) {

        }
    }

    private void restartEmittingPresenceIfNecessary() {
        if (!_scanner.isRunning()) {
            return;
        }

        startEmittingPresence();
    }

    // # Internals - model

    private void updateClientsData() {
        if (_data.getValues().equals(_clients.copyData())) {
            return;
        }

        _data = new BEClientsAdapterData(_clients.copyData());

        if (_delegate == null) {
            return;
        }

        _delegate.updateClientsListData(_data);
    }

    private void addClient(@NonNull BEClient client) {
        boolean alreadyAdded = false;

        for (BEClient element : _clients.copyData()) {
            if (element.getIdentifier() == client.getIdentifier()) {
                alreadyAdded = true;
                break;
            }
        }

        if (!alreadyAdded) {
            _clients.add(client);
        }
    }

    private void removeClient(@NonNull BEClient client) {
        for (BEClient element: _clients.copyData()) {
            if (element.equals(client)) {
                _clients.remove(client);
            }
        }
    }
}
