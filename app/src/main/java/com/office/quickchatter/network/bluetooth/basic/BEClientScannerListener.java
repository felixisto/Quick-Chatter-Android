package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

public interface BEClientScannerListener {
    void onScanStart();
    void onScanRestart();
    void onScanEnd();

    void onClientFound(@NonNull BEClient client);
    void onClientUpdate(@NonNull BEClient client);
    void onClientLost(@NonNull BEClient client);
}
