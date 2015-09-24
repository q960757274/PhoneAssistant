package com.libowen.assistant.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 号码归属地查询-操作数据库
 * @author
 */
public class NumberAddressDao {

	/**
	 * 获取电话号码的归属地
	 * @param number
	 * @return
	 */
	public static String getAddress(String number) {
		// 如果没有查询到号码的归属地 ，就返回当前的电话号码
		String address = number;
		//数据库在手机系统中的全路径
		String path = "/data/data/com.guoshisp.mobilesafe/files/address.db";
		//打开数据库。参数二：CursorFactory游标工厂，null表示使用系统默认的
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		//判断数据库是否被打开
		if (db.isOpen()) {
			// 判断号码的类型.
			if (number.matches("^1[3458]\\d{9}$")) {// 手机号码 ^代表开始   开始为1  第二位可以是3458  \d表示0-9的一个数字 \是用于转意“\” ｛9｝表示有9个数字，这里表示的是有9个0-9的数字
				//返回一个查询的结果集
				Cursor cursor = db
						.rawQuery(
								"select city from address_tb where _id=(select outkey from numinfo where mobileprefix =?)",
								new String[] { number.substring(0, 7) });//匹配手机的前七位
				if (cursor.moveToFirst()) {
					address = cursor.getString(0);//获取第0列即可
				}
				cursor.close();

			} else {// 其他号码 (固定电话)  
				Cursor cursor;
				switch (number.length()) {//号码的长度
				case 4:
					address = "模拟器";
					break;
				case 7://本地号码不显示区号
					address = "本地号码";
					break;
				case 8:
					address = "本地号码";
					break;
				case 10:
					//从查询返回结果中获取第一条数据  （limit表示只获取第一条数据）
					cursor = db
							.rawQuery(
									"select city from address_tb where area = ? limit 1",
									new String[] { number.substring(0, 3) });
					if (cursor.moveToFirst()) {
						address = cursor.getString(0);
					}
					cursor.close();
					break;
				case 12://4位的区号+8位号码
					cursor = db
							.rawQuery(
									"select city from address_tb where area = ? limit 1",
									new String[] { number.substring(0, 4) });
					if (cursor.moveToFirst()) {
						address = cursor.getString(0);
					}
					cursor.close();
					break;
				case 11://3位区号+8位的号码，或者是4位的区号+7位号码
					cursor = db
							.rawQuery(
									"select city from address_tb where area = ? limit 1",
									new String[] { number.substring(0, 3) });
					if (cursor.moveToFirst()) {
						address = cursor.getString(0);
					}
					cursor.close();
					cursor = db
							.rawQuery(
									"select city from address_tb where area = ? limit 1",
									new String[] { number.substring(0, 4) });
					if (cursor.moveToFirst()) {
						address = cursor.getString(0);
					}
					cursor.close();
					break;
				}
			}

			db.close();
		}

		return address;
	}
}
