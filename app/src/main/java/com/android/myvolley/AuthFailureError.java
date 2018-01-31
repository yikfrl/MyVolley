package com.android.myvolley;

import android.content.Intent;

/**
 * Created by yangz on 2018/1/29.
 */

public class AuthFailureError extends VolleyError {

    private Intent mResolutionIntent;

    public AuthFailureError() {
    }

    public AuthFailureError(Intent intent) {
        this.mResolutionIntent = intent;
    }

    public AuthFailureError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public AuthFailureError(String exceptionMessage) {
        super(exceptionMessage);
    }

    public AuthFailureError(String detailMessage, Exception reason) {
        super(detailMessage, reason);
    }

    public Intent getResolutionIntent() {
        return mResolutionIntent;
    }

    @Override
    public String getMessage() {
        if(mResolutionIntent != null){
            return "Users needs to (re)enter credentials";
        }
        return super.getMessage();
    }
}
