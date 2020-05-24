package com.office.quickchatter.network.bluetooth.bluedroid;

import android.bluetooth.BluetoothSocket;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BESocket;

public class BDSocket implements BESocket<BluetoothSocket> {
    private final @NonNull BluetoothSocket _socket;

    public BDSocket(@NonNull BluetoothSocket socket) {
        _socket = socket;
    }

    // # BESocket

    @Override
    public void close() throws Exception {
        _socket.close();
    }

    @Override
    public @NonNull BluetoothSocket getSocket() {
        return _socket;
    }
}
