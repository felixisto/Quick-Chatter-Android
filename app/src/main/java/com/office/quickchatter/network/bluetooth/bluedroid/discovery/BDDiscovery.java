package com.office.quickchatter.network.bluetooth.bluedroid.discovery;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BEAdapter;
import com.office.quickchatter.network.bluetooth.bluedroid.BDAdapter;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.CollectionUtilities;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperClient;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SafeMutableArray;
import com.office.quickchatter.utilities.TimeValue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/// Searches for devices. The devices data is wiped when starting a new scan.
/// When searching, our device may not necessarily be visible to other devices, the visibility
/// must be broadcasted from a separate component.
public class BDDiscovery implements LooperClient {
    private final @NonNull Object lock = new Object();

    private final @NonNull BDDiscovery self;

    private final @NonNull Context _context;
    private final @NonNull BDAdapter _adapter;

    private final AtomicBoolean _isBroadcastRegistered = new AtomicBoolean(false);

    private final @NonNull HashSet<BluetoothDevice> _previousScanDevices = new HashSet<>();
    private final @NonNull AtomicReference<Date> _previousScanDate = new AtomicReference<>(null);

    private final @NonNull HashSet<BluetoothDevice> _lastScanDevices = new HashSet<>();
    private final @NonNull AtomicReference<Date> _lastScanDate = new AtomicReference<>(null);

    private final @NonNull SafeMutableArray<Callback<Set<BluetoothDevice>>> _scanCompletions = new SafeMutableArray<>();

    public BDDiscovery(@NonNull Context context, @NonNull BDAdapter adapter) {
        _context = context.getApplicationContext();
        _adapter = adapter;
        self = this;
    }

    // # Properties

    // Note: Discovery keeps track of all devices, that have ever been found, including lost devices
    public @NonNull Set<BluetoothDevice> getFoundDevicesFromLastScan() {
        synchronized (lock) {
            return CollectionUtilities.copy(_lastScanDevices);
        }
    }

    public @Nullable Date getLastScanDate() {
        synchronized (lock) {
            return _lastScanDate.get();
        }
    }

    public @NonNull TimeValue getTimePassedSinceLastScan() {
        Date date = getLastScanDate();

        if (date == null) {
            return TimeValue.zero();
        }

        Date now = new Date();
        long ms = now.getTime() - date.getTime();

        return TimeValue.buildMS((int) ms);
    }

    public @NonNull Set<BluetoothDevice> getFoundDevicesFromPreviousScan() {
        synchronized (lock) {
            return CollectionUtilities.copy(_previousScanDevices);
        }
    }

    public @Nullable Date getPreviousScanDate() {
        synchronized (lock) {
            return _previousScanDate.get();
        }
    }

    // # Operations

    public boolean isRunning() {
        if (!_adapter.isAvailable()) {
            return false;
        }

        return _adapter.getAdapter().isDiscovering();
    }

    public void runDiscoveryScan(@Nullable Callback<Set<BluetoothDevice>> completion) throws Exception {
        Logger.message(this, "Start.");

        synchronized (lock) {
            if (!_isBroadcastRegistered.getAndSet(true)) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                _context.registerReceiver(_onDeviceFound, filter);
                filter = new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED);
                _context.registerReceiver(_onBoundStateChanged, filter);
                filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
                _context.registerReceiver(_onConnected, filter);
            }

            if (isRunning()) {
                return;
            }

            LooperService.getShared().subscribe(this);

            startDiscovery();

            if (completion != null) {
                _scanCompletions.add(completion);
            }
        }
    }

    public void stop() {
        Logger.message(this, "Stop.");

        synchronized (lock) {
            stopDiscovery();
        }
    }

    // # LooperClient

    @Override
    public void loop() {
        Set<BluetoothDevice> devices = getFoundDevicesFromLastScan();

        synchronized (lock) {
            if (!isRunning()) {
                LooperService.getShared().unsubscribe(this);

                List<Callback<Set<BluetoothDevice>>> callbacks = _scanCompletions.copyData();

                _scanCompletions.removeAll();

                for (Callback<Set<BluetoothDevice>> callback: callbacks) {
                    callback.perform(devices);
                }
            }
        }
    }

    // # Internals

    private void startDiscovery() throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth adapter is not available");
        }

        _previousScanDevices.clear();
        _previousScanDevices.addAll(_lastScanDevices);
        _lastScanDevices.clear();

        _previousScanDate.set(_lastScanDate.get());
        _lastScanDate.set(new Date());

        _adapter.getAdapter().startDiscovery();
    }

    private void stopDiscovery() {
        if (!_adapter.isAvailable()) {
            return;
        }

        try {
            _adapter.getAdapter().cancelDiscovery();
        } catch (Exception e) {

        }

        if (_isBroadcastRegistered.getAndSet(false)) {
            _context.unregisterReceiver(_onDeviceFound);
            _context.unregisterReceiver(_onBoundStateChanged);
        }
    }

    private void onDeviceFound(@NonNull BluetoothDevice device) {
        Logger.message(this, "Device found! Name: " + device.getName());

        synchronized (lock) {
            _lastScanDevices.remove(device);
            _lastScanDevices.add(device);
        }
    }

    // # Callbacks

    private final BroadcastReceiver _onDeviceFound = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (device != null) {
                self.onDeviceFound(device);
            }
        }
    };

    private final BroadcastReceiver _onBoundStateChanged = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (device != null && name != null) {
                self.onDeviceFound(device);
            }
        }
    };

    private final BroadcastReceiver _onConnected = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (device != null) {
                self.onDeviceFound(device);
            }
        }
    };
}
