package com.android.myvolley.toolbox;

import com.android.myvolley.NetworkResponse;
import com.android.myvolley.ParseError;
import com.android.myvolley.Response;
import com.android.myvolley.Response.Listener;
import com.android.myvolley.Response.ErrorListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by yangz on 2018/2/2.
 */

public class JsonArrayRequest extends JsonRequest<JSONArray> {

    public JsonArrayRequest(int method, String url, String requestBody,
                            Listener<JSONArray> listener, ErrorListener errorListener){
        super(method,url,requestBody,listener,errorListener);
    }

    public JsonArrayRequest(String url, Listener<JSONArray> listener, ErrorListener errorListener){
        super(Method.GET, url, null, listener, errorListener);
    }

    public JsonArrayRequest(int method, String url, Listener<JSONArray> listener, ErrorListener errorListener){
        super(method, url, null, listener, errorListener);
    }

    public JsonArrayRequest(int method, String url, JSONArray jsonRequest,
                            Listener<JSONArray> listener, ErrorListener errorListener){
        super(method,url,( jsonRequest == null) ? null : jsonRequest.toString() ,listener,errorListener);
    }

    public JsonArrayRequest(int method, String url, JSONObject jsonRequest,
                            Listener<JSONArray> listener, ErrorListener errorListener){
        super(method,url,( jsonRequest == null) ? null : jsonRequest.toString() ,listener,errorListener);
    }

    public JsonArrayRequest(String url, JSONArray jsonRequest,
                            Listener<JSONArray> listener, ErrorListener errorListener){
        this(jsonRequest == null ? Method.GET : Method.POST, url,jsonRequest ,listener,errorListener);
    }

    public JsonArrayRequest(String url, JSONObject jsonRequest,
                            Listener<JSONArray> listener, ErrorListener errorListener){
        this(jsonRequest == null ? Method.GET : Method.POST ,url,jsonRequest ,listener,errorListener);
    }

    @Override
    protected Response<JSONArray> parseNetwordResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONArray(jsonString) , HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }
}
