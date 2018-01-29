package com.android.myvolley.toolbox;

import com.android.myvolley.AuthFailureError;
import com.android.myvolley.Request;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by yangz on 2018/1/26.
 */

public interface HttpStack {

    public HttpResponse performRequest(Request<?> request, Map<String,String> additionalHeaders) throws IOException, AuthFailureError;
}
