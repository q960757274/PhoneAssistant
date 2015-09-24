package com.libowen.assistant.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSInfoProvider {
	private static GPSInfoProvider mGPSInfoProvider;
	private static LocationManager lm;//位置管理器
	private static MyListener listener;//位置变化的监听器。监听动作比较耗电
	private static SharedPreferences  sp;//持久化位置的信息（经纬度）
	//私有化构造方法，做成单例模式。目的在于 减少往系统服务注册监听，避免程序挂掉 ，减少耗电量
	private GPSInfoProvider(){
		
	}
	public synchronized static GPSInfoProvider getInstance(Context context){
		if(mGPSInfoProvider==null){
			mGPSInfoProvider = new GPSInfoProvider();
			// 获取位置管理器
			lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			// 获取查询地理位置的查询条件对象（内部是一个Map集合）
			Criteria criteria = new Criteria();
			// 设置精确度，这里传递的是最精准的精确度
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			// gps定位是否允许产生开销（true表示允许，例如好用流量）
			criteria.setCostAllowed(true);
			// 手机的功耗消耗情况（实时定位时，应该设置为高）
			criteria.setPowerRequirement(Criteria.POWER_HIGH);
			// 获取海拔信息
			criteria.setAltitudeRequired(true);
			// 对手机的移动的速度是否敏感
			criteria.setSpeedRequired(true);
			// 获取到当前手机最好用的位置提供者：参数一：查询的选择条件 参数二：传递为true时，表示只有可用的位置提供者时才会被返回回去
			String provider = lm.getBestProvider(criteria, true);
			// System.out.println(provider);
			listener = new GPSInfoProvider().new MyListener();
			// 调用更新位置方法：参数一：位置提供者，参数二：最短的更新位置信息时间（最好要大于60000（一分钟）），参数三：最短通知距离，参数四：位置改变时的监听对象
			lm.requestLocationUpdates(provider, 60000, 100, listener);
			// 在Sdcard对应的包中创建一个config.xml文件，文件的操作类型设置为PRIVATE
			sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		return mGPSInfoProvider;
	}
	
	/**
	 * 取消位置的监听
	 */
	public void stopLinsten(){
		lm.removeUpdates(listener);
		listener = null;
	}
	
	protected class MyListener implements LocationListener {

		/**
		 * 当手机的位置发生改变的时候 调用的方法
		 */
		public void onLocationChanged(Location location) {
			String latitude = "latitude :" + location.getLatitude(); // 纬度
			String longitude = "longitude: " + location.getLongitude(); // 经度
			String meter = "accuracy :" + location.getAccuracy();// 精确度
			System.out.println(latitude + "-" + longitude + "-" + meter);

			Editor editor = sp.edit();
			editor.putString("last_location", latitude + "-" + longitude + "-"
					+ meter);
			editor.commit();
		}

		/**
		 * 当位置提供者 状态发生改变的时候 调用的方法
		 */
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		/**
		 * 当某个位置提供者可用的时候.
		 */
		public void onProviderEnabled(String provider) {

		}

		/**
		 * 当某个位置提供者 不可用的时候
		 */
		public void onProviderDisabled(String provider) {

		}
	}
	
	/**
	 * 获取手机的位置
	 * @return
	 */
	public String getLocation(){
		return sp.getString("last_location", "");
	}
	
}
