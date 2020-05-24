package com.office.quickchatter.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.R;
import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.presenter.BasePresenterDelegate;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Logger;

public class FragmentFileSystemNavigation extends Fragment implements BasePresenterDelegate.FileSystemNavigation {
    private BasePresenter.FileSystemNavigation _presenter;
    private boolean _startPresenter;

    private ImageButton backButton;
    private TextView locationText;

    public static @NonNull FragmentFileSystemNavigation build(@NonNull BasePresenter.FileSystemNavigation presenter) {
        return build(presenter, true);
    }

    public static @NonNull FragmentFileSystemNavigation build(@NonNull BasePresenter.FileSystemNavigation presenter, boolean startPresenter) {
        FragmentFileSystemNavigation fragment = new FragmentFileSystemNavigation();
        fragment._presenter = presenter;
        fragment._startPresenter = startPresenter;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");
        View root = inflater.inflate(R.layout.fragment_filesystemnavigation, container, false);
        setupFirstTimeUI(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (_startPresenter) {
            _presenter.start(this);
        }
    }

    private void setupFirstTimeUI(@NonNull View root) {
        backButton = root.findViewById(R.id.backButton);
        locationText = root.findViewById(R.id.locationText);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _presenter.navigateBack();
            }
        });
    }

    // # PresenterDelegate.FileSystemNavigation

    @Override
    public void setCurrentPath(@NonNull DirectoryPath path) {
        locationText.setText(path.toString());

        if (_presenter.getRootDirectoryPath().equals(path)) {
            backButton.setVisibility(View.GONE);
        } else {
            backButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setEntityData(@NonNull FileSystemEntityViewModel entity) {
    }
}
