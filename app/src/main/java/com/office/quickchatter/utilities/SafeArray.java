package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import androidx.arch.core.util.Function;

public interface SafeArray <T> {
    @NonNull List<T> copyData();

    // # Query

    int size();

    T get(int pos);

    boolean isEmpty();

    boolean contains(@NonNull T value);

    public int indexOf(@NonNull T value);

    // # Other

    @NonNull ArrayList<T> filter(@NonNull Function<T, Boolean> filter);

    void perform(@NonNull Callback<T> callback);
}
