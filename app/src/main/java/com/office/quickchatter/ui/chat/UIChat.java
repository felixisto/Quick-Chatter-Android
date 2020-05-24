package com.office.quickchatter.ui.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.quickchatter.utilities.StringUtilities;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UIChat {
    public final @NonNull String newLine = "\n";

    public final @NonNull String localName;
    public final @NonNull String clientName;
    public final boolean displayDate;
    public final int maxLines;

    private @NonNull String _log = "";

    private @Nullable String _bottomLine = null;

    public UIChat(@NonNull String localName, @NonNull String clientName, boolean displayDate, int maxLines) {
        this.localName = localName;
        this.clientName = clientName;
        this.displayDate = displayDate;
        this.maxLines = maxLines;
    }

    public @NonNull String getLog() {
        return _log;
    }

    public @NonNull String parseStringForSendMessage(@NonNull byte[] bytes) {
        String result = new String(bytes, Charset.defaultCharset());
        return buildTimestamp(localName) + result;
    }

    public @NonNull String parseStringForReceiveMessage(@NonNull byte[] bytes) {
        String result = new String(bytes, Charset.defaultCharset());
        return buildTimestamp(clientName) + result;
    }

    public void onMessageReceived(@NonNull String message) {
        addTextLine(message);
    }

    public void onMessageSent(@NonNull String message) {
        _log = _log.concat(newLine + message);
    }

    public void addTextLine(@NonNull String message) {
        // Remove first line if the max lines is exceeded
        if (numberOfTextLines() + 1 >= maxLines) {
            int firstNewLine = _log.indexOf(newLine);

            if (firstNewLine >= 0 && firstNewLine+1 < _log.length()) {
                _log = _log.substring(firstNewLine+1);
            }
        }

        if (!_log.isEmpty()) {
            _log = _log.concat(newLine + message);
        } else {
            _log = message;
        }
    }

    public @NonNull String buildTimestamp(@NonNull String name) {
        String timestamp = displayDate ? getCurrentTimestamp() + " " : "";

        timestamp = timestamp.concat(!name.isEmpty() ? name + ": " : "");

        return timestamp;
    }

    public @NonNull String getCurrentTimestamp() {
        String pattern = "HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return simpleDateFormat.format(new Date());
    }

    public int numberOfTextLines() {
        return StringUtilities.occurrencesCount(_log, newLine);
    }

    public void setBottomLineText(@NonNull String message) {
        removeBottomLineText();

        if (message.isEmpty()) {
            return;
        }

        _bottomLine = message;

        _log = _log.concat(buildBottomLineText(message));
    }

    public void removeBottomLineText() {
        if (_bottomLine == null) {
            return;
        }

        _log = StringUtilities.replaceLast(_log, buildBottomLineText(_bottomLine), "");

        _bottomLine = null;
    }

    private @NonNull String buildBottomLineText(@NonNull String message) {
        return newLine + newLine  + message;
    }
}
