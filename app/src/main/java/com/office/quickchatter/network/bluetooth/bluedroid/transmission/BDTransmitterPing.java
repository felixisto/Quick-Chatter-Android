package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.Copyable;
import com.office.quickchatter.utilities.TimeValue;

import java.util.concurrent.atomic.AtomicBoolean;

public class BDTransmitterPing implements Copyable<BDTransmitterPing> {
    public final @NonNull TimeValue delay;

    private final @NonNull AtomicBoolean _active = new AtomicBoolean(true);

    public BDTransmitterPing(@NonNull TimeValue delay) {
        this.delay = delay;
    }

    public boolean isActive() {
        return _active.get();
    }

    public void setActive(boolean active) {
        _active.set(active);
    }

    // # Copyable

    @Override
    public BDTransmitterPing copy() {
        BDTransmitterPing ping = new BDTransmitterPing(delay);
        ping._active.set(_active.get());
        return ping;
    }
}
