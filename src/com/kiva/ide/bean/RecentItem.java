package com.kiva.ide.bean;

import com.myopicmobile.textwarrior.android.RecentFiles.RecentFile;

public class RecentItem extends RecentFile {
	
	public RecentItem(String arg0, long arg1, int arg2, int arg3, int arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		// TODO Auto-generated constructor stub
	}

	public RecentItem(String arg0) {
		super(arg0);
	}
	
	@Override
	public String toString() {
		return this.getFileName();
	}
	
	

}
