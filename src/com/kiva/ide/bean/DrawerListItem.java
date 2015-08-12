package com.kiva.ide.bean;


public class DrawerListItem {

	public static final int ID_NEW = -10;
	public static final int ID_OPEN = -9;
	public static final int ID_SAVE = -8;
	public static final int ID_SAVEAS = -7;
	public static final int ID_SEARCH = -6;
	public static final int ID_GOTO = -5;
	
	
	public String title;
	public int id;
	
	
	public DrawerListItem() {
		// TODO Auto-generated constructor stub
	}

	public DrawerListItem(int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	
}
