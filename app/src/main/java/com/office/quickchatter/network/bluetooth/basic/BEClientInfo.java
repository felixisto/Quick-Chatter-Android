package com.office.quickchatter.network.bluetooth.basic;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.Copyable;

public class BEClientInfo implements Copyable<BEClientInfo> {
    public @NonNull String name = "";

    @Override
    public BEClientInfo copy() {
        BEClientInfo info = new BEClientInfo();
        info.name = name;
        return info;
    }
}
