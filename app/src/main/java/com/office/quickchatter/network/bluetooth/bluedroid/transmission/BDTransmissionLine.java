package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.StreamBandwidth;
import com.office.quickchatter.network.basic.TransmissionLine;
import com.office.quickchatter.network.basic.TransmissionReadStream;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.network.basic.TransmissionWriteStream;

/// Combines read and write transmission lines.
public class BDTransmissionLine implements TransmissionLine.InputAndOutput {
    public final @NonNull BDTransmissionReadLine read;
    public final @NonNull BDTransmissionWriteLine write;

    public @NonNull BDTransmissionLine build(@NonNull BDTransmissionReadLine read, @NonNull BDTransmissionWriteLine write) {
        return new BDTransmissionLine(read, write);
    }

    public BDTransmissionLine(@NonNull TransmissionReadStream input, @NonNull TransmissionWriteStream output, @NonNull TransmissionType type) {
        this.read = BDTransmissionReadLine.build(input, type);
        this.write = BDTransmissionWriteLine.build(output, type);
    }

    public BDTransmissionLine(@NonNull BDTransmissionReadLine read, @NonNull BDTransmissionWriteLine write) {
        this.read = read;
        this.write = write;
    }

    // # TransmissionLine.InputAndOutput

    @Override
    public @NonNull TransmissionType getType() {
        return this.read.getType();
    }

    @Override
    public long getNumberOfBytesTransmitted() {
        return read.getNumberOfBytesTransmitted() + write.getNumberOfBytesTransmitted();
    }

    public @NonNull StreamBandwidth getReadBandwidth() {
        return read.getReadBandwidth();
    }

    public @NonNull StreamBandwidth getWriteBandwidth() {
        return write.getWriteBandwidth();
    }

    @Override
    public void close() {
        read.close();
        write.close();
    }

    @Override
    public @NonNull Input getInput() {
        return this.read;
    }

    @Override
    public @NonNull Output getOutput() {
        return this.write;
    }
}
