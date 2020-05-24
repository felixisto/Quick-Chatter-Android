package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

public interface Scanner {
    boolean isRunning();

    void start() throws Exception;
    void stop() throws Exception;

    @NonNull ScannerConfiguration getConfiguration();
    void setConfiguration(@NonNull ScannerConfiguration configuration) throws Exception;
}
