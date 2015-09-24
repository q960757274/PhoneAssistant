package com.libowen.assistant.engine;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.domain.ProcessInfo;

public class ProcessInfoProvider {
	private Context context;

	public ProcessInfoProvider(Context context) {
		this.context = context;
	}

	/**
	 * 返回所有的正在运行的程序信息
	 * @return
	 */
	public List<ProcessInfo> getProcessInfos() {
		//am可以动态的获取应用的进程信息，相当于PC机上的进程管理器
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		//pm可以静态的获取到手机中的所有应用程序信息，相当于PC机上的程序管理器
		PackageManager pm = context.getPackageManager();
		//返回所有正在运行的进程
		List<RunningAppProcessInfo> runingappsInfos = am
				.getRunningAppProcesses();
		//用于存放进程信息
		List<ProcessInfo> processInfos = new ArrayList<ProcessInfo>();
		//遍历出每个进程，并将每个进程的信息封装在ProcessInfo对象中，最后将所有的进程存放在List<ProcessInfo>中返回
		for (RunningAppProcessInfo info : runingappsInfos) {
			//用于封装进程信息
			ProcessInfo processInfo = new ProcessInfo();
			//获取进程的pid（进程的标记）
			int pid = info.pid;
			//将进程的pid、processName、memsize封装到ProcessInfo对象中
			processInfo.setPid(pid);
			String packname = info.processName;
			processInfo.setPackname(packname);
			//获取到该进程对应的应用程序所占用的内存空间
			long memsize = am.getProcessMemoryInfo(new int[] { pid })[0]
					.getTotalPrivateDirty() * 1024;
			processInfo.setMemsize(memsize);

			try {
				//通过进程的packname来获取到该进程对应的应用程序对象（获取到应用程序的对象后，就可以通过该对象获取应用程序信息）
				ApplicationInfo applicationInfo = pm.getApplicationInfo(packname, 0);
				//判断该应用程序是否是第三方应用程序，便于以后分类
				if(filterApp(applicationInfo)){
					processInfo.setUserprocess(true);
				}else{
					processInfo.setUserprocess(false);
				}
				//分别获取到应用程序的图标和名称，并将其封装到ProcessInfo对象中
				processInfo.setIcon(applicationInfo.loadIcon(pm));
				processInfo.setAppname(applicationInfo.loadLabel(pm).toString());
			} catch (Exception e) {
				//这里会抛出一个包名未找到异常，我们将其设置为系统进程，应用图标为默认的系统图标
				e.printStackTrace();
				processInfo.setUserprocess(false);
				processInfo.setIcon(context.getResources().getDrawable(R.drawable.ic_launcher));
				processInfo.setAppname(packname);
			}
			processInfos.add(processInfo);
			processInfo = null;
			
		}
		return  processInfos;
	}
	
	/**
	 * 三方应用的过滤器 ,如
	 * 
	 * @param info
	 * @return true 三方应用 false 系统应用
	 */
	public boolean filterApp(ApplicationInfo info) {
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return true;
		}
		return false;
	}
}
