package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Collection of utility methods for converting dates to strings.
 *
 * @author Bytevi
 */
public class DateUtilities {
    public static final SimpleDateFormat StandartFormatter = new SimpleDateFormat("YYY.MM.dd HH:mm");
    public static final SimpleDateFormat TimestampFormatter = new SimpleDateFormat("YYY.MM.dd HH:mm:ss.SSS");
    public static final SimpleDateFormat YYYMMDDFormatter = new SimpleDateFormat("YYY.MM.dd");
    public static final SimpleDateFormat MMDDFormatter = new SimpleDateFormat("MM.dd");
    public static final SimpleDateFormat HMSFormatter = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat HMSMMFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
    public static final SimpleDateFormat HMFormatter = new SimpleDateFormat("HH:mm");

    public static @NonNull String asStandartDate(@NonNull Date date)
    {
        return StandartFormatter.format(date);
    }

    public static @NonNull String asTimestamp(@NonNull Date date)
    {
        return TimestampFormatter.format(date);
    }

    public static @NonNull String asYearMonthDay(@NonNull Date date)
    {
        return YYYMMDDFormatter.format(date);
    }

    public static @NonNull String asMonthDay(@NonNull Date date)
    {
        return MMDDFormatter.format(date);
    }

    public static @NonNull String asHourMinuteSecond(@NonNull Date date)
    {
        return HMSFormatter.format(date);
    }

    public static @NonNull String asHourMinuteSecondMilisecond(@NonNull Date date)
    {
        return HMSMMFormatter.format(date);
    }

    public static @NonNull String asHourMinute(@NonNull Date date)
    {
        return HMFormatter.format(date);
    }
}
