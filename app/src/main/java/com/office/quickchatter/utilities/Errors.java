package com.office.quickchatter.utilities;

import androidx.annotation.NonNull;

/**
 * Defines commonly used errors.
 * 
 * Calling the methods will always cause an exception to be thrown.
 * 
 * @author Bytevi
 */
public class Errors {
    public static void throwUnknownError(@NonNull String description) throws Exception
    {
        throw new Exception(description);
    }

    public static void throwNullPointerError(@NonNull String description) throws Exception
    {
        throw new Exception(description);
    }

    public static void throwInvalidArgument(@NonNull String description)
    {
        throw new IllegalArgumentException(description);
    }
    
    public static void throwIndexOutOfBounds(@NonNull String description)
    {
        throw new IndexOutOfBoundsException(description);
    }
    
    public static void throwIllegalMathOperation(@NonNull String description)
    {
        throw new ArithmeticException(description);
    }
    
    public static void throwNotImplemented(@NonNull String description) throws RuntimeException
    {
        throw new UnsupportedOperationException(description);
    }

    public static void throwUnsupportedOperation(@NonNull String description) throws RuntimeException
    {
        throw new UnsupportedOperationException(description);
    }
    
    public static void throwIllegalStateError(@NonNull String description) throws RuntimeException
    {
        throw new IllegalStateException(description);
    }
    
    public static void throwInternalLogicError(@NonNull String description) throws RuntimeException
    {
        throw new RuntimeException(description);
    }
    
    public static void throwCannotStartTwice(@NonNull String description) throws RuntimeException
    {
        throw new RuntimeException(description);
    }
    
    public static void throwSerializationFailed(@NonNull String description) throws RuntimeException
    {
        throw new RuntimeException(description);
    }
    
    public static void throwClassNotFound(@NonNull String description) throws ClassNotFoundException
    {
        throw new ClassNotFoundException(description);
    }

    public static void throwTimeoutError(@NonNull String description) throws Exception
    {
        throw new Exception(description);
    }
}
