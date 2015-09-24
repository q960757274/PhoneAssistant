package com.libowen.assistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

	private static final String TAG = "BootCompleteReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"手机重启了");
		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		//手机防盗是否开启
		boolean protecting = sp.getBoolean("protecting", false);
		if(protecting){
			//获取安全号码
			String safemuber = sp.getString("safemuber", "");
			//判断 当前手机的sim卡 和我绑定的sim是否一致.
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			//获取当前sim卡的串号
			String realsim = tm.getSimSerialNumber();
			//获取之前保存的sim卡的串号
			String savedSim = sp.getString("simserial", "");
			//判断两个串号是否相同
			if(!savedSim.equals(realsim)){
				//发报警短信
				Log.i(TAG,"发送短信");
				SmsManager smsManager = SmsManager.getDefault();
				//1.接收短信号码   2.发信的源地址   3.信息内容   4.延期意图（当前事件不会立即执行）   5.送达报告
				smsManager.sendTextMessage(safemuber, null, "sim card changed", null, null);
			
			}
		}
	}
}
