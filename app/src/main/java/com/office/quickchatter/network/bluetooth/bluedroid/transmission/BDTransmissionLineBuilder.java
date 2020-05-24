package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.TransmissionReadStream;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.network.basic.TransmissionWriteStream;
import com.office.quickchatter.network.bluetooth.bluedroid.BDConstants;

public class BDTransmissionLineBuilder {
    public final @NonNull
    TransmissionReadStream input;
    public final @NonNull
    TransmissionWriteStream output;

    public BDTransmissionLineBuilder(@NonNull TransmissionReadStream input, @NonNull TransmissionWriteStream output) {
        this.input = input;
        this.output = output;
    }

    public @NonNull BDTransmissionLine build(@NonNull TransmissionType type) {
        return new BDTransmissionLine(input, output, type);
    }

    public @NonNull BDTransmissionLine buildPing() {
        return new BDTransmissionLine(input, output, BDConstants.getShared().TYPE_PING);
    }
}
