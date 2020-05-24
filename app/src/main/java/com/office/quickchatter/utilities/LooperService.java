package com.office.quickchatter.utilities;

import java.util.ArrayList;
import java.util.Collection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LooperService {
    public static final int LOOPER_INTERVAL_MSEC = 100;

    private static LooperService singleton;
    
    private final ClientsManager _clientsManager = new ClientsManager();

    private final Handler _mainHandler;
    private final Runnable _loopingRunnable;

    private final Handler _backgroundHandler;
    
    private LooperService()
    {
        _mainHandler = new Handler(Looper.getMainLooper());
        _loopingRunnable = new Runnable() {
            @Override
            public void run() {
                loop();
                loopAgain();
            }
        };

        HandlerThread background = new HandlerThread("LooperService.Background");
        background.start();
        _backgroundHandler = new Handler(background.getLooper());
        
        loopAgain();
    }

    synchronized public static LooperService getShared()
    {
        if (singleton == null)
        {
            singleton = new LooperService();
        }

        return singleton;
    }

    // # Subscription

    public void subscribe(final LooperClient client)
    {
        _clientsManager.subscribe(client);
    }

    public void unsubscribe(final LooperClient client)
    {
        _clientsManager.unsubscribe(client);
    }

    // # Main

    // Perform callback immediately if caller is on main thread.
    // Otherwise, the callback is performed on main asynchronously.
    public void performOnMain(final SimpleCallback callback)
    {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            callback.perform();
        } else {
            asyncOnMain(callback);
        }
    }

    public void asyncOnMain(final SimpleCallback callback)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.perform();
            }
        };

        _mainHandler.postDelayed(runnable, 1);
    }

    public <T> void asyncOnMain(final Callback<T> callback, @Nullable final T argument)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.perform(argument);
            }
        };

        _mainHandler.postDelayed(runnable, 1);
    }

    public void asyncOnMainAfterDelay(final SimpleCallback callback, @NonNull TimeValue delay)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.perform();
            }
        };

        _mainHandler.postDelayed(runnable, delay.inMS());
    }

    public <T> void asyncOnMainAfterDelay(final Callback<T> callback, @Nullable final T argument, @NonNull TimeValue delay)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.perform(argument);
            }
        };

        _mainHandler.postDelayed(runnable, delay.inMS());
    }

    // # Background

    public void asyncInBackground(final SimpleCallback callback)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.perform();
            }
        };

        _backgroundHandler.postDelayed(runnable, 1);
    }

    public <T> void asyncInBackground(final Callback<T> callback, @Nullable final T argument)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.perform(argument);
            }
        };

        _backgroundHandler.postDelayed(runnable, 1);
    }

    public void asyncInBackgroundAfterDelay(final SimpleCallback callback, @NonNull TimeValue delay)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.perform();
            }
        };

        _backgroundHandler.postDelayed(runnable, delay.inMS());
    }

    public <T> void asyncInBackgroundAfterDelay(final Callback<T> callback, @Nullable final T argument, @NonNull TimeValue delay)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                callback.perform(argument);
            }
        };

        _backgroundHandler.postDelayed(runnable, delay.inMS());
    }

    // Perform callback immediately if caller is on background thread.
    // Otherwise, the callback is performed in the background asynchronously.
    public void performInBackground(final SimpleCallback callback)
    {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            asyncInBackground(callback);
        } else {
            callback.perform();
        }
    }

    // # Private

    private void loop()
    {
        _clientsManager.loop();
    }

    private void loopAgain()
    {
        _mainHandler.postDelayed(_loopingRunnable, LOOPER_INTERVAL_MSEC);
    }
    
    class ClientsManager 
    {
        private ArrayList<LooperClient> _clients = new ArrayList<>();
        
        private final Object mainLock = new Object();
        
        void subscribe(final LooperClient client)
        {
            synchronized (mainLock)
            {
                if (!_clients.contains(client))
                {
                    _clients.add(client);
                }
            }
        }

        void unsubscribe(final LooperClient client)
        {
            synchronized (mainLock)
            {
                _clients.remove(client);
            }
        }
        
        void loop()
        {
            // This must be called on the main thread
            Collection<LooperClient> clients;

            synchronized (mainLock)
            {
                clients = CollectionUtilities.copy(_clients);
            }
            
            for (LooperClient client : clients)
            {
                client.loop();
            }
        }
    }
}
