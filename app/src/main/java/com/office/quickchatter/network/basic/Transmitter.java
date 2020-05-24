package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.TimeValue;

import java.util.List;

public interface Transmitter {
    void start() throws Exception;
    void stop();

    @NonNull List<TransmissionLine.Input> getInputLines();
    @NonNull List<TransmissionLine.Output> getOutputLines();

    boolean isPingActive();
    @NonNull TimeValue getPingDelay();
    void setPingDelay(@NonNull TimeValue delay);
    void activatePing();
    void deactivatePing();

    interface Writer extends Transmitter {
        void sendMessage(@NonNull TransmissionMessage message) throws Exception;
    }

    interface Reader extends Transmitter {
        void readNewMessages(@NonNull TransmissionType type, @NonNull Callback<List<TransmissionMessage>> completion) throws Exception;
    }

    interface Service extends Transmitter {
        void readAllNewMessages();

        @NonNull PingStatusChecker getPingStatusChecker();

        void subscribe(@NonNull TransmitterListener listener);
        void unsubscribe(@NonNull TransmitterListener listener);
    }

    interface ReaderWriter extends Writer, Reader {

    }
}
