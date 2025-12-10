package com.example.duan1.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Helper class để quản lý polling mechanism cho real-time updates
 */
public class PollingHelper {
    private Handler pollingHandler;
    private Runnable pollingRunnable;
    private static final long DEFAULT_POLLING_INTERVAL = 5000; // 5 giây
    private long pollingInterval;
    private boolean isPollingActive = false;
    private Runnable refreshCallback;
    private String tag;

    public PollingHelper(String tag) {
        this(tag, DEFAULT_POLLING_INTERVAL);
    }

    public PollingHelper(String tag, long interval) {
        this.tag = tag;
        this.pollingInterval = interval;
        this.pollingHandler = new Handler(Looper.getMainLooper());
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    public void startPolling() {
        if (!isPollingActive && refreshCallback != null) {
            isPollingActive = true;
            pollingRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isPollingActive && refreshCallback != null) {
                        refreshCallback.run();
                        pollingHandler.postDelayed(this, pollingInterval);
                    }
                }
            };
            pollingHandler.postDelayed(pollingRunnable, pollingInterval);
            Log.d(tag, "Polling started");
        }
    }

    public void stopPolling() {
        if (isPollingActive) {
            isPollingActive = false;
            if (pollingRunnable != null) {
                pollingHandler.removeCallbacks(pollingRunnable);
            }
            Log.d(tag, "Polling stopped");
        }
    }

    public boolean isPollingActive() {
        return isPollingActive;
    }

    public void setPollingInterval(long interval) {
        this.pollingInterval = interval;
    }
}

