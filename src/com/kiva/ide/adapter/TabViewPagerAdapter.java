package com.kiva.ide.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.kiva.ide.fragment.CodeEditFragment;

public class TabViewPagerAdapter extends FragmentStatePagerAdapter {
	
	List<CodeEditFragment> list = new ArrayList<>();

	public TabViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int arg0) {
		if (arg0 >= getCount()) {
			return null;
		}
		
		return list.get(arg0);
	}

	@Override
	public int getCount() {
		return list.size();
	}
	
	
	@Override
	public CharSequence getPageTitle(int position) {
		CodeEditFragment f = list.get(position);
		
		return new File(f.getFileName()).getName();
	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	
	public int add(CodeEditFragment frag) {
		list.add(frag);
		notifyDataSetChanged();
		
		return indexOf(frag);
	}
	
	public void rm(int pos) {
		list.remove(pos);
		notifyDataSetChanged();
	}

	public int indexOf(CodeEditFragment frag) {
		return list.indexOf(frag);
	}
}
