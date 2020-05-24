package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.TimeValue;

public interface PingStatusChecker {
    @NonNull TimeValue timeElapsedSinceLastPing();
    boolean isConnectionTimeout();
}
