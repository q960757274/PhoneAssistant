package com.libowen.assistant.engine;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.libowen.assistant.domain.ContactInfo;

public class ContactInfoProvider {
	private Context context;

	public ContactInfoProvider(Context context) {
		this.context = context;
	}

	/**
	 * 返回所有的联系人的信息
	 * 
	 * @return
	 */
	public List<ContactInfo> getContactInfos() {
		List<ContactInfo> infos = new ArrayList<ContactInfo>();//将所有联系人存入该集合
		//获取raw_contacts表所对应的Uri
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		//获取data表所对应的Uri
		Uri datauri = Uri.parse("content://com.android.contacts/data");
		//参数二：所要查询的列，即联系人的id。获取一个查询数据库所返回的结果集
		Cursor cursor = context.getContentResolver().query(uri,
				new String[] { "contact_id" }, null, null, null);
		while (cursor.moveToNext()) {//移动游标
			//因为我们只需要查询一列数据-联系人的id，所以我们传入0
			String id = cursor.getString(0);
			//用于封装每个联系人的具体信息
			ContactInfo info = new ContactInfo();
			//得到id后，我们通过该id来查询data表中的联系人的具体数据（data表中的data1中的数据）。参数二：null，会将所有的列返回回来
			//参数三：选择条件    返回一个在data表中查询后的结果集
			Cursor dataCursor = context.getContentResolver().query(datauri,
					null, "raw_contact_id=?", new String[] { id }, null);
			while (dataCursor.moveToNext()) {
				//dataCursor.getString(dataCursor.getColumnIndex("mimetype"))获取data1列中具体数据的数据类型，这里判断的是联系人的姓名
				if ("vnd.android.cursor.item/name".equals(dataCursor
						.getString(dataCursor.getColumnIndex("mimetype")))) {
					//dataCursor.getString(dataCursor.getColumnIndex("data1"))获取data1列中的联系人的具体数据
					info.setName(dataCursor.getString(dataCursor
							.getColumnIndex("data1")));
				} else if ("vnd.android.cursor.item/phone_v2".equals(dataCursor
						.getString(dataCursor.getColumnIndex("mimetype")))) {//数据类型是否是手机号码
					info.setPhone(dataCursor.getString(dataCursor
							.getColumnIndex("data1")));
				}

			}
			//每查询一个联系人后就将其添加到集合中
			infos.add(info);
			info = null;
			dataCursor.close();//关闭结果集

		}
		cursor.close();
		return infos;
	}
}
