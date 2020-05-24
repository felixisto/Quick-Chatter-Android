package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

public interface TransmissionMessagePart {
    @NonNull TransmissionType getType();
    int partIndex();
    int partsCount();

    interface Ping extends TransmissionMessagePart {

    }

    interface Start extends TransmissionMessagePart {
        int expectedLength();
    }

    interface Data extends TransmissionMessagePart {
        @NonNull byte[] getData();
    }

    interface End extends TransmissionMessagePart {

    }
}
