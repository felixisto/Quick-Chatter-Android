package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.Transmitter;

public interface BETransmitter {
    @NonNull BESocket getSocket();

    interface ReaderWriter extends BETransmitter, Transmitter.ReaderWriter {

    }

    interface Service extends BETransmitter, Transmitter.Service, Transmitter.Pinger {

    }
}
