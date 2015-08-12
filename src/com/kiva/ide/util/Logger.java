package com.kiva.ide.util;

import android.util.Log;

public class Logger {

	public static final String TAG = "KivaIDE";

	public static void i(String msg) {
		Log.i(TAG, msg);
	}

	public static void d(String msg) {
		Log.d(TAG, msg);
	}

	public static void e(String msg) {
		Log.e(TAG, msg);
	}

	public static void w(String msg) {
		Log.w(TAG, msg);

	}

	public static void v(String msg) {
		Log.v(TAG, msg);
	}

}
