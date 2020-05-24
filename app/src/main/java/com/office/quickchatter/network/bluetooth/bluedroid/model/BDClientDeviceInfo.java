package com.office.quickchatter.network.bluetooth.bluedroid.model;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.bluedroid.BDClientDevice;

public class BDClientDeviceInfo implements BDClientDevice {
    private final @NonNull BluetoothDevice device;
    private final @NonNull String name;

    public BDClientDeviceInfo(@NonNull BluetoothDevice device) {
        this.device = device;
        this.name = device.getName();
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull BluetoothDevice asBluetoothDevice() {
        return device;
    }
}
