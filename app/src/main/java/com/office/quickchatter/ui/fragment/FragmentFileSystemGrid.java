package com.office.quickchatter.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.R;
import com.office.quickchatter.filesystem.fundamentals.DirectoryPath;
import com.office.quickchatter.presenter.BasePresenterDelegate;
import com.office.quickchatter.ui.adapters.AdapterData;
import com.office.quickchatter.ui.adapters.FileSystemAdapterData;
import com.office.quickchatter.ui.adapters.FileSystemGridAdapter;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.Logger;

import java.util.List;

public class FragmentFileSystemGrid extends Fragment implements BasePresenterDelegate.FileSystemDirectory {
    private Callback<Integer> _onItemClick;
    private Callback<Double> _onScroll;

    private LayoutInflater inflater;
    private GridView grid;

    public static @NonNull FragmentFileSystemGrid build(@NonNull Callback<Integer> onItemClick, @NonNull Callback<Double> onScroll) {
        FragmentFileSystemGrid fragment = new FragmentFileSystemGrid();
        fragment._onItemClick = onItemClick;
        fragment._onScroll = onScroll;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.message(this, "onCreateView()");
        this.inflater = inflater;
        View root = inflater.inflate(R.layout.fragment_filesystemgrid, container, false);
        setupFirstTimeUI(root);
        return root;
    }

    private void setupFirstTimeUI(@NonNull View root) {
        grid = root.findViewById(R.id.grid);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _onItemClick.perform(position);
            }
        });

        grid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                double value = (firstVisibleItem + visibleItemCount) / 2.0;

                if (firstVisibleItem == 0) {
                    value = 0;
                }

                _onScroll.perform(value);
            }
        });
    }

    public void scrollToItemIndex(int index) {
        if (grid == null) {
            return;
        }

        if (index >= grid.getCount()) {
            return;
        }

        grid.setSelection(index);
    }

    public void scrollTo(double value) {
        scrollToItemIndex((int)value);
    }

    // # PresenterDelegate.FileSystemDirectory

    @Override
    public void setCurrentPath(@NonNull DirectoryPath path) {

    }

    @Override
    public void setEntityData(@NonNull List<FileSystemEntityViewModel> entities) {
        AdapterData<FileSystemEntityViewModel> data = new FileSystemAdapterData(entities);
        FileSystemGridAdapter adapter = new FileSystemGridAdapter(inflater, data);
        grid.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
