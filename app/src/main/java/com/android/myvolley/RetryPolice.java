package com.android.myvolley;

/**
 * Created by yikfrl on 2018/1/29.
 */

public interface RetryPolice {
    public int getCurrentTimeout();
    public int getCurrentRetryCount();
    public void retry(VolleyError error) throws VolleyError;
}
