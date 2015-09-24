package com.libowen.assistant;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.domain.TrafficInfo;
import com.libowen.assistant.engine.TrafficInfoProvider;

public class TrafficInfoActivity extends Activity {
	//展示数据列表
	private ListView lv;
	//获取到所有具有Intenet权限的应用的流量信息
	private TrafficInfoProvider provider;
	//ProgressBar和TextView（正在加载...）的父控件，用于控制其显示
	private LinearLayout ll_loading;
	//封装单个具有Intenet权限的应用的流量信息
	private List<TrafficInfo>  trafficInfos;
	//处理子线程发送过来的消息，更新UI
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			ll_loading.setVisibility(View.INVISIBLE);
			lv.setAdapter(new TrafficAdapter());
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.traffic_info);
		super.onCreate(savedInstanceState);
		lv = (ListView) findViewById(R.id.lv_traffic_manager);
		provider = new TrafficInfoProvider(this);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		ll_loading.setVisibility(View.VISIBLE);
		//获取到具有Internet权限的应用所产生的流量
		new Thread(){
			public void run() {
				trafficInfos = provider.getTrafficInfos();
				//想主线程中发送一个空消息，用于通知主线程更新数据
				handler.sendEmptyMessage(0);
			};
		}.start();
	}
	//数据适配器
	private class TrafficAdapter extends BaseAdapter{

		public int getCount() {
			return trafficInfos.size();
		}

		public Object getItem(int position) {
			return trafficInfos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
		//ListView中显示多少个Item，该方法就被调用多少次
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder = new ViewHolder();
			TrafficInfo info = trafficInfos.get(position);
			//复用缓存的View
			if(convertView==null){
				view = View.inflate(getApplicationContext(), R.layout.traffic_item, null);
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_traffic_icon);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_traffic_name);
				holder.tv_rx = (TextView) view.findViewById(R.id.tv_traffic_rx);
				holder.tv_tx = (TextView) view.findViewById(R.id.tv_traffic_tx);
				holder.tv_total = (TextView) view.findViewById(R.id.tv_traffic_total);
				view.setTag(holder);
			}else{
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			holder.iv_icon.setImageDrawable(info.getIcon());
			holder.tv_name.setText(info.getAppname());
			//下载所产生的流量
			long rx = info.getRx();
			//上传所产生的流量
			long tx = info.getTx();
			//增强程序的健壮性。因为在模拟器上运行时返回值为-1.
			if(rx<0){
				rx = 0;
			}
			if(tx<0){
				tx = 0;
			}
			holder.tv_rx.setText(Formatter.formatFileSize(getApplicationContext(), rx));
			holder.tv_tx.setText(Formatter.formatFileSize(getApplicationContext(), tx));
			//总流量
			long total = rx + tx;
			//通过Formatter将long类型的数据转换为MB或这KB，当数字较小时，自动采用KB
			holder.tv_total.setText(Formatter.formatFileSize(getApplicationContext(), total));
			return view;
		}
	}
	//通过static的修饰，保证了栈内存中存在唯一一份字节码且被共用
	static class ViewHolder{
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_tx;
		TextView tv_rx;
		TextView tv_total;
	}
}
