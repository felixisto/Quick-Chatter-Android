package com.office.quickchatter.ui.adapters;

import androidx.annotation.NonNull;

import com.office.quickchatter.network.bluetooth.basic.BEPairing;
import com.office.quickchatter.utilities.CollectionUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BEPairEntitiesListAdapter implements AdapterData<BEPairing.Entity> {
    private final @NonNull List<BEPairing.Entity> _data;

    public BEPairEntitiesListAdapter(@NonNull List<BEPairing.Entity> data) {
        this._data = CollectionUtilities.copy(data);
    }

    public BEPairEntitiesListAdapter(@NonNull Set<BEPairing.Entity> data) {
        this._data = new ArrayList<>(data);
    }

    @Override
    public AdapterData<BEPairing.Entity> copy() {
        return new BEPairEntitiesListAdapter(_data);
    }

    @Override
    public @NonNull List<BEPairing.Entity> getValues() {
        return CollectionUtilities.copy(_data);
    }

    @Override
    public int count() {
        return _data.size();
    }

    @Override
    public @NonNull BEPairing.Entity getValue(int index) {
        return _data.get(index);
    }
}
