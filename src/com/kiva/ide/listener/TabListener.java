package com.kiva.ide.listener;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.kiva.ide.R;
import com.kiva.ide.fragment.CodeEditFragment;

@SuppressWarnings("deprecation")
public class TabListener<T extends Fragment> implements ActionBar.TabListener {

	private T fragment;
	private final Class<T> fragmentClass;
	private final Activity host;
	private final Bundle args;

	public TabListener(Class<T> fragment, Activity host, Bundle args) {
		this.fragmentClass = fragment;
		this.host = host;
		this.args = args;

		this.fragment = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (fragment == null) {
			fragment = (T) Fragment.instantiate(host, fragmentClass.getName(),
					args);

			if (fragment instanceof CodeEditFragment) {
				((CodeEditFragment) fragment).setTab(tab);
				tab.setTag(fragment);
			}

			ft.add(R.id.idMainContainer, fragment);
		}

		ft.attach(fragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (fragment != null) {
			ft.detach(fragment);
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

}
