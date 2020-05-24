package com.office.quickchatter.filesystem.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.utilities.CollectionUtilities;

import java.util.ArrayList;
import java.util.List;

// A list of directory entities (files and directories).
// Thread safe: yes (immutable)
public class DirectoryContents {
    private final @NonNull List<EntityInfo> _entities;

    public static @NonNull DirectoryContents buildBlank() {
        return new DirectoryContents();
    }

    public DirectoryContents() {
        this._entities = new ArrayList<EntityInfo>();
    }

    public DirectoryContents(@Nullable List<EntityInfo> entities) {
        this._entities = entities != null ? CollectionUtilities.copy(entities) : new ArrayList<EntityInfo>();
    }

    public @NonNull List<EntityInfo> entitiesCopy() {
        return CollectionUtilities.copy(_entities);
    }

    public int size() {
        return _entities.size();
    }

    public @NonNull EntityInfo get(int index) {
        return _entities.get(index);
    }
}
