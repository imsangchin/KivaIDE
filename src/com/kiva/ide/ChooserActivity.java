package com.kiva.ide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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

public class ChooserActivity extends Activity implements OnClickListener,
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

		initData();
		initView();
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

	private void loadDir(File dir) {
		if (dir == null) {
			return;
		}

		List<Holder> data = listCurDir(dir);

		adapter = new FileExplorerAdapter(this, data);
		listView.setAdapter(adapter);

		this.currentPath = dir;
		this.pathView.setText(dir.getAbsolutePath());
	}

	private List<Holder> listCurDir(File path) {
		List<Holder> list = new ArrayList<ChooserActivity.Holder>();

		Holder h = new Holder();
		h.file = new File("..");
		list.add(h);

		if (path == null || path.getAbsolutePath().length() == 0) {
			return list;
		}

		if ((!path.exists()) || (!path.isDirectory()) || (!path.canRead())) {
			return list;
		}

		if (path.getAbsolutePath().equals("/")) {
			list.remove(0);
		}

		File[] files = path.listFiles();
		if (files == null || files.length == 0) {
			return list;
		}

		for (File file1 : files) {
			h = new Holder();

			h.file = file1;
			list.add(h);
		}

		Collections.sort(list, new Comparator<Holder>() {
			@SuppressLint("DefaultLocale")
			public int compare(Holder file1, Holder file2) {
				boolean a = file1.file.isDirectory();
				boolean b = file2.file.isDirectory();

				if (a && !b) {
					return -1;
				} else if (!a && b) {
					return 1;
				} else if (a && b) {
					return file1.file.getName().toLowerCase()
							.compareTo(file2.file.getName().toLowerCase());
				} else {
					return file1.file.getName().compareTo(file2.file.getName());
				}
			}
		});

		if (path.canWrite()) {
			h = new Holder();
			h.file = new File(getString(R.string.new_folder));
			list.add(1, h);
		}

		return list;
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
