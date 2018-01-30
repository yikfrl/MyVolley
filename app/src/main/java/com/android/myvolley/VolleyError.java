package com.android.myvolley;

/**
 * Created by yangz on 2018/1/29.
 */

public class VolleyError extends Exception {
    public final NetworkResponse networkResponse;
    private long networkTimeMs;

    public VolleyError() {
        networkResponse = null;
    }

    public VolleyError(NetworkResponse networkResponse) {
        this.networkResponse = networkResponse;
    }

    public VolleyError(String exceptionMessage) {
        super(exceptionMessage);
        networkResponse = null;
    }

    public VolleyError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        networkResponse = null;
    }

    public VolleyError(Throwable cause) {
        super(cause);
        networkResponse = null;
    }

    void setNetworkTimeMs(long networkTimeMs){
        this.networkTimeMs = networkTimeMs;
    }

    public long getNetworkTimeMs(){
        return networkTimeMs;
    }
}
