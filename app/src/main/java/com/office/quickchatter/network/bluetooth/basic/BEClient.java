package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

/// Supports equals() and hashCode().
public interface BEClient {
    int getIdentifier();

    @NonNull String getName();

    @NonNull BEClientDevice getDevice();
}
