package com.android.myvolley;

/**
 * Created by yangz on 2018/1/29.
 */

public interface ResponseDelivery {

    public void postResponse(Request<?> request, Response<?> response);
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable);
    public void postError(Request<?> request, VolleyError error);

}
