package com.office.quickchatter.filesystem.fundamentals;

import androidx.annotation.NonNull;

public enum FileExtension {
    unknown, txt, pdf, doc, dox, jpeg, png, gif, mp3, mp4, avi, mkv, wav, webm, flv, temp, xml;

    public @NonNull String asString() {
        if (this == unknown) {
            return "";
        }

        return this.name();
    }
}
