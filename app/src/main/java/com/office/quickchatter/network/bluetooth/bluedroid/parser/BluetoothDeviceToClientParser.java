package com.office.quickchatter.network.bluetooth.bluedroid.parser;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.bluedroid.model.BDClient;
import com.office.quickchatter.network.bluetooth.bluedroid.model.BDClientDeviceInfo;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Parser;

public class BluetoothDeviceToClientParser implements Parser<BluetoothDevice, BEClient> {
    @Override
    public @NonNull BEClient parse(@NonNull BluetoothDevice data) throws Exception {
        if (data.getName() == null) {
            Errors.throwInvalidArgument("Bluetooth device needs to have a non null name");
        }

        return new BDClient(new BDClientDeviceInfo(data));
    }
}
