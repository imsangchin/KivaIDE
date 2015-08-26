package com.kiva.ide.editor;

import android.graphics.Color;

import com.myopicmobile.textwarrior.common.ColorSchemeObsidian;

public class AideLikeColorScheme extends ColorSchemeObsidian {

	public static final int BLUE = Color.parseColor("#33b5e5");
	public static final int COMMENT = Color.parseColor("#00B000");
	public static final int STRING = Color.parseColor("#f08080");

	public AideLikeColorScheme() {
		setColor(Colorable.BACKGROUND, Color.BLACK);
		setColor(Colorable.KEYWORD, BLUE);
		setColor(Colorable.COMMENT, COMMENT);
		setColor(Colorable.LINETEXT, BLUE);
		setColor(Colorable.LITERAL, STRING);
		setColor(Colorable.SECONDARY, BLUE);
		setColor(Colorable.FOREGROUND, Color.WHITE);
	}
}
