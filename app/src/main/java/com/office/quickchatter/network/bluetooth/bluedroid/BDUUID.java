package com.office.quickchatter.network.bluetooth.bluedroid;

import androidx.annotation.NonNull;

import java.util.UUID;

public class BDUUID {
    public static @NonNull UUID get() {
        return UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }
}
