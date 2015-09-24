package com.libowen.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.guoshisp.mobilesafe.R;
import com.libowen.assistant.db.dao.NumberAddressDao;

public class NumberQueryActivity extends Activity {
	private EditText et_number_query;// 输入要查询的号码
	private TextView tv_number_address;// 显示号码归属地位置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.number_query);
		super.onCreate(savedInstanceState);
		et_number_query = (EditText) findViewById(R.id.et_number_query);
		tv_number_address = (TextView) findViewById(R.id.tv_number_address);
	}

	/**
	 * 点击“查询”时执行的监听方法
	 * 
	 * @param view
	 */
	public void query(View view) {
		// 查询前，需要将号码前后的空格清空掉
		String number = et_number_query.getText().toString().trim();
		// 判断要查询的号码是否为空
		if (TextUtils.isEmpty(number)) {
			Toast.makeText(this, "号码不能为空", 1).show();
			// 使用动画工具来加载一个动画资源一个动画资源
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			// 当号码输入框中没有输入号码而点击“查询”时播放一个动画，用来提示用户输入号码后才可以执行查询操作。
			et_number_query.startAnimation(shake);
			return;
		} else {// 号码不为空时要返回归属地信息
				// 返回查询到的归属地信息
			String address = NumberAddressDao.getAddress(number);
			// 将归属地信息显示在屏幕上
			tv_number_address.setText(address);
		}
	}
}
