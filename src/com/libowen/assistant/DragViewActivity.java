package com.libowen.assistant;

import com.guoshisp.mobilesafe.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class DragViewActivity extends Activity {
	protected static final String TAG = "DragViewActivity";
	private ImageView iv_drag_view;// 要移动的View
	private TextView tv_drag_view;// 提示框
	private int windowHeight;// 定义屏幕的高度
	private int windowWidth;// 定义屏幕的宽度
	private SharedPreferences sp;// 用于存储View的位置信息
	private long firstclicktime;//记录“双击居中”时的第一次点击时间，记录的原因在于判断是否属于双击事件
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drag_view);
		iv_drag_view = (ImageView) findViewById(R.id.iv_drag_view);
		tv_drag_view = (TextView) findViewById(R.id.tv_drag_view);
		windowHeight = getWindowManager().getDefaultDisplay().getHeight();
		windowWidth = getWindowManager().getDefaultDisplay().getWidth();
		sp = getSharedPreferences("config", MODE_PRIVATE);
		// 初始化上次移动后的View的显示位置。注意:onCreate方法初始化界面的时候，是在第一个阶段，该阶段用来测量控件的大小和位置
		RelativeLayout.LayoutParams params = (LayoutParams) iv_drag_view
				.getLayoutParams();
		params.leftMargin = sp.getInt("lastx", 0);// 获取到被移动的View离窗体左端的X值
		params.topMargin = sp.getInt("lasty", 0);// 获取到被移动的View离窗体顶端的Y值
		iv_drag_view.setLayoutParams(params);
		//处理View双击居中的点击事件
		iv_drag_view.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Log.i(TAG, "我被点击啦.......................");
				//判断是第一次点击 还是第二次点击.
				if (firstclicktime > 0) {// 第二次的点击事件。因为firstclicktime是一个成员变量，默认值为0
					long secondclickTime = System.currentTimeMillis();
					if (secondclickTime - firstclicktime < 500) {//设定双击的阀值为0.5秒
						Log.i(TAG, "双击啦.......................");
						//双击后，需要将第一次的点击时间设置为0，以便下次点击。
						firstclicktime = 0;
						//计算出View的宽度
						int right = iv_drag_view.getRight();
						int left = iv_drag_view.getLeft();
						int iv_width = right - left;//计算出View的长度
						//计算出View在窗体正中央时的View左端和离窗体左边边框的距离和View右端和离窗体右边边框的距离
						int iv_left = windowWidth / 2 - iv_width / 2;
						int iv_right = windowWidth / 2 + iv_width / 2;
						// 将View显示到界面的最中央.
						iv_drag_view.layout(iv_left, iv_drag_view.getTop(),
								iv_right, iv_drag_view.getBottom());
						//将View在中央显示的位置数据存入sp中
						Editor editor = sp.edit();
						int lasty = iv_drag_view.getTop();
						int lastx = iv_drag_view.getLeft();
						editor.putInt("lastx", lastx);
						editor.putInt("lasty", lasty);
						editor.commit();

					}
				}
				firstclicktime = System.currentTimeMillis();
				//解决用户的奇怪操作：单击一下停留较长，然后双击
				new Thread() {
					public void run() {
						try {
							Thread.sleep(500);
							firstclicktime = 0;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					};
				}.start();
			}
		});
		// 为View注册一个被触摸事件的监听器
		iv_drag_view.setOnTouchListener(new OnTouchListener() {
			// 记录起始触摸点的坐标
			int startx;// 记录起始时的X坐标
			int starty;// 记录起始时的Y坐标

			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// 手指第一次接触屏幕
					Log.i(TAG, "摸到");
					startx = (int) event.getRawX();// 获取到手指触摸点的X坐标
					starty = (int) event.getRawY();// 获取到手指触摸点的Y坐标

					break;

				case MotionEvent.ACTION_MOVE:// 手指在屏幕上移动
					int x = (int) event.getRawX();// 获取到当前手指触摸点的X坐标
					int y = (int) event.getRawY();// 获取到当前手指触摸点的Y坐标
					// 获取提示框的高度
					int tv_height = tv_drag_view.getBottom()
							- tv_drag_view.getTop();
					// 判断View是处于窗体的上方还是下方
					if (y > (windowHeight / 2)) {// 手指移动到了窗体的下一半
						// 将提示框移动到窗体的上半部分。四个参数分别为：提示框距离窗体的左、上、右、下端的距离。
						tv_drag_view.layout(tv_drag_view.getLeft(), 60,
								tv_drag_view.getRight(), 60 + tv_height);
					} else {// 手指移动到了窗体的上一半.
							// 将提示框移动到窗体的下半部分
						tv_drag_view.layout(tv_drag_view.getLeft(),
								windowHeight - 20 - tv_height,
								tv_drag_view.getRight(), windowHeight - 20);
					}

					int dx = x - startx;// 计算出View在屏幕X轴方向上被移动的距离
					int dy = y - starty;// 计算出View在屏幕Y轴方向上被移动的距离
					// 计算出被拖动的View距离窗体上、下、左、右的距离
					int t = iv_drag_view.getTop();
					int b = iv_drag_view.getBottom();
					int l = iv_drag_view.getLeft();
					int r = iv_drag_view.getRight();
					// 获取到移动后的View的在窗体中的位置
					int newl = l + dx;
					int newt = t + dy;
					int newr = r + dx;
					int newb = b + dy;
					// 通过对移动刚结束的View距离手机屏幕的四个边框的大小的判断，来避免View被移出屏幕
					if (newl < 0 || newt < 0 || newr > windowWidth
							|| newb > windowHeight) {
						break;
					}
					// 将移动后的View在窗体上重新的显示出来
					iv_drag_view.layout(newl, newt, newr, newb);
					// 立即更新手指第一次触摸屏幕的位置坐标，以便下次继续移动
					startx = (int) event.getRawX();
					starty = (int) event.getRawY();
					Log.i(TAG, "移动");
					break;
				case MotionEvent.ACTION_UP:// 手指离开屏幕
					Log.i(TAG, "松手");
					// 记录当前imageview在窗体中的位置（左上角的顶点距离屏幕的宽度和高度）
					Editor editor = sp.edit();
					int lasty = iv_drag_view.getTop();
					int lastx = iv_drag_view.getLeft();
					editor.putInt("lastx", lastx);
					editor.putInt("lasty", lasty);
					editor.commit();
					break;
				}
				// true 会消费调当前的触摸事件，那么后面的移动和离开事件会被响应到
				// false 不会消费当前的触摸事件，那么后面的移动和离开事件都不会被响应到
				return false;
			}
		});
	}
}