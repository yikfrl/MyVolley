package com.android.myvolley.toolbox;

import com.android.myvolley.NetworkResponse;
import com.android.myvolley.Request;
import com.android.myvolley.Response;
import com.android.myvolley.Response.Listener;
import com.android.myvolley.Response.ErrorListener;

import java.io.UnsupportedEncodingException;

/**
 * Created by yangz on 2018/2/2.
 */

public class StringRequest extends Request<String> {
    private Listener<String> mListener;

    public StringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    public StringRequest(String url, Listener<String> listener, ErrorListener errorListener){
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        mListener = null;
    }

    @Override
    protected void deliverResponse(String response) {
        if(mListener != null){
            mListener.onResponse(response);
        }
    }

    @Override
    protected Response<String> parseNetwordResponse(NetworkResponse response) {
        String parsed;
        try{
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}
