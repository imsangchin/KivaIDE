package com.kiva.ide.task;

import android.os.Bundle;
import android.os.Handler;

public interface ITask {
	void startTask();
	void cancelTask();
	
	int getRequestCode();
	int getResultCode();
	Bundle getData();
	
	void setRequestCode(int req);
	void setHandler(Handler handler);
}
