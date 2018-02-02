package com.android.myvolley.toolbox;

import com.android.myvolley.AuthFailureError;
import com.android.myvolley.NetworkResponse;
import com.android.myvolley.Request;
import com.android.myvolley.Response;
import com.android.myvolley.Response.Listener;
import com.android.myvolley.Response.ErrorListener;
import com.android.myvolley.VolleyLog;

import java.io.UnsupportedEncodingException;

/**
 * Created by yangz on 2018/2/2.
 */

public abstract class JsonRequest<T> extends Request<T> {

    protected static final String PROTOCOL_CHARSET = "utf-8";

    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json: charset=%s",PROTOCOL_CHARSET);

    private Listener<T> mListener;
    private final String mRequestBody;

    public JsonRequest(String url, String requestBody, Listener<T> listener, ErrorListener errorListener){
        this(Method.DEPRECATED_GET_OR_POST, url, requestBody, listener, errorListener);
    }

    public JsonRequest(int method, String url, String requestBody, Listener<T> listener, ErrorListener errorListener){
        super(method, url, errorListener);
        mListener = listener;
        mRequestBody = requestBody;
    }

    @Override
    protected void onFinish() {
        super.onFinish();
        mListener = null;
    }

    @Override
    protected void deliverResponse(T response) {
        if(mListener != null){
            mListener.onResponse(response);
        }
    }

    @Override
    abstract protected Response<T> parseNetwordResponse(NetworkResponse response);

    @Override
    public String getPostBodyContentType(){
        return getBodyContentType();
    }

    @Override
    public byte[] getPostBody() {
        return getBody();
    }
    @Override
    public String getBodyContentType(){
        return PROTOCOL_CONTENT_TYPE;
    }
    @Override
    public byte[] getBody(){
        try{
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException e) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }
}
