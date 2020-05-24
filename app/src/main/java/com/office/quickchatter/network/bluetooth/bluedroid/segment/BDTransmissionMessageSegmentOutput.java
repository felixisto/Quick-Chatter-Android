package com.office.quickchatter.network.bluetooth.bluedroid.segment;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.utilities.Logger;

/// Provides information about output segment data.
/// Information passed trough streams is broken into segments, this class helps reading those segments.
public class BDTransmissionMessageSegmentOutput {
    public final @NonNull byte[] bytes;

    private static final @NonNull byte[] EMPTY_BYTES = new byte[0];
    private static final byte CLASS_VALUE_START = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.CLASS_VALUE_START)[0];
    private static final byte CLASS_VALUE_DATA = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.CLASS_VALUE_DATA)[0];
    private static final byte CLASS_VALUE_END = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.CLASS_VALUE_END)[0];
    private static final byte CLASS_VALUE_PING = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.CLASS_VALUE_PING)[0];

    public static @NonNull BDTransmissionMessageSegmentOutput build(@NonNull TransmissionMessagePart message) {
        return new BDTransmissionMessageSegmentOutput(message);
    }

    BDTransmissionMessageSegmentOutput(@NonNull TransmissionMessagePart message) {
        byte[] bytesValue;

        if (message instanceof TransmissionMessagePart.Ping) {
            bytesValue = bytesFor((TransmissionMessagePart.Ping)message);
        } else if (message instanceof TransmissionMessagePart.Start) {
            bytesValue = bytesFor((TransmissionMessagePart.Start)message);
        } else if (message instanceof TransmissionMessagePart.Data) {
            bytesValue = bytesFor((TransmissionMessagePart.Data)message);
        } else if (message instanceof TransmissionMessagePart.End) {
            bytesValue = bytesFor((TransmissionMessagePart.End)message);
        } else {
            bytesValue = new byte[1];
        }

        this.bytes = bytesValue;
    }

    public static @NonNull byte[] bytesFor(@NonNull TransmissionMessagePart.Ping message) {
        byte[] headerValue = BDTransmissionMessageSegment.stringToBytes(message.getType().value);
        byte classValue = CLASS_VALUE_PING;
        return segmentFromValues(headerValue, classValue, EMPTY_BYTES, EMPTY_BYTES);
    }

    public static @NonNull byte[] bytesFor(@NonNull TransmissionMessagePart.Start message) {
        byte[] typeValue = BDTransmissionMessageSegment.stringToBytes(message.getType().value);
        byte classValue = CLASS_VALUE_START;
        byte[] headerValue = headerValueOfExpectedLength(message.expectedLength());
        return segmentFromValues(typeValue, classValue, headerValue, EMPTY_BYTES);
    }

    public static @NonNull byte[] bytesFor(@NonNull TransmissionMessagePart.Data message) {
        byte[] typeValue = BDTransmissionMessageSegment.stringToBytes(message.getType().value);
        byte classValue = CLASS_VALUE_DATA;
        byte[] headerValue = headerValueOfExpectedLength(message.getData().length);
        return segmentFromValues(typeValue, classValue, headerValue, message.getData());
    }

    public static @NonNull byte[] bytesFor(@NonNull TransmissionMessagePart.End message) {
        byte[] typeValue = BDTransmissionMessageSegment.stringToBytes(message.getType().value);
        byte classValue = CLASS_VALUE_END;
        return segmentFromValues(typeValue, classValue, EMPTY_BYTES, EMPTY_BYTES);
    }

    public static @NonNull byte[] segmentFromValues(@NonNull byte[] type, byte classValue, @NonNull byte[] headerValue, @NonNull byte[] value) {
        if (type.length != BDTransmissionMessageSegment.MESSAGE_TYPE_LENGTH) {
            Logger.warning("BDTransmissionMessageSegmentOutput", "Segment type does not equal to the fixed length!");
        }

        if (headerValue.length != 0 && headerValue.length != BDTransmissionMessageSegment.MESSAGE_HEADER_LENGTH) {
            Logger.warning("BDTransmissionMessageSegmentOutput", "Segment header does not equal to the fixed length or 0!");
        }

        int total = type.length + 1 + headerValue.length + value.length;

        if (total == 0) {
            return new byte[0];
        }

        total += BDTransmissionMessageSegment.TOTAL_SEPARATOR_LENGTH;

        byte[] segment = new byte[total];

        byte[] startSeparator = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.MESSAGE_START_SEPARATOR);
        byte[] classStartSeparator = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.MESSAGE_CLASS_START_SEPARATOR);
        byte[] classEndSeparator = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.MESSAGE_CLASS_END_SEPARATOR);
        byte[] headerStartSeparator = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.MESSAGE_HEADER_START_SEPARATOR);
        byte[] headerEndSeparator = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.MESSAGE_HEADER_END_SEPARATOR);
        byte[] endSeparator = BDTransmissionMessageSegment.stringToBytes(BDTransmissionMessageSegment.MESSAGE_END_SEPARATOR);

        int pos = 0;

        for (byte b : startSeparator) {
            segment[pos] = b;
            pos += 1;
        }

        for (byte b : type) {
            segment[pos] = b;
            pos += 1;
        }

        for (byte b : classStartSeparator) {
            segment[pos] = b;
            pos += 1;
        }

        segment[pos] = classValue;
        pos += 1;

        for (byte b : classEndSeparator) {
            segment[pos] = b;
            pos += 1;
        }

        for (byte b : headerStartSeparator) {
            segment[pos] = b;
            pos += 1;
        }

        for (byte b : headerValue) {
            segment[pos] = b;
            pos += 1;
        }

        for (byte b : headerEndSeparator) {
            segment[pos] = b;
            pos += 1;
        }

        for (byte b : value) {
            segment[pos] = b;
            pos += 1;
        }

        for (byte b : endSeparator) {
            segment[pos] = b;
            pos += 1;
        }

        return segment;
    }

    public static @NonNull byte[] headerValueOfExpectedLength(int length) {
        String value = headerValueOf(String.valueOf(length + ".0"), "0");
        return BDTransmissionMessageSegment.stringToBytes(value);
    }

    // Builds a valid header value - adds additional padding if necessary.
    public static @NonNull String headerValueOf(@NonNull String value, @NonNull String padding) {
        if (value.isEmpty()) {
            return "";
        }

        while (value.length() < BDTransmissionMessageSegment.MESSAGE_HEADER_LENGTH) {
            value = value.concat(padding);
        }

        while (value.length() > BDTransmissionMessageSegment.MESSAGE_HEADER_LENGTH) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }
}
