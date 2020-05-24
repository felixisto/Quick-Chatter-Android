package com.office.quickchatter.filesystem.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.model.DirectoryContents;
import com.office.quickchatter.utilities.DataSize;

// Holds business logic data.
// Usually written to by use cases and read by view models.
// Thread safe: yes
public interface Repository {
    // A repository that can be deep copied and assigned to another prototype of the same type.
    interface Copyable <T> extends Repository {
        @NonNull T copy();
    }

    // A repository that can be deep copied and assigned to another prototype of the same type.
    interface Assignable <T> extends Repository {
        void assign(@NonNull T prototype);
    }

    // A repository that can be deep copied and assigned to another prototype of the same type.
    interface CopyableAndAssignable <T> extends Repository.Copyable <T>, Repository.Assignable <T> {

    }

    // A repository cache, that contains copies of already loaded copies of repositories.
    // Each repository copy is identified by a key string.
    interface Cache <T, C> extends Repository {
        @NonNull CacheKeyToValueRelation<T, C> getRelation();

        @Nullable T cachedRepository(@NonNull String key);
        void cacheRepository(@NonNull T repo);
    }

    // Maps keys to values.
    interface CacheKeyToValueRelation<T, C> {
        @NonNull String buildCacheKeyFromRepo(@NonNull T repo);
        @NonNull String buildCacheKeyFromProperty(@NonNull C property);
    }

    interface Directory extends Repository {
        boolean isRoot();
        @NonNull DirectoryPath getDirPath();
        @NonNull String getAbsolutePath();
        @NonNull String getName();
        @NonNull DataSize getSize();
        @NonNull DirectoryContents getContents();
    }

    interface MutableDirectory extends Directory, Repository.Copyable<MutableDirectory>, Repository.Assignable<Directory> {

    }
}
