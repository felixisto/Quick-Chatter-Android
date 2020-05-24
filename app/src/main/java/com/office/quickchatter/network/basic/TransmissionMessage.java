package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

public interface TransmissionMessage {
    @NonNull TransmissionType getType();

    @NonNull byte[] getBytes();

    int length();
}
