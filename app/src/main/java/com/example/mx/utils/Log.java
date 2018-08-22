package com.example.mx.utils;


/**
 * Created by admin on 2018/2/26.
 */

public class Log {

    /**
     * debug日志
     *
     * @param tag
     * @param log
     */
    public static void d(String tag, String log) {
            android.util.Log.d(tag, log);
    }

    /**
     * info日志
     *
     * @param tag
     * @param log
     */
    public static void i(String tag, String log) {
        android.util.Log.i(tag, log);
    }

    /**
     * warn日志
     *
     * @param tag
     * @param log
     */
    public static void w(String tag, String log) {
        android.util.Log.w(tag, log);
    }

    /**
     * error日志
     *
     * @param tag
     * @param log
     */
    public static void e(String tag, String log) {
        android.util.Log.e(tag, log);
    }
}
