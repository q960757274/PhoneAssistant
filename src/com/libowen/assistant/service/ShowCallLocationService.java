package com.libowen.assistant.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.db.dao.NumberAddressDao;
//在后台监听电话呼入的状态
public class ShowCallLocationService extends Service {
	private TelephonyManager tm;//电话管理器
	private MyPhoneListener listener;//电话状态改变的监听器
	private WindowManager windowManager;//窗体管理器
	private SharedPreferences sp;//用于取出归属地风格显示风格的Item对应的id
	//"半透明","活力橙","卫士蓝","苹果绿","金属灰"
	private static final  int[] bgs = {R.drawable.call_locate_white,R.drawable.call_locate_orange,
			R.drawable.call_locate_blue,R.drawable.call_locate_green,R.drawable.call_locate_gray};
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
		sp =getSharedPreferences("config",MODE_PRIVATE);
		// 注册系统的电话状态改变的监听器.
		listener = new MyPhoneListener();
		//获取系统的电话管理器
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		//为电话设置一个监听。参数一：监听器，参数二：要监听的电话改变类型 （这里监听的是通话状态）
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
	}

	private class MyPhoneListener extends PhoneStateListener {
		private View view;
		//参数一：手机的状态      参数二：呼叫进来的手机号码
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:// 手机铃声正在响.
				//获取呼叫进来号码的地址（查询我们之前的号码归属地数据库）
				String address = NumberAddressDao.getAddress(incomingNumber);
				//使用系统的吐司来显示归属地信息，但显示的时间较短
				//Toast.makeText(getApplicationContext(), "归属地:"+address, 1).show();
				//通过布局填充器将一个显示号码归属地的布局转成View，该View是一个吐司
				view = View.inflate(getApplicationContext(), R.layout.show_address, null);
				//获取到显示号码归属地布局的根布局LinearLayout
				LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll_show_address);
				//从sp文件中获取显示归属地风格的Item的id
				int which = sp.getInt("which", 0);
				//设置来电归属地显示的背景图片
				ll.setBackgroundResource(bgs[which]);
				//查找view中的用于显示归属地的TextView
				TextView tv = (TextView) view.findViewById(R.id.tv_show_address);
				//将归属地信息设置到TextView
				tv.setText(address);
				//获取到与窗体相关的布局的参数（这里用于设置窗体上显示来电归属地的吐司的参数信息）
	            final WindowManager.LayoutParams params = new LayoutParams();
	            //指定吐司的重心为图形的左上角对应的点
	            params.gravity = Gravity.LEFT | Gravity.TOP;
	            //设置吐司在窗体中的显示位置。获取到吐司离窗体左端的X值、获取到吐司离窗体顶端的Y值
	            params.x = sp.getInt("lastx", 0);
	            params.y = sp.getInt("lasty", 0);
	            //设置窗体布局View的高度
	            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
	            //设置窗体布局View的宽度
	            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
	            //窗体View不可以获取焦点、不可以被触摸、保持在屏幕上
	            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
	                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
	                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
	            //显示在窗体上的style为半透明
	            params.format = PixelFormat.TRANSLUCENT;
	            //窗体View的类型为吐司
	            params.type = WindowManager.LayoutParams.TYPE_TOAST;
	            //将吐司挂载在窗体上。窗体服务是一个全局的系统服务，该服务开启后会在后台运行。一般情况下，在窗体上一旦挂载一个View并显示后，并不会自动消失
				windowManager.addView(view, params);
				break;

			case TelephonyManager.CALL_STATE_IDLE: // 手机的空闲状态
				if(view!=null){
					//将窗体上的吐司移除掉
					windowManager.removeView(view);
					view = null;
				}
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:// 手机接通通话时的状态
				/*if(view!=null){
					//将窗体上的吐司移除掉
					windowManager.removeView(view);
					view = null;
				}*/
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
}
