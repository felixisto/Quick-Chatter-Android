package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.basic.PingStatusChecker;
import com.office.quickchatter.network.basic.TransmissionMessage;
import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.network.bluetooth.bluedroid.BDConstants;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.TimeValue;
import com.office.quickchatter.utilities.Timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/// Transmits ping messages when no data is sent.
public class BDTransmitterPinger extends BDTransmitterPerformer implements PingStatusChecker {
    private final @NonNull TimeValue _delay;

    private final @NonNull BDTransmissionMessagePartBuilder _builder;

    // Used to tell if other side is sending us data.
    private final @NonNull AtomicLong _numberOfBytesRead = new AtomicLong(0);

    // Used to tell if we are sending data. If no data is sent, a ping message will be build and transmitted.
    private final @NonNull AtomicLong _numberOfBytesWritten = new AtomicLong(0);

    private final @NonNull AtomicReference<Timer> _lastReceivedPingTimer = new AtomicReference<>();

    private final @NonNull AtomicReference<Timer> _lastSentPingTimer = new AtomicReference<>();

    public BDTransmitterPinger(@NonNull BDTransmissionLine line, @NonNull TimeValue delay, @Nullable BDTransmitterPerformerDelegate delegate) {
        super(line, delegate);

        _delay = delay;

        _builder = new BDTransmissionMessagePartBuilder(line.getType());

        refreshLastReceivedPing();
        refreshLastSentPing();
    }

    // # BDTransmitterPerformer

    @Override
    public void transmit(@NonNull byte[] bytes) {
        super.transmit(bytes);

        refreshLastSentPing();
    }

    @Override
    public @NonNull List<TransmissionMessagePart> buildMessagePartsFromBuffer(@NonNull byte[] bytes) {
        ArrayList<TransmissionMessagePart> list = new ArrayList<>();

        if (!hasNewDataBeenTransmittedSinceLastUpdate()) {
            list.add(_builder.buildPing());
        }

        return list;
    }

    @Override
    public void readNewMessages(final @NonNull Callback<List<TransmissionMessage>> completion) {
        super.readNewMessages(new Callback<List<TransmissionMessage>>() {
            @Override
            public void perform(List<TransmissionMessage> messages) {
                if (hasNewPingArrivedSinceLastUpdate()) {
                    refreshLastReceivedPing();
                }

                completion.perform(messages);
            }
        });
    }

    public void refreshLastReceivedPing() {
        _lastReceivedPingTimer.set(new Timer(_delay, new Date()));
        _numberOfBytesRead.set(line.getInput().getNumberOfBytesTransmitted());
    }

    public void refreshLastSentPing() {
        _lastSentPingTimer.set(new Timer(_delay, new Date()));
        _numberOfBytesWritten.set(line.getOutput().getNumberOfBytesTransmitted());
    }

    // # PingStatusChecker

    @Override
    public @NonNull TimeValue timeElapsedSinceLastPing() {
        return _lastReceivedPingTimer.get().timeElapsedSinceNow();
    }

    @Override
    public boolean isConnectionTimeout() {
        return timeElapsedSinceLastPing().inMS() >= BDConstants.CONNECTION_TIMEOUT.inMS();
    }

    // # Internals

    private boolean hasNewPingArrivedSinceLastUpdate() {
        long bytesRead = line.getInput().getNumberOfBytesTransmitted();
        return _numberOfBytesRead.get() != bytesRead;
    }

    private boolean hasNewDataBeenTransmittedSinceLastUpdate() {
        long bytesWritten = line.getOutput().getNumberOfBytesTransmitted();
        return _numberOfBytesWritten.get() != bytesWritten;
    }
}
