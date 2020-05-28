package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.Callback;

import java.util.UUID;

/// Opens server or client sockets when pairing with another bluetooth device.
public interface BEConnector {
    interface Server extends BEConnector {
        void start(@NonNull Callback<BESocket> success, @NonNull Callback<Exception> failure) throws Exception, BEError;
        void stop();
    }

    interface Client extends BEConnector {
        @NonNull BEClient getServer();

        void connect(@NonNull Callback<BESocket> success, @NonNull Callback<Exception> failure) throws Exception, BEError;
        void stop();
    }
}
