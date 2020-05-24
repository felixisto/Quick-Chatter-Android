package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.Errors;

public class TransmissionType {
    public final @NonNull String value;

    public TransmissionType(@NonNull String value) throws Exception {
        if (value.isEmpty()) {
            Errors.throwInvalidArgument("Type cannot be empty");
        }

        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TransmissionType) {
            return hashCode() == other.hashCode();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
