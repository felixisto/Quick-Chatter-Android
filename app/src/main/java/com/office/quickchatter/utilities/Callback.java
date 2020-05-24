package com.office.quickchatter.utilities;

import androidx.annotation.Nullable;

/**
 * A function callback that takes 1 parameter and returns no value.
 * 
 * @author Bytevi
 */
public abstract class Callback<T> {
    abstract public void perform(T argument);
    
    public static <C> Callback createDoNothing()
    {
        return new Callback<C>() {
            @Override
            public void perform(C argument) {
                // Do nothing
            }
        };
    }
}
