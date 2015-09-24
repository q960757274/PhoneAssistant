package com.libowen.assistant.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.libowen.assistant.db.dao.AppLockDao;
/**
 * 在内容提供者中，我们只需要关心对数据库的增、删操作。
 * @author Administrator
 *
 */
public class AppLockDBProvider extends ContentProvider {
	private static final int ADD = 1;
	// content://com.guoshisp.applock/ADD
	// content://com.guoshisp.applock/DELETE
	//获取操作数据库的对象
	private AppLockDao dao;
	//定义匹配码
	private static final int DELETE = 2;
	public static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	//定义匹配路径
	static {
		//匹配Uri。参数一：主机名，参数二：指定数据库中的表名或者一些业务逻辑（add、delete等）
		//参数三：匹配码。也即执行匹配时判断匹配是否正确：matcher.match(uri)
		matcher.addURI("com.guoshisp.applock", "ADD", ADD);
		matcher.addURI("com.guoshisp.applock", "DELETE", DELETE);
	}
	@Override
	public boolean onCreate() {
		dao = new AppLockDao(getContext());
		return false;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//匹配URI
		int result = matcher.match(uri);
		//判断是否是添加的匹配操作
		if (result == ADD) {
			//获取到添加的包名（ContentValues是在Item被点击是添加的）
			String packname = values.getAsString("packname");
			//添加到数据库中
			dao.add(packname);
			//发布内容的变化通知
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return null;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int result = matcher.match(uri);
		if (result == DELETE) {
			dao.delete(selectionArgs[0]);
			//发布内容的变化通知
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}
