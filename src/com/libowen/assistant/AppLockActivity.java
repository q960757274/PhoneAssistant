package com.libowen.assistant;

import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.db.dao.AppLockDao;
import com.libowen.assistant.domain.AppInfo;
import com.libowen.assistant.engine.AppInfoProvider;

public class AppLockActivity extends Activity {
	//展示手机中的所有应用
	private ListView lv_applock;
	//ProgressBar和TextView对应的父控件，用于控制ProgressBar和TextView的显示
	private LinearLayout ll_loading;
	//获取手机中已安装的应用程序
	private AppInfoProvider provider;
	//存放当前手机上所有应用程序的信息
	private List<AppInfo> appinfos;
	//操作存放已锁定的应用程序的数据库
	private AppLockDao dao;
	//存放所有已经被锁定的应用程序的包名信息
	private List<String> lockedPacknames;
	//处理子线程中获取到的当前手机中所有应用程序
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			ll_loading.setVisibility(View.INVISIBLE);
			//为ListView适配数据
			lv_applock.setAdapter(new AppLockAdapter());
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.app_lock);
		super.onCreate(savedInstanceState);
		provider = new AppInfoProvider(this);
		lv_applock = (ListView) findViewById(R.id.lv_applock);
		ll_loading = (LinearLayout) findViewById(R.id.ll_applock_loading);
		dao =new AppLockDao(this);
		//从数据库中获取到所有被锁定的应用程序包名
		lockedPacknames = dao.findAll();
		//正在从数据库中获取数据时，应该显示ProgressBar和TextView对应的“正在加载...”字样
		ll_loading.setVisibility(View.VISIBLE);
		//开启一个子线程获取手机中所有应用程序的信息
		new Thread(){
			public void run() {
				appinfos = provider.getInstalledApps();
				//向主线程中发送一个空消息，通知主线程更新数据
				handler.sendEmptyMessage(0);
			};
		}.start();
		//为ListView中的Item设置点击事件的监听器
		lv_applock.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//获取当前Item的对象
				AppInfo appinfo = (AppInfo) lv_applock.getItemAtPosition(position);
				//获取到当前Item对象的包名信息
				String packname = appinfo.getPackname();
				//查找到Item对应的锁控件（ImageView）
				ImageView iv = (ImageView) view.findViewById(R.id.iv_applock_status);
				//设置一个左右移动的动画
				TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.2f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
				//设置动画播放的时长（毫秒）
				ta.setDuration(200);
				//判断当前的Item是否处于锁定状态，如果是，则应该解锁，否则应该加锁。
				if(lockedPacknames.contains(packname)){//锁定状态
					//dao.delete(packname);
					//采用内容提供者来观察数据库中的数据变化
					Uri uri = Uri.parse("content://com.guoshisp.applock/DELETE");
					getContentResolver().delete(uri, null, new String[]{packname});
					//解锁
					iv.setImageResource(R.drawable.unlock);
					//将当前应用程序的包名从集合（存放已锁定应用程序的包名）中移除，以便界面的刷新
					lockedPacknames.remove(packname);
				}else{//未锁定状态
					//dao.add(packname);
					Uri uri = Uri.parse("content://com.guoshisp.applock/ADD");
					ContentValues values = new ContentValues();
					values.put("packname", packname);
					getContentResolver().insert(uri, values);
					iv.setImageResource(R.drawable.lock);
					lockedPacknames.add(packname);
				}
				//为当前的Item播放动画
				view.startAnimation(ta);
			}
		});
		
	}
	//自定义适配器对象
	private class AppLockAdapter extends BaseAdapter{

		public int getCount() {
			return appinfos.size();
		}

		public Object getItem(int position) {
			return appinfos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			//复用历史缓存的View对象
			if(convertView==null){
				view = View.inflate(getApplicationContext(),R.layout.app_lock_item, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView)view.findViewById(R.id.iv_applock_icon);
				holder.iv_status = (ImageView)view.findViewById(R.id.iv_applock_status);
				holder.tv_name = (TextView)view.findViewById(R.id.tv_applock_appname);
				view.setTag(holder);
			}else{//为View做一个标记，以便复用
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			//获取到当前应用程序对象
			AppInfo appInfo = appinfos.get(position);
			holder.iv_icon.setImageDrawable(appInfo.getAppicon());
			holder.tv_name.setText(appInfo.getAppname());
			//查看被当前的Item是否是被绑定的应用，以此来为Item设置对应的锁（锁定或未锁定）
			if(lockedPacknames.contains(appInfo.getPackname())){
				holder.iv_status.setImageResource(R.drawable.lock);
			}else{
				holder.iv_status.setImageResource(R.drawable.unlock);
			}
			return view;
		}
	}
	//View对应的View对象只会在堆内存中存在一份，所有的Item都公用该View
	public static class ViewHolder{
		ImageView iv_icon;
		ImageView iv_status;
		TextView tv_name;
	}
}
