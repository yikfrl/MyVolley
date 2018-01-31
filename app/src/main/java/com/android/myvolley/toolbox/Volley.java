package com.android.myvolley.toolbox;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;

import com.android.myvolley.Network;
import com.android.myvolley.RequestQueue;

import java.io.File;

/**
 * Created by yangz on 2018/1/26.
 */

public class Volley {

    private static final String DEFAULT_CACHE_DIR = "volley";

    public static RequestQueue newRequestQueue(Context context, HttpStack stack, int maxDiskCacheBytes){
        File cacheDir = new File(context.getCacheDir(),DEFAULT_CACHE_DIR);

        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName,0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }

        if(stack == null){
            if(Build.VERSION.SDK_INT >= 9){
                stack = new HurlStack();
            } else{
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue;
        if(maxDiskCacheBytes <= -1){
            queue = new RequestQueue(new DiskBaseCache(cacheDir), network);
        }else{
            queue = new RequestQueue(new Disk)
        }

    }


    public static RequestQueue newRequestQueue(Context context, int maxDiskCacheBytes){
        return newRequestQueue(context,null,maxDiskCacheBytes);
    }

    public static RequestQueue newRequestQueue(Context context, HttpStack stack){
        return newRequestQueue(context,stack,-1);
    }

    public static RequestQueue newRequestQueue(Context contexxt){
        return newRequestQueue(contexxt, null);
    }
}
