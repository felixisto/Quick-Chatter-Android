package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;

import java.util.Locale;

public class FileSize {
    public static @NonNull FileSize zero() {
        return new FileSize(0);
    }

    public static @NonNull FileSize copy(@NonNull FileSize other) {
        return new FileSize(other);
    }

    public static @NonNull FileSize build(@NonNull DataSize other) {
        return new FileSize(other.inBytes());
    }

    public static @NonNull FileSize buildBytes(int bytes) {
        return new FileSize((int) bytes);
    }
    public static @NonNull FileSize buildBytes(long bytes) {
        return new FileSize(bytes);
    }

    public static @NonNull FileSize buildKB(double kb) {
        return new FileSize(kb * 1024);
    }

    public static @NonNull FileSize buildMB(double mb) {
        return new FileSize(mb * 1024 * 1024);
    }

    public static @NonNull FileSize buildGB(double gb) {
        return new FileSize(gb * 1024 * 1024 * 1024);
    }

    private final long _value;

    private FileSize(double bytes) {
        this._value = bytes >= 0 ? ((int)bytes) : 0;
    }
    private FileSize(@NonNull FileSize other) {
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
