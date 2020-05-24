package com.office.quickchatter.network.bluetooth.bluedroid.model;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class BDDiscoveredClient {
    public final @NonNull BEClient client;
    private final @NonNull AtomicReference<Date> _dateFound = new AtomicReference<>();

    public BDDiscoveredClient(@NonNull BEClient client, @NonNull Date dateFound) {
        this.client = client;
        this._dateFound.set(dateFound);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BDDiscoveredClient) {
            return hashCode() == other.hashCode();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return client.getIdentifier();
    }

    public @NonNull Date getDateFound() {
        return _dateFound.get();
    }

    public void updateDateFound(@NonNull Date dateFound) {
        _dateFound.set(dateFound);
    }
}
