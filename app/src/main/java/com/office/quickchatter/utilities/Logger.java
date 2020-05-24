package com.office.quickchatter.utilities;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs various messages to the standard output.
 * 
 * @author Bytevi
 */
public class Logger {
    public static final String DEFAULT_TAG = "quickchatter";

    public static final boolean DISPLAY_TIMESTAMPS = true;
    
    public static final String MESSAGE_ACTION_PREFIX = "#ACTION#";
    public static final String MESSAGE_EVENT_PREFIX = "#EVENT#";
    public static final String WARNING_PREFIX = "#WARNING#";
    public static final String ERROR_PREFIX = "#ERROR#";
    public static final String UTILITY_ERROR_PREFIX = "#UTILITY_ERROR#";
    public static final String SYSTEM_ERROR_PREFIX = "#SYSTEM_ERROR#";
    
    public static final String OVERRIDE_ME_MESSAGE = "Calling non-overriden base method ";
    
    private static final PrintStream _outStream = System.out;
    
    public static void printLine(@Nullable String tag, @Nullable String message)
    {
        if (tag == null || message == null) {
            return;
        }

        Log.v(tag, message);
    }
    
    public static <T> void message(@NonNull T origin, @Nullable String string)
    {
        printLine(generateTag(origin), generateText("", string));
    }
    
    public static <T> void message(@NonNull String prefix, @Nullable String string)
    {
        printLine(DEFAULT_TAG, generateText(prefix, string));
    }
    
    public static <T> void messageAction(@NonNull T origin, @Nullable String string)
    {
        printLine(generateTag(origin), generateText(MESSAGE_ACTION_PREFIX, string));
    }
    
    public static <T> void messageEvent(@NonNull T origin, @Nullable String string)
    {
        printLine(generateTag(origin), generateText(MESSAGE_EVENT_PREFIX, string));
    }
    
    public static <T> void warning(@NonNull String prefix, @Nullable String string)
    {
        String message = WARNING_PREFIX + " " + generateText(prefix, string);
        
        printLine(prefix, message);
    }
    
    public static <T> void warning(@NonNull T origin, @Nullable String string)
    {
        printLine(generateTag(origin), generateText(WARNING_PREFIX, string));
    }
    
    public static <T> void error(@NonNull T origin, @Nullable String string)
    {
        printLine(generateTag(origin), generateText(ERROR_PREFIX, string));
    }
    
    public static <T> void error(@NonNull String prefix, @Nullable String string)
    {
        String message = ERROR_PREFIX + " " + generateText(prefix, string);
        
        printLine(prefix, generateText(prefix, string));
    }
    
    public static <T> void utilityError(@NonNull T origin, @Nullable String string)
    {
        printLine(generateTag(origin), generateText(UTILITY_ERROR_PREFIX, string));
    }
    
    public static <T> void systemError(@NonNull T origin, @Nullable String string)
    {
        printLine(generateTag(origin), generateText(SYSTEM_ERROR_PREFIX, string));
    }
    
    public static <T> void overrideMe(@NonNull T origin, @Nullable String methodName)
    {
        printLine(generateTag(origin), generateText(WARNING_PREFIX, OVERRIDE_ME_MESSAGE + methodName));
    }

    private static <T> String generateTag(@NonNull T origin)
    {
        String name = origin.getClass().getSimpleName();

        if (name != null && name.length() > 0) {
            return name;
        }

        name = origin.getClass().getCanonicalName();

        if (name != null && name.length() > 0) {
            return name;
        }

        return DEFAULT_TAG;
    }

    private static <T> String generateText(@Nullable String label, @Nullable String text)
    {
        String textPrefix = generatePrefix(label);
        
        if (textPrefix.isEmpty())
        {
            return text != null ? text : "";
        }
        
        return textPrefix + ": " + text;
    }
    
    private static @NonNull String generatePrefix(@Nullable String prefix)
    {
        if (!DISPLAY_TIMESTAMPS || prefix == null)
        {
            return prefix != null ? prefix : "";
        }
        
        return prefix.length() > 0 ? getTimestamp() + " " + prefix : getTimestamp();
    }
    
    private static @NonNull String getTimestamp()
    {
        String pattern = "HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        
        return simpleDateFormat.format(new Date());
    }
}
