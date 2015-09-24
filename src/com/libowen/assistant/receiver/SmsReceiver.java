package com.libowen.assistant.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.db.dao.BlackNumberDao;
import com.libowen.assistant.engine.GPSInfoProvider;

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "SmsReceiver";
	private SharedPreferences sp;
	private BlackNumberDao dao;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "短信到来了");
		dao = new BlackNumberDao(context);
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		String safenumber = sp.getString("safemuber", "");
		// 获取短信中的内容。系统接收到一个信息广播时，会将接收到的信息存放到pdus数组中
		Object[] objs = (Object[]) intent.getExtras().get("pdus");
		// 获取手机设备管理器
		DevicePolicyManager dm = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		// 创建一个与MyAdmin相关联的组件
		ComponentName mAdminName = new ComponentName(context, MyAdmin.class);
		// 遍历出信息中的所有内容
		for (Object obj : objs) {
			SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
			// 获取发件人的号码
			String sender = smsMessage.getOriginatingAddress();
			// 判断短信号码是否是黑名单号码&短信拦截
			int result = dao.findNumberMode(sender);
			if (result == 1 || result == 2) {// 判断该黑名单号码是否需要拦截短信
				Log.i(TAG, "拦截黑名单短信");
				abortBroadcast();
			}
			// 获取短信信息内容
			String body = smsMessage.getMessageBody();

			if ("Baojing".equals(body)) {
				Log.i(TAG, "播放报警音乐");
				// 得到音频播放器
				MediaPlayer player = MediaPlayer.create(context, R.raw.baojing);// res\raw\ylzs.mp3
				// 即使手机是静音模式也有音乐的声音
				player.setVolume(1.0f, 1.0f);
				// 开始播放音乐
				player.start();
				// 终止掉发送过来的信息，在本地查看不到该信息
				abortBroadcast();

			}
		}
	}
}
