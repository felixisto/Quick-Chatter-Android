package com.office.quickchatter.network.bluetooth.bluedroid.transmission;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.network.basic.TransmissionMessage;
import com.office.quickchatter.network.basic.TransmissionMessagePart;
import com.office.quickchatter.network.basic.TransmissionType;

import java.util.ArrayList;
import java.util.List;

public class BDTransmissionMessageBuilder {
    public final @NonNull
    TransmissionType type;

    private final @NonNull BDTransmissionMessagePartBuilder _messageBuilder;

    public BDTransmissionMessageBuilder(@NonNull TransmissionType type) {
        this.type = type;
        this._messageBuilder = new BDTransmissionMessagePartBuilder(type);
    }

    public @Nullable
    TransmissionMessage buildFromMessageParts(@NonNull List<TransmissionMessagePart> parts) {
        @Nullable ArrayList<TransmissionMessagePart.Data> currentMessageData = null;

        for (TransmissionMessagePart part : parts) {
            if (part instanceof TransmissionMessagePart.Start) {
                currentMessageData = new ArrayList<>();
            }

            if (part instanceof TransmissionMessagePart.Data) {
                TransmissionMessagePart.Data data = (TransmissionMessagePart.Data)part;

                if (currentMessageData != null) {
                    currentMessageData.add(data);
                }
            }

            if (part instanceof TransmissionMessagePart.End) {
                if (currentMessageData != null) {
                    return new BDTransmissionMessage(_messageBuilder.buildDataFromList(currentMessageData));
                }

                currentMessageData = null;
            }
        }

        return null;
    }
}
