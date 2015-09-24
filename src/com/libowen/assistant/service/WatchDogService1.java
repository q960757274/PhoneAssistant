package com.libowen.assistant.service;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.libowen.assistant.EnterPwdActivity;
import com.libowen.assistant.IService;
import com.libowen.assistant.db.dao.AppLockDao;

public class WatchDogService1 extends Service {
	protected static final String TAG = "WatchDogService";
	//是否要停止掉看门狗服务。true表示继续运行，false表示停止运行。
	boolean flag;
	//要进入一个已被锁定的应用程序前，需要输入正确的密码后才可以进入。这是一个用于激活输入密码的界面。
	private Intent pwdintent;
	//将所有已被锁定的应用程序的包名存放在该集合缓存中
	private List<String> lockPacknames;
	//操作数据库的对象
	private AppLockDao dao;
	//存放临时需要被保护的应用程序包名
	private List<String> tempStopProtectPacknames;
	//返回到EnterPwdActivity中的ServiceConnection对象中onServiceConnected(ComponentName name, IBinder service)方法的第二个参数
	private MyBinder binder;
	//内容观察者
	private MyObserver observer;
	//锁屏的广播接收者
	private LockScreenReceiver receiver;
	@Override
	public IBinder onBind(Intent intent) {
		binder = new MyBinder();
		return binder;
	}
	private class MyBinder extends Binder implements IService{
		public void callTempStopProtect(String packname) {
			tempStopProtect(packname);
		}
	}
	//临时停止保护一个被锁定的应用程序的方法
	public void tempStopProtect(String packname){
		//将需要临时停止保护的程序的包名添加到对应的集合中
		tempStopProtectPacknames.add(packname);
	}
	@Override
	public void onCreate() {
		//设置要匹配的Uri路径
		Uri uri = Uri.parse("content://com.guoshisp.applock/");
		observer = new MyObserver(new Handler());
		//第二个参数如果为true，Uri中的content://com.guoshisp.applock/匹配正确即可感应到，后面的（ADD或DELETE）不用继续在匹配下去
		getContentResolver().registerContentObserver(uri, true, observer);
		//以代码动态注册一个广播接收者
		IntentFilter filter = new IntentFilter();
		filter.setPriority(1000);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		receiver = new LockScreenReceiver();
		// 采用代码动态的注册广播接受者.
		registerReceiver(receiver, filter);
		
		super.onCreate();
		dao = new AppLockDao(this);
		//将看门狗服务的标记设置为true，让其一直在后台运行。
		flag = true;
		tempStopProtectPacknames = new ArrayList<String>();
		//从程序锁对应的数据库中取出所有应用程序的包名。
		lockPacknames = dao.findAll();
		pwdintent = new Intent(this,EnterPwdActivity.class);
		//因为服务本身没有任务栈，如果要开启一个需要在任务栈中运行的Activity的话，需要为该Activity创建一个任务栈。
		pwdintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//开启一个线程不断的运行看门狗服务。
		new Thread() {
			public void run() {
				//设置一个死循环，如果为true，则一直运行。
				while (flag){
					//获取一个Activity的管理器，ActivityManager可以动态的观察到当前存在哪些进程。
					ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
					//获取到当前正在栈顶运行的Activity。
					RunningTaskInfo taskinfo = am.getRunningTasks(1).get(0);
					//获取到当前任务栈顶程序所对应的包名。
					String packname = taskinfo.topActivity.getPackageName();
					Log.i(TAG,packname);
					//判断当前栈顶应用程序对应的包名是否是临时被保护的程序
					if(tempStopProtectPacknames.contains(packname)){
						try {
							//看门狗服务非常耗电，这里用于让该服务暂停200毫秒
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//当前栈顶应用程序对应的包名是临时被保护的程序，则跳出当前的if语句，继续执行while循环
						continue;
					}
					//将任务栈顶的程序的包名信息存入意图中（以键值对的形式存入，可以在被激活的Activity中通过getIntent()来获取该意图，然后再获取意图对象中的数据）。
					pwdintent.putExtra("packname", packname);
					//判断运行在栈顶的程序所对应的包名是否是已锁定的应用程序。
					if(lockPacknames.contains(packname)){
						//发现当前应用程序为已锁定的应用程序，需要进入输入密码的界面。
						startActivity(pwdintent);
					}
					try {
						//看门狗服务非常耗电，这里用于让该服务暂停200毫秒
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	//当服务被停止时，我们应停止看门狗继续运行，同时将内容观察者给反注册掉，反注册掉广播接收者
	@Override
	public void onDestroy() {
		flag = false;
		//将内容观察者反注册掉
		getContentResolver().unregisterContentObserver(observer);
		observer = null;
		//反注册掉广播接收者
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	private class MyObserver extends ContentObserver{
		public MyObserver(Handler handler) {
			super(handler);
		}
		//当对应的Uri中的数据发生改变时调用该方法
		@Override
		public void onChange(boolean selfChange) {
			//重新从数据库中获取数据
			lockPacknames = dao.findAll();
			super.onChange(selfChange);
		}
	}
	private class LockScreenReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG,"锁屏了");
			//清空集合，继续保护。
			tempStopProtectPacknames.clear();
		}
		
	}
}
