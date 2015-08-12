package com.kiva.ide.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kiva.ide.ChooserActivity;
import com.kiva.ide.ChooserActivity.Holder;
import com.kiva.ide.R;
import com.kiva.ide.util.AdapterUtil;

@SuppressLint("InflateParams") public class FileExplorerAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private List<ChooserActivity.Holder> datas;
	private Context ctx;
	
	public FileExplorerAdapter(Context ctx, List<Holder> datas) {
		this.inflater = LayoutInflater.from(ctx);
		this.datas = datas;
		this.ctx = ctx;
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.drawer_list_item, null, false);
			
			holder.img = (ImageView) convertView.findViewById(R.id.idItemImg);
			holder.title = (TextView) convertView.findViewById(R.id.idItemTitle);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Holder h = (Holder) getItem(position);
		int resId = AdapterUtil.getFileIcon(ctx, h.file);
		
		holder.title.setText(h.file.getName());
		holder.img.setImageResource(resId);
		
		return convertView;
	}
	
	public void setDatas(List<ChooserActivity.Holder> datas) {
		this.datas.clear();
		this.datas.addAll(datas);
		
		notifyDataSetChanged();
	}
	
	class ViewHolder {
		public ImageView img;
		public TextView title;
	}

}
