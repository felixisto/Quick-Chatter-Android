package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.TransmissionMessage;
import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.network.basic.TransmissionType;

/// Full transmission message.
public class BDTransmissionMessage implements TransmissionMessage {
    public static final @NonNull byte[] EMPTY_BYTES = new byte[0];

    public final @NonNull
    TransmissionType type;
    public final @NonNull byte[] bytes;

    public BDTransmissionMessage(@NonNull TransmissionType type) {
        this(type, EMPTY_BYTES);
    }

    public BDTransmissionMessage(@NonNull TransmissionType type, @NonNull byte[] bytes) {
        this.type = type;
        this.bytes = bytes;
    }

    public BDTransmissionMessage(@NonNull TransmissionMessagePart.Data message) {
        this(message.getType(), message.getData());
    }

    @Override
    public @NonNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public @NonNull byte[] getBytes() {
        return bytes;
    }

    @Override
    public int length() {
        return bytes.length;
    }
}
