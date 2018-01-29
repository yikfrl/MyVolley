package com.android.myvolley;

/**
 * Created by yangz on 2018/1/29.
 */
import android.net.TrafficStats;

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

    private final int mMethod;
    private final String mUrl;
    private String mRedirectUrl;
    private String mIdentifier;
    /** Default tag for {@link TrafficStats}. */
    private final int mDefaultTrafficStatsTag;
    private Response.ErrorListener mErrorListener;
    /** Sequence number of this request, used to enforce FIFO ordering. */
    private Integer mSequence;
    private RequestQueue mRequestQueue;
    private boolean mShouldCache = true;
    private boolean mCanceled = false;
    private boolean mResionseDelivered = false;
    private RetryPolice mRetryPolice;
    private Cache.Entry mCacheEntry = null;
    private Object mTag;

    @Deprecated
    public Request(String url, Response.ErrorListener listener){
        this(Method.DEPRECATED_GET_OR_POST, url, listener);
    }

    public Request(int method, String url, Response.ErrorListener listener){
        mMethod = method;
        mUrl = url;
        mIdentifier = createIdentifier(method, url);
        mErrorListener = listener;
        setRetryPolice(new DefaultRetryPolice());

        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }



    abstract protected Response<T> parseNetwordResponse(NetworkResponse response);

    abstract protected void deliverResponse(T response);

    @Override
    public int compareTo(Request another) {
        return 0;
    }

    private static long sCounter;
    private static String createIdentifier(int method, String url){
        return InternalUtils.sha1Hash("Request:"+method+":"+url+":"+System.currentTimeMillis()+":"+(sCounter++));
    }
}
