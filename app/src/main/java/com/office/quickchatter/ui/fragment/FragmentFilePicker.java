package com.office.quickchatter.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.office.quickchatter.R;
import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.navigation.Router;
import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.presenter.BasePresenterDelegate;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Logger;
import com.office.quickchatter.utilities.LooperService;
import com.office.quickchatter.utilities.SimpleCallback;

public class FragmentFilePicker extends Fragment implements BasePresenterDelegate.FilePicker {
    private Router.System _router;
    private BasePresenter.FilePicker _presenter;

    private final Callback<Path> _pickCallback;
    private final SimpleCallback _noPickCallback;

    private String _description;

    private TextView labelDescription;
    private ImageButton buttonClose;
    private Button buttonPick;

    // The file system fragment will pick files for us. No need to check or do anything in this fragment.
    private FragmentFileSystemList fragmentFileSystem;
    private FragmentFileSystemNavigation fragmentNavigation;

    private FragmentTransaction subfragmentTransaction;

    public static @NonNull FragmentFilePicker build(@NonNull Router.System router,
                                                    @NonNull BasePresenter.FilePicker presenter,
                                                    @NonNull Callback<Path> pickCallback,
                                                    @NonNull SimpleCallback noPickCallback,
                                                    @NonNull String description) {
        FragmentFilePicker fragment = new FragmentFilePicker(router, presenter, pickCallback, noPickCallback, description);
        return fragment;
    }

    public FragmentFilePicker(@NonNull Router.System router,
                              @NonNull BasePresenter.FilePicker presenter,
                              @NonNull Callback<Path> pickCallback, @NonNull SimpleCallback noPickCallback,
                              @NonNull String description) {
        _router = router;
        _description = description;
        _presenter = presenter;
        _pickCallback = pickCallback;
        _noPickCallback = noPickCallback;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");
        View root = inflater.inflate(R.layout.fragment_filepicker, container, false);
        setupFirstTimeUI(root);
        return root;
    }

    private void setupFirstTimeUI(@NonNull View root) {
        labelDescription = root.findViewById(R.id.labelDescription);
        buttonClose = root.findViewById(R.id.buttonClose);
        buttonPick = root.findViewById(R.id.buttonPick);

        if (isSelectButtonEnabled()) {
            buttonPick.setVisibility(View.VISIBLE);
        } else {
            buttonPick.setVisibility(View.GONE);
        }

        labelDescription.setText(_description);

        // Subfragments construction and placement
        FragmentManager manager = getFragmentManager();

        // Subfragments callbacks
        Callback<Integer> onItemClick = new Callback<Integer>() {
            @Override
            public void perform(Integer index) {
                onItemClick(index);
            }
        };

        fragmentNavigation = FragmentFileSystemNavigation.build(_presenter.getNavigationSubpresenter(), false);
        fragmentFileSystem = FragmentFileSystemList.build(onItemClick);

        if (manager != null) {
            subfragmentTransaction = manager.beginTransaction().replace(R.id.contentNavigation, fragmentNavigation);
            subfragmentTransaction.replace(R.id.contentDirectory, fragmentFileSystem);
            subfragmentTransaction.commit();
        }

        subfragmentTransaction.runOnCommit(new Runnable() {
            @Override
            public void run() {
                startPresenter();
            }
        });

        // Callbacks
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseClick();
            }
        });

        if (isSelectButtonEnabled()) {
            buttonPick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPickClick();
                }
            });
        }
    }

    private void startPresenter() {
        final FragmentFilePicker self = this;

        // Starting this is difficult, because its shared by 3 fragments
        LooperService.getShared().performOnMain(new SimpleCallback() {
            @Override
            public void perform() {
                _presenter.start(self, fragmentNavigation, fragmentFileSystem);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // # FragmentFilePicker user events

    public boolean isSelectButtonEnabled() {
        return true;
    }

    public void onCloseClick() {
        _presenter.closeWithoutPick();
    }

    public void onPickClick() {
        // Do nothing by default
    }

    public void onItemClick(int index) {
        if (index < 0) {
            return;
        }

        // If clicked entity is directory, navigate
        BasePresenter.FileSystemDirectory dirPresenter = _presenter.getSystemDirectorySubpresenter();
        FileSystemEntityViewModel entity = dirPresenter.getEntityInfoAt(index);

        if (entity == null) {
            return;
        }

        if (entity.isDirectory) {
            dirPresenter.navigateToEntityAt(index);
        } else {
            onPickFileEntity(entity);
        }
    }

    public void onPickFileEntity(@NonNull FileSystemEntityViewModel entity) {
        _presenter.pickFile(entity);
    }

    // # PresenterDelegate.FilePicker

    @Override
    public void setCurrentPath(@NonNull DirectoryPath path) {

    }

    @Override
    public void onCloseWithoutPicking() {
        _router.navigateBack();

        _noPickCallback.perform();
    }

    @Override
    public void onPickFile(@NonNull FilePath path) throws Exception {
        _router.navigateBack();

        _pickCallback.perform(path);
    }

    @Override
    public void onPickDirectory(@NonNull DirectoryPath path) throws Exception {
        // Do nothing, this fragment picks only files
    }
}
