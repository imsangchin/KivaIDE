package com.kiva.ide.action;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.kiva.ide.R;
import com.kiva.ide.fragment.CodeEditFragment;
import com.kiva.ide.view.FastCodeEditor;

public class EditAction implements ActionMode.Callback {
	private CodeEditFragment act;
	private ActionMode actionMode;
	private boolean showing = false;

	public EditAction(CodeEditFragment a) {
		this.act = a;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		actionMode = mode;
		setShowing(true);
		if (menu.size() < 4) {
			mode.getMenuInflater().inflate(R.menu.edit_action, menu);
		}
		updateMenu();

		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		actionMode = mode;
		return this.onCreateActionMode(mode, menu);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		actionMode = mode;
		setShowing(true);
		return act.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		actionMode = null;
		setShowing(false);
	}

	public void updateMenu() {
		if (actionMode == null) {
			return;
		}

		FastCodeEditor editor = act.getEditor();
		updateMenu(editor.isSelectText());
	}

	public void updateMenu(boolean select) {
		if (actionMode == null) {
			return;
		}

		Menu menu = actionMode.getMenu();
		
		if (menu == null) {
			return;
		}

		MenuItem i = menu.findItem(R.id.menu_copy);
		if (i != null) {
			i.setVisible(select);
		}

		i = menu.findItem(R.id.menu_cut);
		if (i != null) {
			i.setVisible(select);
		}
	}

	public boolean isShowing() {
		return showing;
	}

	private void setShowing(boolean showing) {
		this.showing = showing;
	}

}
