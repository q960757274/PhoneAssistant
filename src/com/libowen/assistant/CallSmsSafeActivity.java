package com.libowen.assistant;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.db.dao.BlackNumberDao;
import com.libowen.assistant.domain.BlackNumber;

public class CallSmsSafeActivity extends Activity {
	protected static final int LOAD_DATA_FINISH = 40;
	public static final String TAG = "CallSmsSafeActivity";
	//用于展现出所有的黑名单号码
	private ListView lv_call_sms_safe;
	//操作黑名单号码数据库的对象
	private BlackNumberDao dao;
	//将黑名单号码从数据库中一次性取出存入缓存集合中（避免在适配器中频繁的操作数据库）
	private List<BlackNumber> blacknumbers;
	//显示黑名单号码的适配器对象
	private BlackNumberAdapter adpater;
	//ProgressBar控件的父控件，用于控制子控件的显示（包括了ProgressBar）
	private LinearLayout ll_call_sms_safe_loading;
	
	//private String initnumber;
	//用于接收子线程发送过来的消息，实现UI的更新
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_DATA_FINISH://从数据库中加载黑名单号码完成
				//将进度条及“正在加载数据...”隐藏
				ll_call_sms_safe_loading.setVisibility(View.INVISIBLE);
				//为lv_call_sms_safe设置适配器
				adpater = new BlackNumberAdapter();
				lv_call_sms_safe.setAdapter(adpater);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_sms_safe);
		ll_call_sms_safe_loading = (LinearLayout) findViewById(R.id.ll_call_sms_safe_loading);
		dao = new BlackNumberDao(this);
		lv_call_sms_safe = (ListView) findViewById(R.id.lv_call_sms_safe);
		//ll_call_sms_safe_loading控件中的所有子控件设置为可见（ProgressBar和“正在加载数据...”）
		ll_call_sms_safe_loading.setVisibility(View.VISIBLE);
				
		/*Intent intent  = getIntent();//获取激活当前组件的intent;
		initnumber = intent.getStringExtra("blacknumber");
		Log.i(TAG,"initnumber:"+initnumber);
		if(initnumber!=null){
			showBlackNumberDialog(0, 0);
		}*/

		// 1.为lv_call_sms_safe注册一个上下文菜单
		registerForContextMenu(lv_call_sms_safe);
		//一次性获取数据库中的所有数据的操作是一个比较耗时的操作，建议在子线程中完成
		new Thread() {
			public void run() {
				blacknumbers = dao.findAll();
				//通知主线程更新界面
				Message msg = Message.obtain();
				msg.what = LOAD_DATA_FINISH;
				handler.sendMessage(msg);
			};
		}.start();
	}

	/*@Override
	protected void onNewIntent(Intent intent) {
		initnumber = intent.getStringExtra("blacknumber");
		Log.i(TAG,"initnumber:"+initnumber);
		if(initnumber!=null){
			showBlackNumberDialog(0, 0);
		}
		super.onNewIntent(intent);
	}*/
	
	// 2.重写创建上下文菜单的方法
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		//设置长按Item后要显示的布局
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.call_sms_safe_menu, menu);
	}

	// 3.响应上下文菜单的点击事件
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//获取到Item对应的对象
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int position = (int) info.id; // 当前上下文菜单对应的listview里面的哪一个条目
		switch (item.getItemId()) {
		case R.id.item_delete:
			Log.i(TAG, "删除黑名单记录");
			deleteBlackNumber(position);
			return true;
		case R.id.item_update:
			Log.i(TAG, "更新黑名单记录");
			updateBlackNumber(position);

			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/**
	 * 更新黑名单号码
	 * 
	 * @param position
	 */
	private void updateBlackNumber(int position) {
		showBlackNumberDialog(1, position);
	}

	/**
	 * 删除一条黑名单记录
	 * 
	 * @param position
	 */
	private void deleteBlackNumber(int position) {
		BlackNumber blackNumber = (BlackNumber) lv_call_sms_safe
				.getItemAtPosition(position);
		String number = blackNumber.getNumber();
		dao.delete(number); // 删除了 数据库里面的记录
		blacknumbers.remove(blackNumber);// 删除当前listview里面的数据.
		adpater.notifyDataSetChanged();
	}
	/**
	 * 为黑名单号码中的lv_call_sms_safe中的Item适配数据
	 * @author Administrator
	 *
	 */
	private class BlackNumberAdapter extends BaseAdapter {
		//获取Item的数目
		public int getCount() {
			return blacknumbers.size();
		}
		//获取Item的对象
		public Object getItem(int position) {
			return blacknumbers.get(position);
		}
		//获取Item对应的id
		public long getItemId(int position) {
			return position;
		}

		//在屏幕上，每显示一个Item就调用一次该方法
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			//复用历史缓存的View对象
			if (convertView == null) {
				Log.i(TAG, "创建新的view对象");
				//将Item转成View对象
				view = View.inflate(getApplicationContext(),
						R.layout.call_sms_item, null);
				holder = new ViewHolder();
				holder.tv_number = (TextView) view
						.findViewById(R.id.tv_callsms_item_number);
				holder.tv_mode = (TextView) view
						.findViewById(R.id.tv_callsms_item_mode);
				view.setTag(holder);// 把控件id的引用 存放在view对象里面
			} else {
				view = convertView;
				Log.i(TAG, "使用历史缓存的view对象");
				holder = (ViewHolder) view.getTag();
			}
			//为Item设置拦截模式
			BlackNumber blacknumber = blacknumbers.get(position);
			holder.tv_number.setText(blacknumber.getNumber());
			int mode = blacknumber.getMode();
			if (mode == 0) {
				holder.tv_mode.setText("电话拦截");
			} else if (mode == 1) {
				holder.tv_mode.setText("短信拦截");
			} else {
				holder.tv_mode.setText("全部拦截");
			}
			return view;
		}
	}
	//将Item中的控件使用static修饰，被static修饰的类的字节码在JVM中只会存在一份。tv_number与tv_mode在栈中也会只存在一份
	private static class ViewHolder {
		TextView tv_number;
		TextView tv_mode;
	}

	/**
	 * 为“添加黑名单号码”注册的点击事件
	 * 添加一条黑名单号码
	 * @param view
	 */
	public void addBlackNumber(View view) {
		showBlackNumberDialog(0, 0);
	}

	/**
	 * 显示添加黑名单时的添加对话框或者修改对话框（两者共用同一个对话框）
	 * 
	 * @param flag
	 *            0 代表添加， 1 代表修改
	 * @param position
	 *            被修改的Item在窗体中的位置。如果添加 数据，添加的数据可以为空
	 */
	private void showBlackNumberDialog(final int flag, final int position) {
		//获得一个窗体构造器
		AlertDialog.Builder builder = new Builder(this);
		//将添加号码的布局文件转换成一个View
		View dialogview = View.inflate(this, R.layout.add_black_number, null);
		//获取输入黑名单号码的EditText
		final EditText et_number = (EditText) dialogview
				.findViewById(R.id.et_add_black_number);
		/*if(!TextUtils.isEmpty(initnumber)){
			et_number.setText(initnumber);
		}*/
		//获取到弹出的对话框中的各个组件
		final CheckBox cb_phone = (CheckBox) dialogview
				.findViewById(R.id.cb_block_phone);
		final CheckBox cb_sms = (CheckBox) dialogview
				.findViewById(R.id.cb_block_sms);
		TextView tv_title = (TextView) dialogview
				.findViewById(R.id.tv_black_number_title);
		if (flag == 1) {//修改黑名单数据
			tv_title.setText("修改");
			//将要修改的黑名单号码回显到号码输入框中
			BlackNumber blackNumber = (BlackNumber) lv_call_sms_safe
					.getItemAtPosition(position);
			String oldnumber = blackNumber.getNumber();
			et_number.setText(oldnumber);
			int m = blackNumber.getMode();
			//通过拦截模式来指定Checkbox的勾选状态
			if(m==0){//电话拦截
				cb_phone.setChecked(true);
				cb_sms.setChecked(false);
			}else if(m==1){//短信拦截
				cb_sms.setChecked(true);
				cb_phone.setChecked(false);
			}else{//电话与短信拦截
				cb_phone.setChecked(true);
				cb_sms.setChecked(true);
			}
		}
		//将转换的布局文件添加到窗体上
		builder.setView(dialogview);
		//窗体对话框中的“确定”按钮
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				//获取到输入的号码，并将号码前后的空格清除掉
				String number = et_number.getText().toString().trim();
				//flag=1代表的是修改，处理更改的时候 避免更改出来相同的电话号码.
				if(flag==1&&dao.find(number)){
					Toast.makeText(getApplicationContext(), "要修改的电话号码已经存在",0).show();
					return ;
				}
				//如果输入的是空，则直接结束当前方法
				if (TextUtils.isEmpty(number)) {
					return;
				} else {//输入的号码不为空
					// 添加结果。如果添加成功 ，需要通知界面更新黑名单数据。默认的是添加失败
					boolean result = false;
					BlackNumber blacknumber = new BlackNumber();
					blacknumber.setNumber(number);
					//电话拦截狂和短信拦截狂都被选中的话，拦截模式应该为2
					if (cb_phone.isChecked() && cb_sms.isChecked()) {
						if (flag == 0) {//flag=1表示是添加黑名单号码
							result = dao.add(number, "2");
							blacknumber.setMode(2);
						} else {//修改黑名单号码
							//获取到要修改的Item对象
							BlackNumber blackNumber = (BlackNumber) lv_call_sms_safe
									.getItemAtPosition(position);
							//更新数据库中要修改的那条数据
							dao.update(blackNumber.getNumber(), number, "2");
							blackNumber.setMode(2);
							blackNumber.setNumber(number);
							//通知适配器重新显示数据（此时，界面上的数据被刷新）
							adpater.notifyDataSetChanged();
						}
					} else if (cb_phone.isChecked()) {//电话拦截，拦截模式为0
						if (flag == 0) {//添加黑名单数据
							result = dao.add(number, "0");
							blacknumber.setMode(0);
						} else {//修改黑名单数据
							//获取到要修改的Item对象
							BlackNumber blackNumber = (BlackNumber) lv_call_sms_safe
									.getItemAtPosition(position);
							//更新数据库中要修改的那条数据
							dao.update(blackNumber.getNumber(), number, "0");
							blackNumber.setMode(0);
							blackNumber.setNumber(number);
							//通知适配器重新显示数据（此时，界面上的数据被刷新）
							adpater.notifyDataSetChanged();

						}
					} else if (cb_sms.isChecked()) {//拦截模式为短信拦截（对应的数字为1）
						if (flag == 0) {//添加黑名单数据
							result = dao.add(number, "1");
							blacknumber.setMode(1);
						}else{//修改黑名单数据
							//获取到要修改的Item对象
							BlackNumber blackNumber = (BlackNumber) lv_call_sms_safe
									.getItemAtPosition(position);
							//更新数据库中要修改的那条数据
							dao.update(blackNumber.getNumber(), number, "1");
							blackNumber.setMode(1);
							blackNumber.setNumber(number);
							//通知适配器重新显示数据（此时，界面上的数据被刷新）
							adpater.notifyDataSetChanged();
						}
					} else {//没有选择任何拦截模式
						Toast.makeText(getApplicationContext(), "拦截模式不能为空", 0)
								.show();
						return;
					}
					if (result) {//添加或修改数据成功，此时需要更新界面列表中的数据
						//将新添加的数据添加到集合中，因为适配器是从集合中取数据的
						blacknumbers.add(blacknumber);
						//通知适配器重新显示数据（此时，界面上的数据被刷新）
						adpater.notifyDataSetChanged();
					}
				}
			}
		});
		//窗体对话框中的“取消按钮”对应的点击事件
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		//创建并显示出窗体对话框
		builder.create().show();
	}
}
