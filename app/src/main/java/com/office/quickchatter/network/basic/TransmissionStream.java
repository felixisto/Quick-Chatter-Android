package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

public interface TransmissionStream {
    void close();

    @NonNull StreamBandwidth getBandwidth();

    interface Read extends TransmissionStream {
        long getTotalBytesRead();

        void read();

        @NonNull byte[] getBuffer();
        void clearBufferUntilEndIndex(int endIndex);
    }

    interface Write extends TransmissionStream {
        long getTotalBytesWritten();

        void write(@NonNull byte[] bytes);
        void flush();
    }
}
