package com.office.quickchatter.network.bluetooth.bluedroid.parser;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.network.bluetooth.basic.BEPairing;
import com.office.quickchatter.utilities.Parser;

public class PairEntityToClientParser implements Parser<BEPairing.Entity, BEClient> {
    @Override
    public @NonNull BEClient parse(@NonNull BEPairing.Entity data) throws Exception {
        return data.getClient();
    }
}