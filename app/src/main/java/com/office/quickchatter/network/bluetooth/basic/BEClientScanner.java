package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.Scanner;

public interface BEClientScanner extends Scanner {
    void subscribe(@NonNull BEClientScannerListener listener);
    void unsubscribe(@NonNull BEClientScannerListener listener);
}
