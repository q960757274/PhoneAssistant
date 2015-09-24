package com.libowen.assistant.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceStatusUtil {

	/**
	 * 判断某个服务是否处于运行状态
	 * @param context
	 * @param serviceClassName 服务的完整的类名
	 * @return true表示正在运行   false表示没有运行
	 */
	public static boolean isServiceRunning(Context context,String serviceClassName){
		ActivityManager  am  = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		//参数100：表示要获取正在运行的100个服务。如果没有100，则返回所有正在运行的；如果超过100，则只返回100个。
		List<RunningServiceInfo>  infos = am.getRunningServices(100);
		//遍历返回的服务，判断我们查看的服务是否处于运行状态
		for(RunningServiceInfo info: infos){
			if(serviceClassName.equals(info.service.getClassName())){
				return true;
			}
		}
		return false;
	}
}
