package com.office.quickchatter.presenter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.bluetooth.basic.BETransmitter;
import com.office.quickchatter.network.basic.StreamBandwidth;
import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.basic.TransmissionMessage;
import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.network.basic.TransmitterListener;
import com.office.quickchatter.network.bluetooth.bluedroid.BDConstants;
import com.office.quickchatter.network.bluetooth.bluedroid.transmission.BDTransmissionMessage;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.ui.chat.UIChat;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperClient;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SimpleCallback;
import com.office.quickchatter.utilities.TimeValue;
import com.office.quickchatter.utilities.Timer;
import com.office.quickchatter.presenter.worker.SendFilePerformer;
import com.office.quickchatter.presenter.worker.SendFilePerformerDelegate;

public class ChatPresenter implements BasePresenter.Chat, LooperClient, TransmitterListener, SendFilePerformerDelegate {
    private @NonNull final Context _context;
    private @Nullable BasePresenterDelegate.Chat _delegate;
    private @NonNull final BEClient _client;
    private @NonNull final BETransmitter.ReaderWriter _transmitter;
    private @NonNull final BETransmitter.Service _transmitterService;

    private @NonNull final UIChat _chat;

    private @NonNull final Timer _updateTimer = new Timer(TimeValue.buildSeconds(0.5));

    private @NonNull final SendFilePerformer _sendFilePerformer;

    private boolean _timeoutWarningSent = false;
    private boolean _timeoutSent = false;

    public ChatPresenter(@NonNull Context context,
                         @NonNull BEClient client,
                         @NonNull BETransmitter.ReaderWriter transmitter,
                         @NonNull BETransmitter.Service transmitterService) {
        _context = context;
        _client = client;
        _transmitter = transmitter;
        _transmitterService = transmitterService;

        _chat = new UIChat("Me", client.getName(), true, 100);

        _sendFilePerformer = new SendFilePerformer(_context, _transmitter, _transmitterService);
    }

    // # Presenter.UIChat

    @Override
    public void start(@NonNull BasePresenterDelegate.Chat delegate) {
        if (_delegate != null) {
            return;
        }

        _delegate = delegate;

        try {
            _transmitter.start();
        } catch (Exception e) {
            _delegate = null;

            Logger.error(this, "Failed to start, transmitter error: " + e.toString());

            return;
        }

        LooperService.getShared().subscribe(this);
        _transmitterService.subscribe(this);

        _delegate.updateClientInfo(_client.getName());

        _chat.addTextLine("Connected on " + _chat.getCurrentTimestamp() + "!");

        _delegate.updateChat("", _chat.getLog());

        _sendFilePerformer.start(this);
    }

    @Override
    public void stop() {
        final ChatPresenter self = this;

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _delegate = null;

                LooperService.getShared().unsubscribe(self);

                _transmitterService.unsubscribe(self);

                _sendFilePerformer.stop();

                _transmitter.stop();
                _transmitterService.stop();
            }
        });
    }

    @Override
    public void sendMessage(@NonNull String message) {
        if (message.isEmpty()) {
            return;
        }

        Logger.message(this, "Send message '" + message + "'");

        try {
            TransmissionType type = BDConstants.getShared().TYPE_CHAT;
            byte[] bytes = message.getBytes();

            _transmitter.sendMessage(new BDTransmissionMessage(type, bytes));

            message = _chat.parseStringForSendMessage(bytes);

            _chat.onMessageSent(message);

            if (_delegate != null) {
                _delegate.updateChat(message, _chat.getLog());
                _delegate.clearChatTextField();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public boolean canSendFile() {
        return _sendFilePerformer.isIdle();
    }

    @Override
    public void sendFile(@NonNull Path path) {
        if (!canSendFile()) {
            return;
        }

        Logger.message(this, "Attempting to send file at " + path.toString());

        try {
            _sendFilePerformer.sendFile(new FilePath(path));

            final String message = "Asking other side for transfer file...";

            _chat.addTextLine(message);

            LooperService.getShared().performOnMain(new SimpleCallback() {
                @Override
                public void perform() {
                    if (_delegate != null) {
                        _delegate.updateChat(message, _chat.getLog());
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    // # TransmitterListener

    @Override
    public void onMessageReceived(@NonNull TransmissionType type, final @NonNull TransmissionMessage message) {
        if (!type.equals(BDConstants.getShared().TYPE_CHAT)) {
            return;
        }

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                String text = _chat.parseStringForReceiveMessage(message.getBytes());

                _chat.onMessageReceived(text);

                if (_delegate != null) {
                    _delegate.updateChat(text, _chat.getLog());
                }
            }
        });
    }

    @Override
    public void onMessageDataChunkReceived(@NonNull TransmissionType type, double progress) {

    }

    @Override
    public void onMessageDataChunkSent(@NonNull TransmissionType type, double progress) {

    }

    @Override
    public void onMessageFullySent(@NonNull TransmissionType type) {

    }

    @Override
    public void onMessageFailedOrCancelled(final @NonNull TransmissionType type) {

    }

    // # LooperClient

    @Override
    public void loop() {
        if (_updateTimer.update()) {
            readAllNewMessages();
            updatePingState();
        }
    }

    // # SendFilePerformerDelegate

    @Override
    public void onAskedToReceiveFile(@NonNull final Callback<Path> accept,
                                     @NonNull final SimpleCallback deny,
                                     @NonNull final String name,
                                     @NonNull final String description) {
        final String message = "Other side wants to transfer file...";
        Logger.message(this, message);

        final ChatPresenter self = this;

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate == null) {
                    return;
                }

                _delegate.onAskToAcceptTransferFile(new Callback<Path>() {
                    @Override
                    public void perform(Path path) {
                        Logger.message(self, "Attempting to save receiving file to " + path.toString());

                        final String message = "Accepted file transfer! Saving to " + path.toString();
                        _chat.addTextLine(message);
                        updateDelegateChat(message);

                        accept.perform(path);
                    }
                }, new SimpleCallback() {
                    @Override
                    public void perform() {
                        Logger.message(self, "Denied file transfer");

                        final String message = "Denied file transfer!";
                        _chat.addTextLine(message);
                        updateDelegateChat(message);

                        deny.perform();
                    }
                }, name, description);

                _chat.addTextLine(message);
                updateDelegateChat(message);
            }
        });
    }

    @Override
    public void onOtherSideAcceptedTransferAsk() {
        if (_delegate == null) {
            return;
        }

        final String message = "Other side agreed to transfer file to them! Begin transfer...";
        _chat.addTextLine(message);
        updateDelegateChat(message);
    }

    @Override
    public void onOtherSideDeniedTransferAsk() {
        if (_delegate == null) {
            return;
        }

        final String message = "Other side denied file transfer.";
        _chat.addTextLine(message);
        updateDelegateChat(message);
    }

    @Override
    public void onFileTransferComplete(@NonNull FilePath path) {
        _chat.removeBottomLineText();

        final String message = "File transferred successfully!";
        _chat.addTextLine(message);
        updateDelegateChat(message);
    }

    @Override
    public void onFileTransferCancelled() {
        _chat.removeBottomLineText();

        final String message = "File transfer cancelled!";
        _chat.addTextLine(message);
        updateDelegateChat(message);
    }

    @Override
    public void fileTransferProgressUpdate(double progress) {
        Logger.message(this, "File transfer update: " + progress);

        String currentTransferRate = "";

        if (_sendFilePerformer.getState() == SendFilePerformer.State.receivingData) {
            StreamBandwidth bandwidth = _transmitter.getInputLines().get(0).getReadBandwidth();

            if (bandwidth instanceof StreamBandwidth.Tracker.Monitor) {
                currentTransferRate = " (" + ((StreamBandwidth.Tracker.Monitor)bandwidth).getEstimatedCurrentRate().toString() + "/sec)";
            }
        }

        _chat.setBottomLineText("> Transfer progress " + (int)(progress * 100.0) + "%" + currentTransferRate);
        updateDelegateChat("");
    }

    @Override
    public void fileSaveFailed(final @NonNull Exception error) {
        _chat.removeBottomLineText();

        final String message = "Failed to save file, error: " + error.toString();
        _chat.addTextLine(message);
        updateDelegateChat(message);

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate != null) {
                    _delegate.showError("Failed to save file", error.toString());
                }
            }
        });
    }

    // # Internals

    private void readAllNewMessages() {
        _transmitterService.readAllNewMessages();
    }

    private void updatePingState() {
        TimeValue pingDelay = _transmitterService.getPingStatusChecker().timeElapsedSinceLastPing();

        if (pingDelay.inMS() > BDConstants.CONNECTION_TIMEOUT_WARNING.inMS()) {
            if (pingDelay.inMS() > BDConstants.CONNECTION_TIMEOUT.inMS()) {
                handleConnectionTimeout();
            } else {
                handleConnectionTimeoutWarning();
            }
        } else {
            if (_timeoutWarningSent || _timeoutSent) {
                performOnDelegate(new Callback<BasePresenterDelegate.Chat>() {
                    @Override
                    public void perform(BasePresenterDelegate.Chat delegate) {
                        delegate.onConnectionRestored();
                    }
                });
            }

            _timeoutWarningSent = false;
            _timeoutSent = false;
        }
    }

    private void handleConnectionTimeout() {
        if (!_timeoutSent) {
            Logger.warning(this, "Connection timeout detected!");

            _timeoutSent = true;

            performOnDelegate(new Callback<BasePresenterDelegate.Chat>() {
                @Override
                public void perform(BasePresenterDelegate.Chat delegate) {
                    delegate.onConnectionTimeout(false);
                }
            });
        }
    }

    private void handleConnectionTimeoutWarning() {
        if (!_timeoutWarningSent) {
            Logger.warning(this, "Connection timeout - warning.");

            _timeoutWarningSent = true;

            performOnDelegate(new Callback<BasePresenterDelegate.Chat>() {
                @Override
                public void perform(BasePresenterDelegate.Chat delegate) {
                    delegate.onConnectionTimeout(true);
                }
            });
        }
    }

    private void performOnDelegate(@NonNull final Callback<BasePresenterDelegate.Chat> callback) {
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_delegate == null) {
                    return;
                }

                callback.perform(_delegate);
            }
        });
    }

    private void updateDelegateChat(@NonNull final String message) {
        performOnDelegate(new Callback<BasePresenterDelegate.Chat>() {
            @Override
            public void perform(BasePresenterDelegate.Chat delegate) {
                delegate.updateChat(message, _chat.getLog());
            }
        });
    }
}
