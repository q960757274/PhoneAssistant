package com.libowen.assistant;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.service.WatchDogService1;

public class EnterPwdActivity extends Activity {
	//密码输入框
	private EditText et_password;
	//应用名称
	private TextView tv_name;
	//应用图标
	private ImageView iv_icon;
	//用于启动看门狗服务的意图对象
	private Intent serviceIntent;
	//停止保护一个应用程序（接口）
	private IService iService;
	//连接服务时的一个对象（在绑定服务时需要传入）
	private MyConn conn;
	//应用包名
	private String packname;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_pwd);
		et_password = (EditText) findViewById(R.id.et_password);
		//获取到激活当前Activity的意图（WatchDogService1中的pwdintent）
		Intent intent = getIntent();
		//获取到意图中存入的数据（要进入被锁定的应用的包名）
		packname = intent.getStringExtra("packname");
		tv_name = (TextView) findViewById(R.id.tv_enterpwd_name);
		iv_icon = (ImageView) findViewById(R.id.iv_enterpwd_icon);
		serviceIntent = new Intent(this,WatchDogService1.class);
		conn = new MyConn();
		//绑定服务（非startService()）。执行服务中的onCreate-->onBind方法（该方法的返回值不能为null）。
		bindService(serviceIntent, conn, BIND_AUTO_CREATE);
		
		try {
			//根据包名获取到包信息对象
			PackageInfo info = getPackageManager().getPackageInfo(packname, 0);
			//info.applicationInfo.loadLabel(getPackageManager())获取到该包名的应用程序所对应的应用名称
			tv_name.setText(info.applicationInfo.loadLabel(getPackageManager()));
			//info.applicationInfo.loadIcon(getPackageManager())获取到该包名的应用程序所对应的应用图标
			iv_icon.setImageDrawable(info.applicationInfo.loadIcon(getPackageManager()));
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private class MyConn implements ServiceConnection{
		//在操作者在连接一个服务成功时被调用。IBinder对象就是onBind(Intent intent)返回的IBinder对象。
		public void onServiceConnected(ComponentName name, IBinder service) {
			//因为返回的IBinder实现了iService接口（向上转型）
			iService = (IService) service;
		}
		//在服务崩溃或被杀死导致的连接中断时被调用，而如果我们自己解除绑定时则不会被调用
		public void onServiceDisconnected(ComponentName name) {
			
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//解除绑定
		unbindService(conn);
	}
	/**
	 * 点击“确定”按钮是执行的方法
	 */
	public void enterPassword(View view){
		//获取到输入框中的密码，并将密码前后的空格清除掉。
		String pwd = et_password.getText().toString().trim();
		//判断输入的密码是否为空
		if(TextUtils.isEmpty(pwd)){
			Toast.makeText(this, "密码不能为空", 0).show();
			return ;
		}
		//判断密码是否为123（正确密码，没有提供设置密码的界面，这里简单的处理一下）。
		if("123".equals(pwd)){
			//通知看门狗 临时的停止对 packname的保护
			iService.callTempStopProtect(packname);
			/*Intent intent = new Intent();
			intent.setAction("cn.itcast.mobilesafe.stopprotect");
			intent.putExtra("packname", packname);
			sendBroadcast(intent);*/
			finish();
			
		}else{
			Toast.makeText(this, "密码不正确", 0).show();
			return ;
		}
	}
	
	/**
	 * 当进入当前的界面后，屏蔽掉Back键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getKeyCode()==KeyEvent.KEYCODE_BACK){
			return true;//消费掉当前的Back键
		}
		return super.onKeyDown(keyCode, event);
	}
}
