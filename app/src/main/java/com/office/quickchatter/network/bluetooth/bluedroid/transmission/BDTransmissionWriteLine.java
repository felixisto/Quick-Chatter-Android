package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.bluedroid.segment.BDTransmissionMessageSegment;
import com.office.quickchatter.network.bluetooth.bluedroid.segment.BDTransmissionMessageSegmentOutput;
import com.office.quickchatter.network.basic.StreamBandwidth;
import com.office.quickchatter.network.basic.TransmissionLine;
import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.network.basic.TransmissionWriteStream;
import com.office.quickchatter.utilities.Logger;

import java.util.List;

/// Writes data to a TransmissionWriteStream.
public class BDTransmissionWriteLine implements TransmissionLine.Output {
    public static final boolean DEBUG_LOG = true;

    public final @NonNull TransmissionType type;

    private final @NonNull TransmissionWriteStream _stream;

    public static @NonNull BDTransmissionWriteLine build(@NonNull TransmissionWriteStream stream, @NonNull TransmissionType type) {
        return new BDTransmissionWriteLine(stream, type);
    }

    BDTransmissionWriteLine(@NonNull TransmissionWriteStream stream, @NonNull TransmissionType type) {
        this.type = type;
        this._stream = stream;
    }

    // # TransmissionLine.Output

    @Override
    public void close() {
        _stream.close();
    }

    @Override
    public @NonNull TransmissionType getType() {
        return type;
    }

    @Override
    public long getNumberOfBytesTransmitted() {
        return _stream.getTotalBytesWritten();
    }

    @Override
    public @NonNull StreamBandwidth getWriteBandwidth() {
        return _stream.getBandwidth();
    }

    @Override
    public void writeMessages(@NonNull List<TransmissionMessagePart> messages) throws Exception {
        for (int e = 0; e < messages.size(); e++) {
            TransmissionMessagePart message = messages.get(e);
            byte[] data = BDTransmissionMessageSegmentOutput.build(message).bytes;

            if (DEBUG_LOG) {
                if (data.length <= 64) {
                    log("Writing: " + BDTransmissionMessageSegment.bytesToString(data));
                } else {
                    log("Writing more than 64 bytes");
                }
            }

            _stream.write(data);
        }
    }

    // # Internals

    void log(@NonNull String message) {
        if (!DEBUG_LOG) {
            return;
        }

        Logger.message(this, message);
    }
}

