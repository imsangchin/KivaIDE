package com.kiva.ide.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;

import com.kiva.ide.ChooserActivity;
import com.kiva.ide.ChooserActivity.Holder;
import com.kiva.ide.R;

public class ExplorerUtil {
	public static List<Holder> listDir(Context ctx, File path) {
		List<Holder> list = new ArrayList<ChooserActivity.Holder>();

		Holder h = new Holder();
		h.file = new File("..");
		list.add(h);

		if (path == null || path.getAbsolutePath().length() == 0) {
			return list;
		}

		if ((!path.exists()) || (!path.isDirectory()) || (!path.canRead())) {
			return list;
		}

		list.remove(0);

		File[] files = path.listFiles();
		if (files == null || files.length == 0) {
			return list;
		}

		for (File file1 : files) {
			h = new Holder();

			h.file = file1;
			list.add(h);
		}

		Collections.sort(list, new Comparator<Holder>() {
			@SuppressLint("DefaultLocale")
			public int compare(Holder file1, Holder file2) {
				boolean isDir1 = file1.file.isDirectory();
				boolean isDir2 = file2.file.isDirectory();

				if (isDir1 && !isDir2) {
					return -1;
				} else if (!isDir1 && isDir2) {
					return 1;
				} else if (isDir1 && isDir2) {
					return file1.file.getName().toLowerCase()
							.compareTo(file2.file.getName().toLowerCase());
				} else {
					return file1.file.getName().compareTo(file2.file.getName());
				}
			}
		});
		
		if (!path.getAbsolutePath().equals("/")) {
			h = new Holder();
			h.file = new File("..");
			list.add(0, h);
		}

		if (path.canWrite()) {
			h = new Holder();
			h.file = new File(ctx.getString(R.string.new_folder));
			list.add(1, h);
		}

		return list;
	}
}
