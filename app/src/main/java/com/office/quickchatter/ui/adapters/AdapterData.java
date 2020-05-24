package com.office.quickchatter.ui.adapters;

import androidx.annotation.NonNull;

import com.office.quickchatter.utilities.Copyable;

import java.util.List;

public interface AdapterData <T> extends Copyable<AdapterData <T>> {
    @NonNull List<T> getValues();
    int count();
    @NonNull T getValue(int index);
}
