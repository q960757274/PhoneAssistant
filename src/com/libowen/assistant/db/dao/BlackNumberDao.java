package com.libowen.assistant.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.libowen.assistant.db.BlackNumberDBOpenHelper;
import com.libowen.assistant.domain.BlackNumber;

public class BlackNumberDao {
	private BlackNumberDBOpenHelper helper;

	public BlackNumberDao(Context context) {
		helper = new BlackNumberDBOpenHelper(context);
	}

	/**
	 * 查找一条黑名单号码（其返回值是用于判断数据库中是否存在该号码）
	 */
	public boolean find(String number) {
		// 默认情况下是没有该条数据
		boolean result = false;
		// 打开数据库
		SQLiteDatabase db = helper.getReadableDatabase();
		if (db.isOpen()) {
			// 执行查询语句后，返回一个结果集
			Cursor cursor = db.rawQuery(
					"select * from blacknumber where number =?",
					new String[] { number });
			// 默认情况下，游标指针指向在第一条数据的上方
			if (cursor.moveToFirst()) {
				// 返回true，说明数据库中已经存在了该条数据
				result = true;
			}
			// 关闭数据库
			cursor.close();
			db.close();
		}
		return result;
	}

	/**
	 * 查找一条黑名单号码的拦截模式
	 */
	public int findNumberMode(String number) {
		// 拦截模式只有3种：0代表拦截短信，1代表拦截电话，2代表拦截短信与电话。这里的默认值为-1，表示的是没有标记拦截模式
		int result = -1;
		SQLiteDatabase db = helper.getReadableDatabase();
		if (db.isOpen()) {
			// 由于一条号码值对应一个拦截模式，所以，该结果集中只有一条数据
			Cursor cursor = db.rawQuery(
					"select mode from blacknumber where number =?",
					new String[] { number });
			if (cursor.moveToFirst()) {
				// 获取第一条数据（也仅有一条数据）
				result = cursor.getInt(0);
			}
			cursor.close();
			db.close();
		}
		return result;
	}

	/**
	 * 添加一条黑名单号码
	 */
	public boolean add(String number, String mode) {
		// 首先判断数据库中是否已经存在该条数据， 防止添加重复的数据显示到黑名单列表中
		if (find(number))
			// 如果数据库中已经存在要添加的数据，直接停止掉该方法的执行
			return false;
		SQLiteDatabase db = helper.getWritableDatabase();
		if (db.isOpen()) {
			// 执行添加数据的SQL语句
			db.execSQL("insert into blacknumber (number,mode) values (?,?)",
					new Object[] { number, mode });
			db.close();
		}
		// 如果代码能够执行到这一步，说明上面的添加操作也执行了。所以查询的返回值必定为true
		return find(number);
	}

	/**
	 * 删除一条黑名单号码
	 */
	public void delete(String number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		if (db.isOpen()) {
			// 执行删除操作
			db.execSQL("delete from blacknumber where number=?",
					new Object[] { number });
			db.close();
		}
	}

	/**
	 * 更改黑名单号码
	 * 
	 * @param oldnumber
	 *            旧的的电话号码
	 * @param newnumber
	 *            新的号码 可以留空
	 * @param mode
	 *            新的模式
	 */
	public void update(String oldnumber, String newnumber, String mode) {

		SQLiteDatabase db = helper.getWritableDatabase();
		if (db.isOpen()) {
			if (TextUtils.isEmpty(newnumber)) {
				// 如果新的号码为空的话，则说明用户并没有修改该号码（ListView中的item设置有删除功能）
				newnumber = oldnumber;
			}
			// 执行更新操作
			db.execSQL(
					"update blacknumber set number=?, mode=? where number=?",
					new Object[] { newnumber, mode, oldnumber });
			db.close();
		}
	}

	/**
	 * 查找全部的黑名单号码
	 * 
	 * @return
	 */
	public List<BlackNumber> findAll() {
		//定义好要返回的对象
		List<BlackNumber> numbers = new ArrayList<BlackNumber>();
		SQLiteDatabase db = helper.getReadableDatabase();
		if (db.isOpen()) {
			//查询blacknumber表中的所有号码
			Cursor cursor = db.rawQuery("select number,mode from blacknumber",
					null);
			//循环遍历结果集，将每个结果集封装后添加到集合中
			while (cursor.moveToNext()) {
				BlackNumber blackNumber = new BlackNumber();
				blackNumber.setNumber(cursor.getString(0));
				blackNumber.setMode(cursor.getInt(1));
				numbers.add(blackNumber);
				blackNumber = null;
			}
			cursor.close();
			db.close();
		}
		return numbers;
	}
}
