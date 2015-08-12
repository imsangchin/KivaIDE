package com.kiva.ide.crash;

import static com.kiva.ide.util.Constant.CRASH_REPORTER_EXTENSION;
import static com.kiva.ide.util.Constant.STACK_TRACE;
import static com.kiva.ide.util.Constant.VERSION_CODE;
import static com.kiva.ide.util.Constant.VERSION_NAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.free.ceditor.FileSystem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import com.kiva.ide.App;
import com.kiva.ide.R;
import com.kiva.ide.util.Logger;

public class CrashHandler implements UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler defaultHandler;
	private static CrashHandler sInstance;
	private Context appContext;
	private Properties deviceInfo = new Properties();
	private boolean handled = false;

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		if (sInstance == null) {
			sInstance = new CrashHandler();
		}
		return sInstance;
	}

	public void init(Context ctx) {
		appContext = ctx;
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (handled)
			return;
		handled = true;

		if (!handleException(ex) && defaultHandler != null) {
			defaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Logger.w("Thread has been interrupted: " + e.toString());
			}

			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	private boolean handleException(final Throwable ex) {
		if (ex == null) {
			return true;
		}

		final String msg = ex.getLocalizedMessage();
		if (msg == null) {
			return false;
		}

		collectCrashDeviceInfo(appContext);
		final String dumpFile = saveCrashInfoToFile(ex);

		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast t = Toast.makeText(App.get(),
						App.get().getString(R.string.see_dump, dumpFile),
						Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				Looper.loop();
				reportToServer(dumpFile);
			}
		}.start();

		return true;
	}

	private void reportToServer(String dumpFile) {
		try {
			File f = new File(dumpFile);

			String text = FileSystem.StringFromFile(f);
			if (text == null) {
				return;
			}

			String uriAPI = "http://remote.12.7cloud.net/kiva/crash.php";
			HttpPost httpRequst = new HttpPost(uriAPI);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("filename", dumpFile));
			params.add(new BasicNameValuePair("content", text));

			httpRequst.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequst);

			int status = httpResponse.getStatusLine().getStatusCode();

			f.delete();
			Logger.v("server returned status code: " + status);

		} catch (Exception e) {
			Logger.w("error while sending crush dump to server: "
					+ e.toString());
		}
	}

	@SuppressWarnings("resource")
	@SuppressLint("SimpleDateFormat")
	private String saveCrashInfoToFile(Throwable ex) {
		Writer info = new StringWriter();
		PrintWriter printWriter = new PrintWriter(info);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}

		String result = info.toString();
		printWriter.close();
		deviceInfo.put("EXEPTION", ex.getLocalizedMessage());
		deviceInfo.put(STACK_TRACE, result);
		try {
			Date date = new Date();
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String fileName = String.format("Crash_%s%s", fmt.format(date),
					CRASH_REPORTER_EXTENSION);

			FileOutputStream trace = null;

			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File f = new File(Environment.getExternalStorageDirectory(),
						fileName);

				trace = new FileOutputStream(f);
			} else {
				trace = appContext.openFileOutput(fileName,
						Context.MODE_PRIVATE);
			}

			for (Object key : deviceInfo.keySet()) {
				Object val = deviceInfo.get(key);

				String l = String.format(
						"%s\n========================\n%s\n\n\n",
						key.toString(), val.toString());
				trace.write(l.getBytes());
			}

			trace.flush();
			trace.close();
			return new File(appContext.getFilesDir(), fileName)
					.getAbsolutePath();
		} catch (Exception e) {
			Logger.w("Error while writing crush dump: " + e.toString());
		}
		return null;
	}

	public void collectCrashDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				deviceInfo.put(VERSION_NAME, pi.versionName == null ? "not set"
						: pi.versionName);
				deviceInfo.put(VERSION_CODE, "" + pi.versionCode);
			}
		} catch (NameNotFoundException e) {
			Logger.w("error while collect crash device info: " + e.toString());
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				deviceInfo.put(field.getName(), "" + field.get(null));
			} catch (Exception e) {
				Logger.w("error while reflecting field info: " + e.toString());
			}
		}
	}

}
