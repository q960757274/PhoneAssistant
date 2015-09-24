package com.libowen.assistant.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CommonNumDao {

	/**
	 * 返回数据库有多少个分组
	 * 
	 * @return
	 */
	public static int getGroupCount() {
		int count = 0;
		// 所要打开的数据库在手机系统中的位置
		String path = "/data/data/com.guoshisp.mobilesafe/files/commonnum.db";
		// 打开数据库
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		if (db.isOpen()) {
			// 从classlist表中查询出有多少组公用号码
			Cursor cursor = db.rawQuery("select * from classlist", null);
			count = cursor.getCount();
			// 使用完数据库后需要关闭
			cursor.close();
			db.close();
		}
		return count;
	}

	/**
	 * 获取所有的分组集合信息（也即每个分组的名字）
	 * 
	 * @return
	 */
	public static List<String> getGroupNames() {
		// 用于存放各个分组的名字信息
		List<String> groupNames = new ArrayList<String>();
		String path = "/data/data/com.guoshisp.mobilesafe/files/commonnum.db";
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		if (db.isOpen()) {
			// 获取各个分组的名字的结果集（游标）
			Cursor cursor = db.rawQuery("select name from classlist", null);
			// 遍历出每个分组对应的名字
			while (cursor.moveToNext()) {
				String groupName = cursor.getString(0);
				groupNames.add(groupName);
				groupName = null;
			}
			cursor.close();
			db.close();
		}
		return groupNames;
	}

	/**
	 * 通过点击的分组对应的id来获取某该分组名称
	 * 
	 * @param groupPosition
	 * @return
	 */
	public static String getGroupNameByPosition(int groupPosition) {
		String name = null;
		String path = "/data/data/com.guoshisp.mobilesafe/files/commonnum.db";
		// 因为classlist表中的name的id是从1开始的，而ExpandableListView中的id是从0开始的
		int newposition = groupPosition + 1;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		if (db.isOpen()) {
			// 通过id的查询，来获取该id多对应的name
			Cursor cursor = db.rawQuery(
					"select name from classlist where idx=?",
					new String[] { newposition + "" });
			if (cursor.moveToFirst()) {// cursor指针的默认位置是在第一条数据上面的，所以，想获取数据的话，指针必须往下移动
				// 因为一个id只是对应一个name，所以只需要获取到第一个即可
				name = cursor.getString(0);
			}
			cursor.close();
			db.close();
		}
		return name;
	}

	/**
	 * 获取对应的分组里面有多少个子孩子（也即每个分组里面有多少条号码）
	 * 而每个孩子都各自对应一张表，所以在查询子孩子的信息的时候需要确定是查询哪张孩子所对应的表：table+position
	 * @param groupPosition
	 * @return
	 */
	public static int getChildrenCount(int groupPosition) {
		int count = 0;
		String path = "/data/data/com.guoshisp.mobilesafe/files/commonnum.db";
		// 打开数据库
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		// 因为groupPosition的起始值是从开始的，而table表中的_id是从1开始的
		int newposition = groupPosition + 1;
		String sql = "select * from table" + newposition;
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery(sql, null);
			// 获取到查询结果的所有列数（也相当于有多少条号码）
			count = cursor.getCount();
			cursor.close();
			db.close();
		}
		return count;
	}

	/**
	 * 获取对应位置的子孩子的信息。
	 * 而每个孩子都各自对应一张表，所以在查询子孩子的信息的时候需要确定是查询哪张孩子所对应的表：table+position
	 */
	public static String getChildNameByPosition(int groupPosition,
			int childPosition) {
		String result = null;
		String path = "/data/data/com.guoshisp.mobilesafe/files/commonnum.db";
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		int newGroupPosition = groupPosition + 1;
		int newChildPosition = childPosition + 1;
		// 查询子孩子的name和number
		String sql = "select name,number from table" + newGroupPosition
				+ " where _id=?";
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery(sql, new String[] { newChildPosition
					+ "" });
			if (cursor.moveToFirst()) {
				//因为查询的是name和number，且name在前，number在后，所以是两条信息
				String name = cursor.getString(0);
				String number = cursor.getString(1);
				result = name + "\n" + number;
			}
			cursor.close();
			db.close();
		}
		return result;
	}

	/**
	 * 获取每一个分组所有的子孩子的信息（name和number）
	 * 而每个孩子都各自对应一张表，所以在查询子孩子的信息的时候需要确定是查询哪张孩子所对应的表：table+position
	 */
	public static List<String> getChildNameByPosition(int groupPosition) {
		String result = null;
		List<String> results = new ArrayList<String>();
		String path = "/data/data/com.guoshisp.mobilesafe/files/commonnum.db";
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		int newGroupPosition = groupPosition + 1;
		// 查询子孩子的name和number
		String sql = "select name,number from table" + newGroupPosition;
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				//因为查询的是name和number，且name在前，number在后，所以是两条信息
				String name = cursor.getString(0);
				String number = cursor.getString(1);
				result = name + "\n" + number;
				results.add(result);
				result = null;
			}
			cursor.close();
			db.close();
		}
		return results;
	}

}
