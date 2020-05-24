package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;

import java.util.Locale;

public class DataSize {
    public static @NonNull DataSize zero() {
        return new DataSize(0);
    }

    public static @NonNull DataSize copy(@NonNull DataSize other) {
        return new DataSize(other);
    }

    public static @NonNull DataSize buildBytes(long bytes) {
        return new DataSize(bytes);
    }

    public static @NonNull DataSize buildKB(double kb) {
        return new DataSize(kb * 1024);
    }

    public static @NonNull DataSize buildMB(double mb) {
        return new DataSize(mb * 1024 * 1024);
    }

    public static @NonNull DataSize buildGB(double gb) {
        return new DataSize(gb * 1024 * 1024 * 1024);
    }

    private final int _value;

    private DataSize(double bytes) {
        this._value = bytes >= 0 ? ((int)bytes) : 0;
    }
    private DataSize(@NonNull DataSize other) {
        this._value = other._value;
    }

    public long inBytes() {
        return _value;
    }

    public double inKB() {
        return inBytes() / 1024.0;
    }

    public double inMB() {
        return inKB() / 1024.0;
    }

    public double inGB() {
        return inMB() / 1024.0;
    }

    @Override
    public @NonNull String toString() {
        double gb = inGB();

        if (gb >= 1) {
            return String.format(Locale.getDefault(), "%.1f GB", gb);
        }

        double mb = inMB();

        if (mb >= 1) {
            return String.format(Locale.getDefault(), "%.1f MB", mb);
        }

        double kb = inKB();

        if (kb >= 1) {
            return String.format(Locale.getDefault(), "%.1f KB", kb);
        }

        long b = inBytes();

        return String.format(Locale.getDefault(), "%d bytes", b);
    }
}

