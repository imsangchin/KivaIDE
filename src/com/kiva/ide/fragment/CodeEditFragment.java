package com.kiva.ide.fragment;

import java.io.File;

import org.free.iface.iMainActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kiva.ide.R;
import com.kiva.ide.action.EditAction;
import com.kiva.ide.util.Constant;
import com.kiva.ide.util.Logger;
import com.kiva.ide.view.FastCodeEditor;
import com.myopicmobile.textwarrior.android.SelectionModeListener;
import com.myopicmobile.textwarrior.common.DocumentProvider;
import com.myopicmobile.textwarrior.common.FindThread;
import com.myopicmobile.textwarrior.common.ProgressObserver;
import com.myopicmobile.textwarrior.common.FindThread.FindResults;

@SuppressWarnings("deprecation")
public class CodeEditFragment extends Fragment implements iMainActivity,
		ProgressObserver {
	public static interface Result {
		void requestFinish();
	}

	private FastCodeEditor editor;
	private Bundle args;
	private String absFilePath;
	private Tab tab;
	public boolean ext;
	private EditAction editAction;
	private boolean inited = false;
	private FindThread findThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		editAction = new EditAction(this);
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
		initDataOnce();

		if (args != null) {
			String content = args.getString(Constant.FILECONTENT, "");
			editor.setText(content);

			int x = args.getInt(Constant.CURX, -1);
			int y = args.getInt(Constant.CURY, -1);
			int p = args.getInt(Constant.CURSOR, -1);

			setEditorStatus(x, y, p);
		}

		setFileName(absFilePath);

		editor.requestFocus();
	}

	private void initDataOnce() {
		if (inited) {
			return;
		}

		editor.setActivty(this);
		editor.setSelModeListener(new SelectionModeListener() {
			@Override
			public void onSelectionModeChanged(boolean arg0) {
				if (arg0) {
					showEditActiton();
				}
				editAction.updateMenu(arg0);
			}
		});
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

	public void replace(String textToFind, String textToReplace, boolean all,
			boolean caseIns, boolean whole) {
		if (!all && editor.isSelectText()) {
			editor.paste(textToReplace);
			find(textToFind, caseIns, whole);
			return;
		}

		FindThread replaceThread = FindThread.createReplaceAllThread(
				editor.getDocumentProvider(), textToFind, textToReplace, 0,
				caseIns, whole);
		replaceThread.registerObserver(this);
		replaceThread.start();
	}

	public void find(String textToFind, boolean caseIns, boolean whole) {
		int start = 0;
		if (findThread != null) {
			FindResults r = findThread.getResults();

			start = r.foundOffset == -1 ? 0 : r.foundOffset;
			start += 1;
			Logger.w(start);
		}

		findThread = FindThread.createFindThread(editor.getDocumentProvider(),
				textToFind, start, true, caseIns, whole);
		findThread.registerObserver(this); // request code == 4

		findThread.start();
	}

	public void undo() {
		DocumentProvider doc = editor.getDocumentProvider();
		if (doc.canUndo()) {

			int x = editor.getScrollX();
			int y = editor.getScrollY();
			int p = editor.getCaretPosition();

			doc.undo();
			editor.setDocumentProvider(doc);
			setEditorStatus(x, y, p);
		}
	}

	public void redo() {
		DocumentProvider doc = editor.getDocumentProvider();
		if (doc.canRedo()) {

			int x = editor.getScrollX();
			int y = editor.getScrollY();
			int p = editor.getCaretPosition();

			doc.redo();
			editor.setDocumentProvider(doc);
			setEditorStatus(x, y, p);
		}
	}

	public void setEditorStatus(int x, int y, int p) {
		Logger.d("x:" + x + "  y:" + y + " p:" + p);

		if (p > 0) {
			editor.moveCaret(p);
		}

		if (x > 0 && y > 0) {
			editor.scrollTo(x, y);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.menu_select_all:
			editor.selectAll();
			break;
		case R.id.menu_copy:
		case R.id.menu_cut:
		case R.id.menu_paste:
			ClipboardManager cb = (ClipboardManager) getActivity()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			if (id == R.id.menu_copy) {
				editor.copy(cb);
			} else if (id == R.id.menu_cut) {
				editor.cut(cb);
			} else if (id == R.id.menu_paste) {
				if (cb.hasPrimaryClip()) {
					if (cb.getPrimaryClip().getItemCount() > 0) {
						CharSequence s = cb.getPrimaryClip().getItemAt(0)
								.getText();
						editor.paste(s.toString());
					}
				}
			}
		}

		return true;
	}

	@Override
	public void onPause() {
		super.onPause();

		int x = editor.getScrollX();
		int y = editor.getScrollY();
		int p = editor.getCaretPosition();

		args.putString(Constant.FILECONTENT, editor.getText());
		args.putInt(Constant.CURX, x);
		args.putInt(Constant.CURY, y);
		args.putInt(Constant.CURSOR, p);
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
		getActivity().startActionMode(editAction);
		return true;
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

	@Override
	public void onCancel(int arg0) {
	}

	@Override
	public void onComplete(final int arg0, final Object arg1) {
		this.getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				FindResults res = (FindResults) arg1;

				if (arg0 == 4) {
					if (res.foundOffset != -1) {
						editor.setSelectionRange(0, 0);
						editor.setSelected(false);
						editor.selectText(false);

						int start = res.foundOffset;
						int end = start + res.searchTextLength - 1;

						Logger.e(start);
						Logger.e(end);
						editor.moveCaret(end);
						editor.setSelectionRange(start, res.searchTextLength);
						editor.respan();
					} else {
						Toast.makeText(getActivity(), R.string.notext_found,
								Toast.LENGTH_SHORT).show();
					}
				} else if (arg0 == 16) {
					editor.respan();
					Toast.makeText(
							getActivity(),
							getString(R.string.replace_succ,
									res.replacementCount), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}

	@Override
	public void onError(int arg0, int arg1, String arg2) {
	}

}
