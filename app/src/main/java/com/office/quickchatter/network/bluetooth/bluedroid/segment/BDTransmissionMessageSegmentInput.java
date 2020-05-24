package com.office.quickchatter.network.bluetooth.bluedroid.segment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BEError;
import com.office.quickchatter.network.bluetooth.bluedroid.transmission.BDTransmissionMessagePartBuilder;
import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.network.basic.TransmissionType;

/// Provides information about input segment data.
/// Information passed trough streams is broken into segments, this class helps reading those segments.
public class BDTransmissionMessageSegmentInput {
    public final @NonNull byte[] data;
    public final @NonNull String dataAsString;

    public final @NonNull BDTransmissionMessageSegment segment;

    public static @NonNull BDTransmissionMessageSegmentInput build(@NonNull byte[] data) {
        return new BDTransmissionMessageSegmentInput(data);
    }

    BDTransmissionMessageSegmentInput(@NonNull byte[] data) {
        this.data = data;
        this.dataAsString = BDTransmissionMessageSegment.bytesToString(data);
        this.segment = BDTransmissionMessageSegment.build(data);
    }

    public @NonNull BDTransmissionMessageSegment firstSegment() {
        return segment;
    }

    public @Nullable
    TransmissionMessagePart buildFirstPart(@NonNull TransmissionType expectedType, int partIndex, int partsCount) throws BEError {
        int startIndex = segment.startIndex();
        int endIndex = segment.endIndex();

        // Check for corruption
        if (startIndex < 0 || endIndex < 0) {
            return null;
        }

        TransmissionType type = segment.getType();

        // Check for corruption
        if (type == null) {
            throw new BEError(BEError.Value.corruptedStreamDataType);
        }

        if (!type.equals(expectedType)) {
            return null;
        }

        BDTransmissionMessagePartBuilder builder = new BDTransmissionMessagePartBuilder(type);

        // > Ping
        if (type.equals(BDTransmissionMessagePartBuilder.PING_TYPE)) {
            return builder.buildPing();
        }

        if (segment.isStartClass()) {
            return builder.buildStart(segment.headerValueAsSizeValue(), partsCount);
        }

        if (segment.isDataClass()) {
            return builder.buildData(segment.value(), partsCount, partIndex);
        }

        if (segment.isEndClass()) {
            return builder.buildEnd(partsCount);
        }

        throw new BEError(BEError.Value.corruptedStreamData);
    }
}
