package com.kiva.ide.task;

import java.io.File;
import java.util.List;

import org.free.ceditor.FileSystem;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.kiva.ide.App;
import com.kiva.ide.util.Constant;
import com.kiva.ide.util.Logger;
import com.myopicmobile.textwarrior.android.RecentFiles.RecentFile;

public class FileWriteTask extends Thread implements ITask {

	private String content;
	private String fileName;
	private Handler handler;

	protected int request;
	protected int result;
	protected Bundle data;

	public FileWriteTask(String content, String fileName, int hash) {
		this.content = content;
		this.fileName = fileName;
		
		data = new Bundle();
		data.putString(Constant.FILENAME, fileName);
		data.putString(Constant.FILECONTENT, content);
		data.putInt(Constant.HASH, hash);
		
	}

	@Override
	public void run() {
		handler.sendEmptyMessage(Constant.WHAT_START);
		
		FileSystem.stringToFile(content, new File(fileName));

		if (FileSystem.stringToFile(content, new File(fileName))) {
			result = Activity.RESULT_OK;
		} else {
			Logger.w("write file " + fileName + " failed");
			
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
