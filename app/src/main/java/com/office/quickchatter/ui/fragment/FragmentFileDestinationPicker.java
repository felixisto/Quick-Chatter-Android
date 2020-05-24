package com.office.quickchatter.ui.fragment;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.SimpleCallback;

public class FragmentFileDestinationPicker extends FragmentDirectoryPicker {
    private Router.System _router;
    private Callback<Path> _pickCallback;
    // Hold strong reference to this
    private BasePresenter.FileDestinationHandler _fileDestinationHandler;

    public static @NonNull FragmentFilePicker build(@NonNull Router.System router,
                                                    @NonNull BasePresenter.FileDestinationPicker presenter,
                                                    @NonNull BasePresenter.FileDestinationHandler fileDestinationHandler,
                                                    @NonNull Callback<Path> pickCallback,
                                                    @NonNull SimpleCallback noPickCallback,
                                                    @NonNull String description) {
        FragmentFileDestinationPicker fragment = new FragmentFileDestinationPicker(router, presenter, fileDestinationHandler, pickCallback, noPickCallback, description);
        return fragment;
    }

    public FragmentFileDestinationPicker(@NonNull Router.System router,
                                         @NonNull BasePresenter.FileDestinationPicker presenter,
                                         @NonNull BasePresenter.FileDestinationHandler fileDestinationHandler,
                                         @NonNull Callback<Path> pickCallback,
                                         @NonNull SimpleCallback noPickCallback,
                                         @NonNull String description) {
        super(router, presenter, pickCallback, noPickCallback, description);

        _router = router;
        _pickCallback = pickCallback;
        _fileDestinationHandler = fileDestinationHandler;
    }

    // # PresenterDelegate.FilePicker

    @Override
    public void onPickFile(@NonNull FilePath path) throws Exception {
        // Override this and call this, because in FragmentDirectoryPicker this method does nothing
        _router.navigateBack();

        _pickCallback.perform(path);
    }
}
