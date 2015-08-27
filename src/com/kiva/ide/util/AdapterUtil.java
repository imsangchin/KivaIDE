package com.kiva.ide.util;

import static com.kiva.ide.bean.DrawerListItem.ID_GOTO;
import static com.kiva.ide.bean.DrawerListItem.ID_NEW;
import static com.kiva.ide.bean.DrawerListItem.ID_OPEN;
import static com.kiva.ide.bean.DrawerListItem.ID_OPEN_REC;
import static com.kiva.ide.bean.DrawerListItem.ID_SAVE;
import static com.kiva.ide.bean.DrawerListItem.ID_SAVEAS;
import static com.kiva.ide.bean.DrawerListItem.ID_SEARCH;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;

import com.kiva.ide.R;

public class AdapterUtil {

	public static int getDrawerIconResById(int id) {

		switch (id) {
		case ID_NEW:
			return R.drawable.ic_l_new;
		case ID_OPEN:
			return R.drawable.ic_l_open;
		case ID_OPEN_REC:
			return R.drawable.ic_l_open_recent;
		case ID_SAVE:
			return R.drawable.ic_l_save;
		case ID_SAVEAS:
			return R.drawable.ic_l_saveas;
		case ID_SEARCH:
			return R.drawable.ic_l_search;
		case ID_GOTO:
			return R.drawable.ic_l_goto;
		}

		return -1;
	}

	@SuppressLint("DefaultLocale")
	public static int getFileIcon(Context ctx, File file) {
		if (file.isDirectory()) {
			return R.drawable.folder;
		}

		if (file.getName().equals(ctx.getString(R.string.new_folder))) {
			return R.drawable.file_new_folder;
		}

		String name = file.getName().toLowerCase();

		if (name.endsWith(".c") || name.endsWith(".cpp")
				|| name.endsWith(".cxx") || name.endsWith(".cc")
				|| name.endsWith(".h") || name.endsWith(".hpp")
				|| name.endsWith(".hxx") || name.endsWith("m")
				|| name.endsWith("mm")) {
			return R.drawable.file_executable_src;

		} else if (name.endsWith(".java") || name.endsWith(".js")) {
			return R.drawable.file_java;

		} else if (name.endsWith(".html") || name.endsWith(".htm")) {
			return R.drawable.file_html;

		} else if (name.endsWith(".php")) {
			return R.drawable.file_php;

		} else if (name.endsWith(".py")) {
			return R.drawable.file_python;
			
		} else if (name.endsWith(".sh") || name.endsWith(".bash")) {
			return R.drawable.file_shell;

		} else if (name.equals("makefile") || name.equals("gnumakefile")
				|| name.endsWith(".mk")) { // lowercase
			return R.drawable.file_makefile;

		} else if (name.contains("readme") || name.contains("license")
				|| name.contains("notice")) { // lowercase
			return R.drawable.file_readme;

		} else if (name.indexOf(".") == -1 && file.canExecute()) {
			return R.drawable.file_executable;
		}

		return R.drawable.file_normal;
	}
	
}
