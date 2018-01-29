package com.android.myvolley.toolbox;

import com.android.myvolley.AuthFailureError;
import com.android.myvolley.Request;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by yangz on 2018/1/29.
 */

public class HurlStack implements HttpStack {
    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        return null;
    }
}
