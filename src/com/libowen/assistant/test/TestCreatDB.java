package com.libowen.assistant.test;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.libowen.assistant.db.BlackNumberDBOpenHelper;

public class TestCreatDB extends AndroidTestCase {
	public void testCreateDB() throws Exception {
		//测试数据库blacknumber.db是否会被创建.
		BlackNumberDBOpenHelper helper = new BlackNumberDBOpenHelper(
				getContext());
		SQLiteDatabase db = helper.getWritableDatabase();
	}
}
