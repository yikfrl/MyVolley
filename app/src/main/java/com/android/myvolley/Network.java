package com.android.myvolley;

/**
 * Created by yangz on 2018/1/29.
 */

public interface Network {
    public NetworkResponse performRequest(Request<?> request) throws VolleyError;
}
