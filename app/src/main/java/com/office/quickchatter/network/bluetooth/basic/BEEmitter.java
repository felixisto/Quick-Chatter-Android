package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.SimpleCallback;
import com.office.quickchatter.utilities.TimeValue;

/// Begins emitting the presence of the user's device.
public interface BEEmitter {
    boolean isBluetoothOn();

    boolean isEmitting();

    @NonNull TimeValue getTime();

    void start() throws Exception;

    // Note: not all emitters support this.
    void stop() throws Exception;

    void addEndCompletion(@NonNull SimpleCallback completion);
}
