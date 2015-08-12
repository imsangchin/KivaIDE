package com.kiva.ide.adapter;

import java.util.List;

import com.kiva.ide.R;
import com.myopicmobile.textwarrior.android.RecentFiles.RecentFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RecentFileAdapter extends ArrayAdapter<RecentFile> {

	public RecentFileAdapter(Context context, List<RecentFile> objects) {
		super(context, 0, objects);
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RecentFile rf = getItem(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.recent_item, null, false);
		}

		((TextView) convertView).setText(rf.getFileName());

		return convertView;
	}

}
