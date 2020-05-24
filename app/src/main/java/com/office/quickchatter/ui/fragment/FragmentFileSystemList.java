package com.office.quickchatter.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.R;
import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.presenter.BasePresenterDelegate;
import com.office.quickchatter.ui.adapters.AdapterData;
import com.office.quickchatter.ui.adapters.FileSystemAdapterData;
import com.office.quickchatter.ui.adapters.FileSystemListAdapter;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Logger;

import java.util.List;

public class FragmentFileSystemList extends Fragment implements BasePresenterDelegate.FileSystemDirectory {
    private Callback<Integer> _onItemClick;

    private LayoutInflater inflater;
    private ListView list;

    public static @NonNull FragmentFileSystemList build(@NonNull Callback<Integer> onItemClick) {
        FragmentFileSystemList fragment = new FragmentFileSystemList();
        fragment._onItemClick = onItemClick;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");
        this.inflater = inflater;
        View root = inflater.inflate(R.layout.fragment_filesystemlist, container, false);
        setupFirstTimeUI(root);
        return root;
    }

    private void setupFirstTimeUI(@NonNull View root) {
        list = root.findViewById(R.id.list);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _onItemClick.perform(position);
            }
        });
    }

    // # PresenterDelegate.FileSystemDirectory

    @Override
    public void setCurrentPath(@NonNull DirectoryPath path) {

    }

    @Override
    public void setEntityData(@NonNull List<FileSystemEntityViewModel> entities) {
        AdapterData<FileSystemEntityViewModel> data = new FileSystemAdapterData(entities);
        FileSystemListAdapter adapter = new FileSystemListAdapter(inflater, data);
        list.setAdapter(adapter);
        adapter.notifyDataSetInvalidated();
    }
}