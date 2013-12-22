package com.hustunique.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.hustunique.musicplayer.MyReceiver;
public class SettingsActivity extends Activity {
	private RadioGroup modeGroup;
	private RadioButton repeat_one,repeat_all,repeat_none,shuffleMusic;
	public static final String MODE = "com.hustunique.action.MODE";   
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		modeGroup = (RadioGroup)findViewById(R.id.mode);
		repeat_one = (RadioButton)findViewById(R.id.repeat_one);
		repeat_all = (RadioButton)findViewById(R.id.repeat_all);
		shuffleMusic = (RadioButton)findViewById(R.id.shuffleMusic);
		repeat_none = (RadioButton)findViewById(R.id.repeat_none);
		modeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(repeat_one.isChecked()){
					Intent intent = new Intent(MODE);
					intent.putExtra("MODE", 1);
					sendBroadcast(intent);
					finish();
				}else if(repeat_all.isChecked()){
					Intent intent = new Intent(MODE);
					intent.putExtra("MODE", 2);
					sendBroadcast(intent);
					finish();
				}else if(repeat_none.isChecked()){
					Intent intent = new Intent(MODE);
					intent.putExtra("MODE", 3);
					sendBroadcast(intent);
					finish();
				}else if(shuffleMusic.isChecked()){
					Intent intent = new Intent(MODE);
					intent.putExtra("MODE", 4);
					sendBroadcast(intent);
					finish();
				}
			}
		});
	}
}
