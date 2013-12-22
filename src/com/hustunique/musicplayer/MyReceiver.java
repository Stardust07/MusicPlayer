package com.hustunique.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver{
	public static final String MODE = "com.hustunique.action.MODE";  
	public static final String CURRENT = "com.hustunique.action.CURRENT"; 
    public static final String MUSIC_PROCESS = "com.hustunique.action.MUSIC_PROCESS"; 
    private int current;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if(action.equals(MODE)){
			PlayMusic.mode = intent.getIntExtra("MODE", 3);  
		}
		else if(action.equals(MUSIC_PROCESS)){
			long process = intent.getIntExtra("currentTime",0);
			PlayActivity.currentTime.setText(PlayActivity.formatTime(process));
			PlayActivity.processSeekBar.setProgress(intent.getIntExtra("currentTime",0));
			if(PlayActivity.find == true){
				PlayActivity.setLrc(process);
			}
		}
		else if(action.equals(CURRENT)){
			current = intent.getIntExtra("current",0);
 			PlayMusic.current = current;
 			PlayActivity.current = current;
 			List.current = current;
 			NewAppWidget.current = current;
 			PlayActivity.title.setText(List.musicList.get(current).get("TITLE"));
			PlayActivity.artist.setText(List.musicList.get(current).get("ARTIST"));
			PlayActivity.duration.setText(List.musicList.get(current).get("DURATION"));
			PlayActivity.processSeekBar.setMax(PlayMusic.mediaPlayer.getDuration());
			PlayActivity.notification.setLatestEventInfo(context, List.musicList.get(current).get("TITLE"), 
					List.musicList.get(current).get("ARTIST"), PlayActivity.contentIntent);
			PlayActivity.notificationManager.notify(0, PlayActivity.notification);
			PlayActivity.getLrc(List.musicList.get(current).get("TITLE"));
		}	
	}
}
