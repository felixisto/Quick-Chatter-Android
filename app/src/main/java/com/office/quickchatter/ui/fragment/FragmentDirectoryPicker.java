package com.office.quickchatter.ui.fragment;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.SimpleCallback;

public class FragmentDirectoryPicker extends FragmentFilePicker {
    private Router.System _router;
    private BasePresenter.FilePicker _presenter;
    private Callback<Path> _pickCallback;

    public FragmentDirectoryPicker(@NonNull Router.System router,
                                   @NonNull BasePresenter.FilePicker presenter,
                                   @NonNull Callback<Path> pickCallback,
                                   @NonNull SimpleCallback noPickCallback,
                                   @NonNull String description) {
        super(router, presenter, pickCallback, noPickCallback, description);

        _presenter = presenter;
        _pickCallback = pickCallback;
    }

    // # FragmentFilePicker

    @Override
    public void onPickClick() {
        try {
            _presenter.pickDirectory(_presenter.getSystemDirectorySubpresenter().getCurrentDirectory());
        } catch (Exception e) {

        }
    }

    // # PresenterDelegate.FilePicker

    @Override
    public void onPickFile(@NonNull FilePath path) throws Exception {

    }

    @Override
    public void onPickDirectory(@NonNull DirectoryPath path) throws Exception {
        _router.navigateBack();

        _pickCallback.perform(path);
    }
}
