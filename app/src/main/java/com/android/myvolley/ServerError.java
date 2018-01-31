package com.android.myvolley;

/**
 * Created by yangz on 2018/1/31.
 */

public class ServerError extends VolleyError {
    public ServerError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public ServerError() {
        super();
    }
}
