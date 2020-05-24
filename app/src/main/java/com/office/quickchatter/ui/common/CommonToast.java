package com.office.quickchatter.ui.common;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class CommonToast {
    public static void showBluetoothIsOff(@Nullable Context context) {
        Toast toast = Toast.makeText(context, "Bluetooth is off", Toast.LENGTH_LONG);
        toast.show();
    }

    public static void showConnectionSlow(@Nullable Context context) {
        Toast toast = Toast.makeText(context, "Connection is slow...", Toast.LENGTH_LONG);
        toast.show();
    }

    public static void showConnectionLost(@Nullable Context context) {
        Toast toast = Toast.makeText(context, "Connection lost!", Toast.LENGTH_LONG);
        toast.show();
    }
}
