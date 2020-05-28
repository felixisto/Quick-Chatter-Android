package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.TimeValue;

import java.util.List;

public interface Transmitter {
    void start() throws Exception;
    void stop();

    interface Pinger {
        boolean isPingActive();
        @NonNull TimeValue getPingDelay();
        void setPingDelay(@NonNull TimeValue delay);
        void activatePing();
        void deactivatePing();
    }

    interface Reader extends Transmitter {
        @NonNull List<TransmissionLine.Input> getInputLines();

        void readNewMessages(@NonNull TransmissionType type, @NonNull Callback<List<TransmissionMessage>> completion) throws Exception;
    }

    interface Writer extends Transmitter {
        @NonNull List<TransmissionLine.Output> getOutputLines();

        void sendMessage(@NonNull TransmissionMessage message) throws Exception;
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
