package com.office.quickchatter.network.bluetooth.basic;

/// Generic component used to see if bluetooth is available and provides fundamental bluetooth
/// functionality such as getting bluetooth scanners.
public interface BEAdapter <T> {
    boolean isAvailable();

    T getAdapter();
}
