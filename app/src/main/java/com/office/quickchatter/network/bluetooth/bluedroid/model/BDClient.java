package com.office.quickchatter.network.bluetooth.bluedroid.model;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEClientDevice;
import com.office.quickchatter.network.bluetooth.basic.BEPairing;

public class BDClient implements BEClient, BEPairing.Entity {
    public final int identifier;
    public final @NonNull BEClientDevice device;
    public final @NonNull String name;

    public BDClient(@NonNull BEClientDevice device) {
        String name = device.getName();

        this.identifier = name.hashCode();
        this.device = device;
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BDClient) {
            return hashCode() == other.hashCode();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getIdentifier();
    }

    // # BEClient

    @Override
    public int getIdentifier() {
        return identifier;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @NonNull BEClientDevice getDevice() {
        return device;
    }

    // # BEPairing.Entity

    @Override
    public @NonNull BEClient getClient() {
        return this;
    }
}
