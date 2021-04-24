package com.office.quickchatter.network.bluetooth.bluedroid;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.TransmissionType;
import com.office.quickchatter.utilities.TimeValue;

public class BDConstants {
    private static BDConstants shared;

    public final TransmissionType TYPE_PING;
    public final TransmissionType TYPE_CHAT; // possible values: <text>
    public final TransmissionType TYPE_SEND_FILE_ASK; // possible values: <file description>
    public final TransmissionType TYPE_SEND_FILE_STATUS; // possible values: 0 (cancel) 1 (accept) 2 (deny)
    public final TransmissionType TYPE_SEND_FILE_DATA; // possible values: <file data>
    public final TransmissionType TYPE_SEND_FILE_FINAL_CONFIRM;

    public static final @NonNull TimeValue DEFAULT_PING_DELAY = TimeValue.buildSeconds(1);
    public static final @NonNull TimeValue CONNECTION_TIMEOUT = TimeValue.buildSeconds(45);
    public static final @NonNull TimeValue CONNECTION_TIMEOUT_WARNING = TimeValue.buildSeconds(15);

    private BDConstants() {
        TransmissionType ping;
        TransmissionType chatType;
        TransmissionType sendFileAsk;
        TransmissionType sendFileStatus;
        TransmissionType sendFileData;
        TransmissionType sendFileConfirm;

        try {
            ping = new TransmissionType("Ping");
            chatType = new TransmissionType("Chat");
            sendFileAsk = new TransmissionType("FAsk");
            sendFileStatus = new TransmissionType("FSta");
            sendFileData = new TransmissionType("FDat");
            sendFileConfirm = new TransmissionType("FEnd");
        } catch (Exception e) {
            ping = null;
            chatType = null;
            sendFileAsk = null;
            sendFileStatus = null;
            sendFileData = null;
            sendFileConfirm = null;
        }

        this.TYPE_PING = ping;
        this.TYPE_CHAT = chatType;
        this.TYPE_SEND_FILE_ASK = sendFileAsk;
        this.TYPE_SEND_FILE_STATUS = sendFileStatus;
        this.TYPE_SEND_FILE_DATA = sendFileData;
        this.TYPE_SEND_FILE_FINAL_CONFIRM = sendFileConfirm;
    }

    public static synchronized @NonNull BDConstants getShared() {
        if (shared == null) {
            shared = new BDConstants();
        }

        return shared;
    }
}
