package com.android.myvolley;

/**
 * Created by yangz on 2018/1/29.
 */
import com.android.myvolley.VolleyLog.MarkerLog;

public abstract class Request<T> implements Comparable<Request> {

    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    public interface Method{
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    private final MarkerLog mEventLog = MarkerLog.ENABLED ? new MarkerLog() : null;



    abstract protected Response<T> parseNetwordResponse(NetworkResponse response);

    abstract protected void deliverResponse(T response);

    @Override
    public int compareTo(Request another) {
        return 0;
    }
}
