package com.libowen.assistant.engine;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.libowen.assistant.domain.AppInfo;
public class AppInfoProvider {
	private PackageManager pm;
	public AppInfoProvider(Context context) {
		pm = context.getPackageManager();
	}
	/**
	 * 获取所有安装程序信息
	 * @return
	 */
	public List<AppInfo> getInstalledApps(){
		//返回所有的安装的程序列表信息。其中，参数：PackageManager.GET_UNINSTALLED_PACKAGES表示：包括哪些被卸载的但是没有清除数据的应用
		List<PackageInfo> packageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		List<AppInfo> appinfos = new ArrayList<AppInfo>();
		for(PackageInfo info : packageinfos){
			AppInfo appinfo = new AppInfo();
			//应用程序的包名
			appinfo.setPackname(info.packageName);
			//应用程序的版本号
			appinfo.setVersion(info.versionName);
			//应用程序的图标 info.applicationInfo.loadIcon(pm);
			appinfo.setAppicon(info.applicationInfo.loadIcon(pm));
			//应用程序的名称 info.applicationInfo.loadLabel(pm);
			appinfo.setAppname(info.applicationInfo.loadLabel(pm).toString());
			//过滤出第三方（非系统）应用程序的名称
			appinfo.setUserapp(filterApp(info.applicationInfo));
			appinfos.add(appinfo);
			appinfo = null;
		}
		return appinfos;
	}
	
	/**
	 * 第三方应用程序的过滤器,
	 * @param info
	 * @return true 三方应用
	 *         false 系统应用.
	 */
    public boolean filterApp(ApplicationInfo info) {
    	//当前应用程序的标记&系统应用程序的标记
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return true;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return true;
        }
        return false;
    }
}
