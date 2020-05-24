package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

import java.util.Date;

public interface BEBinding {
    @NonNull Date getDateBinded();

    boolean isClientMaster();
    @NonNull BEClient getClient();
}
