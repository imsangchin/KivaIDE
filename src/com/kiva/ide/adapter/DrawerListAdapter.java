package com.kiva.ide.adapter;

import static com.kiva.ide.bean.DrawerListItem.ID_GOTO;
import static com.kiva.ide.bean.DrawerListItem.ID_NEW;
import static com.kiva.ide.bean.DrawerListItem.ID_OPEN;
import static com.kiva.ide.bean.DrawerListItem.ID_SAVE;
import static com.kiva.ide.bean.DrawerListItem.ID_SAVEAS;
import static com.kiva.ide.bean.DrawerListItem.ID_SEARCH;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kiva.ide.R;
import com.kiva.ide.bean.DrawerListItem;
import com.kiva.ide.util.AdapterUtil;

public class DrawerListAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private List<DrawerListItem> allDatas = new ArrayList<DrawerListItem>();
	private List<DrawerListItem> datas = new ArrayList<DrawerListItem>();
	
	public DrawerListAdapter(Context ctx, List<DrawerListItem> items) {
		inflater = LayoutInflater.from(ctx);
		
		datas.add(new DrawerListItem(ID_NEW, ctx.getString(R.string.title_new_file)));
		datas.add(new DrawerListItem(ID_OPEN, ctx.getString(R.string.title_open_file)));
		datas.add(new DrawerListItem(ID_SAVE, ctx.getString(R.string.title_save_file)));
		datas.add(new DrawerListItem(ID_SAVEAS, ctx.getString(R.string.title_save_as)));
		datas.add(new DrawerListItem(ID_SEARCH, ctx.getString(R.string.title_search)));
		datas.add(new DrawerListItem(ID_GOTO, ctx.getString(R.string.title_goto)));
		
		allDatas.addAll(datas);
		
		if (items != null) {
			allDatas.addAll(items);
		}
	}
	
	@Override
	public int getCount() {
		return allDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return allDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return allDatas.get(position).id;
	}

	@SuppressLint("InflateParams") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerListItem it = (DrawerListItem) getItem(position);
		ViewHolder holder = null;
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_list_item, null, false);
			holder = new ViewHolder();
			
			holder.titleView = (TextView) convertView.findViewById(R.id.idItemTitle);
			holder.img = (ImageView) convertView.findViewById(R.id.idItemImg);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.titleView.setText(it.title);
		
		int resId = AdapterUtil.getDrawerIconResById(it.id);
		if (resId != -1) {
			holder.img.setImageResource(resId);
		}
		
		return convertView;
	}
	
	public int add(DrawerListItem i) {
		this.allDatas.add(i);
		notifyDataSetChanged();
		
		return allDatas.indexOf(i);
	}
	
	public DrawerListItem get(int pos) {
		return this.allDatas.get(pos);
	}
	
	public void remove(int pos) {
		this.allDatas.remove(pos);
		
		notifyDataSetChanged();
	}
	
	class ViewHolder {
		public ImageView img;
		public TextView titleView;
	}
}
