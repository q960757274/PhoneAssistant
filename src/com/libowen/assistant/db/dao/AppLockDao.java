package com.libowen.assistant.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.libowen.assistant.db.AppLockDBOpenHelper;

public class AppLockDao {
	private AppLockDBOpenHelper helper;

	public AppLockDao(Context context) {
		helper = new AppLockDBOpenHelper(context);
	}
	/**
	 * 查找一条锁定程序的包名
	 * return true代表查找到该包名   false代表没有查找到该包名
	 */
	public boolean find(String packname) {
		boolean result = false;
		//打开数据库
		SQLiteDatabase db = helper.getReadableDatabase();
		if (db.isOpen()) {
			//执行查询SQL语句，返回一个结果集。
			Cursor cursor = db.rawQuery(
					"select * from applock where packname =?",
					new String[] { packname });
			if (cursor.moveToFirst()) {
				result = true;
			}
			//关闭数据库
			cursor.close();
			db.close();
		}
		return result;
	}
	/**
	 * 添加一条锁定的程序的包名
	 */
	public boolean add(String packname) {
		//首先查询一个数据库中是否存在该条数据，防止重复添加
		if (find(packname))
			return false;
		SQLiteDatabase db = helper.getWritableDatabase();
		if (db.isOpen()) {
			//执行添加的SQL语句
			db.execSQL("insert into applock (packname) values (?)",
					new Object[] { packname });
			db.close();
		}
		return find(packname);
	}

	/**
	 * 删除一条包名
	 */
	public void delete(String packname) {
		SQLiteDatabase db = helper.getWritableDatabase();
		if (db.isOpen()) {
			//执行删除的SQL语句
			db.execSQL("delete from applock where packname=?",
					new Object[] { packname });
			db.close();
		}
	}
	/**
	 * 查找全部被锁定的应用包名
	 * 
	 * @return
	 */
	public List<String> findAll() {
		List<String> packnames = new ArrayList<String>();
		SQLiteDatabase db = helper.getReadableDatabase();
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select packname from applock",
					null);
			while (cursor.moveToNext()) {
				packnames.add(cursor.getString(0));
			}
			cursor.close();
			db.close();
		}
		return packnames;
	}
}
