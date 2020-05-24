package com.office.quickchatter.filesystem.repository;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;

import java.util.HashMap;

public class RepoDirectoryCache extends RepoCache<Repository.MutableDirectory, DirectoryPath> {
    public static @NonNull RepoDirectoryCache buildEmpty() {
        return new RepoDirectoryCache();
    }

    public static @NonNull RepoDirectoryCache build(@NonNull HashMap<String, Repository.MutableDirectory> cache) {
        return new RepoDirectoryCache(cache);
    }

    public static @NonNull RepoDirectoryCache buildWithInitial(@NonNull Repository.MutableDirectory initialCachedRepo) {
        RepoDirectoryCache cache = new RepoDirectoryCache();
        cache.cacheRepository(initialCachedRepo);
        return cache;
    }

    public RepoDirectoryCache() {
        this(new HashMap<String, Repository.MutableDirectory>());
    }

    public RepoDirectoryCache(@NonNull HashMap<String, Repository.MutableDirectory> cache) {
        super(cache);
    }

    public @NonNull CacheKeyToValueRelation<Repository.MutableDirectory, DirectoryPath> getRelation() {
        return new CacheKeyToValueRelation<Repository.MutableDirectory, DirectoryPath>() {
            @Override
            public @NonNull String buildCacheKeyFromRepo(@NonNull Repository.MutableDirectory repo) {
                return repo.getDirPath().getPath();
            }

            @Override
            public @NonNull String buildCacheKeyFromProperty(@NonNull DirectoryPath property) {
                return property.getPath();
            }
        };
    }
}
