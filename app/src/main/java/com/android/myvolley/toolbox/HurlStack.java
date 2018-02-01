package com.android.myvolley.toolbox;

import com.android.myvolley.AuthFailureError;
import com.android.myvolley.Request;
import com.android.myvolley.Request.Method;

import org.apache.http.HttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by yangz on 2018/1/29.
 */

public class HurlStack implements HttpStack {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    public interface UrlRewriter{
        public String rewriteUrl(String originalUrl);
    }

    private final UrlRewriter mUrlRewriter;
    private final SSLSocketFactory mSslSocketFactory;

    public HurlStack(){
        this(null);
    }

    public HurlStack(UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }

    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        this.mUrlRewriter = urlRewriter;
        this.mSslSocketFactory = sslSocketFactory;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        if(mUrlRewriter != null){
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if(rewritten == null){
                throw new IOException("URL blocked by rewriter: "+url);
            }
            url = rewritten;
        }

        URL parsedUrl = new URL(url);
        HttpURLConnection connection = openConnection(parsedUrl, request);
        for(String headerName : map.keySet()){
            connection.addRequestProperty(headerName, map.get(headerName));
        }

        setConnectionParametersForRequest(connection, request);

    }

    protected HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException{
        HttpURLConnection connection = createConnection(url);

        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        if("https".equals(url.getProtocol()) && mSslSocketFactory!=null){
            ((HttpsURLConnection)connection).setSSLSocketFactory(mSslSocketFactory);
        }

        return connection;
    }

    @SuppressWarnings("deprecation")
    static void setConnectionParametersForRequest(HttpURLConnection connection, Request<?> request) throws AuthFailureError, IOException {
        switch (request.getMethod()){
            case Method.DEPRECATED_GET_OR_POST:
                byte[] postBody = request.getPostBody();
                if(postBody != null){
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty(HEADER_CONTENT_TYPE,
                            request.getPostBodyContentType());
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.write(postBody);
                    out.close();
                }
                break;
            case Method.GET:

                break;
            case Method.DELETE:

                break;
            case Method.POST:

                break;
            case Method.PUT:

                break;
            case Method.HEAD:

                break;
            case Method.OPTIONS:

                break;
            case Method.TRACE:

                break;
            case Method.PATCH:

                break;
            default:

                break;
        }
    }
}
