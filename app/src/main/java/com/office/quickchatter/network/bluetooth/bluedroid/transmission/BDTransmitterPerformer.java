package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.basic.StreamBandwidth;
import com.office.quickchatter.network.basic.TransmissionMessage;
import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.CollectionUtilities;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperClient;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SimpleCallback;
import com.office.quickchatter.utilities.TimeValue;
import com.office.quickchatter.utilities.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/// Wraps the functionality of BDTransmissionLine, and additionally provides a queue system for sending messages.
/// Multiple messages can be send, which are transmitted one by one in the same order they were added.
/// The read functionality is extended, by providing construction process of the transmission messages.
public class BDTransmitterPerformer implements LooperClient {
    private final @NonNull Object lock = new Object();

    public final @NonNull BDTransmissionLine line;
    private final @Nullable BDTransmitterPerformerDelegate _delegate;

    private final @NonNull AtomicBoolean _readingActive = new AtomicBoolean(true);
    private final @NonNull AtomicBoolean _writingActive = new AtomicBoolean(true);

    private final @NonNull ArrayList<QueuedMessage> _queuedMessages = new ArrayList<>();

    private final @NonNull BDTransmissionMessagePartBuilder _partsBuilder;

    private final @NonNull Timer _resetTimer = new Timer(TimeValue.buildSeconds(1));

    public static @NonNull BDTransmitterPerformer build(@NonNull BDTransmissionLine line, BDTransmitterPerformerDelegate delegate) {
        return new BDTransmitterPerformer(line, delegate);
    }

    BDTransmitterPerformer(@NonNull BDTransmissionLine line, @Nullable BDTransmitterPerformerDelegate delegate) {
        this.line = line;
        this._partsBuilder = new BDTransmissionMessagePartBuilder(line.getType());
        this._delegate = delegate;

        subscribeToLooperService();
    }

    // # Properties

    public @NonNull TransmissionType getType() {
        return line.getType();
    }

    public @NonNull StreamBandwidth getReadBandwidth() {
        return line.getReadBandwidth();
    }

    public @NonNull StreamBandwidth getWriteBandwidth() {
        return line.getWriteBandwidth();
    }

    // # Operations

    public void stop() {
        synchronized (lock) {
            cancelAllMessages();
            stopReading();
            stopWriting();
            line.close();
            unsubscribeToLooperService();
        }
    }

    public boolean isWritingActive() {
        return _writingActive.get();
    }

    public void startWriting() {
        if (!_writingActive.getAndSet(true)) {
            if (!isReadingActive()) {
                subscribeToLooperService();
            }
        }
    }

    public void stopWriting() {
        if (_writingActive.getAndSet(true)) {
            if (!isReadingActive()) {
                unsubscribeToLooperService();
            }
        }
    }

    public boolean isReadingActive() {
        return _readingActive.get();
    }

    public void startReading() {
        if (!_readingActive.getAndSet(true)) {
            if (!isWritingActive()) {
                subscribeToLooperService();
            }
        }
    }

    public void stopReading() {
        if (_readingActive.getAndSet(true)) {
            if (!isWritingActive()) {
                unsubscribeToLooperService();
            }
        }
    }

    public @NonNull List<TransmissionMessagePart> buildMessagePartsFromBuffer(@NonNull byte[] bytes) {
        return _partsBuilder.buildAllMessagePartsFromBuffer(bytes);
    }

    public void transmit(@NonNull byte[] bytes) {
        synchronized (lock) {
            try {
                QueuedMessage message = new QueuedMessage(buildMessagePartsFromBuffer(bytes), bytes.length);
                _queuedMessages.add(message);
            } catch (Exception e) {

            }
        }
    }

    public void readNewMessages(final @NonNull Callback<List<TransmissionMessage>> completion) {
        LooperService.getShared().performInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                completion.perform(readNewDataAndAlertDelegate());
            }
        });
    }

    // # LooperClient

    @Override
    public void loop() {
        LooperService.getShared().performInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                synchronized (lock) {
                    updateReading();
                    updateWriting();
                }
            }
        });
    }

    // # Internals

    private void subscribeToLooperService() {
        LooperService.getShared().subscribe(this);
    }

    private void unsubscribeToLooperService() {
        LooperService.getShared().unsubscribe(this);
    }

    private void updateReading() {
        if (!isReadingActive()) {
            return;
        }

        if (_resetTimer.update()) {
            readNewMessages(new Callback<List<TransmissionMessage>>() {
                @Override
                public void perform(List<TransmissionMessage> argument) {

                }
            });
        }
    }

    private void updateWriting() {
        if (!isWritingActive()) {
            return;
        }

        transmitNextParts(_queuedMessages);

        List<QueuedMessage> queuedMessages = CollectionUtilities.copy(_queuedMessages);
        _queuedMessages.clear();
        _queuedMessages.addAll(clearFullySentMessages(queuedMessages));
    }

    private void transmitNextParts(@NonNull List<QueuedMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }

        try {
            NextParts nextParts = pickNextParts(messages);

            line.write.writeMessages(nextParts.messageParts);

            // Alert delegate
            if (_delegate != null) {
                for (QueuedMessagePart part : nextParts.parts) {
                    if (part.isEnd()) {
                        _delegate.onMessageFullySent(part.getType());
                    } else {
                        _delegate.onMessageDataChunkSent(part.getType(), part.getProgress());
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(this, "Failed to write message to line, error: " + e);
        }
    }

    private @NonNull NextParts pickNextParts(@NonNull List<QueuedMessage> messages) {
        ArrayList<QueuedMessagePart> parts = new ArrayList<>();
        ArrayList<TransmissionMessagePart> messageParts = new ArrayList<>();

        for (QueuedMessage message : messages) {
            int index = message.currentPartIndex;
            TransmissionMessagePart next = message.next();

            if (next != null) {
                parts.add(new QueuedMessagePart(next, index, message.getPartCount()));
                messageParts.add(next);
            }
        }

        return new NextParts(parts, messageParts);
    }

    private @NonNull List<QueuedMessage> clearFullySentMessages(@NonNull List<QueuedMessage> messages) {
        ArrayList<QueuedMessage> result = new ArrayList<>();

        for (QueuedMessage message : messages) {
            if (!message.isFullySent()) {
                result.add(message);
            }
        }

        return result;
    }

    private @NonNull List<TransmissionMessage> readNewDataAndAlertDelegate() {
        TransmissionType type = getType();

        try {
            // Update
            line.read.readNewData();

            List<TransmissionMessagePart> parts = line.read.clearDataUntilFinalEndPart();
            BuiltMessagesResult builtMessages = buildFullySentMessagesFromParts(parts);
            List<TransmissionMessage> messages = builtMessages.messages;

            if (_delegate != null) {
                // Data update
                if (parts.size() > 0) {
                    // Update the last part only
                    TransmissionMessagePart part = parts.get(parts.size()-1);

                    if (!(part instanceof TransmissionMessagePart.Ping)) {
                        double progress = 0;

                        if (part.partsCount() > 0) {
                            double index = part.partIndex();
                            double count = part.partsCount();
                            progress = index / count;
                        }

                        _delegate.onMessageDataChunkReceived(type, progress);
                    }
                }

                // Message failed or cancelled
                for (int e = 0; e < builtMessages.failedMessageCount; e++) {
                    _delegate.onMessageFailedOrCancelled(type);
                }

                // Message received
                for (TransmissionMessage message : messages) {
                    _delegate.onMessageReceived(type, message);
                }
            }

            return messages;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private @NonNull BuiltMessagesResult buildFullySentMessagesFromParts(@NonNull List<TransmissionMessagePart> parts) {
        ArrayList<TransmissionMessagePart> currentMessageParts = new ArrayList<>();
        ArrayList<TransmissionMessage> messages = new ArrayList<>();
        int failedMessageCount = 0;

        for (TransmissionMessagePart part : parts) {
            if (part instanceof TransmissionMessagePart.Ping) {
                messages.add(new BDTransmissionMessage(part.getType()));
            } else {
                currentMessageParts.add(part);

                if (part instanceof TransmissionMessagePart.End) {
                    if (verifyPartsDataIntegrity(currentMessageParts)) {
                        BDTransmissionMessageBuilder builder = new BDTransmissionMessageBuilder(part.getType());
                        TransmissionMessage message = builder.buildFromMessageParts(currentMessageParts);

                        if (message != null) {
                            messages.add(message);
                        }
                    } else {
                        failedMessageCount += 1;
                    }

                    currentMessageParts.clear();
                }
            }
        }

        return new BuiltMessagesResult(messages, failedMessageCount);
    }

    private boolean verifyPartsDataIntegrity(@NonNull List<TransmissionMessagePart> currentMessageParts) {
        if (currentMessageParts.size() < 3) {
            return false;
        }

        if (!(currentMessageParts.get(0) instanceof TransmissionMessagePart.Start)) {
            return false;
        }

        if (!(currentMessageParts.get(currentMessageParts.size()-1) instanceof TransmissionMessagePart.End)) {
            return false;
        }

        TransmissionMessagePart.Start start = (TransmissionMessagePart.Start) currentMessageParts.get(0);

        int expectedLength = start.expectedLength();
        int totalDataLength = 0;

        for (TransmissionMessagePart part : currentMessageParts) {
            if (part instanceof TransmissionMessagePart.Data) {
                totalDataLength += ((TransmissionMessagePart.Data) part).getData().length;
            }
        }

        return expectedLength == totalDataLength;
    }

    private void cancelAllMessages() {
        ArrayList<TransmissionMessagePart> partsToWrite = new ArrayList<>();

        List<QueuedMessage> messages = new ArrayList<>(_queuedMessages);

        for (QueuedMessage message : messages) {
            if (message.parts.size() == 0) {
                continue;
            }

            TransmissionMessagePart last = message.parts.get(message.parts.size()-1);

            if (last instanceof TransmissionMessagePart.End) {
                partsToWrite.add(last);
            }
        }

        try {
            this.line.write.writeMessages(partsToWrite);
        } catch (Exception e) {

        }

        _queuedMessages.clear();
    }
}

class BuiltMessagesResult {
    final @NonNull List<TransmissionMessage> messages;
    final int failedMessageCount;

    BuiltMessagesResult(@NonNull List<TransmissionMessage> messages, int failedMessageCount) {
        this.messages = messages;
        this.failedMessageCount = failedMessageCount;
    }
}

class QueuedMessage {
    int currentPartIndex = 0;

    final @NonNull List<TransmissionMessagePart> parts;
    final int dataSize;

    QueuedMessage(@NonNull List<TransmissionMessagePart> parts, int dataSize) throws Exception {
        if (parts.isEmpty()) {
            Errors.throwInvalidArgument("Message must contain at least one part");
        }

        this.parts = parts;
        this.dataSize = dataSize;
    }

    @Nullable
    TransmissionMessagePart next() {
        if (isFullySent()) {
            return null;
        }

        int index = currentPartIndex;

        currentPartIndex += 1;

        return parts.get(index);
    }

    int getPartCount() {
        return parts.size();
    }

    boolean isFullySent() {
        return currentPartIndex >= parts.size();
    }
}

class QueuedMessagePart {
    final @NonNull
    TransmissionMessagePart part;
    final int index;
    final int messagePartsCount;

    QueuedMessagePart(@NonNull TransmissionMessagePart part, int index, int messagePartsCount) {
        this.part = part;
        this.index = index;
        this.messagePartsCount = messagePartsCount;
    }

    @NonNull
    TransmissionType getType() {
        return part.getType();
    }

    double getProgress() {
        if (messagePartsCount == 0) {
            return 0;
        }

        double dIndex = index;
        double dCount = messagePartsCount;

        return dIndex / dCount;
    }

    boolean isData() {
        return part instanceof TransmissionMessagePart.Data;
    }

    boolean isEnd() {
        return part instanceof TransmissionMessagePart.End;
    }
}

class NextParts {
    final @NonNull List<QueuedMessagePart> parts;
    final @NonNull List<TransmissionMessagePart> messageParts;

    NextParts(@NonNull List<QueuedMessagePart> parts, @NonNull List<TransmissionMessagePart> messageParts) {
        this.parts = parts;
        this.messageParts = messageParts;
    }
}
