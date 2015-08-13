package com.kiva.ide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kiva.ide.adapter.DrawerListAdapter;
import com.kiva.ide.adapter.RecentFileAdapter;
import com.kiva.ide.bean.DrawerListItem;
import com.kiva.ide.fragment.CodeEditFragment;
import com.kiva.ide.listener.TabListener;
import com.kiva.ide.task.FileReadTask;
import com.kiva.ide.task.FileWriteTask;
import com.kiva.ide.task.ITask;
import com.kiva.ide.task.TaskManager;
import com.kiva.ide.util.Constant;
import com.kiva.ide.util.Logger;
import com.kiva.ide.util.UiUtil;
import com.kiva.ide.view.FastCodeEditor;
import com.myopicmobile.textwarrior.android.RecentFiles;
import com.myopicmobile.textwarrior.android.RecentFiles.RecentFile;

@SuppressWarnings("deprecation")
@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements OnItemClickListener,
		OnItemLongClickListener, android.view.View.OnClickListener {

	private DrawerLayout drawer;
	private ListView drawerList;
	private DrawerListAdapter adapter;
	private ProgressDialog waitDialog;
	private Menu menu;

	private ActionBarDrawerToggle toggle;

	private EditText findTextInput, replaceTextInput;
	private Button findBtn, replaceBtn, replaceAllBtn;
	private CheckBox caseInsensitiveCheck, wholeWordCheck;

	private volatile int taskCount = 0;

	private Handler taskHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.WHAT_START:
				taskCount++;
				showWaitDialog();
				break;

			case Constant.WHAT_TASK:
				ITask task = (ITask) msg.obj;
				switch (task.getRequestCode()) {
				case Constant.REQ_READ:
					handleReadTaskFinish(task);
					break;
				case Constant.REQ_WRITE:
					handleWriteTaskFinish(task);
					break;
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initData();
		initView();
	}

	private void initData() {
		UiUtil.setNavigationBarShowMenuKey(this, true);
		UiUtil.setMenuOverflowShowing(this, true);

		waitDialog = UiUtil.createWaitingDialog(this);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}

	private void initView() {
		drawer = (DrawerLayout) findViewById(R.id.idMainDrawerLayout);
		drawerList = (ListView) findViewById(R.id.idMainDrawerList);

		replaceBtn = (Button) findViewById(R.id.idSearchStartReplace);
		replaceAllBtn = (Button) findViewById(R.id.idSearchStartReplaceAll);
		replaceTextInput = (EditText) findViewById(R.id.idSearchNewStr);
		findTextInput = (EditText) findViewById(R.id.idSearchStr);
		findBtn = (Button) findViewById(R.id.idSearchStartFind);
		caseInsensitiveCheck = (CheckBox) findViewById(R.id.idSearchCaseInsCheckBox);
		wholeWordCheck = (CheckBox) findViewById(R.id.idSearchWholeWordCheckBox);

		drawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		toggle = new ActionBarDrawerToggle(this, drawer, R.drawable.ic_drawer,
				R.string.yes, R.string.no);
		toggle.setDrawerIndicatorEnabled(true);
		toggle.syncState();

		adapter = new DrawerListAdapter(this, null);
		drawerList.setAdapter(adapter);
		drawerList.setOnItemClickListener(this);
		drawerList.setOnItemLongClickListener(this);

		findBtn.setOnClickListener(this);
		replaceBtn.setOnClickListener(this);
		replaceAllBtn.setOnClickListener(this);
	}

	private void handleReadTaskFinish(ITask task) {
		taskCount--;
		cancelWaitDialog();

		if (task.getResultCode() != RESULT_OK) {
			return;
		}

		Bundle data = task.getData();
		if (data == null) {
			return;
		}

		addTab(data.getString(Constant.FILENAME, ""), // if "" it returned, tab
														// will not add
				data.getString(Constant.FILECONTENT));
	}

	private void handleWriteTaskFinish(ITask task) {
		taskCount--;
		cancelWaitDialog();

		if (task.getResultCode() != RESULT_OK) {
			return;
		}

		Bundle data = task.getData();
		if (data == null) {
			return;
		}

		int hash = data.getInt(Constant.HASH, -1);
		if (hash == -1) {
			return;
		}

		ActionBar ab = getActionBar();
		int count = ab.getTabCount();
		for (int i = 0; i < count; i++) {
			Tab t = ab.getTabAt(i);
			if (t == null) {
				continue;
			}

			CodeEditFragment f = (CodeEditFragment) t.getTag();
			if (f == null) {
				continue;
			}

			if (f.hashCode() == hash) {
				String name = data.getString(Constant.FILENAME);
				f.setFileName(name);
			}
		}

		Toast.makeText(this, R.string.save_succ, Toast.LENGTH_SHORT).show();
	}

	private void gotoLine() {

	}

	private void saveFileAs() {
		CodeEditFragment frag = getCurrentPage();
		if (frag == null) {
			return;
		}

		Intent i = new Intent(this, ChooserActivity.class);
		i.putExtra(Constant.ISSAVEAS, true);
		i.putExtra(Constant.FILENAME, new File(frag.getFileName()).getName());

		startActivityForResult(i, Constant.REQ_CHOOSE_SAVEAS);
	}

	private void saveFile(String newAbsFileName, CodeEditFragment frag) {
		if (frag == null) {
			frag = getCurrentPage();

			if (frag == null) {
				return;
			}
		}

		FastCodeEditor ed = frag.getEditor();
		if (ed == null) {
			return;
		}

		if (newAbsFileName == null) {
			String fragFileName = frag.getFileName();
			if (fragFileName.equals(Constant.UNTITLED)) {
				saveFileAs();
			} else {
				newAbsFileName = fragFileName;
			}
		}

		ed.setEdited(false);

		FileWriteTask task = new FileWriteTask(ed.getText(),
				newAbsFileName == null ? frag.getFileName() : newAbsFileName,
				frag.hashCode());
		TaskManager.startTask(task, Constant.REQ_WRITE, taskHandler);
	}

	private void openFileBrowser() {
		startActivityForResult(new Intent(this, ChooserActivity.class),
				Constant.REQ_CHOOSE);
	}

	public void loadRecentFile(RecentFile rf) {
		if (rf == null) {
			return;
		}

		Tab t = isFileOpend(rf.getFileName());
		if (t != null) {
			getActionBar().selectTab(t);
			return;
		}

		FileReadTask task = new FileReadTask(rf.getFileName(), rf, true);
		TaskManager.startTask(task, Constant.REQ_READ, taskHandler);
	}

	public void addTab(String text) {
		addTab(null, text);
	}

	public void addTab(String fileName, String text) {
		addTab(fileName, text, true);
	}

	public void addTab(String fileName, String text, boolean go) {
		Bundle args = new Bundle();

		args.putString(Constant.FILENAME, fileName);
		args.putString(Constant.FILECONTENT, text == null ? "" : text);

		addTab(args, go);
	}

	public void addTab(Bundle data, boolean go) {
		if (data == null) {
			return;
		}

		String fileName = data.getString(Constant.FILENAME, null);
		if (fileName != null) {
			if (fileName.equals("") || fileName.trim().equals("")) {
				return;
			}
		}

		ActionBar ab = getActionBar();
		Tab tab = ab.newTab();

		tab.setTabListener(new TabListener<CodeEditFragment>(
				CodeEditFragment.class, this, data));
		tab.setCustomView(R.layout.edit_tab);

		ab.addTab(tab, go);

		updateUndoRedo();
	}

	public void showWaitDialog() {
		if (!waitDialog.isShowing()) {
			waitDialog.show();
		}
	}

	public void cancelWaitDialog() {
		if (taskCount < 0) {
			taskCount = 0;
		}
		if (taskCount == 0 && waitDialog.isShowing()) {
			waitDialog.cancel();
		}
	}

	public CodeEditFragment getCurrentPage() {
		return getPage(getCurrentPos());
	}

	public int getCurrentPos() {
		Tab tab = getActionBar().getSelectedTab();

		return tab == null ? -1 : tab.getPosition();
	}

	public CodeEditFragment getPage(int p) {
		if (p < 0) {
			return null;
		}

		Tab tab = getActionBar().getTabAt(p);
		if (tab == null) {
			return null;
		}

		CodeEditFragment frag = (CodeEditFragment) tab.getTag();

		return frag;
	}

	private void doCloseAt(final Tab t, boolean promptToSave) {
		if (t == null) {
			return;
		}

		final ActionBar ab = getActionBar();
		final CodeEditFragment frag = (CodeEditFragment) t.getTag();

		if (promptToSave && frag.getEditor().isEdited()) {
			new AlertDialog.Builder(this)
					.setTitle(frag.getFileName())
					.setMessage(R.string.save_ask)
					.setPositiveButton(android.R.string.yes,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									saveFile(null, frag);
									ab.removeTab(t);
								}
							})
					.setNegativeButton(android.R.string.no,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									ab.removeTab(t);
								}
							}).show();
		} else {
			ab.removeTab(t);
		}

		updateUndoRedo();
	}

	private void updateUndoRedo() {
		if (menu == null) {
			return;
		}

		boolean hasTab = getActionBar().getTabCount() > 0;

		MenuItem i = menu.findItem(R.id.menu_undo);
		if (i != null) {
			i.setVisible(hasTab);
		}

		i = menu.findItem(R.id.menu_redo);
		if (i != null) {
			i.setVisible(hasTab);
		}
	}

	private void doUndo() {
		CodeEditFragment frag = getCurrentPage();
		if (frag == null) {
			return;
		}

		frag.undo();
	}

	private void doRedo() {
		CodeEditFragment frag = getCurrentPage();
		if (frag == null) {
			return;
		}

		frag.redo();
	}

	private void doCloseThis() {
		ActionBar ab = getActionBar();
		Tab t = ab.getSelectedTab();

		doCloseAt(t, true);
	}

	private void doCloseOther() {
		ActionBar ab = getActionBar();
		Tab tab = ab.getSelectedTab();

		if (tab == null) {
			return;
		}

		doCloseAll(tab);
	}

	private void doCloseAll(final Tab ignore) {
		final ActionBar ab = getActionBar();

		final List<CodeEditFragment> unsavedPage = new ArrayList<CodeEditFragment>();

		for (int i = 0; i < ab.getTabCount(); i++) {
			Tab t = ab.getTabAt(i);

			if (ignore != null && ignore == t) {
				continue;
			}

			CodeEditFragment frag = (CodeEditFragment) t.getTag();
			if (frag.getEditor().isEdited()) {
				unsavedPage.add(frag);
			}

			doCloseAt(t, false);
			i--;
		}

		if (unsavedPage.size() > 0) {
			String[] title = new String[unsavedPage.size()];
			int size = unsavedPage.size();
			for (int i = 0; i < size; i++) {
				title[i] = new File(unsavedPage.get(i).getFileName()).getName();
			}

			new AlertDialog.Builder(this)
					.setTitle(R.string.check_file_to_save)
					.setMultiChoiceItems(title, null,
							new OnMultiChoiceClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									CodeEditFragment frag = unsavedPage
											.get(which);
									frag.ext = isChecked;
								}
							})
					.setPositiveButton(android.R.string.ok,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									for (CodeEditFragment frag : unsavedPage) {
										if (frag.ext) {
											saveFile(null, frag);
										}
									}

								}
							}).setNegativeButton(android.R.string.no, null)
					.show();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK || data == null) {
			return;
		}

		switch (requestCode) {
		case Constant.REQ_CHOOSE_SAVEAS:
		case Constant.REQ_CHOOSE: {
			Uri url = data.getData();
			if (url == null) {
				return;
			}
			String path = url.getPath();
			Logger.d("choosed: " + path);

			if (requestCode == Constant.REQ_CHOOSE) {
				Tab t = isFileOpend(path);
				if (t != null) {
					getActionBar().selectTab(t);
					return;
				}

				FileReadTask task = new FileReadTask(path, null, false);
				TaskManager.startTask(task, Constant.REQ_READ, taskHandler);
			} else if (requestCode == Constant.REQ_CHOOSE_SAVEAS) {
				saveFile(path, null);
			}

			break;
		}
		}
	}

	private Tab isFileOpend(String path) {
		ActionBar ab = getActionBar();
		int count = ab.getTabCount();
		for (int i = 0; i < count; i++) {
			Tab t = ab.getTabAt(i);
			if (t == null)
				continue;

			CodeEditFragment frag = (CodeEditFragment) t.getTag();

			if (frag.getFileName().equals(path)) {
				ab.selectTab(t);
				return t;
			}
		}

		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);

		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case android.R.id.home:
			toggleDrawer();
			break;
		case R.id.menu_close_this:
			doCloseThis();
			break;
		case R.id.menu_close_other:
			doCloseOther();
			break;
		case R.id.menu_close_all:
			doCloseAll(null);
			break;
		case R.id.menu_undo:
			doUndo();
			break;
		case R.id.menu_redo:
			doRedo();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void toggleDrawer() {
		int gravity = Gravity.START;

		if (drawer.isDrawerOpen(gravity)) {
			drawer.closeDrawer(gravity);
		} else {
			drawer.openDrawer(gravity);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			
			toggleDrawer();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			handleBackKey();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		App.get().getRecentFiles().save();
	}

	private void handleBackKey() {
		if (drawer.isDrawerOpen(Gravity.START)) {
			drawer.closeDrawer(Gravity.START);
			return;
		}

		if (drawer.isDrawerOpen(Gravity.END)) {
			drawer.closeDrawer(Gravity.END);
			return;
		}

		CodeEditFragment frag = getCurrentPage();
		if (frag == null) {
			finish();
			return;
		}

		doCloseThis();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		drawer.closeDrawer(Gravity.START);

		switch ((int) id) {
		case DrawerListItem.ID_NEW:
			addTab("");
			break;
		case DrawerListItem.ID_OPEN:
			openFileBrowser();
			break;
		case DrawerListItem.ID_OPEN_REC:
			openRecentsFile();
			break;
		case DrawerListItem.ID_SAVE:
			saveFile(null, null);
			break;
		case DrawerListItem.ID_SAVEAS:
			saveFileAs();
			break;
		case DrawerListItem.ID_SEARCH:
			drawer.openDrawer(Gravity.END);
			break;
		case DrawerListItem.ID_GOTO:
			gotoLine();
			break;
		}
	}

	private void openRecentsFile() {
		final List<RecentFile> list = App.get().getRecentFiles()
				.getRecentFiles();

		new AlertDialog.Builder(this)
				.setTitle(R.string.title_open_rec)
				.setAdapter(new RecentFileAdapter(this, list),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								loadRecentFile(list.get(which));
							}
						}).show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		switch ((int) id) {
		case DrawerListItem.ID_OPEN_REC:
			new AlertDialog.Builder(this)
					.setTitle(R.string.clear_rec_ask)
					.setPositiveButton(android.R.string.yes,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									RecentFiles rf = App.get().getRecentFiles();

									rf.getRecentFiles().clear();
									rf.save();

									Toast.makeText(MainActivity.this,
											R.string.rec_clear_succ,
											Toast.LENGTH_SHORT).show();
								}
							}).setNegativeButton(android.R.string.no, null)
					.show();
			break;
		}

		return true;
	}

	@Override
	public void onClick(View v) {

		CodeEditFragment frag = getCurrentPage();
		if (frag == null) {
			return;
		}

		String textToFind = findTextInput.getText().toString();
		if (TextUtils.isEmpty(textToFind)) {
			return;
		}

		boolean caseInsensitive = caseInsensitiveCheck.isChecked();
		boolean wholeWord = wholeWordCheck.isChecked();
		String textToReplace = replaceTextInput.getText().toString();

		if (v == findBtn) {
			frag.find(textToFind, caseInsensitive, wholeWord);
		} else if (v == replaceBtn) {
			frag.replace(textToFind, textToReplace, false, caseInsensitive,
					wholeWord);
		} else if (v == replaceAllBtn) {
			frag.replace(textToFind, textToReplace, true, caseInsensitive,
					wholeWord);
		}
	}

}
