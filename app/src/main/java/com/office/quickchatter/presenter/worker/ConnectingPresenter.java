package com.office.quickchatter.presenter.worker;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.basic.TransmissionReadStream;
import com.office.quickchatter.network.basic.TransmissionWriteStream;
import com.office.quickchatter.network.bluetooth.basic.BESocket;
import com.office.quickchatter.network.bluetooth.bluedroid.BDConstants;
import com.office.quickchatter.network.bluetooth.bluedroid.BDSocket;
import com.office.quickchatter.network.bluetooth.bluedroid.other.BDStandardReadWriteBandwidth;
import com.office.quickchatter.network.bluetooth.bluedroid.other.BDStandardReadWriteBandwidthMonitor;
import com.office.quickchatter.network.bluetooth.bluedroid.transmission.BDTransmissionLine;
import com.office.quickchatter.network.bluetooth.bluedroid.transmission.BDTransmissionLineBuilder;
import com.office.quickchatter.network.bluetooth.bluedroid.transmission.BDTransmitter;
import com.office.quickchatter.utilities.DataSize;
import com.office.quickchatter.utilities.Errors;

import java.util.ArrayList;
import java.util.List;

public class ConnectingPresenter {
    public static @NonNull BDTransmitter startServer(@NonNull BESocket socket) throws Exception {
        BDStandardReadWriteBandwidth readBandwidth = new BDStandardReadWriteBandwidthMonitor(DataSize.buildBytes(1024 * 3));
        BDStandardReadWriteBandwidth writeBandwidth = new BDStandardReadWriteBandwidthMonitor(DataSize.buildBytes(1024 * 3));

        BDSocket bdSocket = null;

        if (socket instanceof BDSocket) {
            bdSocket = (BDSocket) socket;
        } else {
            Errors.throwInvalidArgument("Given socket must be BDSocket.");
        }

        TransmissionReadStream input = new TransmissionReadStream(bdSocket.getSocket().getInputStream(), readBandwidth);
        TransmissionWriteStream output = new TransmissionWriteStream(bdSocket.getSocket().getOutputStream(), writeBandwidth);

        BDTransmissionLineBuilder builder = new BDTransmissionLineBuilder(input, output);

        List<BDTransmissionLine> lines = new ArrayList<>();
        lines.add(builder.build(BDConstants.getShared().TYPE_CHAT));
        lines.add(builder.build(BDConstants.getShared().TYPE_SEND_FILE_DATA));
        lines.add(builder.build(BDConstants.getShared().TYPE_SEND_FILE_STATUS));
        lines.add(builder.build(BDConstants.getShared().TYPE_SEND_FILE_ASK));
        lines.add(builder.build(BDConstants.getShared().TYPE_SEND_FILE_FINAL_CONFIRM));

        return new BDTransmitter(bdSocket, input, output, lines);
    }
}
