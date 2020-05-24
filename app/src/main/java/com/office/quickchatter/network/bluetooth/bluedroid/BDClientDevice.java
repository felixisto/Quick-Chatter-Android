package com.office.quickchatter.network.bluetooth.bluedroid;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClientDevice;

public interface BDClientDevice extends BEClientDevice {
    @NonNull BluetoothDevice asBluetoothDevice();
}
