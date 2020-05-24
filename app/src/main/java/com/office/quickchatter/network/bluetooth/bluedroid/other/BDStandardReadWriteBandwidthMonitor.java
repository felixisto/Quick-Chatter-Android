package com.office.quickchatter.network.bluetooth.bluedroid.other;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.StreamBandwidth;
import com.office.quickchatter.utilities.DataSize;
import com.office.quickchatter.utilities.SafeMutableArray;
import com.office.quickchatter.utilities.TimeValue;
import com.office.quickchatter.utilities.Timer;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class BDStandardReadWriteBandwidthMonitor extends BDStandardReadWriteBandwidth implements StreamBandwidth.Tracker.Monitor {
    public final int ESTIMATED_VALUES_CAPACITY = 100;
    public final @NonNull TimeValue DELAY = TimeValue.buildSeconds(30);

    private final @NonNull SafeMutableArray<ProcessEntry> _estimatedRateValues = new SafeMutableArray<>();
    private @NonNull AtomicReference<DataSize> _estimatedCurrentRate = new AtomicReference<>(DataSize.zero());

    public BDStandardReadWriteBandwidthMonitor() {
        this(DEFAULT_FLUSH_RATE, DEFAULT_FORCE_FLUSH_TIME);
    }

    public BDStandardReadWriteBandwidthMonitor(@NonNull DataSize flushDataRate) {
        this(flushDataRate, DEFAULT_FORCE_FLUSH_TIME);
    }

    public BDStandardReadWriteBandwidthMonitor(@NonNull DataSize flushDataRate, @NonNull TimeValue forceFlushTime) {
        super(flushDataRate, forceFlushTime);
    }

    // # BDStandardReadWriteBandwidth

    @Override
    public void read(int length) {
        super.read(length);
        updateEstimatedRate(length, new Date());
    }

    @Override
    public void write(int length) {
        super.write(length);
        updateEstimatedRate(length, new Date());
    }

    // # StreamBandwidth.Tracker.Monitor

    @Override
    public @NonNull DataSize getEstimatedCurrentRate() {
        return _estimatedCurrentRate.get();
    }

    // # Internals

    private synchronized void updateEstimatedRate(int length, @NonNull Date now) {
        nullifyOutdatedRates(now);
        updateNewEstimatedRate(length, now);

        _estimatedCurrentRate.set(estimatedRateFromCurrentValues());
    }

    private void nullifyOutdatedRates(@NonNull Date now) {
        for (ProcessEntry entry : _estimatedRateValues.copyData()) {
            if (new Timer(DELAY).timeElapsedSince(now).inMS() > DELAY.inMS()) {
                entry.value = 0;
            }
        }
    }

    private void updateNewEstimatedRate(int length, @NonNull Date now) {
        if (_estimatedRateValues.size() < ESTIMATED_VALUES_CAPACITY) {
            _estimatedRateValues.add(new ProcessEntry(now, length));
            return;
        }

        ProcessEntry newLastEntry = _estimatedRateValues.get(0);
        _estimatedRateValues.removeAt(0);
        _estimatedRateValues.add(newLastEntry);

        newLastEntry.date = now;
        newLastEntry.value = length;
    }

    private @NonNull DataSize estimatedRateFromCurrentValues() {
        int totalRate = 0;

        for (ProcessEntry entry : _estimatedRateValues.copyData()) {
            totalRate += entry.value;
        }

        return DataSize.buildBytes(totalRate / _estimatedRateValues.size());
    }
}
