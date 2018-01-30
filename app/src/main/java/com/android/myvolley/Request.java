package com.android.myvolley;

/**
 * Created by yangz on 2018/1/29.
 */
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.android.myvolley.VolleyLog.MarkerLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

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
    private boolean mResponseDelivered = false;
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

    public int getMethod(){
        return mMethod;
    }

    public Request<?> setTag(Object tag){
        mTag = tag;
        return this;
    }

    public Object getTag(){
        return mTag;
    }

    public Response.ErrorListener getErrorListener(){
        return mErrorListener;
    }

    public int getTrafficStatsTag(){
        return mDefaultTrafficStatsTag;
    }

    private static int findDefaultTrafficStatsTag(String url){
        if(!TextUtils.isEmpty(url)){
            Uri uri = Uri.parse(url);
            if(uri != null){
                String host = uri.getHost();
                if(host != null){
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    public Request<?> setRetryPolice(RetryPolice retryPolice){
        mRetryPolice = retryPolice;
        return this;
    }

    public void addMarker(String tag){
        if(MarkerLog.ENABLED){
            mEventLog.add(tag, Thread.currentThread().getId());
        }
    }

    void finish(final String tag){
        if(mRequestQueue != null){
            mRequestQueue.finish(this);
            onFinish();
        }
        if(MarkerLog.ENABLED){
            final long threadId = Thread.currentThread().getId();
            if(Looper.myLooper() != Looper.getMainLooper()){
                //TODO
                // If we finish marking off of the main thread, we need to
                // actually do it on the main thread to ensure correct ordering.
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        mEventLog.add(tag,threadId);
                        mEventLog.finish(this.toString());
                    }
                });
                return;
            }
            mEventLog.add(tag, threadId);
            mEventLog.finish(this.toString());
        }
    }

    protected void onFinish(){
        mErrorListener = null;
    }

    public Request<?> setRequestQueue(RequestQueue requestQueue){
        mRequestQueue = requestQueue;
        return this;
    }

    public final Request<?> setSequence(int sequence){
        mSequence = sequence;
        return this;
    }

    public final int getSequence(){
        if(mSequence == null){
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return mSequence;
    }

    public String getUrl(){
        return (mRedirectUrl != null)?mRedirectUrl:mUrl;
    }

    public String getOriginUrl(){
        return mUrl;
    }

    public String getIdentifier(){
        return mIdentifier;
    }

    public void setRedirectUrl(String redirectUrl){
        mRedirectUrl = redirectUrl;
    }

    public String getCacheKey(){
        return mMethod + ":" + mUrl;
    }

    public Request<?> setCacheEntry(Cache.Entry entry){
        mCacheEntry = entry;
        return this;
    }

    public Cache.Entry getCacheEntry(){
        return mCacheEntry;
    }

    public void cancel(){
        mCanceled = true;
    }

    public boolean isCanceled(){
        return mCanceled;
    }

    public Map<String, String> getHeaders() throws AuthFailureError{
        return Collections.emptyMap();
    }

    @Deprecated
    protected Map<String, String> getPostParams() throws AuthFailureError{
        return getParams();
    }
    @Deprecated
    protected String getPostParamsEncoding() throws AuthFailureError{
        return getParamsEncoding();
    }
    @Deprecated
    public String getPostBodyContentType() throws AuthFailureError{
        return getBodyContentType();
    }
    @Deprecated
    public byte[] getPostBody() throws AuthFailureError{
        Map<String, String> postParams = getPostParams();
        if(postParams != null && postParams.size()>0){
            return encodeParameters(postParams, getPostParamsEncoding());
        }
        return null;
    }

    public Map<String, String> getParams() throws AuthFailureError{
        return null;
    }

    protected String getParamsEncoding() throws AuthFailureError{
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType() throws AuthFailureError{
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    public byte[] getBody() throws AuthFailureError{
        Map<String, String> params = getParams();
        if(params != null && params.size()>0){
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    public byte[] encodeParameters(Map<String,String> params, String paramsEncoding){
        StringBuilder encodedParams = new StringBuilder();
        try {
            for(Map.Entry<String,String> entry : params.entrySet()){
                encodedParams.append(URLEncoder.encode(entry.getKey(),paramsEncoding));
                encodedParams.append("=");
                encodedParams.append(URLEncoder.encode(entry.getValue(),paramsEncoding));
                encodedParams.append("&");
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, e);
        }
    }

    public final Request<?> setShouldCache(boolean shouldCache){
        mShouldCache = shouldCache;
        return this;
    }

    public final boolean shouldCache(){
        return mShouldCache;
    }

    public enum Priority{
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    public Priority getPriority(){
        return Priority.NORMAL;
    }

    public final int getTimeoutMs(){
        return mRetryPolice.getCurrentTimeout();
    }

    public RetryPolice getRetryPolice(){
        return mRetryPolice;
    }

    public void mardDeliveried(){
        mResponseDelivered = true;
    }

    public boolean hasHadDeliveried(){
        return mResponseDelivered;
    }

    abstract protected Response<T> parseNetwordResponse(NetworkResponse response);

    protected VolleyError parseNetworkError(VolleyError volleyError){
        return volleyError;
    }

    abstract protected void deliverResponse(T response);

    public void deliverError(VolleyError error){
        if(mErrorListener != null){
            mErrorListener.onErrorResponse(error);
        }
    }

    //TODO
    @Override
    public int compareTo(Request other) {
        Priority left = this.getPriority();
        Priority right = this.getPriority();

        return left == right ?
                this.mSequence - other.mSequence :
                right.ordinal() - left.ordinal();
    }

    @Override
    public String toString() {
        String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
        return (mCanceled ? "[X] ":"[ ] ") + getUrl() + " " + trafficStatsTag + " " + getPriority() + " " + mSequence;
    }

    private static long sCounter;
    private static String createIdentifier(int method, String url){
        return InternalUtils.sha1Hash("Request:"+method+":"+url+":"+System.currentTimeMillis()+":"+(sCounter++));
    }
}
