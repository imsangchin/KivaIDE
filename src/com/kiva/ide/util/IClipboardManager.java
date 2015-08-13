package com.kiva.ide.util;

/**
 * 
 * @author Kiva
 * 
 * 对剪贴板读写协议
 *
 */
public interface IClipboardManager {
	CharSequence getText();

	boolean hasText();

    void setText(CharSequence text);
}
