package com.libowen.assistant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.db.dao.CommonNumDao;

public class CommonNumActivity extends Activity {
	protected static final String TAG = "CommonNumActivity";
	private ExpandableListView elv_common_num;//可扩展的ListView
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_num);
		elv_common_num = (ExpandableListView) findViewById(R.id.elv_common_num);
		elv_common_num.setAdapter(new CommonNumberAdapter());//为ExpandableListView设置一个适配器对象，该对象需要是ExpandableListAdapter对象的子类
		//为分组中的每个孩子注册一个监听器
		elv_common_num.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				//获取到TextView中的电话号码
				TextView tv = (TextView) v;
				String number = tv.getText().toString().split("\n")[1];
				//使用隐式意图来激活手机系统中的拨号器
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:"+number));
				startActivity(intent);
				return false;
			}
		});
	}
	//ExpandableListView的适配器对象，该对象是ExpandableListAdapter对象的子类
	private class CommonNumberAdapter extends BaseExpandableListAdapter {
		//存储对应组中的子孩子的详细信息
		private List<String> groupNames;
		//将子孩子的所有信息一次性从数据库中获取出来，这样可以避免重复查询数据库内存缓存集合。key：分组的位置  value：分组里面所有子孩子的信息
		private Map<Integer, List<String>> childrenCache; 

		public CommonNumberAdapter() {
			childrenCache = new HashMap<Integer, List<String>>();
		}

		/**
		 * 返回当前列表有多少组
		 */
		public int getGroupCount() {
			// groupNames = CommonNumDao.getGroupNames();
			// return groupNames.size();
			return CommonNumDao.getGroupCount();
		}

		/**
		 * 返回每一组里面有多少个条目
		 */
		public int getChildrenCount(int groupPosition) {
			// if(childrenCache.containsKey(groupPosition)){
			// return childrenCache.get(groupPosition).size(); //返回缓存的数据
			// }else{
			// List<String> results =
			// CommonNumDao.getChildNameByPosition(groupPosition);
			// childrenCache.put(groupPosition, results);//把数据放在缓存里面
			// return results.size();
			// }
			return CommonNumDao.getChildrenCount(groupPosition);
		}
		/**
		 * 返回分组所对应的对象。这里我们用不到，所以返回null。
		 */
		public Object getGroup(int groupPosition) {
			return null;
		}
		/**
		 * 获取分组中的条目对象。这里我们用不到，所以返回null。
		 */
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}
		/**
		 * 获取分组所对应的id
		 */
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}
		/**
		 * 获取分组中的条目所对应的id
		 */
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}
		/**
		 * 是否要为分组中的条目设置一下id。false代表不用设置。
		 */
		public boolean hasStableIds() {
			return false;
		}

		/**
		 * 返回每一个分组的view对象.
		 * 参数一：当前分组的id
		 * 参数二：当前分组的View是否可扩展
		 * 参数三：缓存的View对象
		 * 参数四：当前分组的父View对象
		 */
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView tv;
			//使用缓存的View对象
			if (convertView == null) {
				tv = new TextView(getApplicationContext());
			} else {
				tv = (TextView) convertView;
			}
			tv.setTextSize(28);
			if (groupNames != null) {
				tv.setText("          " + groupNames.get(groupPosition));
			} else {
				groupNames = CommonNumDao.getGroupNames();
				tv.setText("          " + groupNames.get(groupPosition));
			}

			return tv;
		}

		/**
		 * 返回每一个分组 某一个位置对应的孩子的view对象
		 * 参数一：当前分组的id
		 * 参数二：分组中的子孩子的id
		 * 参数三：分组中的子孩子是否是最后一个
		 * 参数四：子孩子View的缓存对象
		 * 参数五：分组中的子孩子所在的父View对象
		 */
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView tv;
			if (convertView == null) {

				tv = new TextView(getApplicationContext());
			} else {
				tv = (TextView) convertView;
			}
			tv.setTextSize(20);
			String result = null;
			if (childrenCache.containsKey(groupPosition)) {
				result = childrenCache.get(groupPosition).get(childPosition);
			} else {
				List<String> results = CommonNumDao
						.getChildNameByPosition(groupPosition);
				childrenCache.put(groupPosition, results);// 把数据放在缓存里面
				result = results.get(childPosition);
			}
			tv.setText(result);
			return tv;
		}
		/**
		 * 返回值如果为true，则表示每个分组的子孩子都可以响应到点击事件，否则，不可以响应
		 */
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}
}
