package com.libowen.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.service.CallFirewallService;
import com.libowen.assistant.service.ShowCallLocationService;
import com.libowen.assistant.service.WatchDogService1;
import com.libowen.assistant.utils.ServiceStatusUtil;

public class SettingCenterActivity extends Activity implements OnClickListener {
	// 程序的自动更新
	private SharedPreferences sp;// 用于存储自动更新是否开启的boolean值
	// 归属地显示控件的声明
	private TextView tv_setting_show_location_status;// 显示来显归属地是否开启的状态
	private CheckBox cb_setting_show_location;// 是否开启来电归属地的Checkbox
	private RelativeLayout rl_setting_show_location;// “来电归属地是否开启”控件的父控件
	private Intent showLocationIntent;// 开启来电归属地信息显示的意图
	// 归属地显示背景控件的声明
	private RelativeLayout rl_setting_change_bg;// “来电归属地风格设置”控件的父控件
	private TextView tv_setting_show_bg;// “来电归属地风格设置”下用于显示当前的风格文字
	// 归属地提示框的位置
	private RelativeLayout rl_setting_change_location;// “归属地提示框的位置”条目
	// 来电黑名单控件的声明
	private TextView tv_setting_call_firewall_status;// 来电黑名单拦截是否开启对应的TextView控件的显示文字
	private CheckBox cb_setting_call_firewall;// 显示来电黑名单拦截否开启的勾选框
	private RelativeLayout rl_setting_call_firewall;// “来电黑名单设置”控件的父控件
	private Intent callFirewallIntent;// 开启来电黑名单拦截的服务意图
	// 程序锁控件的声明
	private TextView tv_setting_app_lock_status;
	private CheckBox cb_setting_applock;
	private RelativeLayout rl_setting_app_lock;
	private Intent watchDogIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.setting_center);
		super.onCreate(savedInstanceState);
		// 获取Sdcard下的config.xml文件，如果该文件不存在，那么将会自动创建该文件，文件的操作类型为私有类型
		sp = getSharedPreferences("config", MODE_PRIVATE);
		// 显示归属地信息的ui初始化
		tv_setting_show_location_status = (TextView) findViewById(R.id.tv_setting_show_location_status);
		cb_setting_show_location = (CheckBox) findViewById(R.id.cb_setting_show_location);
		rl_setting_show_location = (RelativeLayout) findViewById(R.id.rl_setting_show_location);
		showLocationIntent = new Intent(this, ShowCallLocationService.class);

		rl_setting_show_location.setOnClickListener(this);
		// 归属地显示背景的声明
		rl_setting_change_bg = (RelativeLayout) findViewById(R.id.rl_setting_change_bg);
		tv_setting_show_bg = (TextView) findViewById(R.id.tv_setting_show_bg);

		rl_setting_change_bg.setOnClickListener(this);
		// 归属地提示框的位置
		rl_setting_change_location = (RelativeLayout) findViewById(R.id.rl_setting_change_location);
		rl_setting_change_location.setOnClickListener(this);
		// 黑名单信息的ui初始化
		tv_setting_call_firewall_status = (TextView) findViewById(R.id.tv_setting_call_firewall_status);
		cb_setting_call_firewall = (CheckBox) findViewById(R.id.cb_setting_call_firewall);
		rl_setting_call_firewall = (RelativeLayout) findViewById(R.id.rl_setting_call_firewall);
		callFirewallIntent = new Intent(this, CallFirewallService.class);

		rl_setting_call_firewall.setOnClickListener(this);
		// 程序锁服务ui的初始化
		tv_setting_app_lock_status = (TextView) findViewById(R.id.tv_setting_applock_status);
		cb_setting_applock = (CheckBox) findViewById(R.id.cb_setting_applock);
		rl_setting_app_lock = (RelativeLayout) findViewById(R.id.rl_setting_applock);
		watchDogIntent = new Intent(this, WatchDogService1.class);

		rl_setting_app_lock.setOnClickListener(this);
	}
	/**
	 * 当界面显示在前台时，立即设置Checkbox的状态
	 */
	@Override
	protected void onResume() {
		if (ServiceStatusUtil.isServiceRunning(this,
				"com.guoshisp.mobilesafe.service.CallFirewallService")) {
			cb_setting_call_firewall.setChecked(true);
			tv_setting_call_firewall_status.setText("来电黑名单拦截已经开启");
		} else {
			cb_setting_call_firewall.setChecked(false);
			tv_setting_call_firewall_status.setText("来电黑名单拦截没有开启");
		}
		if (ServiceStatusUtil.isServiceRunning(this,
				"com.guoshisp.mobilesafe.service.ShowCallLocationService")) {
			cb_setting_show_location.setChecked(true);
			tv_setting_show_location_status.setText("来电归属地显示已经开启");
		} else {
			cb_setting_show_location.setChecked(false);
			tv_setting_show_location_status.setText("来电归属地显示没有开启");
		}

		if (ServiceStatusUtil.isServiceRunning(this,
				"com.guoshisp.mobilesafe.service.WatchDogService1")) {
			cb_setting_applock.setChecked(true);
			tv_setting_app_lock_status.setText("程序锁服务已经开启");
		} else {
			cb_setting_applock.setChecked(false);
			tv_setting_app_lock_status.setText("程序锁服务没有开启");
		}
		super.onResume();
	}

	// 响应点击事件
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.rl_setting_show_location:// 来电归属地是否开启
			if (cb_setting_show_location.isChecked()) {
				tv_setting_show_location_status.setText("来电归属地显示没有开启");
				stopService(showLocationIntent);
				cb_setting_show_location.setChecked(false);
			} else {
				tv_setting_show_location_status.setText("来电归属地显示已经开启");
				startService(showLocationIntent);
				cb_setting_show_location.setChecked(true);
			}
			break;
		case R.id.rl_setting_change_bg:// 来电归属地风格设置
			showChooseBgDialog();
			break;
		case R.id.rl_setting_change_location:// 开启一个新的界面,用于实现View的拖动
			Intent intent = new Intent(this, DragViewActivity.class);
			startActivity(intent);
			break;
		case R.id.rl_setting_call_firewall://来点黑名单拦截

			if (cb_setting_call_firewall.isChecked()) {
				tv_setting_call_firewall_status.setText("来电黑名单拦截没有开启");
				stopService(callFirewallIntent);
				cb_setting_call_firewall.setChecked(false);
			} else {
				tv_setting_call_firewall_status.setText("来电黑名单拦截已经开启");
				startService(callFirewallIntent);
				cb_setting_call_firewall.setChecked(true);
			}
			break;
		case R.id.rl_setting_applock://程序锁

			if (cb_setting_applock.isChecked()) {
				tv_setting_app_lock_status.setText("程序锁服务没有开启");
				stopService(watchDogIntent);
				cb_setting_applock.setChecked(false);
			} else {
				tv_setting_app_lock_status.setText("程序锁服务已经开启");
				startService(watchDogIntent);
				cb_setting_applock.setChecked(true);
			}

			break;
		}
	}

	/**
	 * 更改背景颜色的对话框
	 */

	private void showChooseBgDialog() {
		// 获取一个对话框构造器
		AlertDialog.Builder builder = new Builder(this);
		// 设置对话框标题的图标
		builder.setIcon(R.drawable.notification);
		// 设置对话框的标题
		builder.setTitle("归属地提示框风格");
		// 对话框中item的对应显示文字
		final String[] items = { "半透明", "活力橙", "卫士蓝", "苹果绿", "金属灰" };
		// 用于显示对话框中那一个条目被选中。默认的是第一个条目
		int which = sp.getInt("which", 0);
		// 设置单个选择条目。Item中，只能有一个处于选中状态
		builder.setSingleChoiceItems(items, which,
				new DialogInterface.OnClickListener() {
					// 处理Item的点击事件
					public void onClick(DialogInterface dialog, int which) {
						// 将条目的id存入sp中
						Editor editor = sp.edit();
						editor.putInt("which", which);
						editor.commit();
						// 设置Item的文字信息
						tv_setting_show_bg.setText(items[which]);
						// 关闭对话框
						dialog.dismiss();
					}
				});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

			}
		});
		// 创建并显示出对话框
		builder.create().show();
	}
}
