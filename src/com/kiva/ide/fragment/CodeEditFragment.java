package com.kiva.ide.fragment;

import java.io.File;

import org.free.iface.iMainActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kiva.ide.R;
import com.kiva.ide.util.Constant;
import com.kiva.ide.util.Logger;
import com.kiva.ide.view.FastCodeEditor;

@SuppressWarnings("deprecation")
public class CodeEditFragment extends Fragment implements iMainActivity {
	public static interface Result {
		void requestFinish();
	}

	private FastCodeEditor editor;
	private Bundle args;
	private String absFilePath;
	private Tab tab;
	public boolean ext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		args = getArguments();

		if (args != null) {
			absFilePath = args.getString(Constant.FILENAME, Constant.UNTITLED);
		} else {
			Logger.w("Arguments is null, use default!");
			absFilePath = Constant.UNTITLED;
		}
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_code_edit, null, false);

		editor = (FastCodeEditor) view.findViewById(R.id.idFragEditCodeEditor);

		setData();
		return view;
	}

	private void setData() {
		editor.setActivty(this);

		if (args != null) {
			String content = args.getString(Constant.FILECONTENT, "");
			editor.setText(content);

			int x = args.getInt(Constant.CURX, -1);
			int y = args.getInt(Constant.CURY, -1);
			int p = args.getInt(Constant.CURSOR, -1);

			if (p != -1) {
				editor.moveCaret(p);
			}

			if (x != -1 && y != -1) {
				editor.scrollTo(x, y);
			}
		}

		setFileName(absFilePath);

		editor.requestFocus();
	}

	public String getFileName() {
		return absFilePath;
	}

	public void setFileName(String absFilePath) {
		if (tab != null) {
			((TextView) tab.getCustomView().findViewById(R.id.idTabTitle))
					.setText(new File(absFilePath).getName());
		}

		this.absFilePath = absFilePath;
	}

	public void setTab(Tab tab) {
		this.tab = tab;
	}

	public FastCodeEditor getEditor() {
		return editor;
	}

	@Override
	public void onPause() {
		super.onPause();

		int x = editor.getScrollX();
		int y = editor.getScrollY();
		int p = editor.getCaretPosition();

		Logger.d("x:" + x + "  y:" + y + " p:" + p);

		args.putString(Constant.FILECONTENT, editor.getText());
		args.putInt(Constant.CURX, x);
		args.putInt(Constant.CURY, y);
		args.putInt(Constant.CURSOR, p);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void canDiag(boolean arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void codeChange() {
		// TODO Auto-generated method stub
	}

	@Override
	public void formatLine(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void hideWait() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLexDone() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpdataCart() {
		// TODO Auto-generated method stub
	}

	@Override
	public void paste(String arg0, Object arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void popCodeCompiltion() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean showEditActiton() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void showWait() {
		// TODO Auto-generated method stub
	}

	@Override
	public void updataActionMode() {
		// TODO Auto-generated method stub
	}

	@Override
	public void updataCodeComp() {
		// TODO Auto-generated method stub
	}

}
