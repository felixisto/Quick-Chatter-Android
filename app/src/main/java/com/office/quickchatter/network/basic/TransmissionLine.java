package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

import java.util.List;

public interface TransmissionLine {
    void close();

    @NonNull TransmissionType getType();

    // Numbers of bytes read or written from/to stream.
    long getNumberOfBytesTransmitted();

    interface Input extends TransmissionLine {
        @NonNull StreamBandwidth getReadBandwidth();

        int currentReadingSegmentExpectedLength();

        // Read new data, if available.
        void readNewData() throws Exception;

        @NonNull List<TransmissionMessagePart> readBuffer();
        @NonNull List<TransmissionMessagePart> clearDataUntilFinalEndPart();
    }

    interface Output extends TransmissionLine {
        @NonNull StreamBandwidth getWriteBandwidth();

        void writeMessages(@NonNull List<TransmissionMessagePart> messages) throws Exception;
    }

    interface InputAndOutput extends TransmissionLine {
        @NonNull TransmissionLine.Input getInput();
        @NonNull TransmissionLine.Output getOutput();
    }
}
