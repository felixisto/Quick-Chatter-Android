package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;

import java.util.Locale;

public class TimeValue {
    public static @NonNull TimeValue zero() {
        return new TimeValue(0);
    }

    public static @NonNull TimeValue copy(@NonNull TimeValue other) {
        return new TimeValue(other);
    }

    public static @NonNull TimeValue buildMS(int ms) {
        return new TimeValue(ms);
    }

    public static @NonNull TimeValue buildSeconds(double seconds) {
        return new TimeValue(seconds * 1000);
    }

    public static @NonNull TimeValue buildMinutes(double minutes) {
        return new TimeValue(minutes * 1000 * 60);
    }

    public static @NonNull TimeValue buildHours(double hours) {
        return new TimeValue(hours * 1000 * 60 * 60);
    }

    public static @NonNull TimeValue buildDays(double hours) {
        return new TimeValue(hours * 1000 * 60 * 60 * 24);
    }

    private final int _value;

    private TimeValue(double ms) {
        this._value = ms > 0 ? (int)ms : 0;
    }
    private TimeValue(@NonNull TimeValue other) {
        this._value = other._value;
    }

    public int inMS() {
        return _value;
    }

    public double inSeconds() {
        return inMS() / 1000.0;
    }

    public double inMinutes() {
        return inSeconds() / 60.0;
    }

    public double inHours() {
        return inMinutes() / 60.0;
    }

    public double inDays() {
        return inHours() / 24.0;
    }

    @Override
    public @NonNull String toString() {
        double hr = inHours();

        if (hr >= 1) {
            return String.format(Locale.getDefault(), "%.1f hours", hr);
        }

        double min = inMinutes();

        if (min >= 1) {
            return String.format(Locale.getDefault(), "%.1f min", min);
        }

        double sec = inSeconds();

        return String.format(Locale.getDefault(), "%.1f sec", sec);
    }
}
