package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

import java.util.List;

public interface BEPairing {
    interface Entity {
        @NonNull BEClient getClient();
    }

    interface Database {
        @NonNull List<BEPairing.Entity> getKnownPairedClients();
    }
}
