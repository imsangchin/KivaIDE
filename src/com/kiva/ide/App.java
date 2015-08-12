package com.kiva.ide;
import android.app.Application;
import com.kiva.ide.crash.CrashHandler;

public class App extends Application
{
	public static App app;
	
	@Override
	public void onCreate() {
		// TODO: Implement this method
		super.onCreate();
		app = this;
		CrashHandler.getInstance().init(this);
	}
	
	public static App get() {
		return app;
	}
}
