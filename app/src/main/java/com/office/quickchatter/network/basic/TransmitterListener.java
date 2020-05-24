package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

public interface TransmitterListener {
    void onMessageReceived(@NonNull TransmissionType type, @NonNull TransmissionMessage message);
    void onMessageDataChunkReceived(@NonNull TransmissionType type, double progress);
    void onMessageDataChunkSent(@NonNull TransmissionType type, double progress);
    void onMessageFullySent(@NonNull TransmissionType type);
    void onMessageFailedOrCancelled(@NonNull TransmissionType type);
}
