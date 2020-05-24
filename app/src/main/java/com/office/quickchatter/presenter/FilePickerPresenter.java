package com.office.quickchatter.presenter;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.filesystem.model.DirectoryContents;
import com.office.quickchatter.filesystem.model.EntityInfo;
import com.office.quickchatter.filesystem.repository.RepoDirectory;
import com.office.quickchatter.filesystem.repository.RepoDirectoryCache;
import com.office.quickchatter.filesystem.repository.Repository;
import com.office.quickchatter.filesystem.worker.loader.FileSystemLoader;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.Parser;
import com.office.quickchatter.utilities.SafeMutableArray;
import com.office.quickchatter.utilities.SimpleCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/// A massive presenter that implements various file picking functionality.
public class FilePickerPresenter implements BasePresenter.FilePicker, BasePresenter.FileSystemNavigation, BasePresenter.FileSystemDirectory {
    private @NonNull Context _context;
    private @Nullable BasePresenterDelegate.FileSystemDirectory _dirDelegate;
    private @Nullable BasePresenterDelegate.FileSystemNavigation _navDelegate;
    private @Nullable BasePresenterDelegate.FilePicker _pickerDelegate;

    private final EntityInfo.Directory _rootInfo;
    private final @NonNull Repository.MutableDirectory _rootRepo;
    private final @NonNull FileSystemLoader _loader;
    private final @NonNull Parser<EntityInfo, FileSystemEntityViewModel> _parser;

    private final @NonNull RepoDirectoryCache _cache = new RepoDirectoryCache();

    private final @NonNull AtomicReference<DirectoryPath> _currentPath = new AtomicReference<>();

    private final @NonNull RepoDirectory _currentDirectory;
    private final @NonNull SafeMutableArray<FileSystemEntityViewModel> _directoryContentsViewModels = new SafeMutableArray<>();

    public FilePickerPresenter(@NonNull Context context,
                               @NonNull FileSystemLoader loader,
                               @NonNull EntityInfo.Directory rootInfo,
                               @NonNull Parser<EntityInfo, FileSystemEntityViewModel> parser) {
        _context = context;

        _loader = loader;
        _parser = parser;

        _rootInfo = rootInfo;
        _rootRepo = new RepoDirectory(_rootInfo);

        _currentPath.set(_rootInfo.getDirPath());

        _currentDirectory = new RepoDirectory(_rootRepo);
    }

    // # Presenter.FilePicker

    @Override
    public void start(@NonNull BasePresenterDelegate.FilePicker filePickerDelegate,
                      @NonNull BasePresenterDelegate.FileSystemNavigation navigationDelegate,
                      @NonNull BasePresenterDelegate.FileSystemDirectory directoryDelegate) {
        _pickerDelegate = filePickerDelegate;
        start(navigationDelegate);
        start(directoryDelegate);
    }

    @Override
    public @NonNull DirectoryPath getDirectoryPath() {
        return _currentPath.get();
    }

    @Override
    public void navigateBack() {
        // Already at root
        if (getDirectoryPath().equals(_rootInfo.getDirPath())) {
            return;
        }

        Logger.message(this, "Navigate back");

        final Uri parentPath = getDirectoryPath().pathWithoutLastComponent().getURL();
        final DirectoryPath previousDirPath = new DirectoryPath(parentPath);

        LooperService.getShared().performInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                try {
                    navigateTo(_loader.readDirectoryInfo(previousDirPath));
                } catch (Exception e) {

                }
            }
        });
    }

    // # Presenter.FileSystemDirectory

    @Override
    public void start(@NonNull BasePresenterDelegate.FileSystemDirectory delegate) {
        _dirDelegate = delegate;

        updateDirectoryContents();
    }

    @Override
    public @NonNull FileSystemEntityViewModel getCurrentDirectory() {
        try {
            return _parser.parse(_currentDirectory.getInfo());
        } catch (Exception e) {
            // Not sure how to handle this
            return null;
        }
    }

    @Override
    public @Nullable FileSystemEntityViewModel getEntityInfoAt(int index) {
        List<EntityInfo> entities = _currentDirectory.getContents().entitiesCopy();

        if (index < 0 || index >= entities.size()) {
            Logger.warning(this, "Cannot navigate to item out of bounds index " + index);
            return null;
        }

        try {
            return _parser.parse(entities.get(index));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void navigateToEntityAt(int index) {
        List<EntityInfo> entities = _currentDirectory.getContents().entitiesCopy();

        if (index < 0 || index >= entities.size()) {
            Logger.warning(this, "Cannot navigate to item out of bounds index " + index);
            return;
        }

        Logger.message(this, "Try to navigate to entity at index " + index);

        navigateTo(entities.get(index));
    }

    @Override
    public void scrollBy(double scrollValue) {

    }

    @Override
    public void closeWithoutPick() {
        Logger.message(this, "Close without picking");

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_pickerDelegate == null) {
                    return;
                }

                try {
                    _pickerDelegate.onCloseWithoutPicking();
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void pickFile(@NonNull FileSystemEntityViewModel entity) {
        EntityInfo info = entityInfoForViewModel(entity);

        if (info == null || !(info instanceof EntityInfo.File)) {
            Logger.error(this, "Failed to pick file, given model is invalid or corrupted");
            return;
        }

        final FilePath path = ((EntityInfo.File)info).getFilePath();

        Logger.message(this, "Pick file " + path.toString());

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_pickerDelegate == null) {
                    return;
                }

                try {
                    _pickerDelegate.onPickFile(path);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void pickDirectory(@NonNull FileSystemEntityViewModel entity) {
        EntityInfo info = entityInfoForViewModel(entity);

        if (info == null || !(info instanceof EntityInfo.Directory)) {
            Logger.error(this, "Failed to pick directory, given model is invalid or corrupted");
            return;
        }

        final DirectoryPath path = ((EntityInfo.Directory)info).getDirPath();

        Logger.message(this, "Pick directory " + path.toString());

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_pickerDelegate == null) {
                    return;
                }

                try {
                    _pickerDelegate.onPickDirectory(path);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public @NonNull FileSystemNavigation getNavigationSubpresenter() {
        return this;
    }

    @Override
    public @NonNull FileSystemDirectory getSystemDirectorySubpresenter() {
        return this;
    }

    // # Presenter.FileSystemNavigation

    @Override
    public void start(@NonNull BasePresenterDelegate.FileSystemNavigation delegate) {
        _navDelegate = delegate;

        updateNavigationInfo();
    }

    @Override
    public @NonNull DirectoryPath getRootDirectoryPath() {
        return _rootInfo.getDirPath();
    }

    // # Helpers

    // Get the corresponding entitiy info for the given entity view model.
    public @Nullable EntityInfo entityInfoForViewModel(@NonNull FileSystemEntityViewModel vm) {
        String currentDirName = _currentDirectory.getInfo().getPath().getLastComponent();

        if (currentDirName.equals(vm.name)) {
            return _currentDirectory.getInfo();
        }

        @Nullable EntityInfo matchingInfo = null;

        for (EntityInfo info : _currentDirectory.getContents().entitiesCopy()) {
            if (info.getPath().getLastComponent().equals(vm.name)) {
                matchingInfo = info;
                break;
            }
        }

        return matchingInfo;
    }

    // Get the path for the given entity view model.
    public @Nullable Path pathOfEntityInfoViewModel(@NonNull FileSystemEntityViewModel vm) {
        EntityInfo info = entityInfoForViewModel(vm);

        if (info != null) {
            return info.getPath();
        }

        return null;
    }

    // # Navigation

    private void navigateTo(@NonNull EntityInfo info) {
        if (info instanceof EntityInfo.Directory) {
            openDirectory(((EntityInfo.Directory) info).getDirPath());
        }
    }

    private void openDirectory(@NonNull DirectoryPath path) {
        Logger.message(this, "Navigate to directory " + path.toString());

        _currentPath.set(path);

        updateDirectoryContents();
    }

    // # Update data

    private void updateDirectoryContents() {
        final FilePickerPresenter self = this;

        LooperService.getShared().performInBackground(new SimpleCallback() {
            @Override
            public void perform() {
                Repository.MutableDirectory repo = getCurrentDirectoryContents();

                if (repo != null) {
                    updateDirectoryContents(repo);
                    updateDelegateDirectoryContents(repo);
                } else {
                    Logger.warning(self, "Failed to retrieve current directory contents");
                    clearDelegateDirectoryContents();
                }
            }
        });
    }

    private void updateNavigationInfo() {
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                if (_navDelegate != null) {
                    _navDelegate.setCurrentPath(getDirectoryPath());
                }
            }
        });
    }

    private @Nullable Repository.MutableDirectory getCurrentDirectoryContents() {
        Repository.MutableDirectory cached = _cache.cachedRepository(_cache.getRelation().buildCacheKeyFromProperty(getDirectoryPath()));

        if (cached != null) {
            return cached;
        }

        try {
            cached = new RepoDirectory(_loader.readDirectoryInfo(getDirectoryPath()));
            _cache.cacheRepository(cached);
        } catch (Exception e) {
        }

        return cached;
    }

    private void updateDirectoryContents(@NonNull Repository.MutableDirectory repo) {
        _currentDirectory.assign(repo);
        _cache.cacheRepository(repo);
    }

    private void clearDelegateDirectoryContents() {
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _directoryContentsViewModels.removeAll();

                if (_dirDelegate == null) {
                    return;
                }

                _dirDelegate.setEntityData(new ArrayList<FileSystemEntityViewModel>());
            }
        });
    }

    private void updateDelegateDirectoryContents(@NonNull Repository.MutableDirectory repo) {
        final ArrayList<FileSystemEntityViewModel> data = new ArrayList<>();

        try {
            data.addAll(parse(repo.getContents()));
        } catch (Exception e) {

        }

        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _directoryContentsViewModels.removeAll();
                _directoryContentsViewModels.addAll(data);

                if (_dirDelegate == null) {
                    return;
                }

                _dirDelegate.setEntityData(data);

                updateNavigationInfo();
            }
        });
    }

    private @NonNull List<FileSystemEntityViewModel> parse(@NonNull DirectoryContents contents) {
        ArrayList<FileSystemEntityViewModel> entities = new ArrayList<>();

        for (EntityInfo entity : contents.entitiesCopy()) {
            try {
                entities.add(_parser.parse(entity));
            } catch (Exception e) {
            }
        }

        return entities;
    }
}
