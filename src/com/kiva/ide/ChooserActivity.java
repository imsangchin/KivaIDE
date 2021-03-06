package com.kiva.ide;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kiva.ide.adapter.FileExplorerAdapter;
import com.kiva.ide.util.Constant;
import com.kiva.ide.util.ExplorerUtil;

public class ChooserActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener {

	public static class Holder {
		public File file;
	}

	private SharedPreferences sp;
	private File currentPath;
	private FileExplorerAdapter adapter;
	private int selectedFilePos = -1;

	private ListView listView;
	private Button btnCancel, btnOk;
	private TextView pathView;
	private EditText editText;
	private boolean saveAs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chooser);

		initToolbar();
		initData();
		initView();
	}
	
	private void initToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.idToolbar);  
		toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				return onOptionsItemSelected(arg0);
			}
		});
		toolbar.setLogo(R.drawable.folder);
		toolbar.setTitle(R.string.goto_choose);
		setSupportActionBar(toolbar);
	}

	private void initView() {
		listView = (ListView) findViewById(R.id.idChooserListView);
		btnCancel = (Button) findViewById(R.id.idChooserBtnCancel);
		btnOk = (Button) findViewById(R.id.idChooserBtnOk);
		pathView = (TextView) findViewById(R.id.idChooserPathView);
		editText = (EditText) findViewById(R.id.idChooserInput);

		btnOk.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		pathView.setText(currentPath.getAbsolutePath());
		listView.setOnItemClickListener(this);
		editText.setVisibility(View.VISIBLE);

		if (saveAs) {
			btnOk.setEnabled(true);
			editText.setFocusable(true);
			editText.setText(getIntent().getStringExtra(Constant.FILENAME));
		} else {
			btnOk.setEnabled(false);
			editText.setFocusable(false);
			editText.setTextColor(Color.WHITE);
			editText.setEnabled(false);
		}

		loadDir(currentPath);
	}

	private void initData() {
		setResult(RESULT_CANCELED);

		sp = PreferenceManager.getDefaultSharedPreferences(this);

		String path = sp.getString(Constant.LAST_PATH, null);

		if (path == null || !(currentPath = new File(path)).exists()) {
			currentPath = Environment.getExternalStorageDirectory();
		}

		Intent i = getIntent();
		saveAs = i.getBooleanExtra(Constant.ISSAVEAS, false);

		selectedFilePos = -1;
	}

	private boolean loadDir(File dir) {
		if (dir == null) {
			return false;
		}

		List<Holder> data = ExplorerUtil.listDir(this, dir);

		adapter = new FileExplorerAdapter(this, data);
		listView.setAdapter(adapter);

		this.currentPath = dir;
		this.pathView.setText(dir.getAbsolutePath());
		
		return true;
	}

	

	@Override
	public void onClick(View v) {
		if (v == btnCancel) {
			setResult(RESULT_CANCELED);
			finish();
		} else if (v == btnOk) {
			Intent data = new Intent();
			Uri url = null;
			if (saveAs) {
				String fileName = editText.getText().toString();
				if (TextUtils.isEmpty(fileName)) {
					return;
				}

				File newFlie = new File(currentPath, editText.getText()
						.toString());
				url = Uri.fromFile(newFlie);
			} else {
				if (selectedFilePos < 1) {
					return;
				}
				url = Uri.fromFile(((Holder) listView
						.getItemAtPosition(selectedFilePos)).file);
			}
			data.setData(url);
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		sp.edit().putString(Constant.LAST_PATH, currentPath.getAbsolutePath())
				.commit();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Holder it = (Holder) adapter.getItem(position);
		File f = it.file;

		editText.setText("");

		if (f.getName().equals("..")) {
			loadDir(currentPath.getParentFile());
			return;
		}

		if (f.getName().equals(getString(R.string.new_folder))) {
			createNewFolder(position);
			return;
		}

		if (f.isDirectory()) {
			loadDir(f);
			return;
		}

		listView.setScrollY(0);
		btnOk.setEnabled(true);
		editText.setText(f.getName());
		selectedFilePos = position;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (loadDir(currentPath.getParentFile())) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void createNewFolder(int position) {
		final EditText nameInput = new EditText(this);
		nameInput.setHint(R.string.input_folder_name);

		new AlertDialog.Builder(this)
				.setTitle(R.string.new_folder)
				.setView(nameInput)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String name = nameInput.getText().toString();
								if (TextUtils.isEmpty(name)) {
									Toast.makeText(ChooserActivity.this,
											R.string.input_folder_name,
											Toast.LENGTH_SHORT).show();
									return;
								}

								File dir = new File(currentPath, name);
								if (dir.exists()) {
									Toast.makeText(ChooserActivity.this,
											R.string.already_exists,
											Toast.LENGTH_SHORT).show();
									return;
								}

								if (!dir.mkdir()) {
									Toast.makeText(ChooserActivity.this,
											R.string.mkdir_fail,
											Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(ChooserActivity.this,
											R.string.mkdir_succ,
											Toast.LENGTH_SHORT).show();
								}

								loadDir(currentPath);
							}
						}).setNegativeButton(R.string.no, null).show();
	}

}
