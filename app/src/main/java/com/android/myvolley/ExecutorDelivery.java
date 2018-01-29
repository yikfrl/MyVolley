package com.android.myvolley;

import android.os.Handler;

import java.util.concurrent.Executor;

/**
 * Created by yangz on 2018/1/29.
 */

public class ExecutorDelivery implements ResponseDelivery {

    private final Executor mResponsePoster;

    public ExecutorDelivery(final Handler handler){
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    public ExecutorDelivery(Executor executor){
        mResponsePoster = executor;
    }


    @Override
    public void postResponse(Request<?> request, Response<?> response) {

    }

    @Override
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable) {

    }

    @Override
    public void postError(Request<?> request, VolleyError error) {

    }
}
