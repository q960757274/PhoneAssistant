package com.libowen.assistant.service;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.libowen.assistant.db.dao.BlackNumberDao;

public class CallFirewallService extends Service {
	public static final String TAG = "CallFirewallService";
	private TelephonyManager tm;
	private MyPhoneListener listener;
	private BlackNumberDao dao;
//	private long  starttime;
//	private long endtime;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 当服务第一次被创建的时候 调用
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		dao = new BlackNumberDao(this);
		// 注册系统的电话状态改变的监听器.
		listener = new MyPhoneListener();
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		// 系统的电话服务 就监听了 电话状态的变化,
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

	}

	private class MyPhoneListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:// 手机铃声正在响.
				//starttime = System.currentTimeMillis();
				// 判断 incomingNumber 是否是黑名单号码
				int mode = dao.findNumberMode(incomingNumber);
				if (mode == 0 || mode == 2) {
					// 黑名单号码
					Log.i(TAG, "挂断电话");
					//挂断电话
					endcall(incomingNumber);
				}
				break;

			case TelephonyManager.CALL_STATE_IDLE: // 手机的空闲状态
				/*endtime = System.currentTimeMillis();
				if(dao.find(incomingNumber)){
					break;
				}
				if(endtime - starttime<3000){
					Log.i(TAG,"骚扰电话");
					//showNotification(incomingNumber);
				}*/
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:// 手机接通通话的状态

				break;
			}

			super.onCallStateChanged(state, incomingNumber);
		}

	}

	/**
	 * 取消电话状态的监听.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
		
	}

	/**
	 * 显示添加黑名单号码的notification
	 * @param incomingNumber
	 *//*
	public void showNotification(String incomingNumber) {
		//1.创建一个notification的管理者
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		//2.创建一个notification 
		int icon = R.drawable.notification;
		CharSequence tickerText = "拦截到一个一声响号码";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		//3.定义notification的具体内容 和点击事件
		Context context = getApplicationContext();
		CharSequence contentTitle = "发现响一声号码";
		CharSequence contentText = "号码为:"+incomingNumber;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent notificationIntent = new Intent(this, CallSmsSafeActivity.class);
		notificationIntent.putExtra("blacknumber", incomingNumber);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT );
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		//4.利用notification的manager 显示一个notification
		mNotificationManager.notify(0, notification);
		
	}*/

	/**
	 * 挂断电话
	 * 需要拷贝两个aidl文件
	 * 添加权限<uses-permission android:name="android.permission.CALL_PHONE" />
	 * @param incomingNumber
	 */
	public void endcall(String incomingNumber) {
		try {
			//使用反射获取系统的service方法
			Method method = Class.forName("android.os.ServiceManager")
					.getMethod("getService", String.class);
			IBinder binder = (IBinder) method.invoke(null,
					new Object[] { TELEPHONY_SERVICE });
			//通过aidl实现方法的调用
			ITelephony telephony = ITelephony.Stub.asInterface(binder);
			telephony.endCall();//该方法是一个异步方法，他会新开启一个线程将呼入的号码存入数据库中
			
			//deleteCallLog(incomingNumber);

			// 注册一个内容观察者 观察uri数据的变化      
			getContentResolver().registerContentObserver(
					CallLog.Calls.CONTENT_URI, true, new MyObserver(new Handler(), incomingNumber));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 定义自己的内容观察者 ,
	 * 在构造方法里面传递 观察的号码
	 * @author 
	 *
	 */
	
	private class MyObserver extends ContentObserver {
		private String incomingNumber;
		public MyObserver(Handler handler, String incomingNumber) {
			super(handler);
			this.incomingNumber = incomingNumber;
		}
		/**
		 * 数据库内容发生改变的时候调用的方法
		 */
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			//立即执行删除操作
			deleteCallLog(incomingNumber);
			//停止数据的观察
			getContentResolver().unregisterContentObserver(this);
		}
	}

	/**
	 * 删除呼叫记录
	 * 
	 * @param incomingNumber
	 */
	private void deleteCallLog(String incomingNumber) {
		// 呼叫记录内容提供者对应的uri
		Uri uri = Uri.parse("content://call_log/calls");
		// CallLog.Calls.CONTENT_URI;
		Cursor cursor = getContentResolver().query(uri, new String[] { "_id" },
				"number=?", new String[] { incomingNumber }, null);
		while (cursor.moveToNext()) {
			String id = cursor.getString(0);
			getContentResolver().delete(uri, "_id=?", new String[] { id });
		}
		cursor.close();
	}
}
