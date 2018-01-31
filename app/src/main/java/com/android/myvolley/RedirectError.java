package com.android.myvolley;

/**
 * Created by yangz on 2018/1/31.
 */

public class RedirectError extends VolleyError {
    public RedirectError() {
    }

    public RedirectError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public RedirectError(Throwable cause) {
        super(cause);
    }
}
