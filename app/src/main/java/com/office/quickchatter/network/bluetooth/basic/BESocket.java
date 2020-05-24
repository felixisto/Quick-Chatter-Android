package com.office.quickchatter.network.bluetooth.basic;

public interface BESocket <T> {
    T getSocket();

    void close() throws Exception;
}
