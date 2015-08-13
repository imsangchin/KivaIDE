package com.kiva.ide.util;

import android.content.ClipData;
import android.content.Context;
import android.text.ClipboardManager;

/**
 * 
 * @author Kiva
 * 
 */
@SuppressWarnings("deprecation")
public class ClipboardUtil {

    private ClipboardUtil() {
    }

    /**
     * 得到对应系统版本的剪贴板管理器
     * @param context
     * @return
     */
    public static IClipboardManager getManager(Context context) {
        if (AndroidHelper.getSdkVersion() < 11) {
            return new ClipboardManagerV1(context);
        } else {
            return new ClipboardManagerV11(context);
        }
    }


    private static class ClipboardManagerV1 implements IClipboardManager {
		private final ClipboardManager clip;

		public ClipboardManagerV1(Context context) {
            clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        }

		@Override
        public CharSequence getText() {
            return clip.getText();
        }

		@Override
        public boolean hasText() {
            return clip.hasText();
        }

		@Override
        public void setText(CharSequence text) {
            clip.setText(text);
        }
    }


    private static class ClipboardManagerV11 implements IClipboardManager {
        private final android.content.ClipboardManager clip;

        public ClipboardManagerV11(Context context) {
            clip = (android.content.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
        }

        @Override
        public CharSequence getText() {
           return clip.getPrimaryClip().getItemAt(0).getText();
        }

        @Override
        public boolean hasText() {
            return clip.hasPrimaryClip();
        }

        @Override
        public void setText(CharSequence text) {
            clip.setPrimaryClip(ClipData.newPlainText(null, text));
        }
    }

}
