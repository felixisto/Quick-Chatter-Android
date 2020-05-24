package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

public class BEError extends Exception {
    public final @NonNull Value value;

    public BEError(@NonNull Value value) {
        this.value = value;
    }

    @Override
    public @NonNull String toString() {
        return value.toString();
    }

    public enum Value {
        bluetoothUnsupported("Bluetooth unsupported"),
        bluetoothDisabled("Bluetooth is disabled"),
        alreadyRunning("Already running"),
        alreadyStopped("Already stopped"),
        cannotConfigurateWhileRunning("Cannot configurate while running"),
        corruptedStreamData("Corrupted stream data"),
        corruptedStreamDataHeader("Corrupted stream data - header"),
        corruptedStreamDataType("Corrupted stream data - type");

        private final String text;

        Value(final String text) {
            this.text = text;
        }

        @Override
        public @NonNull String toString() {
            return text;
        }
    }
}
