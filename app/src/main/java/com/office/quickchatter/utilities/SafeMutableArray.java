package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SafeMutableArray <T> implements SafeArray<T> {
    private final @NonNull Object lock = new Object();
    private final @NonNull ArrayList<T> _value;

    public SafeMutableArray() {
        _value = new ArrayList<>();
    }

    public SafeMutableArray(@Nullable List<T> values) {
        _value = values != null ? new ArrayList<>(values) : new ArrayList<T>();
    }

    public @NonNull List<T> copyData() {
        synchronized (lock) {
            return new ArrayList<>(_value);
        }
    }

    // # Query

    @Override
    public int size() {
        synchronized (lock) {
            return _value.size();
        }
    }

    @Override
    public T get(int pos) {
        synchronized (lock) {
            return _value.get(pos);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return _value.isEmpty();
        }
    }

    @Override
    public boolean contains(@NonNull T value) {
        synchronized (lock) {
            return _value.contains(value);
        }
    }

    @Override
    public int indexOf(@NonNull T value) {
        synchronized (lock) {
            return _value.indexOf(value);
        }
    }

    // # Operations

    public void add(@NonNull T value) {
        synchronized (lock) {
            _value.add(value);
        }
    }

    public void addAll(@NonNull Collection<T> value) {
        synchronized (lock) {
            _value.addAll(value);
        }
    }

    public void insert(@NonNull T value, int pos) {
        synchronized (lock) {
            _value.add(pos, value);
        }
    }

    public void removeAt(int index) {
        synchronized (lock) {
            _value.remove(index);
        }
    }

    public void remove(@NonNull T value) {
        synchronized (lock) {
            _value.remove(value);
        }
    }

    public void remove(@NonNull Function<T, Boolean> filter) {
        synchronized (lock) {
            ArrayList<T> copiedValues = new ArrayList<>(_value);

            for (T element: copiedValues) {
                if (filter.apply(element)) {
                    _value.remove(element);
                }
            }
        }
    }

    public void removeAll() {
        synchronized (lock) {
            _value.clear();
        }
    }

    // # Other

    @Override
    public @NonNull ArrayList<T> filter(@NonNull Function<T, Boolean> filter) {
        ArrayList<T> filteredValues = new ArrayList<>();

        synchronized (lock) {
            for (T element: _value) {
                if (filter.apply(element)) {
                    filteredValues.add(element);
                }
            }
        }

        return filteredValues;
    }

    @Override
    public void perform(@NonNull Callback<T> callback) {
        ArrayList<T> valueCopied;

        synchronized (lock) {
            valueCopied = new ArrayList<>(_value);
        }

        for (T element: valueCopied) {
            callback.perform(element);
        }
    }
}
