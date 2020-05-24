package com.office.quickchatter.network.bluetooth.bluedroid.discovery;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEClientScanner;
import com.office.quickchatter.network.bluetooth.basic.BEClientScannerListener;
import com.office.quickchatter.network.bluetooth.basic.BEError;
import com.office.quickchatter.network.basic.ScannerConfiguration;
import com.office.quickchatter.network.bluetooth.bluedroid.model.BDDiscoveredClient;
import com.office.quickchatter.network.bluetooth.bluedroid.parser.BluetoothDeviceToClientParser;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.CollectionUtilities;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperClient;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.Parser;
import com.office.quickchatter.utilities.SafeMutableArray;
import com.office.quickchatter.utilities.TimeValue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BDClientScanner implements BEClientScanner, LooperClient {
    public static final TimeValue CLIENT_DISCOVERY_TIMEOUT = TimeValue.buildSeconds(10);

    private final Object lock = new Object();

    private final AtomicBoolean _running = new AtomicBoolean();

    private final BDDiscovery _discovery;
    private final @NonNull Parser<BluetoothDevice, BEClient> _parser;
    private final @NonNull HashSet<BDDiscoveredClient> _clients = new HashSet<>();

    private final AtomicReference<TimeValue> _clientTimeout = new AtomicReference<>(CLIENT_DISCOVERY_TIMEOUT);
    private final AtomicInteger _scanTryCount = new AtomicInteger();

    private @NonNull ScannerConfiguration _configuration = new ScannerConfiguration();

    private final @NonNull SafeMutableArray<BEClientScannerListener> _listeners = new SafeMutableArray<>();

    public BDClientScanner(@NonNull BDDiscovery discovery, @NonNull Parser<BluetoothDevice, BEClient> parser) {
        _discovery = discovery;
        _parser = parser;
    }

    public BDClientScanner(@NonNull BDDiscovery discovery) {
        this(discovery, new BluetoothDeviceToClientParser());
    }

    // # BEClientScanner

    @Override
    public void subscribe(@NonNull BEClientScannerListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void unsubscribe(@NonNull BEClientScannerListener listener) {
        _listeners.remove(listener);
    }

    // # Properties

    @Override
    public boolean isRunning() {
        return _running.get();
    }

    public @NonNull TimeValue getClientTimeout() {
        return _clientTimeout.get();
    }

    public void setClientTimeout(@NonNull TimeValue value) {
        _clientTimeout.set(value);
    }

    // # Operations

    @Override
    public void start() throws Exception {
        synchronized (lock) {
            if (isRunning()) {
                throw new BEError(BEError.Value.alreadyRunning);
            }

            _scanTryCount.set(0);

            LooperService.getShared().subscribe(this);

            startScan(false);
        }
    }

    @Override
    public void stop() throws Exception {
        synchronized (lock) {
            if (!isRunning()) {
                throw new BEError(BEError.Value.alreadyStopped);
            }

            stopScan();
        }
    }

    @Override
    public @NonNull ScannerConfiguration getConfiguration() {
        synchronized (lock) {
            return _configuration;
        }
    }

    @Override
    public void setConfiguration(@NonNull ScannerConfiguration configuration) throws Exception {
        synchronized (lock) {
            if (isRunning()) {
                throw new BEError(BEError.Value.cannotConfigurateWhileRunning);
            }

            _configuration = configuration.copy();
        }
    }

    // Updates the scan data based on the currently gathered info.
    void updateScanWithCurrentState(boolean updateTimeOut) {
        ScanUpdateResult result;

        synchronized (lock) {
            result = updateScan(new Date(), updateTimeOut);
        }

        alertListeners(result);
    }

    // # LooperClient

    @Override
    public void loop() {
        if (!isRunning()) {
            return;
        }

        // Update scan data, and the listeners
        updateScanWithCurrentState(false);

        // Timer
        retryScanIfNecessary();
    }

    // # Timeout

    public boolean isClientConsideredTimedOut(@NonNull BDDiscoveredClient client, @NonNull Date now) {
        long timeout = getClientTimeout().inMS();
        long timePassedInMS = now.getTime() - client.getDateFound().getTime();

        return timeout <= timePassedInMS;
    }

    // # Internals

    private void startScan(final boolean isRetry) {
        Logger.message(this, "Running a " + (!isRetry ? "scan" : "rescan"));

        _running.set(true);

        _scanTryCount.incrementAndGet();

        _listeners.perform(new Callback<BEClientScannerListener>() {
            @Override
            public void perform(BEClientScannerListener listener) {
                if (!isRetry) {
                    listener.onScanStart();
                } else {
                    listener.onScanRestart();
                }
            }
        });

        if (!_discovery.isRunning()) {
            try {
                Logger.message(this, "Starting discovery");

                _discovery.runDiscoveryScan(new Callback<Set<BluetoothDevice>>() {
                    @Override
                    public void perform(Set<BluetoothDevice> argument) {
                        updateScanWithCurrentState(true);
                    }
                });
            } catch (Exception e) {
                Logger.error(this, "Failed to runDiscoveryScan discovery, error: " + e);
            }
        }
    }

    private void retryScanIfNecessary() {
        if (_discovery.isRunning()) {
            return;
        }

        boolean isExhausted = _scanTryCount.get() > getConfiguration().retryCount;

        if (isExhausted) {
            Logger.message(this, "Exhausted retry, stopping scan.");
            stopScan();
            return;
        }

        int retryDelayInMS = getConfiguration().retryDelay.inMS();

        boolean retry = _discovery.getTimePassedSinceLastScan().inMS() > retryDelayInMS;

        if (retry) {
            restartScan();
        }
    }

    private void restartScan() {
        startScan(true);
    }

    private void stopScan() {
        LooperService.getShared().unsubscribe(this);

        _listeners.perform(new Callback<BEClientScannerListener>() {
            @Override
            public void perform(BEClientScannerListener listener) {
                listener.onScanEnd();
            }
        });

        _discovery.stop();

        _running.set(false);
    }

    private @NonNull ScanUpdateResult updateScan(@NonNull Date currentDate, boolean updateTimeOut) {
        ScanUpdateResult result = updateScanData(currentDate, updateTimeOut);

        Logger.message(this, "Scan updated, added " + result.newClients.size() + " new clients and lost " + result.clientsLost.size());

        return result;
    }

    private @NonNull ScanUpdateResult updateScanData(@NonNull Date currentDate, boolean updateTimeOut) {
        Set<BDDiscoveredClient> clientsBeforeUpdate = CollectionUtilities.copy(_clients);

        Set<BluetoothDevice> devicesFromScan = _discovery.getFoundDevicesFromLastScan();

        // Make up clients from the found devices
        HashSet<BDDiscoveredClient> scannedClients = new HashSet<>();

        for (BluetoothDevice device : devicesFromScan) {
            if (device.getName() == null) {
                continue;
            }

            try {
                BEClient client = _parser.parse(device);

                scannedClients.add(new BDDiscoveredClient(client, currentDate));
            } catch (Exception e) {
                Logger.error(this, "Failed to parse BluetoothDevice, error: " + e);
            }
        }

        // Update and record new clients
        Set<BDDiscoveredClient> newClients = addNewClients(scannedClients);

        // Update timeout
        if (updateTimeOut) {
            removedAllTimedOutClients(currentDate);
        }

        // Make a list of all the lost clients
        Set<BDDiscoveredClient> clientsLost = new HashSet<>();

        Set<BDDiscoveredClient> clientsAfterUpdate = CollectionUtilities.copy(_clients);

        for (BDDiscoveredClient client : clientsBeforeUpdate) {
            if (!clientsAfterUpdate.contains(client)) {
                clientsLost.add(client);
            }
        }

        return new ScanUpdateResult(clientsAfterUpdate, newClients, clientsLost);
    }

    // Adds the clients from the given set to the current clients data.
    // Returns a list of the new added clients.
    private @NonNull Set<BDDiscoveredClient> addNewClients(@NonNull Set<BDDiscoveredClient> clients) {
        HashSet<BDDiscoveredClient> addedClients = new HashSet<>();

        synchronized (lock) {
            for (BDDiscoveredClient newClient : clients) {
                int beforeCount = _clients.size();

                _clients.remove(newClient);

                boolean wasAlreadyPresent = beforeCount != _clients.size();

                _clients.add(newClient);

                // If the size changed, then the client was already present
                if (!wasAlreadyPresent) {
                    addedClients.add(newClient);
                }
            }
        }

        return addedClients;
    }

    private void removedAllTimedOutClients(@NonNull Date now) {
        Set<BDDiscoveredClient> currentClients;

        synchronized (lock) {
            currentClients = CollectionUtilities.copy(_clients);
        }

        for (BDDiscoveredClient client: currentClients) {
            if (isClientConsideredTimedOut(client, now)) {
                _clients.remove(client);
            }
        }
    }

    private void alertListeners(@NonNull ScanUpdateResult scanResult) {
        for (final BDDiscoveredClient addedClient : scanResult.newClients) {
            _listeners.perform(new Callback<BEClientScannerListener>() {
                @Override
                public void perform(BEClientScannerListener listener) {
                    listener.onClientFound(addedClient.client);
                }
            });
        }

        for (final BDDiscoveredClient oldClient : scanResult.allClients) {
            _listeners.perform(new Callback<BEClientScannerListener>() {
                @Override
                public void perform(BEClientScannerListener listener) {
                    listener.onClientUpdate(oldClient.client);
                }
            });
        }

        for (final BDDiscoveredClient lostClient : scanResult.clientsLost) {
            _listeners.perform(new Callback<BEClientScannerListener>() {
                @Override
                public void perform(BEClientScannerListener listener) {
                    listener.onClientLost(lostClient.client);
                }
            });
        }
    }
}

class ScanUpdateResult {
    final @NonNull Set<BDDiscoveredClient> allClients;
    final @NonNull Set<BDDiscoveredClient> newClients;
    final @NonNull Set<BDDiscoveredClient> clientsLost;

    ScanUpdateResult(@NonNull Set<BDDiscoveredClient> allClients,
                     @NonNull Set<BDDiscoveredClient> newClients,
                     @NonNull Set<BDDiscoveredClient> clientsLost) {
        this.allClients = allClients;
        this.newClients = newClients;
        this.clientsLost = clientsLost;
    }
}
