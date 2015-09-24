package com.libowen.assistant.domain;

import android.graphics.drawable.Drawable;

public class ProcessInfo {
	//应用程序包名
	private String packname; 
	//应用程序图标
	private Drawable icon;
	//应用程序所占用的内存空间，单位是byte
	private long memsize; 
	//是否属于用户进程
	private boolean userprocess;
	//进程的pid（进程的标记）
	private int pid;
	//应哟个程序名称
	private String appname;
	//应用程序在Item中是否处于被选中状态（默认下没有被选中）
	private boolean checked;
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public long getMemsize() {
		return memsize;
	}
	public void setMemsize(long memsize) {
		this.memsize = memsize;
	}
	public boolean isUserprocess() {
		return userprocess;
	}
	public void setUserprocess(boolean userprocess) {
		this.userprocess = userprocess;
	}
}
