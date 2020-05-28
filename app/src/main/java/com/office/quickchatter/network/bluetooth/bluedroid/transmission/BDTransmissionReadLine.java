package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.bluedroid.segment.BDTransmissionMessageSegment;
import com.office.quickchatter.network.bluetooth.bluedroid.segment.BDTransmissionMessageSegmentInput;
import com.office.quickchatter.network.basic.StreamBandwidth;
import com.office.quickchatter.network.basic.TransmissionLine;
import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.network.basic.TransmissionReadStream;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.SafeMutableArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/// Reads data given by a TransmissionReadStream.
/// Information is encapsulated by TransmissionMessagePart.
public class BDTransmissionReadLine implements TransmissionLine.Input {
    public static final boolean DEBUG_LOG = true;

    private final @NonNull Object lock = new Object();

    public final @NonNull TransmissionType type;

    private final @NonNull TransmissionReadStream _stream;

    private final @NonNull SafeMutableArray<TransmissionMessagePart> _currentSegmentParts = new SafeMutableArray<>();
    private final @NonNull AtomicReference<TransmissionMessagePart.Start> _currentSegmentStart = new AtomicReference<>();

    public static @NonNull BDTransmissionReadLine build(@NonNull TransmissionReadStream stream, @NonNull TransmissionType type) {
        return new BDTransmissionReadLine(stream, type);
    }

    BDTransmissionReadLine(@NonNull TransmissionReadStream stream, @NonNull TransmissionType type) {
        this.type = type;
        _stream = stream;
    }

    // # TransmissionLine.Input

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
        return _stream.getTotalBytesRead();
    }

    @Override
    public @NonNull StreamBandwidth getReadBandwidth() {
        return _stream.getBandwidth();
    }

    @Override
    public int currentReadingSegmentExpectedLength() {
        TransmissionMessagePart.Start start = _currentSegmentStart.get();

        return start != null ? start.expectedLength() : 0;
    }

    @Override
    public void readNewData() throws Exception {
        synchronized (lock) {
            _stream.read();
            read();
        }
    }

    @Override
    public @NonNull List<TransmissionMessagePart> readBuffer() {
        return _currentSegmentParts.copyData();
    }

    @Override
    public @NonNull List<TransmissionMessagePart> clearDataUntilFinalEndPart() {
        List<TransmissionMessagePart> parts;
        ArrayList<TransmissionMessagePart> leftoverParts = new ArrayList<>();

        synchronized (lock) {
            parts = _currentSegmentParts.copyData();

            int indexOfFinalEndPart = -1;

            for (int e = parts.size() - 1; e >= 0; e--) {
                TransmissionMessagePart part = parts.get(e);

                if (part instanceof TransmissionMessagePart.End || part instanceof TransmissionMessagePart.Ping) {
                    indexOfFinalEndPart = e;
                    break;
                }
            }

            if (indexOfFinalEndPart >= 0) {
                for (int e = indexOfFinalEndPart + 1; e < parts.size(); e++) {
                    leftoverParts.add(parts.get(e));
                }

                _currentSegmentParts.removeAll();
                _currentSegmentParts.addAll(leftoverParts);
            }
        }

        return parts;
    }

    // # Internals

    private void read() {
        // Try to create as many messages as possible.
        byte[] bytes = _stream.getBuffer();

        if (DEBUG_LOG) {
            if (bytes.length <= 256) {
                log("Reading: " + BDTransmissionMessageSegment.bytesToString(bytes));
            } else {
                log("Reading more than 64 bytes");
            }
        }

        while (bytes.length > 0) {
            BDTransmissionMessageSegmentInput streamData = BDTransmissionMessageSegmentInput.build(bytes);

            if (!streamData.firstSegment().isValid()) {
                log("Read data does not contain a full data segment, yet. Waiting for more data to arrive.");
                break;
            }

            int segmentLength = streamData.firstSegment().length();
            int partsCount = estimatedCurrentSegmentTotalCount();
            int partIndex = _currentSegmentParts.size();

            log("Reading data segment with length " + segmentLength + " bytes");

            try {
                TransmissionMessagePart part = streamData.buildFirstPart(getType(), partIndex, partsCount);

                // Handle success build - part may be null
                if (part != null) {
                    _stream.clearBufferUntilEndIndex(segmentLength);
                    bytes = _stream.getBuffer();

                    _currentSegmentParts.add(part);

                    if (part instanceof TransmissionMessagePart.Ping) {
                        return;
                    }

                    if (part instanceof TransmissionMessagePart.Start) {
                        _currentSegmentStart.set((TransmissionMessagePart.Start) part);
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                // Handle failure build - corrupted data
                Logger.error(this, "Failed to build stream data, probably corrupted, error: " + e);

                // There is no point of keeping the current buffer, discard it and move to next segment
                _stream.clearBufferUntilEndIndex(segmentLength);

                // We could loop again, but to be safe, interrupt instead
                break;
            }
        }
    }

    private int maxChunkSizeSegment() {
        return BDTransmissionMessageSegment.MESSAGE_CHUNK_MAX_SIZE;
    }

    private int estimatedCurrentSegmentTotalCount() {
        int expectedLength = currentReadingSegmentExpectedLength();
        int chunkSize = maxChunkSizeSegment();

        if (expectedLength <= 0 || chunkSize <= 0) {
            return 0;
        }

        if (expectedLength < chunkSize) {
            return 3;
        }

        return expectedLength / chunkSize;
    }

    void log(@NonNull String message) {
        if (!DEBUG_LOG) {
            return;
        }

        Logger.message(this, message);
    }
}
