package com.office.quickchatter.presenter.worker;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.basic.TransmissionMessage;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.network.basic.Transmitter;
import com.office.quickchatter.network.basic.TransmitterListener;
import com.office.quickchatter.network.bluetooth.bluedroid.BDConstants;
import com.office.quickchatter.network.bluetooth.bluedroid.transmission.BDTransmissionMessage;
import com.office.quickchatter.network.bluetooth.bluedroid.segment.BDTransmissionMessageSegment;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.filesystem.simple.SimpleFileSystem;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.DataSize;
import com.office.quickchatter.utilities.Errors;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.SimpleCallback;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicReference;

public class SendFilePerformer implements TransmitterListener {
    public enum State {
        idle, sendingAsk, sendingData, receivingAsk, receivingData, finalConfirmation
    }

    public static final char STATUS_CANCEL = '0';
    public static final char STATUS_ACCEPT = '1';
    public static final char STATUS_DENY = '2';

    private @NonNull final Object lock = new Object();

    private @NonNull BDConstants constants = BDConstants.getShared();

    private @NonNull AtomicReference<State> _state = new AtomicReference<>(State.idle);
    private @NonNull AtomicReference<FilePath> _path = new AtomicReference<>();
    private @NonNull AtomicReference<FilePath> _savePath = new AtomicReference<>();

    private final @NonNull Context _context;
    private final @NonNull Transmitter.ReaderWriter _readerWriter;
    private final @NonNull Transmitter.Service _service;

    private @Nullable SendFilePerformerDelegate _delegate;

    private SendFilePerformer self = this;

    private final @NonNull Callback<Path> askReceiveFileAcceptCallback = new Callback<Path>() {
        @Override
        public void perform(Path path) {
            synchronized (lock) {
                Logger.message(self, "Accepted to transfer, beginning to receive....");

                _state.set(State.receivingData);

                _savePath.set(new FilePath(path));

                sendAcceptStatusToOtherSide();
            }
        }
    };

    private final @NonNull SimpleCallback askReceiveFileDenyCallback = new SimpleCallback() {
        @Override
        public void perform() {
            synchronized (lock) {
                Logger.message(self, "Denied to transfer.");

                _state.set(State.idle);

                sendDenyStatusToOtherSide();
            }
        }
    };

    public SendFilePerformer(@NonNull Context context, @NonNull Transmitter.ReaderWriter readerWriter, @NonNull Transmitter.Service service) {
        _context = context;
        _readerWriter = readerWriter;
        _service = service;

        SimpleFileSystem system = new SimpleFileSystem();
        _savePath.set(new FilePath(system.getExternalStorageDirectory(), "file"));
    }

    // # Properties

    public boolean isIdle() {
        return getState() == State.idle;
    }

    public @NonNull State getState() {
        return _state.get();
    }

    // # Operations

    public void start(@NonNull SendFilePerformerDelegate delegate) {
        synchronized (lock) {
            _delegate = delegate;
            _service.subscribe(this);
        }
    }

    public void stop() {
        synchronized (lock) {
            _delegate = null;

            _state.set(State.idle);

            _service.unsubscribe(this);

            sendCancelStatusToOtherSide();
        }
    }

    public void sendFile(@NonNull FilePath path) throws Exception {
        synchronized (lock) {
            if (getState() != State.idle) {
                Errors.throwIllegalStateError("Cannot send file, already doing something else");
            }

            Logger.message(this, "Send file commencing, first ask other side about permission...");

            _path.set(path);

            _state.set(State.sendingAsk);

            sendAskStatusToOtherSide(path);
        }
    }

    // # TransmitterListener

    @Override
    public void onMessageReceived(@NonNull TransmissionType type, @NonNull TransmissionMessage message) {
        if (message.getType().equals(constants.TYPE_PING)) {
            return;
        }

        synchronized (lock) {
            if (_delegate == null) {
                return;
            }

            if (isMessageAsk(message)) {
                if (getState() == State.idle) {
                    Logger.message(this, "Other side is asking to transfer file to us! Accept or deny?");

                    _state.set(State.receivingAsk);

                    onOtherSideAsk(bytesToString(message.getBytes()));
                } else {
                    Logger.message(this, "Other side is asking to transfer file to us but we are currently busy! Sending cancel...");

                    sendCancelStatusToOtherSide();
                }

                return;
            }

            if (isMessageStatusCancel(message)) {
                Logger.message(this, "Other side is cancelling all operations!");

                _state.set(State.idle);

                onTransferCancel();

                return;
            }

            if (isMessageReceiveData(message) && getState() == State.receivingData) {
                Logger.message(this, "File successfully received! Sending final confirmation to other side and switching to idle mode.");

                _state.set(State.idle);

                onTransferCompleted();
                saveFileData(message.getBytes());

                sendFinalConfirmation();

                return;
            }

            if (getState() == State.sendingAsk) {
                if (isMessageStatusAccept(message)) {
                    Logger.message(this, "Other side agreed to receive the file! Begin transfer...");

                    _state.set(State.sendingData);

                    onOtherSideAcceptedAsk();
                }

                if (isMessageStatusDeny(message)) {
                    Logger.message(this, "Other side denied to receive the file!");

                    _state.set(State.idle);

                    onOtherSideDeniedAsk();
                }

                return;
            }

            if (getState() == State.finalConfirmation) {
                if (isMessageFinalConfirm(message)) {
                    Logger.message(this, "Other side has received the upload! Switching back to idle mode");

                    _state.set(State.idle);

                    onTransferCompletedAndConfirmed();
                }
            }
        }
    }

    @Override
    public void onMessageDataChunkReceived(@NonNull TransmissionType type, double progress) {
        if (!type.equals(constants.TYPE_SEND_FILE_DATA)) {
            return;
        }

        if (_delegate != null) {
            _delegate.fileTransferProgressUpdate(progress);
        }
    }

    @Override
    public void onMessageDataChunkSent(@NonNull TransmissionType type, double progress) {
        if (!type.equals(constants.TYPE_SEND_FILE_DATA)) {
            return;
        }

        if (_delegate != null) {
            _delegate.fileTransferProgressUpdate(progress);
        }
    }

    @Override
    public void onMessageFullySent(@NonNull TransmissionType type) {
        if (type.equals(constants.TYPE_PING)) {
            return;
        }

        synchronized (lock) {
            if (getState() == State.idle) {
                return;
            }

            if (type.equals(constants.TYPE_SEND_FILE_DATA) && getState() == State.sendingData) {
                Logger.message(this, "File successfully sent! Waiting for final confirmation...");

                _state.set(State.finalConfirmation);

                onTransferCompleted();

                return;
            }
        }
    }

    @Override
    public void onMessageFailedOrCancelled(final @NonNull TransmissionType type) {
        if (!type.equals(constants.TYPE_SEND_FILE_DATA)) {
            return;
        }

        synchronized (lock) {
            if (getState() != State.receivingData) {
                return;
            }

            Logger.message(this, "File transfer was cancelled or failed!");

            _state.set(State.idle);

            onTransferCancel();
        }
    }

    // # Build message

    private @NonNull BDTransmissionMessage buildCancelStatusMessage() {
        return new BDTransmissionMessage(constants.TYPE_SEND_FILE_STATUS, charToByte(STATUS_CANCEL));
    }

    private @NonNull BDTransmissionMessage buildAcceptStatusMessage() {
        return new BDTransmissionMessage(constants.TYPE_SEND_FILE_STATUS, charToByte(STATUS_ACCEPT));
    }

    private @NonNull BDTransmissionMessage buildDenyStatusMessage() {
        return new BDTransmissionMessage(constants.TYPE_SEND_FILE_STATUS, charToByte(STATUS_DENY));
    }

    private @NonNull BDTransmissionMessage buildSendAskMessage(@NonNull FilePath path) {
        String name = path.getLastComponent();
        DataSize size = getSize(path);

        byte[] value = stringToBytes(name + " (" + size.toString() + ")");

        return new BDTransmissionMessage(constants.TYPE_SEND_FILE_ASK, value);
    }

    private @NonNull BDTransmissionMessage buildDataMessage(@NonNull FilePath path) {
        return new BDTransmissionMessage(constants.TYPE_SEND_FILE_DATA, getData(path));
    }

    private @NonNull BDTransmissionMessage buildFinalConfirmMessage() {
        return new BDTransmissionMessage(constants.TYPE_SEND_FILE_FINAL_CONFIRM);
    }

    // # Message validators

    private boolean isMessageAsk(@NonNull TransmissionMessage message) {
        return message.getType().equals(constants.TYPE_SEND_FILE_ASK);
    }

    private boolean isMessageStatusAccept(@NonNull TransmissionMessage message) {
        String data = bytesToString(message.getBytes());

        if (data.isEmpty()) {
            return false;
        }

        return message.getType().equals(constants.TYPE_SEND_FILE_STATUS) && data.charAt(0) == STATUS_ACCEPT;
    }

    private boolean isMessageStatusDeny(@NonNull TransmissionMessage message) {
        String data = bytesToString(message.getBytes());

        if (data.isEmpty()) {
            return false;
        }

        return message.getType().equals(constants.TYPE_SEND_FILE_STATUS) && data.charAt(0) == STATUS_DENY;
    }

    private boolean isMessageStatusCancel(@NonNull TransmissionMessage message) {
        String data = bytesToString(message.getBytes());

        if (data.isEmpty()) {
            return false;
        }

        return message.getType().equals(constants.TYPE_SEND_FILE_STATUS) && data.charAt(0) == STATUS_CANCEL;
    }

    private boolean isMessageReceiveData(@NonNull TransmissionMessage message) {
        return message.getType().equals(constants.TYPE_SEND_FILE_DATA);
    }

    private boolean isMessageFinalConfirm(@NonNull TransmissionMessage message) {
        return message.getType().equals(constants.TYPE_SEND_FILE_FINAL_CONFIRM);
    }

    // # Transfer steps

    private void sendAskStatusToOtherSide(@NonNull FilePath path) {
        try {
            _readerWriter.sendMessage(buildSendAskMessage(path));
        } catch (Exception e) {
            Logger.error(this, "Something went wrong, failed to ask other side, error: " + e);
            _state.set(State.idle);
        }
    }

    private void onOtherSideAsk(@NonNull String description) {
        if (_delegate == null) {
            return;
        }

        String name = getFileNameFromSentDescription(description);

        _delegate.onAskedToReceiveFile(askReceiveFileAcceptCallback, askReceiveFileDenyCallback, name, description);
    }

    private void onOtherSideAcceptedAsk() {
        beginTransferData(_path.get());

        if (_delegate == null) {
            return;
        }

        _delegate.onOtherSideAcceptedTransferAsk();
    }

    private void onOtherSideDeniedAsk() {
        if (_delegate == null) {
            return;
        }

        _delegate.onOtherSideDeniedTransferAsk();
    }

    private void beginTransferData(@NonNull FilePath path) {
        try {
            _readerWriter.sendMessage(buildDataMessage(path));
        } catch (Exception e) {
            Logger.error(this, "Something went wrong, failed to transfer file, error: " + e);
            _state.set(State.idle);
            sendCancelStatusToOtherSide();
        }
    }

    private void onTransferCompleted() {

    }

    private void onTransferCompletedAndConfirmed() {
        if (_delegate == null) {
            return;
        }

        FilePath path = _savePath.get();

        _delegate.onFileTransferComplete(path);
    }

    private void onTransferCancel() {
        if (_delegate == null) {
            return;
        }

        _delegate.onFileTransferCancelled();
    }

    private void sendAcceptStatusToOtherSide() {
        try {
            _readerWriter.sendMessage(buildAcceptStatusMessage());
        } catch (Exception e) {
        }
    }

    private void sendDenyStatusToOtherSide() {
        try {
            _readerWriter.sendMessage(buildDenyStatusMessage());
        } catch (Exception e) {
        }
    }

    private void sendCancelStatusToOtherSide() {
        try {
            _readerWriter.sendMessage(buildCancelStatusMessage());
        } catch (Exception e) {
        }
    }

    private void saveFileData(@NonNull byte[] bytes) {
        FilePath path = _savePath.get();

        Logger.message(this, "Saving transferred data to new file, to directory " + path.getURL().toString() + ". Transfer data length is " + bytes.length);

        try {
            Uri uri = path.getURL();

            if (uri.getPath() == null) {
                Errors.throwUnknownError("Bad url path");
            }

            File newFile = new File(uri.getPath());

            if (newFile.exists()) {
                if (!newFile.delete()) {
                    Errors.throwUnknownError("Failed to replace file at " + path.getURL());
                }
            }

            if (!newFile.createNewFile()) {
                Errors.throwUnknownError("Failed to create file at " + path.getURL());
            }

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
            bos.write(bytes);
            bos.flush();
            bos.close();

            if (_delegate != null) {
                _delegate.onFileTransferComplete(path);
            }
        } catch (Exception e) {
            Logger.error(this, "Failed to save transferred file, error: " + e);

            if (_delegate != null) {
                _delegate.fileSaveFailed(e);
            }
        }
    }

    private void sendFinalConfirmation() {
        try {
            _readerWriter.sendMessage(buildFinalConfirmMessage());
        } catch (Exception e) {
            Logger.error(this, "Something went wrong, failed to send final confirmation, error: " + e);
            _state.set(State.idle);
            sendCancelStatusToOtherSide();
        }
    }

    // # Other

    private @NonNull byte[] charToByte(char value) {
        return BDTransmissionMessageSegment.stringToBytes(String.valueOf(value));
    }

    private @NonNull byte[] stringToBytes(@NonNull String value) {
        return BDTransmissionMessageSegment.stringToBytes(value);
    }

    private @NonNull String bytesToString(@NonNull byte[] data) {
        return BDTransmissionMessageSegment.bytesToString(data);
    }

    private @NonNull DataSize getSize(@NonNull FilePath path) {
        String filePath = path.getPath();

        if (filePath == null) {
            return DataSize.zero();
        }

        File file = new File(filePath);
        return DataSize.buildBytes(file.length());
    }

    private @NonNull byte[] getData(@NonNull FilePath path) {
        try {
            RandomAccessFile file = new RandomAccessFile(path.getPath(), "r");
            byte[] data = new byte[(int)file.length()];
            file.readFully(data);
            return data;
        } catch (Exception e) {

        }

        return new byte[0];
    }

    private @NonNull String getFileNameFromSentDescription(@NonNull String description) {
        String[] components = description.split(" ");

        if (components.length <= 1) {
            return "transferredFile";
        }

        return components[0];
    }
}
