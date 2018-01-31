package com.android.myvolley.toolbox;

import android.os.SystemClock;

import com.android.myvolley.AuthFailureError;
import com.android.myvolley.Cache;
import com.android.myvolley.Cache.Entry;
import com.android.myvolley.Network;
import com.android.myvolley.NetworkError;
import com.android.myvolley.NetworkResponse;
import com.android.myvolley.NoConnectionError;
import com.android.myvolley.RedirectError;
import com.android.myvolley.Request;
import com.android.myvolley.RetryPolice;
import com.android.myvolley.ServerError;
import com.android.myvolley.TimeoutError;
import com.android.myvolley.VolleyError;
import com.android.myvolley.VolleyLog;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by yangz on 2018/1/31.
 */

public class BasicNetwork implements Network {

    protected static final boolean DEBUG = VolleyLog.DEBUG;

    private static int SLOW_REQUEST_THRESHOLD_MS = 3000;

    private static int DEFAULT_POOL_SIZE = 4096;

    protected final HttpStack mHttpStack;

    protected final ByteArrayPool mPool;

    public BasicNetwork(HttpStack httpStack){
        this(httpStack,new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    public BasicNetwork(HttpStack httpStack, ByteArrayPool pool){
        mHttpStack = httpStack;
        mPool = pool;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        long requestStart = SystemClock.elapsedRealtime();
        while(true){
            HttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, String> responseHeaders = Collections.emptyMap();

            try{
                Map<String, String> headers = new HashMap<>();
                addCacheHeaders(headers, request.getCacheEntry());
                httpResponse = mHttpStack.performRequest(request, headers);
                StatusLine statusLine = httpResponse.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                responseHeaders = convertHeaders(httpResponse.getAllHeaders());

                if(statusCode == HttpStatus.SC_NOT_MODIFIED){
                    Entry entry = request.getCacheEntry();
                    if(entry == null){
                        //TODO 既然是NOT_MODIFIED 说明之前已有缓存，为什么会出现 entry == null 的情景呢？
                        return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED, null,
                                responseHeaders, true, SystemClock.elapsedRealtime() - requestStart);
                    }

                    entry.responseHeaders.putAll(responseHeaders);
                    return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED, entry.data,
                            entry.responseHeaders, true, SystemClock.elapsedRealtime() - requestStart);
                }

                if(statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY){
                    String newUrl = responseHeaders.get("Location");
                    request.setRedirectUrl(newUrl);
                }

                if(httpResponse.getEntity() != null){
                    responseContents = entityToBytes(httpResponse.getEntity());
                }else{
                    responseContents = new byte[0];
                }

                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
                logSlowRequests(requestLifetime, request, responseContents, statusLine);

                if(statusCode < 200 || statusCode >299){
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false,
                        SystemClock.elapsedRealtime() - requestStart);
            } catch (SocketTimeoutException e){
                attemptRetryOnException("Socket",request, new TimeoutError());
            } catch (ConnectTimeoutException e){
                attemptRetryOnException("connection",request,new TimeoutError());
            } catch (MalformedURLException e){
                throw new RuntimeException("Bad URL "+request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                NetworkResponse networkResponse = null;
                if(httpResponse != null){
                    statusCode = httpResponse.getStatusLine().getStatusCode();
                }else{
                    throw new NoConnectionError(e);
                }
                if(statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                        statusCode == HttpStatus.SC_MOVED_TEMPORARILY){
                    VolleyLog.e("Request at %s has been redirected to %s",request.getOriginUrl(),request.getUrl());
                }else{
                    VolleyLog.e("Unexcepted response code %d for %s",statusCode,request.getUrl());
                }
                if(responseContents != null){
                    networkResponse = new NetworkResponse(statusCode, responseContents,
                            responseHeaders, false, SystemClock.elapsedRealtime() - requestStart);
                    if(statusCode == HttpStatus.SC_UNAUTHORIZED ||
                            statusCode == HttpStatus.SC_FORBIDDEN){
                        attemptRetryOnException("auth", request, new AuthFailureError(networkResponse));
                    }else if(statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                            statusCode == HttpStatus.SC_MOVED_PERMANENTLY){
                        attemptRetryOnException("redirect", request, new RedirectError(networkResponse));
                    }else{
                        throw new ServerError(networkResponse);
                    }
                }else{
                    throw new NetworkError(networkResponse);
                }
            }
        }
    }

    private void logSlowRequests(long requestLifetime, Request<?> request, byte[] responseContents, StatusLine statusLine){
        if(DEBUG || requestLifetime > SLOW_REQUEST_THRESHOLD_MS){
            VolleyLog.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], " +
                    "[rc=%d], [retryCount=%d]",request,requestLifetime,responseContents != null?responseContents.length:"null",
                    statusLine.getStatusCode(),request.getRetryPolice().getCurrentRetryCount());
        }
    }

    private static void attemptRetryOnException(String logPrefix, Request<?> request, VolleyError exception) throws VolleyError {
        RetryPolice retryPolice = request.getRetryPolice();
        int oldTimeout = request.getTimeoutMs();

        try {
            retryPolice.retry(exception);
        } catch (VolleyError e) {
            request.addMarker(String.format("%s-timeout-giveup [timeout=%s]", logPrefix,oldTimeout));
            throw e;
        }
        request.addMarker(String.format("%s-retry [timeout=%s]",logPrefix,oldTimeout));
    }

    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry){
        if(entry == null){
            return;
        }

        if(entry.etag != null){
            headers.put("If-None-Match", entry.etag);
        }

        if(entry.lastModified > 0){
            Date refTime = new Date(entry.lastModified);
            headers.put("If-Modified-Since", DateUtils.formatDate(refTime));
        }
    }

    protected void logError(String what, String url, long start){
        long now = SystemClock.elapsedRealtime();
        VolleyLog.v("HTTP ERROR(%s) %d ms to fetch %s", what, (now - start), url);
    }

    private byte[] entityToBytes(HttpEntity entity) throws IOException, ServerError {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, (int)entity.getContentLength());

        byte[] buffer = null;
        try{
            InputStream in = entity.getContent();
            if(in == null){
                throw new ServerError();
            }
            buffer = mPool.getBuf(1024);
            int count;
            while((count = in.read(buffer)) != -1 ){
                bytes.write(buffer,0,count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                entity.consumeContent();
            } catch (IOException e) {
                VolleyLog.v("Error occured when calling consumingContent");
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }

    protected static Map<String, String> convertHeaders(Header[] headers){
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for(int i = 0; i < headers.length; i++){
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
}
