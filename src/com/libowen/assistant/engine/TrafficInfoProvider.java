package com.libowen.assistant.engine;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;

import com.libowen.assistant.domain.TrafficInfo;

public class TrafficInfoProvider {
	private PackageManager pm;
	private Context context;

	public TrafficInfoProvider(Context context) {
		this.context = context;
		pm = context.getPackageManager();
	}

	/**
	 * 返回所有的有互联网访问权限的应用程序的流量信息。
	 * @return
	 */
	public List<TrafficInfo> getTrafficInfos() {
		//获取到配置权限信息的应用程序
		List<PackageInfo> packinfos = pm
				.getInstalledPackages(PackageManager.GET_PERMISSIONS);
		//存放具有Internet权限信息的应用
		List<TrafficInfo> trafficInfos = new ArrayList<TrafficInfo>();
		for(PackageInfo packinfo : packinfos){
			//获取该应用的所有权限信息
			String[] permissions = packinfo.requestedPermissions;
			if(permissions!=null&&permissions.length>0){
				for(String permission : permissions){
					//筛选出具有Internet权限的应用程序
					if("android.permission.INTERNET".equals(permission)){
						//用于封装具有Internet权限的应用程序信息
						TrafficInfo trafficInfo = new TrafficInfo();
						//封装应用信息
						trafficInfo.setPackname(packinfo.packageName);
						trafficInfo.setIcon(packinfo.applicationInfo.loadIcon(pm));
						trafficInfo.setAppname(packinfo.applicationInfo.loadLabel(pm).toString());
						//获取到应用的uid（user id）
						int uid = packinfo.applicationInfo.uid;
						//TrafficStats对象通过应用的uid来获取应用的下载、上传流量信息
						trafficInfo.setRx(TrafficStats.getUidRxBytes(uid));
						trafficInfo.setTx(TrafficStats.getUidTxBytes(uid));
						trafficInfos.add(trafficInfo);
						trafficInfo = null;
						break;
					}
				}
			}
		}
		return trafficInfos;
	}
}
