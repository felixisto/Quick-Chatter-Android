package com.office.quickchatter.presenter.worker;

import androidx.annotation.NonNull;

import com.office.quickchatter.filesystem.fundamentals.FilePath;
import com.office.quickchatter.filesystem.fundamentals.Path;
import com.office.quickchatter.utilities.Callback;
import com.office.quickchatter.utilities.SimpleCallback;

public interface SendFilePerformerDelegate {
    void onAskedToReceiveFile(@NonNull Callback<Path> accept,
                              @NonNull SimpleCallback deny,
                              @NonNull String name,
                              @NonNull String description);

    void onOtherSideAcceptedTransferAsk();
    void onOtherSideDeniedTransferAsk();

    void onFileTransferComplete(@NonNull FilePath path);
    void onFileTransferCancelled();

    void fileTransferProgressUpdate(double progress);

    void fileSaveFailed(@NonNull Exception error);
}
