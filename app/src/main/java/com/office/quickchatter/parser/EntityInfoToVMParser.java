package com.office.quickchatter.parser;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.model.EntityInfo;
import com.office.quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import com.office.quickchatter.utilities.Parser;

public class EntityInfoToVMParser implements Parser<EntityInfo, FileSystemEntityViewModel> {
    @Override
    public @NonNull FileSystemEntityViewModel parse(@NonNull EntityInfo info) throws Exception {
        boolean isDir = EntityInfo.Helper.isDirectory(info);
        String name = info.getPath().getLastComponent();
        String size = "";
        String dateCreated = "";
        String dateModified = "";

        if (info instanceof EntityInfo.Directory) {
            EntityInfo.Directory dir = (EntityInfo.Directory)info;

            isDir = true;
            size = dir.getSize().toString();
        }

        if (info instanceof EntityInfo.File) {
            EntityInfo.File file = (EntityInfo.File)info;

            isDir = false;
            size = file.getSize().toString();
        }

        return new FileSystemEntityViewModel(isDir, name, size, dateCreated, dateModified);
    }
}
