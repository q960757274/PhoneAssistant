package com.libowen.assistant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AppLockDBOpenHelper extends SQLiteOpenHelper {

	public AppLockDBOpenHelper(Context context) {
		//参数一：应用上下文，参数二：数据库名称
		//参数三：游标工厂对象，null表示使用系统默认的游标工厂对象，参数四：版本号
		super(context, "applock.db", null, 1);
	}
	
	/**
	 * 数据库第一次被创建的时候执行该方法
	 * 在该方法中，一般用于指定数据库的表结构
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建程序锁的表 (表中包含_id,包名 )
		db.execSQL("create table applock (_id integer primary key autoincrement, packname varchar(20))");
	}

	/**
	 * 当数据库的版本号 发生增加的时候调用的方法.
	 * 一般用于升级程序后,更新数据库的表结构.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
