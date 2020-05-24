package com.office.quickchatter.network.bluetooth.bluedroid;

import android.bluetooth.BluetoothAdapter;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BEAdapter;

public class BDAdapter implements BEAdapter<BluetoothAdapter> {
    private final @Nullable BluetoothAdapter _adapter;

    public BDAdapter(@Nullable BluetoothAdapter adapter) {
        _adapter = adapter;
    }

    // # BDAdapter

    @Override
    public boolean isAvailable() {
        return _adapter != null;
    }

    public BluetoothAdapter getAdapter() {
        return _adapter;
    }
}
