package com.office.quickchatter.filesystem.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;

// Stores copies of repositories in a cache.
public abstract class RepoCache<T extends Repository.Copyable<T>, C> implements Repository.Cache <T, C> {
    private final @NonNull Object lock = new Object();

    private final @NonNull HashMap<String, T> _cache;

    public RepoCache() {
        _cache = new HashMap<String, T>();
    }

    public RepoCache(@NonNull HashMap<String, T> cache) {
        _cache = new HashMap<String, T>(cache);
    }

    @Override
    public @Nullable T cachedRepository(@NonNull String key) {
        synchronized (lock) {
            return _cache.get(key);
        }
    }

    @Override
    public void cacheRepository(@NonNull T repo) {
        String key = getRelation().buildCacheKeyFromRepo(repo);

        if (cachedRepository(key) != null) {
            return;
        }

        T repoCopy = repo.copy();

        synchronized (lock) {
            _cache.put(key, repoCopy);
        }
    }
}
