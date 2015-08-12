package com.kiva.ide;
import java.util.List;

import android.app.Application;
import com.kiva.ide.crash.CrashHandler;
import com.myopicmobile.textwarrior.android.RecentFiles;
import com.myopicmobile.textwarrior.android.RecentFiles.RecentFile;

public class App extends Application
{
	public static App app;
	private RecentFiles recentFiles;
	
	@Override
	public void onCreate() {
		// TODO: Implement this method
		super.onCreate();
		app = this;
		recentFiles = new RecentFiles(this);
		CrashHandler.getInstance().init(this);
	}
	
	public static App get() {
		return app;
	}
	
	public RecentFiles getRecentFiles() {
		return recentFiles;
	}
	
	public void addRecent(RecentFile rf) {
		List<RecentFile> l = getRecentFiles().getRecentFiles();
		if (l.contains(rf)) {
			l.remove(rf);
		}
		
		l.add(0, rf);
	}
}
