package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;

public interface Parser <Source, Destination> {
    @NonNull Destination parse(@NonNull Source data) throws Exception;
}
