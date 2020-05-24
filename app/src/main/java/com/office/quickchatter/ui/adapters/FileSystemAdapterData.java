package com.office.quickchatter.ui.adapters;

import androidx.annotation.NonNull;

import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.CollectionUtilities;

import java.util.List;

public class FileSystemAdapterData implements AdapterData<FileSystemEntityViewModel> {
    private final @NonNull List<FileSystemEntityViewModel> _data;

    public FileSystemAdapterData(@NonNull List<FileSystemEntityViewModel> data) {
        this._data = CollectionUtilities.copy(data);
    }

    @Override
    public AdapterData<FileSystemEntityViewModel> copy() {
        return new FileSystemAdapterData(_data);
    }

    @Override
    public @NonNull List<FileSystemEntityViewModel> getValues() {
        return CollectionUtilities.copy(_data);
    }

    @Override
    public int count() {
        return _data.size();
    }

    @Override
    public @NonNull FileSystemEntityViewModel getValue(int index) {
        return _data.get(index);
    }
}
