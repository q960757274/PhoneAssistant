package com.libowen.assistant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.domain.AppInfo;
import com.libowen.assistant.engine.AppInfoProvider;
import com.libowen.assistant.utils.DensityUtil;

public class AppManagerActivity extends Activity implements OnClickListener{
	protected static final int LOAD_APP_FINSISH = 50;
	private static final String TAG = "AppManagerActivity";
	private TextView tv_appmanager_mem_avail;//显示手机可用内存
	private TextView tv_appmanager_sd_avail;//显示Sdcard可用内存
	private ListView lv_appmanager;//展示用户程序、系统程序
	private LinearLayout ll_loading;//ProgressBar的父控件，用于控制该控件中的子控件的显示
	private PackageManager pm; // 相当于windows系统下面的程序管理器（可以获取手机中所有的应用程序）
	private List<AppInfo> appinfos;//存放手机中所有的应用程序（用户程序+系统程序）
	private List<AppInfo> userappInfos;//存放用户程序
	private List<AppInfo> systemappInfos;//存放系统程序
	//PopupWindow中contentView对应的三个控件
	private LinearLayout ll_uninstall;//卸载
	private LinearLayout ll_start;//启动
	private LinearLayout ll_share;//分享

	private PopupWindow popupWindow;

	private String clickedpackname;
	//当应用程序在子线程中全部加载成功后，通知主线程显示数据
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_APP_FINSISH:
				ll_loading.setVisibility(View.INVISIBLE);
				lv_appmanager.setAdapter(new AppManagerAdapter());
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.app_manager);
		super.onCreate(savedInstanceState);
		tv_appmanager_mem_avail = (TextView) findViewById(R.id.tv_appmanager_mem_avail);
		tv_appmanager_sd_avail = (TextView) findViewById(R.id.tv_appmanager_sd_avail);
		ll_loading = (LinearLayout) findViewById(R.id.ll_appmanager_loading);
		lv_appmanager = (ListView) findViewById(R.id.lv_appmanager);
		tv_appmanager_sd_avail.setText("SD卡可用" + getAvailSDSize());
		tv_appmanager_mem_avail.setText("内存可用:" + getAvailROMSize());

		pm = getPackageManager();
		//加载所有应用程序的数据
		fillData();
		//为ListView设置点击事件
		lv_appmanager.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//当用户点击下一个Item时，需要关闭已经存在的PopupWindow
				dismissPopupWindow();
				//将布局文件转成view，该view用于显示PopupWindow中的内容
				View contentView = View.inflate(getApplicationContext(),
						R.layout.popup_item, null);
				//分别获取到PopupWindow窗体中的"卸载、启动、分享"控件所对应的父控件
				ll_uninstall = (LinearLayout) contentView
						.findViewById(R.id.ll_popup_uninstall);
				ll_start = (LinearLayout) contentView
						.findViewById(R.id.ll_popup_start);
				ll_share = (LinearLayout) contentView
						.findViewById(R.id.ll_popup_share);
				//为"卸载、启动、分享"设置点击事件
				ll_share.setOnClickListener(AppManagerActivity.this);
				ll_start.setOnClickListener(AppManagerActivity.this);
				ll_uninstall.setOnClickListener(AppManagerActivity.this);
				//获取用于显示PopupWindow内容的View的根布局，这里是要为该布局设置动画（相当于为PopupWindow设置动画）
				LinearLayout ll_popup_container = (LinearLayout) contentView
						.findViewById(R.id.ll_popup_container);
				//设置一个缩放的动画效果
				ScaleAnimation sa = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f);
				//设置动画执行的时间
				sa.setDuration(300);
				//获取到当前Item的对象
				Object obj = lv_appmanager.getItemAtPosition(position);
				//当Item为系统应用时，此时为PopupWindow中的"卸载"设置一个标记，在卸载时判断该标记，禁止卸载系统应用
				if (obj instanceof AppInfo) {
					AppInfo appinfo = (AppInfo) obj;
					clickedpackname = appinfo.getPackname();
					if (appinfo.isUserapp()) {
						ll_uninstall.setTag(true);
					} else {
						ll_uninstall.setTag(false);
					}
				} else {
					return;
				}
				//获取到当前Item离顶部、底部的距离
				int top = view.getTop();
				int bottom = view.getBottom();            
				//创建PopupWindow窗体时必须要指定窗体的大小，否则不会显示在界面上。参数一：窗体中用于显示内容的viewContent，参数二、三：表示PopupWindow窗体的宽和高
				popupWindow = new PopupWindow(contentView, DensityUtil.dip2px(getApplicationContext(), 200), bottom - top
						+ DensityUtil.dip2px(getApplicationContext(), 20));
				// 注意:一定要给popwindow设置背景图片或背景资源,如果不设置背景资源 , 动画、 焦点的处理 都会产生问题。
				popupWindow.setBackgroundDrawable(new ColorDrawable(
						Color.TRANSPARENT));
				//获取到Item在窗体中显示的位置
				int[] location = new int[2];
				view.getLocationInWindow(location);
				//参数一：PopupWindow挂载在那个View上，参数二：设置PopupWindow显示的重心位置
				//参数三：PopupWindow在View上X轴的偏移量，参数四：PopupWindow在View上Y轴的偏移量。X、Y轴的偏移量是相对于当前Activity所在的窗体，参照点为（0，0）
				popupWindow.showAtLocation(view, Gravity.TOP | Gravity.LEFT,
						location[0] + 20, location[1]);
				// 播放一个缩放的动画.
				ll_popup_container.startAnimation(sa);
			}
		});

		/**
		 * 当用户滑动窗体的时候,需要关闭已经存在的PopupWindow
		 */
		lv_appmanager.setOnScrollListener(new OnScrollListener() {

			// 当listview的滚动状态发生改变的时候 调用的方法.
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			// 当listview处于滚动状态的时候 调用的方法
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				   dismissPopupWindow();
			}
		});

	}

	/**	
	 * 将手机中的应用程序全部获取出来
	 */
	private void fillData() {
		//加载数据时，ll_loading控件中的ProgressBar以及TextView对应的“正在加载数据...”显示出来
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				AppInfoProvider provider = new AppInfoProvider(
						AppManagerActivity.this);
				appinfos = provider.getInstalledApps();
				initAppInfo();
				//向主线程发送消息
				Message msg = Message.obtain();
				msg.what = LOAD_APP_FINSISH;
				handler.sendMessage(msg);
			};

		}.start();
	}

	/**
	 * 初始化系统和用户appinfos的集合
	 */
	protected void initAppInfo() {
		systemappInfos = new ArrayList<AppInfo>();
		userappInfos = new ArrayList<AppInfo>();
		for (AppInfo appinfo : appinfos) {
			//区分出用户程序和系统程序
			if (appinfo.isUserapp()) {
				userappInfos.add(appinfo);
			} else {
				systemappInfos.add(appinfo);
			}
		}
	}
	/**
	 * 当Activity销毁时，需要关闭PopupWindow，因为PopupWindow挂载有TextView，
	 * 如果不关闭该窗体的话，并不影响程序的执行，但Log中会出现"AppManagerActivity has leaked window"的红色错误提示。
	 */
	@Override
	protected void onDestroy() {
		dismissPopupWindow();
		super.onDestroy();
	}

	//适配器对象
	private class AppManagerAdapter extends BaseAdapter {
		//获取ListView中Item的数据
		public int getCount() {
			// 因为listview要多显示两个条目（用户程序和系统程序）
			return userappInfos.size() + 1 + systemappInfos.size() + 1;
		}
		//获取到Item所对应的对象
		public Object getItem(int position) {
			//当position == 0则对应的是“用户程序”条目
			if (position == 0) {
				return position;
			} else if (position <= userappInfos.size()) {//当position <= userappInfos.size()则对应的是手机中所有用户程序条目
				// 要显示的用户程序的条目在集合中的位置（因为用户程序对应的Item是从1开始的，而集合中的角标是从0开始的）
				int newpostion = position - 1;
				return userappInfos.get(newpostion);
			} else if (position == (userappInfos.size() + 1)) {//当position == (userappInfos.size() + 1)则对应的是“系统程序”条目
				return position;
			} else {//所有系统应用条目
				int newpostion = position - userappInfos.size() - 2;
				return systemappInfos.get(newpostion);
			}
		}
		//获取Item所对应的id
		public long getItemId(int position) {
			return position;
		}
		//将View显示在Item上，每显示一个Item，调用一次该方法
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == 0) {//当position == 0则对应的是“用户程序”条目，我们创建出该条目对应的View
				TextView tv = new TextView(getApplicationContext());
				tv.setTextSize(20);
				tv.setText("用户程序 (" + userappInfos.size() + ")");
				return tv;
			} else if (position <= userappInfos.size()) {//当position <= userappInfos.size()则对应的是手机中所有用户程序条目
				// 要显示的用户程序的条目在集合中的位置（因为用户程序对应的Item是从1开始的，而集合中的角标是从0开始的）
				int newpostion = position - 1;
				View view;
				ViewHolder holder;
				//复用历史缓存
				if (convertView == null || convertView instanceof TextView) {
					view = View.inflate(getApplicationContext(),
							R.layout.app_manager_item, null);
					holder = new ViewHolder();
					holder.iv_icon = (ImageView) view
							.findViewById(R.id.iv_appmanger_icon);
					holder.tv_name = (TextView) view
							.findViewById(R.id.tv_appmanager_appname);
					holder.tv_version = (TextView) view
							.findViewById(R.id.tv_appmanager_appversion);
					view.setTag(holder);
				} else {
					view = convertView;
					holder = (ViewHolder) view.getTag();
				}
				//为用户应用程序适配数据
				AppInfo appInfo = userappInfos.get(newpostion); // 从用户程序集合里面获取数据的条目
				holder.iv_icon.setImageDrawable(appInfo.getAppicon());
				holder.tv_name.setText(appInfo.getAppname());
				holder.tv_version.setText("版本号:" + appInfo.getVersion());
				return view;

			} else if (position == (userappInfos.size() + 1)) {//当position == (userappInfos.size() + 1)则对应的是“系统程序”条目
				TextView tv = new TextView(getApplicationContext());
				tv.setTextSize(20);
				tv.setText("系统程序 (" + systemappInfos.size() + ")");
				return tv;
			} else {//所有系统应用的Item
				int newpostion = position - userappInfos.size() - 2;
				View view;
				ViewHolder holder;
				if (convertView == null || convertView instanceof TextView) {
					view = View.inflate(getApplicationContext(),
							R.layout.app_manager_item, null);
					holder = new ViewHolder();
					holder.iv_icon = (ImageView) view
							.findViewById(R.id.iv_appmanger_icon);
					holder.tv_name = (TextView) view
							.findViewById(R.id.tv_appmanager_appname);
					holder.tv_version = (TextView) view
							.findViewById(R.id.tv_appmanager_appversion);
					view.setTag(holder);
				} else {
					view = convertView;
					holder = (ViewHolder) view.getTag();
				}
				//为系统应用程序适配数据
				AppInfo appInfo = systemappInfos.get(newpostion); // 从系统程序集合里面获取数据的条目
				holder.iv_icon.setImageDrawable(appInfo.getAppicon());
				holder.tv_name.setText(appInfo.getAppname());
				holder.tv_version.setText("版本号:" + appInfo.getVersion());
				return view;

			}
		}

		/**
		 * 屏蔽掉两个TextView（用户程序和系统程序）被点击时的焦点
		 */
		@Override
		public boolean isEnabled(int position) {
			if (position == 0 || position == (userappInfos.size() + 1)) {
				return false;
			}
			return super.isEnabled(position);
		}
	}
	//将Item中的控件使用static修饰，被static修饰的类的字节码在JVM中只会存在一份。iv_icon，tv_name与tv_version在栈中也会只存在一份
	private static class ViewHolder {
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_version;
	}

	/**
	 * 获取sd卡可用的内存大小
	 * 
	 * @return
	 */
	private String getAvailSDSize() {
		//获取Sdcard根目录所在的文件对象
		File path = Environment.getExternalStorageDirectory();
		//状态空间对象
		StatFs stat = new StatFs(path.getPath());
		// 获取Sdcard卡中有多少块分区（整个Sdcard的空间被分为多块）
		long totalBlocks = stat.getBlockCount();
		// 获取Sdcard卡可用的分区数量
		long availableBlocks = stat.getAvailableBlocks();
		// 获取Sdcard卡每一块分区可以存放的byte数量
		long blockSize = stat.getBlockSize();
		//计算中总的byte
		long availSDsize = availableBlocks * blockSize;
		//借助Formatter来将其转换为M
		return Formatter.formatFileSize(this, availSDsize);
	}

	/**
	 * 获取手机剩余可用的内存空间
	 * 
	 * @return
	 */
	private String getAvailROMSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return Formatter.formatFileSize(this, availableBlocks * blockSize);
	}
	/**
	 * 当用户在界面上点击下一个Item时，要关闭上一个PopupWindow
	 */
	private void dismissPopupWindow() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}
	/**
	 * PopupWindow中的点击事件
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_popup_share:
			Log.i(TAG, "分享");
			shareApplication();
			break;

		case R.id.ll_popup_start:
			Log.i(TAG, "开启");
			startAppliction();

			break;
		case R.id.ll_popup_uninstall:
			//获取到Item为“ll_popup_uninstall”设置的标记
			boolean result = (Boolean) v.getTag();
			//禁止卸载系统应用
			if (result) {
				Log.i(TAG, "卸载" + clickedpackname);
				uninstallApplication();
			}else{
				Toast.makeText(this, "系统应用不能被卸载", 1).show();
			}
			break;
		}

	}

	/**
	 * 分享一个应用程序
	 */
	private void shareApplication() {
        /*<intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
        </intent-filter>*/
		Intent intent = new Intent();
		//通过意图的的动作、类型来激活手机中具有分享功能的应用（短信，互联网...），这写具有分享功能的应用会以列表的格式展现出来
		intent.setAction("android.intent.action.SEND");
		intent.addCategory("android.intent.category.DEFAULT");
		//输入的内容为文本类型
		intent.setType("text/plain");
		//设置分享的标题
		intent.putExtra("subject", "分享的标题");
		//设置分享的默认内容
		intent.putExtra("sms_body", "推荐你使用一款软件"+clickedpackname);
		intent.putExtra(Intent.EXTRA_TEXT, "extra_text");
		startActivity(intent);
	}

	/**
	 * 卸载一个应用程序
	 */
	private void uninstallApplication() {
		
		/* * <intent-filter> <action android:name="android.intent.action.VIEW" />
		 * <action android:name="android.intent.action.DELETE" /> <category
		 * android:name="android.intent.category.DEFAULT" /> <data
		 * android:scheme="package" /> </intent-filter>*/
		 
		dismissPopupWindow();
		Intent intent = new Intent();
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + clickedpackname));
		//卸载一个应用程序后，对应的Sdcard或内存会发生变化，此时我们应当更新该信息。并且需要将卸载的应用从列表中移除
		startActivityForResult(intent, 1);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			// 通知界面更新数据.
			fillData();
			tv_appmanager_sd_avail.setText("SD卡可用" + getAvailSDSize());
			tv_appmanager_mem_avail.setText("内存可用:" + getAvailROMSize());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 开启一个应用程序
	 */
	private void startAppliction() {
		dismissPopupWindow();
		Intent intent = new Intent();
		PackageInfo packinfo;
		try {
			//PackageManager.GET_ACTIVITIES告诉包管理者，在解析清单文件时，只解析Activity对应的节点
			packinfo = pm.getPackageInfo(clickedpackname,
					PackageManager.GET_ACTIVITIES);

			ActivityInfo[] activityinfos = packinfo.activities;
			//判断清单文件中是否存在Activity对应的节点
			if (activityinfos != null && activityinfos.length > 0) {
				//启动清单文件中的第一个Activity节点
				String className = activityinfos[0].name;
				intent.setClassName(clickedpackname, className);
				startActivity(intent);
			} else {
				Toast.makeText(this, "不能启动当前应用", 0).show();
			}
		} catch (NameNotFoundException e) {//使用C语言实现的应用程序，在DDMS中没有对应的包名
			e.printStackTrace();
			Toast.makeText(this, "不能启动当前应用", 0).show();
		}
	}
	/**
	 * 获取具有启动属性的intent 系统桌面应用(luncher)
	 */
	public Intent getIntent() {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		List<ResolveInfo> resoveInfo = pm.queryIntentActivities(intent,
				PackageManager.GET_INTENT_FILTERS
						| PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo info : resoveInfo) {
			// info.activityInfo.packageName;
		}
		return null;
	}
}
