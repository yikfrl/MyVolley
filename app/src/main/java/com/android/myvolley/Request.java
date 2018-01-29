package com.android.myvolley;

/**
 * Created by yangz on 2018/1/29.
 */

public abstract class Request<T> implements Comparable<Request> {

    abstract protected Response<T> parseNetwordResponse(NetworkResponse response);

    abstract protected void deliverResponse(T response);

    @Override
    public int compareTo(Request another) {
        return 0;
    }
}
