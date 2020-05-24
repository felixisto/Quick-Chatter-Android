package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.Copyable;
import com.office.quickchatter.utilities.TimeValue;

public class ScannerConfiguration implements Copyable<ScannerConfiguration> {
    public boolean retryForever = false;
    public int retryCount = 3;
    public @NonNull TimeValue retryDelay = TimeValue.buildSeconds(5);

    @Override
    public ScannerConfiguration copy() {
        ScannerConfiguration conf = new ScannerConfiguration();

        conf.retryForever = retryForever;
        conf.retryCount = retryCount;
        conf.retryDelay = TimeValue.copy(retryDelay);

        return conf;
    }
}
