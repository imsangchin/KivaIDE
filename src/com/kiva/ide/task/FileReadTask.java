package com.kiva.ide.task;

import java.io.File;

import org.free.ceditor.FileSystem;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.kiva.ide.util.Constant;
import com.kiva.ide.util.Logger;

public class FileReadTask extends Thread implements ITask {

	private String fileName;
	private Handler handler;

	protected int request;
	protected int result;
	protected Bundle data;

	public FileReadTask(String fileName) {
		this.fileName = fileName;
		
		data = new Bundle();
		data.putString(Constant.FILENAME, fileName);
	}

	@Override
	public void run() {
		handler.sendEmptyMessage(Constant.WHAT_START);
		
		String text = FileSystem.StringFromFile(new File(fileName));

		if (text != null) {
			result = Activity.RESULT_OK;
			
			data.putString(Constant.FILECONTENT, text);
		} else {
			Logger.w("read file " + fileName + " failed");
			
			result = Activity.RESULT_CANCELED;
			data = null;
		}

		Message msg = new Message();
		msg.what = Constant.WHAT_TASK;
		msg.obj = this;

		handler.sendMessage(msg);
	}

	@Override
	public void startTask() {
		this.start();
	}

	@Override
	public void cancelTask() {
		this.interrupt();
	}

	@Override
	public int getRequestCode() {
		return request;
	}

	@Override
	public int getResultCode() {
		return result;
	}

	@Override
	public Bundle getData() {
		return data;
	}

	@Override
	public void setRequestCode(int req) {
		this.request = req;
	}

	@Override
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

}
