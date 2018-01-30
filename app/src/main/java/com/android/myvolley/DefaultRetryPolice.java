package com.android.myvolley;

/**
 * Created by yangz on 2018/1/30.
 */

public class DefaultRetryPolice implements RetryPolice {

    private int mCurrentTimeoutMs;
    private int mCurrentRetryCount;
    private final int mMaxNumRetries;
    //TODO
    /** The backoff multiplier for the policy. */
    private final float mBackoffMultiplier;
    public static final int DEFAULT_TIMEOUT_MS = 2500;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    public DefaultRetryPolice(){
        this(DEFAULT_TIMEOUT_MS,DEFAULT_MAX_RETRIES,DEFAULT_BACKOFF_MULT);
    }

    public DefaultRetryPolice(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier){
        mCurrentTimeoutMs = initialTimeoutMs;
        mMaxNumRetries = maxNumRetries;
        mBackoffMultiplier = backoffMultiplier;
    }

    @Override
    public int getCurrentTimeout() {
        return mCurrentTimeoutMs;
    }

    @Override
    public int getCurrentRetryCount() {
        return mCurrentRetryCount;
    }

    public float getBackoffMultiplier(){
        return mBackoffMultiplier;
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {
        mCurrentRetryCount++;
        mCurrentTimeoutMs += (mCurrentTimeoutMs * mBackoffMultiplier);
        if(!hasAttemptRemaining()){
            throw error;
        }
    }

    protected boolean hasAttemptRemaining(){
        return mCurrentRetryCount <= mMaxNumRetries;
    }
}
