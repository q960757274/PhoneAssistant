package com.libowen.assistant;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.domain.ContactInfo;
import com.libowen.assistant.engine.ContactInfoProvider;

public class SelectContactActivity extends Activity {
	private ListView lv_select_contact;//用于展现联系人的列表
	private ContactInfoProvider provider;//获取手机联系人的对象
	private List<ContactInfo> infos;//接收获取到的所有联系人
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_contact);
		lv_select_contact = (ListView) findViewById(R.id.lv_select_contact);
		provider = new ContactInfoProvider(this);
		infos = provider.getContactInfos();
		//为lv_select_contact设置一个数据适配器，用于将所有联系人展现到界面上
		lv_select_contact.setAdapter(new ContactAdapter());
		//为lv_select_contact中的item设置监听
		lv_select_contact.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//获取到点击item对应的联系人的信息对象
				ContactInfo info = (ContactInfo) lv_select_contact.getItemAtPosition(position);
				//获取到该联系人的号码
				String number = info.getPhone();
				//将该联系人的号码返回给激活当前Activity的Activity
				Intent data = new Intent();
				//将数据存入，用于返回给Activity
				data.putExtra("number", number);
				//返回数据，参数一：返回结果码  参数二：返回数据
				setResult(0, data);
				//关闭当前的activity
				finish();
			}
		});
	}
	/**
	 * 展现所有联系人
	 * @author Administrator
	 *
	 */
	private class ContactAdapter extends BaseAdapter{

		public int getCount() {
			return infos.size();
		}

		public Object getItem(int position) {
			return infos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ContactInfo info = infos.get(position);
			TextView tv = new TextView(getApplicationContext());
			tv.setTextSize(24);
			tv.setTextColor(Color.WHITE);
			tv.setText(info.getName()+"\n"+info.getPhone());
			return tv;
		}
		
	}
}
