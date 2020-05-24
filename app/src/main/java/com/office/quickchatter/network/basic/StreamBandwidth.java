package com.office.quickchatter.network.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.DataSize;
import com.office.quickchatter.utilities.TimeValue;

public interface StreamBandwidth {
    // Streams will flush in this specific data chunk size.
    @NonNull DataSize getFlushDataRate();

    // When a stream flushes a timer is started. If the timer expires before being
    // restarted by another flush data rate, it will force flush.
    @NonNull TimeValue getForceFlushTime();

    interface Boostable extends StreamBandwidth {
        void boostFlushRate(double multiplier);
        void revertBoost();
    }

    interface Tracker extends StreamBandwidth {
        interface Read extends Tracker {
            void read(int length);
        }

        interface Write extends Tracker {
            void write(int length);
        }

        interface Monitor extends Tracker {
            // Returns estimated rate that is transferred per second.
            @NonNull DataSize getEstimatedCurrentRate();
        }
    }
}
