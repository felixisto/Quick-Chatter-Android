package com.office.quickchatter.network.bluetooth.bluedroid.other;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.StreamBandwidth;
import com.office.quickchatter.utilities.DataSize;
import com.office.quickchatter.utilities.TimeValue;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class BDStandardReadWriteBandwidth implements StreamBandwidth.Tracker.Read, StreamBandwidth.Tracker.Write, StreamBandwidth.Boostable {
    public static final @NonNull DataSize DEFAULT_FLUSH_RATE = DataSize.buildBytes(128);
    public static final @NonNull TimeValue DEFAULT_FORCE_FLUSH_TIME = TimeValue.buildSeconds(1);

    private final @NonNull Object lock = new Object();

    public final @NonNull DataSize _flushDataRate;
    public final @NonNull TimeValue _forceFlushTime;
    private final @NonNull AtomicReference<DataSize> _currentFlushDataRate = new AtomicReference<DataSize>();
    private final @NonNull AtomicReference<Double> _maxRateBoost = new AtomicReference<>(1.0);

    public BDStandardReadWriteBandwidth() {
        this(DEFAULT_FLUSH_RATE, DEFAULT_FORCE_FLUSH_TIME);
    }

    public BDStandardReadWriteBandwidth(@NonNull DataSize flushDataRate) {
        this(flushDataRate, DEFAULT_FORCE_FLUSH_TIME);
    }

    public BDStandardReadWriteBandwidth(@NonNull DataSize flushDataRate, @NonNull TimeValue forceFlushTime) {
        _flushDataRate = flushDataRate;
        _forceFlushTime = forceFlushTime;
        _currentFlushDataRate.set(_flushDataRate);
    }

    // # StreamBandwidth.Tracker.Read

    @Override
    public @NonNull DataSize getFlushDataRate() {
        return _currentFlushDataRate.get();
    }

    @Override
    public @NonNull TimeValue getForceFlushTime() {
        return _forceFlushTime;
    }

    @Override
    public void read(int length) {
        process(length);
    }

    // # StreamBandwidth.Tracker.Write

    @Override
    public void write(int length) {
        process(length);
    }

    // # StreamBandwidth.Boostable

    @Override
    public void boostFlushRate(double multiplier) {
        synchronized (lock) {
            if (multiplier >= 0) {
                _maxRateBoost.set(multiplier);
                _currentFlushDataRate.set(DataSize.buildBytes((int)(_flushDataRate.inBytes() * _maxRateBoost.get())));
            }
        }
    }

    @Override
    public void revertBoost() {
        _maxRateBoost.set(1.0);
    }

    // # Internals

    private void updateCurrentMaxRate() {
        process(0);
    }

    private synchronized void process(int length) {

    }
}

class ProcessEntry {
    @NonNull Date date;
    int value;

    ProcessEntry(@NonNull Date date, int value) {
        this.date = date;
        this.value = value;
    }
}
