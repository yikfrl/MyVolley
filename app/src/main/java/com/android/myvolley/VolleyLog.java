package com.android.myvolley;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yangz on 2018/1/29.
 */

public class VolleyLog {
    public static String TAG = "Volley";

    public static boolean DEBUG = Log.isLoggable(TAG, Log.VERBOSE);

    public static void setTAG(String tag){
        d("Changing log tag to %s",tag);
        TAG = tag;

        DEBUG = Log.isLoggable(TAG,Log.VERBOSE);
    }

    public static void v(String format, Object... args){
        if(DEBUG){
            Log.v(TAG, buildMessage(format,args));
        }
    }

    public static void d(String format, Object... args){
        Log.d(TAG, buildMessage(format, args));
    }

    public static void e(String format, Object... args){
        Log.e(TAG, buildMessage(format, args));
    }

    public static void e(Throwable tr, String format, Object... args){
        Log.e(TAG, buildMessage(format, args),tr);
    }

    public static void wtf(String format, Object... args){
        Log.wtf(TAG, buildMessage(format, args));
    }

    public static void wtf(Throwable tr, String format, Object... args){
        Log.wtf(TAG, buildMessage(format, args), tr);
    }

    private static String buildMessage(String format, Object... args){
        String msg = (args == null) ? format : String.format(Locale.US, format, args);
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();

        String caller = "<unknown>";

        for(int i = 2; i<trace.length; i++){
            Class<?> clazz = trace[i].getClass();
            if(!clazz.equals(VolleyLog.class)){
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s:%s", Thread.currentThread().getId(), caller, msg);
    }

    static class MarkerLog{
        public static final boolean ENABLED = VolleyLog.DEBUG;

        private static final long MIN_DURATION_FOR_LOGGING_MS = 0;

        private static class Marker{
            public final String name;
            public final Long thread;
            public final Long time;

            public Marker(String name, Long thread, Long time) {
                this.name = name;
                this.thread = thread;
                this.time = time;
            }
        }

        private final List<Marker> mMarkers = new ArrayList<>();
        private boolean mFinished = false;

        public synchronized void add (String name, long threadId){
            if(mFinished){
                throw new IllegalArgumentException("Marker added to finished log");
            }
            mMarkers.add(new Marker(name,threadId, SystemClock.elapsedRealtime()));
        }

        public synchronized void finish(String header){
            mFinished = true;

            long duration = getTotalDuration();
            if(duration < MIN_DURATION_FOR_LOGGING_MS){
                return;
            }

            long prevTime = mMarkers.get(0).time;
            d("(%-4d ms) %s", duration, header);
            for (Marker marker : mMarkers){
                long thisTime = marker.time;
                d("(+%-4d) [%2d] %s",(thisTime - prevTime), marker.thread, marker.name);
                prevTime = thisTime;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            if(!mFinished){
                finish("Request on the loose");
                e("Marker log finalized without finish() - uncaught exit point for request");
            }
        }

        private long getTotalDuration() {
            if(mMarkers.size() == 0){
                return 0;
            }
            long first = mMarkers.get(0).time;
            long last = mMarkers.get(mMarkers.size() - 1).time;
            return last - first;
        }
    }
}
