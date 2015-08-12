package com.kiva.ide;
import android.app.Activity;
import android.os.Bundle;

public class CrashReportActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crashreport);
	}

	@Override
	protected void onDestroy() {
		// TODO: Implement this method
		super.onDestroy();
		
		System.exit(1);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	
}
