package com.android.myvolley.toolbox;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.myvolley.DefaultRetryPolice;
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


}
