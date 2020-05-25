package com.office.quickchatter.network.bluetooth.bluedroid.discovery;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEAdapter;
import com.office.quickchatter.network.bluetooth.basic.BEEmitter;
import com.office.quickchatter.network.bluetooth.bluedroid.BDAdapter;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.SafeMutableArray;
import com.office.quickchatter.utilities.SimpleCallback;
import com.office.quickchatter.utilities.TimeValue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.bluetooth.BluetoothAdapter.EXTRA_SCAN_MODE;

/// Broadcast ourselves to other devices.
public class BDEmitter extends BroadcastReceiver implements BEEmitter {
    public static final @NonNull TimeValue DEFAULT_EMIT_TIME = TimeValue.buildSeconds(30);

    private final @NonNull Context _context;
    private final @NonNull BDAdapter _adapter;
    private final @NonNull TimeValue _time;
    private final @NonNull AtomicBoolean _active = new AtomicBoolean();

    private final @NonNull SafeMutableArray<SimpleCallback> _endCompletions = new SafeMutableArray<>();

    public BDEmitter(@NonNull Context context, @NonNull BDAdapter adapter, @NonNull TimeValue time) {
        _context = context;
        _adapter = adapter;
        _time = time;
    }

    // # BEEmitter

    @Override
    public boolean isBluetoothOn() {
        return _adapter.isAvailable();
    }

    @Override
    public boolean isEmitting() {
        return _active.get();
    }

    @Override
    public @NonNull TimeValue getTime() {
        return _time;
    }

    @Override
    public void start() throws Exception {
        if (!_adapter.isAvailable()) {
            Errors.throwUnsupportedOperation("Bluetooth unavailable");
        }

        if (_active.getAndSet(true)) {
            return;
        }

        Logger.message(this, "Discovering...");

        startListeningForBroadcastEvent();

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, getTime().inSeconds());
        _context.startActivity(discoverableIntent);
    }

    @Override
    public void stop() throws Exception {
        // Errors.throwUnsupportedOperation("Cannot stop");
    }

    @Override
    public void addEndCompletion(@NonNull SimpleCallback completion) {
        _endCompletions.add(completion);
    }

    // # BroadcastReceiver

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action == null) {
            return;
        }

        if (!action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
            return;
        }

        int scanMode = intent.getIntExtra(EXTRA_SCAN_MODE, 0);

        if (scanMode == BluetoothAdapter.SCAN_MODE_NONE || scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
            Logger.message(this, "Discovering ended!");

            _active.set(false);

            stopListeningForBroadcastEvent();
            performAndClearEndCompletions();
        }
    }

    // # Internals

    private void startListeningForBroadcastEvent() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        _context.registerReceiver(this, filter);
    }

    private void stopListeningForBroadcastEvent() {
        _context.unregisterReceiver(this);
    }

    private void performAndClearEndCompletions() {
        List<SimpleCallback> completions = _endCompletions.copyData();

        _endCompletions.removeAll();

        for (SimpleCallback c : completions) {
            c.perform();
        }
    }
}
