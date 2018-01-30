package com.android.myvolley;

import org.apache.http.HttpStatus;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Created by yangz on 2018/1/29.
 */

public class NetworkResponse implements Serializable{
    private static final long serialVersionUID = -20150728102000L;

    public final int statusCode;
    public final byte[] data;
    public final Map<String, String> headers;
    public final boolean notModified;
    public final long networdTimeMs;

    public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers, boolean notModified, long networdTimeMs) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        this.notModified = notModified;
        this.networdTimeMs = networdTimeMs;
    }

    public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers, boolean notModified) {
        this(statusCode, data, headers, notModified, 0);
    }

    public NetworkResponse(byte[] data) {
        this(HttpStatus.SC_OK, data, Collections.<String, String>emptyMap(), false, 0);
    }

    public NetworkResponse(byte[] data, Map<String, String> headers) {
        this(HttpStatus.SC_OK, data, headers, false, 0);
    }
}
