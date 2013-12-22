package com.hustunique.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
	boolean first = true;
	public static final int NOTIFY_1 = 0x1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
		first = sp.getBoolean("isfirst", true);
		if (first == true) {
			setContentView(R.layout.activity_main);
			SharedPreferences.Editor e = sp.edit();
			e.putBoolean("isfirst", false);
			e.commit();
		} 
		else {
			startActivity(new Intent(getApplicationContext(),List.class));
			finish();
		}
	}

	public  void begin(View v) {
		// TODO Auto-generated method stub
		startActivity(new Intent(getApplicationContext(),List.class));
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.action_exit) {
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle("退出")
					.setMessage("您确定要退出？")
					.setNegativeButton("取消", null)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									Intent intent = new Intent(
											MainActivity.this, PlayMusic.class);

									stopService(intent); // 停止后台服务
									new Thread(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub
											try {
												Thread.sleep(1000);
											} catch (Exception e) {
												// TODO: handle exception
											}
										}
									}).start();

									finish();
								}
							}).show();
		}
		return super.onOptionsItemSelected(item);
	}
}
