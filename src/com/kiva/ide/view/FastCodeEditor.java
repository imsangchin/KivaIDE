package com.kiva.ide.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.kiva.ide.editor.AideLikeColorScheme;
import com.myopicmobile.textwarrior.android.FreeScrollingTextField;
import com.myopicmobile.textwarrior.common.Document;
import com.myopicmobile.textwarrior.common.DocumentProvider;
import com.myopicmobile.textwarrior.common.Language;
import com.myopicmobile.textwarrior.common.LanguageC;
import com.myopicmobile.textwarrior.common.Lexer;

public class FastCodeEditor extends FreeScrollingTextField {
	
	private boolean firstSetText = true;
	
	public FastCodeEditor(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		init();
	}

	public FastCodeEditor(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		init();
	}

	private void init() {
		setLang(LanguageC.getInstance());
		setFocusable(true);
		setZoom(12);
		setAutoIndent(true);
		setTypeface(Typeface.MONOSPACE);
		setHorizontalScrollBarEnabled(false);
		setWordWrap(true);
		setColorScheme(new AideLikeColorScheme());
		scrollTo(0, 0);
	}

	public DocumentProvider getDocumentProvider() {
		return createDocumentProvider();
	}

	public void setText(String text) {
		Document doc = new Document(this);
		doc.insert(text.toCharArray(), 0, 0, false);

		DocumentProvider dp = new DocumentProvider(doc);
		setDocumentProvider(dp);
		
		if (!firstSetText) {
			setEdited(true);
		} else {
			setEdited(false);
		}
		

		scrollTo(0, 0);
	}

	public String getText() {
		DocumentProvider dp = getDocumentProvider();
		
		return new String(dp.subSequence(0, dp.docLength() - 1));
	}

	public void setLang(Language l) {
		Lexer.setLanguage(l);
	}
	
	
	@Override
	public void showIME(boolean show) {
		if (viewmode()) {
			return;
		}
		
		super.showIME(!show);
	}
	
	public void setViewOnly(boolean on) {
		super.viewmode(on);
	}

}
