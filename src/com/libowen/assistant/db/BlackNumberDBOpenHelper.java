package com.libowen.assistant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BlackNumberDBOpenHelper extends SQLiteOpenHelper {
	
	public BlackNumberDBOpenHelper(Context context) {
		//参数一：上下文对象，参数二：数据库名称，参数三：游标工厂对象，null表示使用系统默认的，参数四：当前数据库的版本号
		super(context, "blacknumber.db", null, 1);
	}
	
	/**
	 * 数据库第一次被创建的时候执行 oncreate().
	 * 一般用于指定数据库的表结构
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		//黑名单号码的表结构 （_id , 黑名单号码, 拦截模式（0 表示电话拦截 ，1表示短信拦截 ，2表示全部拦截（电话&短信））
		db.execSQL("create table blacknumber (_id integer primary key autoincrement, number varchar(20), mode integer)");
	}

	/**
	 * 当数据库的版本号升级的时候 调用的方法.
	 * 一般用于升级程序后,更新数据库的表结构.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
