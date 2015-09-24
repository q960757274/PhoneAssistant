package com.libowen.assistant;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.utils.AssetCopyUtil;

public class AtoolsActivity extends Activity implements OnClickListener {
	protected static final int COPY_SUCCESS = 30;
	protected static final int COPY_FAILED = 31;
	protected static final int COPY_COMMON_NUMBER_SUCCESS = 32;
	private TextView tv_atools_address_query;// 当点击该条目时，要执行拷贝号码归属地信息的数据库文件
	private TextView tv_atools_common_num;// 常用号码
	private TextView tv_atools_applock;//程序锁
	private ProgressDialog pd;// 拷贝数据库时要显示的进度条
	// 拷贝数据库是一个相对耗时的操作，拷贝完成后，给主线程发送消息
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// 无论拷贝是否成功，都需要关闭进度显示条
			pd.dismiss();
			switch (msg.what) {
			case COPY_SUCCESS:
				// 拷贝数据库成功后，进入号码归属地查询的界面
				loadQueryUI();
				break;
			case COPY_COMMON_NUMBER_SUCCESS:
				//拷贝数据库成功后，进入常用号码显示的界面
				loadCommNumUI();
				break;
			case COPY_FAILED:
				Toast.makeText(getApplicationContext(), "拷贝数据失败", 0).show();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.atools);// 高级工具对应的界面
		tv_atools_address_query = (TextView) findViewById(R.id.tv_atools_address_query);
		tv_atools_address_query.setOnClickListener(this);
		pd = new ProgressDialog(this);
		tv_atools_common_num = (TextView) findViewById(R.id.tv_atools_common_num);
		tv_atools_common_num.setOnClickListener(this);
		tv_atools_applock = (TextView)findViewById(R.id.tv_atools_applock);
		tv_atools_applock.setOnClickListener(this);
		// 设置进度条显示的风格
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_atools_address_query:// 号码归属地查询
			// 创建出数据库要拷贝到的系统文件：data\data\包名\files\address.db
			final File file = new File(getFilesDir(), "address.db");
			// 判断数据库是否存在，如果存在，则直接进入号码归属地的查询界面，否则，执行拷贝动作
			if (file.exists() && file.length() > 0) {
				// 数据库文件拷贝成功，进入查询号码归属地界面
				loadQueryUI();
			} else {
				// 数据库的拷贝.开始拷贝时需要开始显示进度条
				pd.show();
				// 拷贝数据库也是一个相对耗时的操作，在子线程中执行该操作
				new Thread() {
					public void run() {
						AssetCopyUtil asu = new AssetCopyUtil(
								getApplicationContext());
						// 返回拷贝成功与否的结果
						boolean result = asu.copyFile("naddress.db", file, pd);
						if (result) {// 拷贝成功
							Message msg = Message.obtain();
							msg.what = COPY_SUCCESS;
							handler.sendMessage(msg);
						} else {// 拷贝失败
							Message msg = Message.obtain();
							msg.what = COPY_FAILED;
							handler.sendMessage(msg);
						}
					};
				}.start();
			}
			break;
		case R.id.tv_atools_common_num:// 公用号码查询
			// 判读数据库是否已经拷贝到系统目录（ data/data/包名/files/address.db）
			final File commonnumberfile = new File(getFilesDir(),
					"commonnum.db");
			if (commonnumberfile.exists() && commonnumberfile.length() > 0) {
				loadCommNumUI();// 进入公共号码的显示界面
			} else {
				// 数据库的拷贝.
				pd.show();
				// 拷贝数据库是一个相对耗时的工作，我们为其开启一个子线程
				new Thread() {
					public void run() {
						// 将数据库拷贝到手机系统中
						AssetCopyUtil asu = new AssetCopyUtil(
								getApplicationContext());
						boolean result = asu.copyFile("commonnum.db",
								commonnumberfile, pd);
						if (result) {// 拷贝成功
							Message msg = Message.obtain();
							msg.what = COPY_COMMON_NUMBER_SUCCESS;
							handler.sendMessage(msg);
						} else {// 拷贝失败
							Message msg = Message.obtain();
							msg.what = COPY_FAILED;
							handler.sendMessage(msg);
						}
					};
				}.start();
			}
			break;
		case R.id.tv_atools_applock://程序锁
			Intent applockIntent = new Intent(this,AppLockActivity.class);
			startActivity(applockIntent);
			break;
		}
	}

	/**
	 * 进入常用号码界面
	 */
	private void loadCommNumUI() {
		Intent intent = new Intent(this, CommonNumActivity.class);
		startActivity(intent);
	}

	/**
	 * 进入到号码归属地查询界面
	 */
	private void loadQueryUI() {
		Intent intent = new Intent(this, NumberQueryActivity.class);
		startActivity(intent);
	}
}
