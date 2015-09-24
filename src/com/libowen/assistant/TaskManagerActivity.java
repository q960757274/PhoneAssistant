package com.libowen.assistant;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.domain.ProcessInfo;
import com.libowen.assistant.engine.ProcessInfoProvider;
import com.libowen.assistant.view.MyToast;

public class TaskManagerActivity extends Activity implements OnClickListener {
	// 显示用户进程
	private ListView lv1;
	// 显示系统进程
	private ListView lv2;
	// 判断当前显示的列表是用户进程还是系统进程
	private boolean showUserApp;
	// 切换用户进程和系统进程的按钮（用于响应“全选”与“一键清理”按钮时：判断是用户进程，还是系统进程）
	private Button bt_user, bt_system;
	// 用户进程所在列表的适配器
	private UserAdapter useradapter;
	// 系统进程所在列表的适配器
	private SystemAdapter systemadapter;
	// 用于获取手机中的进程
	private ProcessInfoProvider provider;
	// 为系统进程添加的一个Item，该Item上显示“杀死系统进程会导致系统不稳定”文字。
	private TextView tvheader;
	// 存放用户进程的集合
	private List<ProcessInfo> userProcessInfos;
	// 存放系统进程的集合
	private List<ProcessInfo> systemProcessInfos;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_manager);
		// 默认情况下，显示的是用户进程列表
		showUserApp = true;
		provider = new ProcessInfoProvider(this);
		userProcessInfos = new ArrayList<ProcessInfo>();
		systemProcessInfos = new ArrayList<ProcessInfo>();
		// 通过provider来获取手机中的所有进程
		List<ProcessInfo> mRunningProcessInfos = provider.getProcessInfos();
		// 将获取到的所有进程进行分类存储（用户进程和系统进程）
		for (ProcessInfo info : mRunningProcessInfos) {
			if (info.isUserprocess()) {
				userProcessInfos.add(info);
			} else {
				systemProcessInfos.add(info);
			}
		}
		// 用户进程对应的ListView及设置ListView的点击事件
		lv1 = (ListView) findViewById(R.id.lv_usertask);
		lv1.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CheckBox cb = (CheckBox) view.findViewById(R.id.cb_taskmanager);
				// 获取到被点击的Item对象
				ProcessInfo info = (ProcessInfo) lv1
						.getItemAtPosition(position);
				// 判断被点击的Item是否是我们自己的手机安全卫士条目（我们不允许杀死自己的应用程序）
				if (info.getPackname().equals(getPackageName())) {
					return;
				}
				// 手动的设置Checkbox的状态
				if (info.isChecked()) {
					info.setChecked(false);
					cb.setChecked(false);
				} else {
					info.setChecked(true);
					cb.setChecked(true);
				}

			}
		});
		// 系统进程对应的ListView及设置ListView的点击事件
		lv2 = (ListView) findViewById(R.id.lv_systemtask);
		lv2.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 禁止 响应系统进程中的第一个Item的点击事件
				if (view instanceof TextView) {
					return;
				}
				CheckBox cb = (CheckBox) view.findViewById(R.id.cb_taskmanager);
				// 获取到被点击的Item对象
				ProcessInfo info = (ProcessInfo) lv2
						.getItemAtPosition(position);
				// 手动的设置Checkbox的状态
				if (info.isChecked()) {
					info.setChecked(false);
					cb.setChecked(false);
				} else {
					info.setChecked(true);
					cb.setChecked(true);
				}
			}
		});
		// 为“用户进程”按钮注册一个监听器
		bt_user = (Button) findViewById(R.id.bt_user);
		bt_user.setOnClickListener(this);
		bt_user.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.bt_pressed));
		// 为“系统进程”按钮注册一个监听器
		bt_system = (Button) findViewById(R.id.bt_system);
		bt_system.setOnClickListener(this);
		bt_system.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.bg_normal));
		// 默认情况下显示的是用户进程列表，所以我们应当将系统进程列表设置为不可见。View.GONE：View无效，且不占用空间
		lv2.setVisibility(View.GONE);
		// 为用户进程列表设置数据适配器
		useradapter = new UserAdapter();
		lv1.setAdapter(useradapter);
		// 将该TextView做为系统进程对应的ListView的第一个Item
		tvheader = new TextView(getApplicationContext());
		tvheader.setText("杀死系统进程会导致系统不稳定");
		tvheader.setBackgroundColor(Color.YELLOW);
		// 将tvheader添加到系统进程对应的ListView中，此时，tvheader属于该ListView的一员（第一个Item即是）。必须在适配数据前添加
		lv2.addHeaderView(tvheader);
		// 为系统进程列表设置数据适配器
		systemadapter = new SystemAdapter();
		lv2.setAdapter(systemadapter);
	}

	// 为用户进程对应的ListView适配数据
	private class UserAdapter extends BaseAdapter {

		public int getCount() {
			return userProcessInfos.size();
		}

		public Object getItem(int position) {
			return userProcessInfos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			// 使用堆内存中的唯一的一份字节码（ListView的优化）
			ViewHolder holder = new ViewHolder();
			// 复用缓存（ListView的优化）
			if (convertView == null) {
				view = View.inflate(getApplicationContext(),
						R.layout.task_manager_item, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view
						.findViewById(R.id.iv_taskmanger_icon);
				holder.tv_name = (TextView) view
						.findViewById(R.id.tv_taskmanager_appname);
				holder.tv_mem = (TextView) view
						.findViewById(R.id.tv_taskmanager_mem);
				holder.cb = (CheckBox) view.findViewById(R.id.cb_taskmanager);
				view.setTag(holder);
			} else {
				// 使用缓存的view
				view = convertView;
				// 获取到缓存的view的标记
				holder = (ViewHolder) view.getTag();
			}
			// 从用户进程对应的集合中取出对应的元素做数据的适配
			ProcessInfo info = userProcessInfos.get(position);
			// 如果应用程序是我们自己的手机卫士，隐藏掉Checkbox（避免杀死自己）
			if (info.getPackname().equals(getPackageName())) {
				holder.cb.setVisibility(View.INVISIBLE);
			} else {
				holder.cb.setVisibility(View.VISIBLE);
			}
			// 为Item适配数据：应用图标、名称、占用内存大小、是否处于选中状态（默认情况下都是未选中状态）
			holder.iv_icon.setImageDrawable(info.getIcon());
			holder.tv_name.setText(info.getAppname());
			holder.tv_mem.setText(Formatter.formatFileSize(
					getApplicationContext(), info.getMemsize()));
			holder.cb.setChecked(info.isChecked());
			// 返回Item对应的view
			return view;
		}
	}

	// 使用static修饰，可以保证该对象在堆内存中只存在一份字节码文件（所有的Item共用该字节码文件）
	static class ViewHolder {
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_mem;
		CheckBox cb;
	}

	// 为系统进程对应的ListView适配数据
	private class SystemAdapter extends BaseAdapter {

		public int getCount() {
			return systemProcessInfos.size();
		}

		public Object getItem(int position) {
			return systemProcessInfos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			View view;
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				view = View.inflate(getApplicationContext(),
						R.layout.task_manager_item, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view
						.findViewById(R.id.iv_taskmanger_icon);
				holder.tv_name = (TextView) view
						.findViewById(R.id.tv_taskmanager_appname);
				holder.tv_mem = (TextView) view
						.findViewById(R.id.tv_taskmanager_mem);
				holder.cb = (CheckBox) view.findViewById(R.id.cb_taskmanager);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			ProcessInfo info = systemProcessInfos.get(position);
			holder.iv_icon.setImageDrawable(info.getIcon());
			holder.tv_name.setText(info.getAppname());
			holder.tv_mem.setText(Formatter.formatFileSize(
					getApplicationContext(), info.getMemsize()));
			holder.cb.setChecked(info.isChecked());

			return view;
		}
	}

	// 响应用户进程、系统进程的按钮的点击事件（完成用户进程列表和系统进程列表的切换）
	public void onClick(View v) {
		switch (v.getId()) {
		// 由系统进程列表切换到用户进程列表（或者当前列表切换到当前列表）
		case R.id.bt_user:
			// 移除系统进程列表中的第一个用于提示的Item
			if (tvheader != null) {
				lv2.removeHeaderView(v);
				tvheader = null;
			}
			// 当前显示的是用户进程
			showUserApp = true;
			// 设置两个按钮的背景色，以示区分
			bt_user.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.bt_pressed));
			bt_system.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.bg_normal));
			// 将lv1设置为可见，lv2设置为不可见
			lv1.setVisibility(View.VISIBLE);
			lv2.setVisibility(View.INVISIBLE);

			break;
		// 由系用户程列表切换到系统进程列表（或者当前列表切换到当前列表）
		case R.id.bt_system:
			showUserApp = false;
			bt_system.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.bt_pressed));
			bt_user.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.bg_normal));
			lv2.setVisibility(View.VISIBLE);
			lv1.setVisibility(View.INVISIBLE);

			break;
		}

	}

	/**
	 * 全选按钮的点击事件
	 */
	public void selectAll(View view) {
		//判断是用户进程全选，还是系统进程全选
		if (showUserApp) {
			//迭代进程集合，将每个info中的Checkbox都设置为true（选中），然后通知适配器刷新数据
			for (ProcessInfo info : userProcessInfos) {
				info.setChecked(true);
				useradapter.notifyDataSetChanged();
			}

		} else {
			//迭代进程集合，将每个info中的Checkbox都设置为true（选中），然后通知适配器刷新数据
			for (ProcessInfo info : systemProcessInfos) {
				info.setChecked(true);
				systemadapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * 一键清理的点击事件
	 */
	public void oneKeyClear(View v) {
		//获取到ActivityManager对象，该对象中有杀死进程的操作
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		//计数要被杀死多少个进程
		int count = 0;
		//计数要被杀死的所有进程所占用的内存大小
		long memsize = 0;
		//存放已被被杀死的进程
		List<ProcessInfo> killedProcessInfo = new ArrayList<ProcessInfo>();
		//判断是清理用户进程还是清理系统进程
		if (showUserApp) {//用户进程
			for (ProcessInfo info : userProcessInfos) {
				//根据Checkbox的勾选状态来确定哪些进程需要被清理
				if (info.isChecked()) {
					//执行累加操作
					count++;
					memsize += info.getMemsize();
					//执行kill操作
					am.killBackgroundProcesses(info.getPackname());
					//将杀死后的进程存入集合中
					killedProcessInfo.add(info);
				}
			}

		} else {//系统进程
			for (ProcessInfo info : systemProcessInfos) {
				if (info.isChecked()) {
					count++;
					memsize += info.getMemsize();
					am.killBackgroundProcesses(info.getPackname());
					killedProcessInfo.add(info);

				}
			}
		}
		//迭代出被杀死的进程，判断哪个集合中包含该进程，如果包含，则移除掉（用于更新列表显示）
		for (ProcessInfo info : killedProcessInfo) {
			if (info.isUserprocess()) {
				if (userProcessInfos.contains(info)) {
					userProcessInfos.remove(info);
				}
			} else {
				if (systemProcessInfos.contains(info)) {
					systemProcessInfos.remove(info);
				}
			}
		}
		//更新数据显示
		if (showUserApp) {
			useradapter.notifyDataSetChanged();
		} else {
			systemadapter.notifyDataSetChanged();
		}

		
		/* Toast.makeText( this, "杀死了" + count + "个进程,释放了" +
		  Formatter.formatFileSize(this, memsize) + "内存", 1) .show();*/
		
		//使用自定义的Toast来显示杀死的进程数，以及释放的内存空间。
		MyToast.showToast(
				this,
				"杀死了" + count + "个进程,释放了"
						+ Formatter.formatFileSize(this, memsize) + "内存");
	}
}