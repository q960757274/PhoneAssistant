package com.libowen.assistant;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.guoshisp.mobilesafe.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CleanCacheActivity extends Activity {
	// 显示扫描的进度
	private ProgressBar pd;
	// 提示扫描的状态
	private TextView tv_clean_cache_status;
	// 系统的包管理器
	private PackageManager pm;
	// 存储带有缓存的应用的名称
	private List<String> cachePagenames;
	// 显示所有带有缓存的应用程序信息
	private LinearLayout ll_clean;
	// 存放缓存信息
	private Map<String, Long> cacheinfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ceanl_ce);
		pd = (ProgressBar) findViewById(R.id.progressBar1);
		ll_clean = (LinearLayout) findViewById(R.id.ll_clean_cache_cont);
		tv_clean_cache_status = (TextView) findViewById(R.id.tv_clean_cache_status);
		pm = getPackageManager();
		scanPackages();
	}

	// 扫描出带有缓存的应用程序
	private void scanPackages() {
		// 开启一个异步任务扫描带有缓存的应用程序
		new AsyncTask<Void, Integer, Void>() {
			// 存储手机中所有已安装的应用程序的包信息
			List<PackageInfo> packinfos;

			@Override
			protected Void doInBackground(Void... params) {
				int i = 0;
				for (PackageInfo info : packinfos) {
					// 获取到应用程序的包名信息
					String packname = info.packageName;
					getSize(pm, packname);
					i++;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					publishProgress(i);
				}
				return null;
			}

			@Override
			protected void onPreExecute() {
				cachePagenames = new ArrayList<String>();
				cacheinfo = new HashMap<String, Long>();
				packinfos = pm.getInstalledPackages(0);
				pd.setMax(packinfos.size());
				tv_clean_cache_status.setText("开始扫描...");

				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(Void result) {

				tv_clean_cache_status.setText("扫描完毕..." + "发现有"
						+ cachePagenames.size() + "个缓存信息");
				for (final String packname : cachePagenames) {
					// 获取这些应用程序的图标，名称，展现在界面上。
					View child = View.inflate(getApplicationContext(),
							R.layout.cache_item, null);
					// 为child注册一个监听器。
					child.setOnClickListener(new OnClickListener() {
						// 点击child时响应的点击事件
						@Override
						public void onClick(View v) {
							// 判断SDK的版本号
							if (Build.VERSION.SDK_INT >= 9) {
								// 跳转至“清理缓存”的界面（可以通过：设置-->应用程序-->点击任意应用程序后的界面）
								Intent intent = new Intent();
								intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
								intent.addCategory(Intent.CATEGORY_DEFAULT);
								intent.setData(Uri.parse("package:" + packname));
								startActivity(intent);
							} else {
								Intent intent = new Intent();
								intent.setAction("android.intent.action.VIEW");
								intent.addCategory(Intent.CATEGORY_DEFAULT);
								intent.addCategory("android.intent.category.VOICE_LAUNCH");
								intent.putExtra("pkg", packname);
								startActivity(intent);
							}
						}
					});
					// 为child中的控件设置数据
					ImageView iv_icon = (ImageView) child
							.findViewById(R.id.iv_cache_icon);
					iv_icon.setImageDrawable(getApplicationIcon(packname));
					TextView tv_name = (TextView) child
							.findViewById(R.id.tv_cache_name);
					tv_name.setText(getApplicationName(packname));
					TextView tv_size = (TextView) child
							.findViewById(R.id.tv_cache_size);
					tv_size.setText("缓存大小 :"
							+ Formatter.formatFileSize(getApplicationContext(),
									cacheinfo.get(packname)));
					// 将child添加到ll_clean控件上。
					ll_clean.addView(child);
				}
				super.onPostExecute(result);
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				pd.setProgress(values[0]);
				tv_clean_cache_status.setText("正在扫描" + values[0] + "条目");
				super.onProgressUpdate(values);
			}
		}.execute();

	}

	// 通过反射的方式调用packageManager中的方法
	private void getSize(PackageManager pm, String packname) {

		try {
			// 获取到getPackageSizeInfo。调用getPackageSizeInfo方法需要在清单文件中配置权限信息：<uses-permission
			// android:name="android.permission.GET_PACKAGE_SIZE"/>
			Method method = pm.getClass().getDeclaredMethod(
					"getPackageSizeInfo",
					new Class[] { String.class, IPackageStatsObserver.class });
			// 执行getPackageSizeInfo方法
			method.invoke(pm,
					new Object[] { packname, new MyObersver(packname) });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 执行packageManager中的getPackageSizeInfo方法时需要传入IPackageStatsObserver.Stub接口，该接口通过aidl调用。
	private class MyObersver extends IPackageStatsObserver.Stub {
		private String packname;

		public MyObersver(String packname) {
			this.packname = packname;
		}

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			// 以下是根据ApplicationsState代码中的SizeInfo对象中定义的
			// 缓存大小
			long cacheSize = pStats.cacheSize;
			// 代码大小
			long codeSize = pStats.codeSize;
			// 数据的大小
			long dataSize = pStats.dataSize;
			// 判断这个包名对应的应用程序是否有缓存，如果有，则存入到集合中。
			if (cacheSize > 0) {
				cachePagenames.add(packname);
				cacheinfo.put(packname, cacheSize);
			}
		}
	}

	// 获取到应用程序的名称
	private String getApplicationName(String packname) {
		try {
			PackageInfo packinfo = pm.getPackageInfo(packname, 0);
			return packinfo.applicationInfo.loadLabel(pm).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return packname;
		}
	}

	// 获取到应用程序的图标
	private Drawable getApplicationIcon(String packname) {
		try {
			PackageInfo packinfo = pm.getPackageInfo(packname, 0);
			return packinfo.applicationInfo.loadIcon(pm);

		} catch (Exception e) {
			e.printStackTrace();
			return getResources().getDrawable(R.drawable.ic_launcher);
		}
	}
}
