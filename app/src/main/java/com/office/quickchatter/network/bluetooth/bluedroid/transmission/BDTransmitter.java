package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import android.bluetooth.BluetoothSocket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BESocket;
import com.office.quickchatter.network.bluetooth.basic.BETransmitter;
import com.office.quickchatter.network.basic.PingStatusChecker;
import com.office.quickchatter.network.basic.TransmissionLine;
import com.office.quickchatter.network.basic.TransmissionMessage;
import com.office.quickchatter.network.basic.TransmissionReadStream;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.network.basic.TransmissionWriteStream;
import com.office.quickchatter.network.basic.TransmitterListener;
import com.office.quickchatter.network.bluetooth.bluedroid.BDConstants;
import com.office.quickchatter.network.bluetooth.bluedroid.BDSocket;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.CollectionUtilities;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperClient;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SafeMutableArray;
import com.office.quickchatter.utilities.TimeValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/// A component responsible for transmitting and receiving information over one or multiple transmission lines.
public class BDTransmitter implements BETransmitter.ReaderWriter, BETransmitter.Service, LooperClient, BDTransmitterPerformerDelegate {
    private final @NonNull Object lock = new Object();

    public final @NonNull BDSocket socket;

    private final @NonNull List<BDTransmitterPerformer> _performers;
    private BDTransmitterPinger _pinger;

    private final @NonNull AtomicReference<BDTransmitterPing> _ping = new AtomicReference<>();
    private @NonNull Date _lastPingDate = new Date();

    private final @NonNull SafeMutableArray<TransmitterListener> _listeners = new SafeMutableArray<>();

    private final @NonNull AtomicBoolean _isActive = new AtomicBoolean(false);

    public BDTransmitter(@NonNull BDSocket socket, @NonNull TransmissionReadStream input, @NonNull TransmissionWriteStream output, @NonNull List<BDTransmissionLine> lines, @NonNull BDTransmitterPing ping) {
        this.socket = socket;

        _performers = new ArrayList<>();
        _ping.set(ping.copy());

        // Build and add performers
        lines = CollectionUtilities.copy(lines);

        ArrayList<BDTransmitterPerformer> performers = new ArrayList<>();

        for (BDTransmissionLine line : lines) {
            performers.add(new BDTransmitterPerformer(line, this));
        }

        performers.add(new BDTransmitterPinger(new BDTransmissionLineBuilder(input, output).buildPing(), BDConstants.DEFAULT_PING_DELAY, this));

        stripPerformersOfDuplicates(performers);

        _performers.addAll(performers);

        for (BDTransmitterPerformer performer : _performers) {
            if (performer instanceof BDTransmitterPinger) {
                _pinger = (BDTransmitterPinger) performer;
            }
        }

        // Sub to looper
        LooperService.getShared().subscribe(this);
    }

    public BDTransmitter(@NonNull BDSocket socket, @NonNull TransmissionReadStream input, @NonNull TransmissionWriteStream output, @NonNull List<BDTransmissionLine> lines) {
        this(socket, input, output, lines, new BDTransmitterPing(BDConstants.DEFAULT_PING_DELAY));
    }

    // # Transmitter.ReaderWriter, Transmitter.Service

    @Override
    public @NonNull BESocket<BluetoothSocket> getSocket() {
        return this.socket;
    }

    @Override
    public void start() throws Exception {
        if (_isActive.getAndSet(true)) {
            Errors.throwCannotStartTwice("Already started");
        }

        synchronized (lock) {
            LooperService.getShared().subscribe(this);
        }
    }

    @Override
    public void stop() {
        synchronized (lock) {
            LooperService.getShared().unsubscribe(this);

            for (BDTransmitterPerformer performer : _performers) {
                performer.stop();
            }

            _performers.clear();

            _isActive.set(false);
        }
    }

    @Override
    public @NonNull List<TransmissionLine.Input> getInputLines() {
        ArrayList<TransmissionLine.Input> lines = new ArrayList<>();

        for (BDTransmitterPerformer l : _performers) {
            lines.add(l.line.getInput());
        }

        return lines;
    }

    @Override
    public @NonNull List<TransmissionLine.Output> getOutputLines() {
        ArrayList<TransmissionLine.Output> lines = new ArrayList<>();

        for (BDTransmitterPerformer l : _performers) {
            lines.add(l.line.getOutput());
        }

        return lines;
    }

    @Override
    public boolean isPingActive() {
        return _ping.get().isActive();
    }

    @Override
    public @NonNull TimeValue getPingDelay() {
        return _ping.get().delay;
    }

    @Override
    public void setPingDelay(@NonNull TimeValue delay) {
        synchronized (lock) {
            BDTransmitterPing ping = new BDTransmitterPing(delay);
            ping.setActive(_ping.get().isActive());
            _ping.set(ping);
        }
    }

    @Override
    public void activatePing() {
        synchronized (lock) {
            _ping.get().setActive(true);
        }
    }

    @Override
    public void deactivatePing() {
        synchronized (lock) {
            _ping.get().setActive(false);
        }
    }

    @Override
    public void sendMessage(@NonNull TransmissionMessage message) throws Exception {
        TransmissionType type = message.getType();
        BDTransmitterPerformer stream = getStream(type);

        if (stream == null) {
            Errors.throwUnsupportedOperation("Transmission line '" + type.value + "' does not exist");
        }

        Logger.message(this, "Transmit message of type '" + type.value + "' length = " + message.length());

        stream.transmit(message.getBytes());
    }

    @Override
    public void readNewMessages(@NonNull TransmissionType type, final @NonNull Callback<List<TransmissionMessage>> completion) throws Exception {
        BDTransmitterPerformer stream = getStream(type);

        if (stream == null) {
            Errors.throwUnsupportedOperation("Transmission line '" + type.value + "' does not exist");
        }

        stream.readNewMessages(completion);
    }

    @Override
    public void readAllNewMessages() {
        for (BDTransmitterPerformer stream : _performers) {
            stream.readNewMessages(new Callback<List<TransmissionMessage>>() {
                @Override
                public void perform(List<TransmissionMessage> argument) {

                }
            });
        }
    }

    @Override
    public @NonNull PingStatusChecker getPingStatusChecker() {
        return _pinger;
    }

    @Override
    public void subscribe(@NonNull TransmitterListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void unsubscribe(@NonNull TransmitterListener listener) {
        _listeners.remove(listener);
    }

    // # LooperClient

    @Override
    public void loop() {
        pingUpdateIfNecessary();
    }

    // # BDTransmitterPerformerDelegate

    @Override
    public void onMessageReceived(final @NonNull TransmissionType type, @NonNull final TransmissionMessage message) {
        Logger.message(this, "Received message of type '" + type.value + "'");

        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener element) {
                element.onMessageReceived(type, message);
            }
        });
    }

    @Override
    public void onMessageDataChunkReceived(final @NonNull TransmissionType type, final double progress) {
        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener listener) {
                listener.onMessageDataChunkReceived(type, progress);
            }
        });
    }

    @Override
    public void onMessageDataChunkSent(final @NonNull TransmissionType type, final double progress) {
        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener listener) {
                listener.onMessageDataChunkSent(type, progress);
            }
        });
    }

    @Override
    public void onMessageFullySent(final @NonNull TransmissionType type) {
        Logger.message(this, "Sent message of type '" + type.value + "'");

        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener element) {
                element.onMessageFullySent(type);
            }
        });
    }

    @Override
    public void onMessageFailedOrCancelled(final @NonNull TransmissionType type) {
        Logger.message(this, "Message failed or cancelled of type '" + type.value + "'");

        _listeners.perform(new Callback<TransmitterListener>() {
            @Override
            public void perform(TransmitterListener element) {
                element.onMessageFailedOrCancelled(type);
            }
        });
    }

    // # Internals

    private @Nullable BDTransmitterPerformer getStream(@NonNull TransmissionType type) {
        BDTransmitterPerformer line = null;

        for (BDTransmitterPerformer l : _performers) {
            if (l.getType().equals(type)) {
                line = l;
                break;
            }
        }

        return line;
    }

    private void pingUpdateIfNecessary() {
        if (!_ping.get().isActive()) {
            return;
        }

        if (timeSinceLastPing().inMS() > _ping.get().delay.inMS()) {
            updatePingDate();
            pingUpdate();
        }
    }

    private void pingUpdate() {
        byte[] noBytes = new byte[0];

        try {
            BDTransmitterPerformer stream = getStream(BDConstants.getShared().TYPE_PING);

            if (stream != null) {
                stream.transmit(noBytes);
            }
        } catch (Exception e) {

        }
    }

    private void updatePingDate() {
        _lastPingDate = new Date();
    }

    private @NonNull TimeValue timeSinceLastPing() {
        Date now = new Date();
        long time = now.getTime() - _lastPingDate.getTime();
        return TimeValue.buildMS((int) time);
    }

    // # Utilities

    public static @NonNull List<BDTransmitterPerformer> stripPerformersOfDuplicates(@NonNull List<BDTransmitterPerformer> performers) {
        ArrayList<BDTransmitterPerformer> result = new ArrayList<>();

        for (BDTransmitterPerformer line : CollectionUtilities.copy(performers)) {
            boolean alreadyPresent = false;

            for (BDTransmitterPerformer resultLine : result) {
                if (line.getType().equals(resultLine.getType())) {
                    alreadyPresent = true;
                    break;
                }
            }

            if (!alreadyPresent) {
                result.add(line);
            }
        }

        return result;
    }
}
