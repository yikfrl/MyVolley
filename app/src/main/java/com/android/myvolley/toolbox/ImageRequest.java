package com.android.myvolley.toolbox;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.myvolley.DefaultRetryPolice;
import com.android.myvolley.NetworkResponse;
import com.android.myvolley.ParseError;
import com.android.myvolley.Request;
import com.android.myvolley.Response;

/**
 * Created by yangz on 2018/2/2.
 */

public class ImageRequest extends Request<Bitmap> {

    private static final int IMAGE_TIMEOUT_MS = 1000;

    private static final int IMAGE_MAX_REIES = 2;

    private static final float IMAGE_BACKOFF_MULT = 2f;

    private final Response.Listener<Bitmap> mListener;
    private final Config mDecodeConfig;
    private final int mMaxWidth;
    private final int mMaxHeight;
    private ScaleType mScaleType;

    /** Decoding lock so that we don't decode more than one image at a time (to avoid OOM's) */
    private static final Object sDecodeLock = new Object();

    public ImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight,
                        ScaleType scaleType, Config decodeConfig, Response.ErrorListener errorListener){
        super(Method.GET, url, errorListener);
        setRetryPolice(new DefaultRetryPolice(IMAGE_TIMEOUT_MS, IMAGE_MAX_REIES, IMAGE_BACKOFF_MULT));
        mListener = listener;
        mDecodeConfig = decodeConfig;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        mScaleType = scaleType;
    }

    public ImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight,
                        Config decodeConfig, Response.ErrorListener errorListener){
        this(url, listener, maxWidth, maxHeight, ScaleType.CENTER_INSIDE, decodeConfig,errorListener);
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary,
                                           int actualSecondary, ScaleType scaleType){
        if((maxPrimary == 0) && (maxSecondary == 0)){
            return actualPrimary;
        }

        if(scaleType == ScaleType.FIT_XY){
            if(maxPrimary == 0){
                return actualPrimary;
            }
            return maxPrimary;
        }

        if(maxPrimary == 0){
            double ratio = (double) maxSecondary/ (double)actualSecondary;
            return (int)(actualPrimary * ratio);
        }

        if(maxSecondary == 0){
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;

        if(scaleType == ScaleType.CENTER_CROP){
            if((resized * ratio) < maxSecondary) {
                resized = (int) (maxSecondary / ratio);
            }
            return resized;
        }

        if((resized * ratio) > maxSecondary){
            resized =(int) (maxSecondary/ratio);
        }
        return resized;
    }

    @Override
    protected Response<Bitmap> parseNetwordResponse(NetworkResponse response) {
        synchronized (sDecodeLock){
            try{
                return doParse(response);
            }
        }
    }

    /**
     * The real guts of parseNetworkResponse. Broken out for readability.
     */
    private Response<Bitmap> doParse(NetworkResponse response) {
        byte[] data = response.data;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        Bitmap bitmap = null;
        if(mMaxWidth == 0 && mMaxHeight == 0){
            decodeOptions.inPreferredConfig = mDecodeConfig;
            bitmap = BitmapFactory.decodeByteArray(data,0,data.length,decodeOptions);
        }else{
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data,0,data.length,decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            int desiredWidth = getResizedDimension(mMaxWidth,mMaxHeight,actualWidth,actualHeight,mScaleType);
            int desiredHeight = getResizedDimension(mMaxHeight,mMaxWidth,actualHeight,actualWidth,mScaleType);

            decodeOptions.inJustDecodeBounds = false;
            decodeOptions.inSampleSize = findBestSampleSize(actualWidth,actualHeight,desiredWidth,desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data,0,data.length, decodeOptions);

            if(tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight)){
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            }else{
                bitmap = tempBitmap;
            }

            if(bitmap == null){
                return Response.error(new ParseError(response));
            }else{
                return Response.success(bitmap, HttpHeaderParser.parseCacheHeaders(response));
            }
        }
    }

    @Override
    protected void deliverResponse(Bitmap response) {
        mListener.onResponse(response);
    }

    static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight){
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while( (n*2) <= ratio ){
            n*=2;
        }

        return (int) n;
    }


}
