package com.kiva.ide.util;

import java.lang.reflect.Field;

import com.kiva.ide.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.ViewConfiguration;
import android.view.WindowManager;

public class UiUtil {

	public static void setNavigationBarShowMenuKey(Activity ctx, boolean b) {
		int flag = 0;

		try {
			flag = WindowManager.LayoutParams.class.getField(
					"FLAG_NEEDS_MENU_KEY").getInt(null);
		} catch (Exception e) {
			Logger.w("could not access WindowManager.LayoutParams.FLAG_NEEDS_MENU_KEY: "
					+ e.toString());
		}

		if (b) {
			ctx.getWindow().addFlags(flag);
		} else {
			ctx.getWindow().clearFlags(flag);
		}

	}

	public static void setMenuOverflowShowing(Context ctx, boolean on) {
		try {
			ViewConfiguration config = ViewConfiguration.get(ctx);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, !on);
		} catch (Exception e) {
			Logger.w("could not access android.view.ViewConfiguration.sHasPermanentMenuKey: "
					+ e.toString());
		}
	}
	
	public static ProgressDialog createWaitingDialog(Context ctx) {
		ProgressDialog waitDialog = new ProgressDialog(ctx, ProgressDialog.STYLE_SPINNER);
		waitDialog.setCancelable(false);
		waitDialog.setCanceledOnTouchOutside(false);
		waitDialog.setTitle(R.string.operating);
		waitDialog.setMessage(ctx.getString(R.string.please_wait));
		
		return waitDialog;
	}

}
