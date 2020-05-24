package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;

import java.util.Date;

public class Timer {
    public final @NonNull TimeValue delay;

    private @NonNull Date _date;

    public Timer(@NonNull TimeValue delay) {
        this(delay, new Date());
    }

    public Timer(@NonNull TimeValue delay, @NonNull Date now) {
        this.delay = delay;
        this._date = now;
    }

    // # Validators

    public @NonNull TimeValue timeElapsedSince(@NonNull Date now) {
        long differenceMS = now.getTime() - _date.getTime();
        return TimeValue.buildMS((int)differenceMS);
    }

    public @NonNull TimeValue timeElapsedSinceNow() {
        return timeElapsedSince(new Date());
    }

    public boolean isExpired() {
        return isExpiredSince(new Date());
    }

    public boolean isExpiredSince(@NonNull Date now) {
        return timeElapsedSince(now).inMS() > delay.inMS();
    }

    // # Operations

    public void reset() {
        _date = new Date();
    }

    public boolean update() {
        boolean expired = isExpired();

        if (expired) {
            reset();
        }

        return expired;
    }
}
