package com.office.quickchatter.ui.adapters;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEClient;
import com.office.quickchatter.utilities.CollectionUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BEClientsAdapterData implements AdapterData<BEClient> {
    private final @NonNull List<BEClient> _data;

    public BEClientsAdapterData(@NonNull List<BEClient> data) {
        this._data = CollectionUtilities.copy(data);
    }

    public BEClientsAdapterData(@NonNull Set<BEClient> data) {
        this._data = new ArrayList<>(data);
    }

    @Override
    public AdapterData<BEClient> copy() {
        return new BEClientsAdapterData(_data);
    }

    @Override
    public @NonNull List<BEClient> getValues() {
        return CollectionUtilities.copy(_data);
    }

    @Override
    public int count() {
        return _data.size();
    }

    @Override
    public @NonNull BEClient getValue(int index) {
        return _data.get(index);
    }
}
