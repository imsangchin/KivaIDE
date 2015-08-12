package com.kiva.ide.task;

import android.os.Handler;

public final class TaskManager {
	
	public static void startTask(ITask task, int request, Handler handler) {
		task.setRequestCode(request);
		task.setHandler(handler);
		
		task.startTask();
	}

	public static void cancelTask(ITask task) {
		task.cancelTask();
	}
	
}
