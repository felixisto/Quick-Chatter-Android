package com.office.quickchatter.network.bluetooth.bluedroid.discovery;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEAdapter;
import com.office.quickchatter.network.bluetooth.basic.BEPairing;
import com.office.quickchatter.network.bluetooth.bluedroid.BDAdapter;
import com.office.quickchatter.network.bluetooth.bluedroid.parser.BluetoothDeviceToPairEntityParser;
import com.office.quickchatter.utilities.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BDPairing implements BEPairing.Database {
    private final @NonNull BDAdapter _adapter;
    private final @NonNull Parser<BluetoothDevice, BEPairing.Entity> _parser;

    public BDPairing(@NonNull BDAdapter adapter, @NonNull Parser<BluetoothDevice, BEPairing.Entity> parser) {
        _adapter = adapter;
        _parser = parser;
    }

    public BDPairing(@NonNull BDAdapter adapter) {
        this(adapter, new BluetoothDeviceToPairEntityParser());
    }

    // # BEPairing.Database

    @Override
    public List<BEPairing.Entity> getKnownPairedClients() {
        ArrayList<BEPairing.Entity> clients = new ArrayList<>();

        if (!_adapter.isAvailable()) {
            return clients;
        }

        BluetoothAdapter adapter = _adapter.getAdapter();
        Set<BluetoothDevice> devices = adapter.getBondedDevices();

        for (BluetoothDevice device : devices) {
            try {
                clients.add(_parser.parse(device));
            } catch (Exception e) {

            }
        }

        return clients;
    }
}
