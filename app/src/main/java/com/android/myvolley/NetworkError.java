package com.android.myvolley;

/**
 * Created by yangz on 2018/1/31.
 */

public class NetworkError extends VolleyError {
    public NetworkError() {
    }

    public NetworkError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public NetworkError(Throwable cause) {
        super(cause);
    }
}
