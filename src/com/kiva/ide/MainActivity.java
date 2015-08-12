package com.kiva.ide;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.kiva.ide.adapter.DrawerListAdapter;
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


@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

	private DrawerLayout drawer;
	private ListView drawerList;
	private DrawerListAdapter adapter;
	private ProgressDialog waitDialog;
	private int taskCount = 0;

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
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	}

	private void initView() {
		drawer = (DrawerLayout) findViewById(R.id.idMainDrawerLayout);
		drawerList = (ListView) findViewById(R.id.idMainDrawerList);

		drawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

		adapter = new DrawerListAdapter(this, null);
		drawerList.setAdapter(adapter);
		drawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				handleOnDrawerItemClick(position, id);
			}
		});

	}

	private void handleOnDrawerItemClick(int position, long id) {
		drawer.closeDrawer(Gravity.START);

		switch ((int) id) {
		case DrawerListItem.ID_NEW:
			addTab("");
			break;
		case DrawerListItem.ID_OPEN:
			openFileBrowser();
			break;
		case DrawerListItem.ID_SAVE:
			saveFile(null);
			break;
		case DrawerListItem.ID_SAVEAS:
			saveFileAs();
			break;
		case DrawerListItem.ID_SEARCH:
			searchInText();
			break;
		case DrawerListItem.ID_GOTO:
			gotoLine();
			break;
		}
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

	private void searchInText() {
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

	private void saveFile(String newAbsFileName) {
		CodeEditFragment frag = getCurrentPage();
		if (frag == null) {
			return;
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

		FileWriteTask task = new FileWriteTask(ed.getText(),
				newAbsFileName == null ? frag.getFileName() : newAbsFileName,
				frag.hashCode());
		TaskManager.startTask(task, Constant.REQ_WRITE, taskHandler);
	}

	private void openFileBrowser() {
		startActivityForResult(new Intent(this, ChooserActivity.class),
				Constant.REQ_CHOOSE);
	}

	public void addTab(String text) {
		addTab(null, text);
	}

	public void addTab(String fileName, String text) {
		addTab(fileName, text, true);
	}

	public void addTab(String fileName, String text, boolean go) {
		Bundle args = new Bundle();

		if (fileName != null) {
			if (fileName.equals("") || fileName.trim().equals("")) {
				return;
			} else {
				args.putString(Constant.FILENAME, fileName);
			}
		}

		if (text != null) {
			args.putString(Constant.FILECONTENT, text);
		}

		ActionBar ab = getActionBar();

		Tab tab = ab.newTab();

		tab.setTabListener(new TabListener<CodeEditFragment>(
				CodeEditFragment.class, this, args));
		tab.setCustomView(R.layout.edit_tab);

		ab.addTab(tab, go);
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

	public FastCodeEditor getCurrentEditor() {
		CodeEditFragment frag = getCurrentPage();

		return frag == null ? null : frag.getEditor();
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

	private void doCloseThis() {
		int pos = getCurrentPos();
		if (pos < 0) {
			return;
		}

		getActionBar().removeTabAt(pos);
	}

	private void doCloseOther() {
		ActionBar ab = getActionBar();
		Tab tab = ab.getSelectedTab();

		if (tab == null) {
			return;
		}

		ab.removeAllTabs();
		ab.addTab(tab);
	}

	private void doCloseAll() {
		getActionBar().removeAllTabs();
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
				
				ActionBar ab = getActionBar();
				int count = ab.getTabCount();
				for(int i=0; i<count; i++) {
					Tab t = ab.getTabAt(i);
					if (t == null)
						continue;
					
					CodeEditFragment frag = (CodeEditFragment) t.getTag();
					
					if (frag.getFileName().equals(path)) {
						ab.selectTab(t);
						return;
					}
				}
				
				FileReadTask task = new FileReadTask(path);
				TaskManager.startTask(task, Constant.REQ_READ, taskHandler);
			} else if (requestCode == Constant.REQ_CHOOSE_SAVEAS) {
				saveFile(path);
			}

			break;
		}
		}
	}

	@Override
	public void finish() {
		if (drawer.isDrawerOpen(Gravity.START)) {
			drawer.closeDrawer(Gravity.START);
			return;
		}

		super.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.menu_close_this:
			doCloseThis();
			break;
		case R.id.menu_close_other:
			doCloseOther();
			break;
		case R.id.menu_close_all:
			doCloseAll();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			int gravity = Gravity.START;

			if (drawer.isDrawerOpen(gravity)) {
				drawer.closeDrawer(gravity);
			} else {
				drawer.openDrawer(gravity);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
