package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.bluedroid.segment.BDTransmissionMessageSegment;
import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.network.bluetooth.bluedroid.BDConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// Builds message parts from bytes.
public class BDTransmissionMessagePartBuilder {
    public static final @NonNull
    TransmissionType PING_TYPE = BDConstants.getShared().TYPE_PING;

    public final @NonNull
    TransmissionType type;

    public BDTransmissionMessagePartBuilder(@NonNull TransmissionType type) {
        this.type = type;
    }

    public @NonNull TransmissionMessagePart.Ping buildPing() {
        return new BDTransmissionMessagePartStandardPing(PING_TYPE);
    }

    public @NonNull TransmissionMessagePart.Start buildStart(int expectedLength, int partsCount) {
        return new BDTransmissionMessagePartStandardStart(type, expectedLength, partsCount);
    }

    public @NonNull TransmissionMessagePart.Data buildData(@NonNull byte[] data, int partIndex, int partsCount) {
        return new BDTransmissionMessagePartStandardData(type, data, partsCount, partIndex);
    }

    public @NonNull TransmissionMessagePart.Data buildDataFromList(@NonNull List<TransmissionMessagePart.Data> data) {
        ArrayList<byte[]> items = new ArrayList<>();

        for (TransmissionMessagePart.Data item : data) {
            items.add(item.getData());
        }

        return buildDataFromBytesList(items);
    }

    public @NonNull TransmissionMessagePart.Data buildDataFromBytesList(@NonNull List<byte[]> data) {
        int length = 0;

        for (byte[] element : data) {
            length += element.length;
        }

        if (length == 0) {
            return buildData(new byte[0], 0, 0);
        }

        byte[] allBytes = new byte[0];

        for (byte[] element : data) {
            allBytes = addAllBytes(allBytes, element);
        }

        return new BDTransmissionMessagePartStandardData(type, allBytes, 1, 0);
    }

    public @NonNull TransmissionMessagePart.End buildEnd(int partsCount) {
        return new BDTransmissionMessagePartStandardEnd(type, partsCount);
    }

    public @NonNull List<TransmissionMessagePart> buildAllMessagePartsFromBuffer(@NonNull byte[] bytes) {
        int chunkSize = BDTransmissionMessageSegment.MESSAGE_CHUNK_MAX_SIZE;

        int length = bytes.length;

        int partsCount = length / chunkSize;

        ArrayList<TransmissionMessagePart> messages = new ArrayList<>();

        int position = 0;

        // First and middle chunks
        while (position + chunkSize < length) {
            int next = position + chunkSize;

            TransmissionMessagePart data = buildData(Arrays.copyOfRange(bytes, position, next), messages.size(), partsCount);
            messages.add(data);

            position = next;
        }

        // Last data chunk
        TransmissionMessagePart data = buildData(Arrays.copyOfRange(bytes, position, length), messages.size(), partsCount);
        messages.add(data);

        // Append end
        TransmissionMessagePart start = buildStart(length, partsCount);
        TransmissionMessagePart end = buildEnd(partsCount);
        messages.add(0, start);
        messages.add(end);

        return messages;
    }

    private static byte[] addAllBytes(final byte[] a, byte[] b) {
        byte[] joinedArray = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, joinedArray, a.length, b.length);
        return joinedArray;
    }
}

class BDTransmissionMessagePartStandardPing implements TransmissionMessagePart.Ping {
    final @NonNull
    TransmissionType type;

    public BDTransmissionMessagePartStandardPing(@NonNull TransmissionType type) {
        this.type = type;
    }

    // # TransmissionMessagePart.Ping

    @Override
    public @NonNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public int partIndex() {
        return 0;
    }

    @Override
    public int partsCount() {
        return 0;
    }
}

class BDTransmissionMessagePartStandardStart implements TransmissionMessagePart.Start {
    final @NonNull
    TransmissionType type;
    final int expectedLength;
    final int partsCount;

    public BDTransmissionMessagePartStandardStart(@NonNull TransmissionType type, int expectedLength, int partsCount) {
        this.type = type;
        this.expectedLength = expectedLength;
        this.partsCount = partsCount;
    }

    // # TransmissionMessagePart.Start

    @Override
    public @NonNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public int expectedLength() {
        return expectedLength;
    }

    @Override
    public int partIndex() {
        return 0;
    }

    @Override
    public int partsCount() {
        return partsCount;
    }
}

class BDTransmissionMessagePartStandardData implements TransmissionMessagePart.Data {
    final @NonNull
    TransmissionType type;
    final @NonNull byte[] data;
    final int partsCount;
    final int partIndex;

    public BDTransmissionMessagePartStandardData(@NonNull TransmissionType type, @NonNull byte[] data, int partIndex, int partsCount) {
        this.type = type;
        this.data = data;
        this.partsCount = partsCount;
        this.partIndex = partIndex;
    }

    // # TransmissionMessagePart.Data

    @Override
    public @NonNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public @NonNull byte[] getData() {
        return data;
    }

    @Override
    public int partIndex() {
        return partIndex;
    }

    @Override
    public int partsCount() {
        return partsCount;
    }
}

class BDTransmissionMessagePartStandardEnd implements TransmissionMessagePart.End {
    final @NonNull
    TransmissionType type;
    final int partsCount;

    public BDTransmissionMessagePartStandardEnd(@NonNull TransmissionType type, int partsCount) {
        this.type = type;
        this.partsCount = partsCount;
    }

    // # TransmissionMessagePart.End

    @Override
    public @NonNull
    TransmissionType getType() {
        return type;
    }

    @Override
    public int partIndex() {
        return partsCount;
    }

    @Override
    public int partsCount() {
        return partsCount;
    }
}
