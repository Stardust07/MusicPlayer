package com.hustunique.musicplayer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PlayActivity extends Activity implements OnGestureListener,OnTouchListener{
	public static TextView title,artist,duration;
	public static TextView currentTime;
	static Lrc lrcView;
	public static SeekBar processSeekBar;
	private Button previous,next;
	public static Button playpause;
	private String ID = "ID";
	private String TITLE = "TITLE";
	private String ARTIST = "ARTIST";
	private String DURATION = "DURATION";
	private String SIZE = "SIZE";
	private String PATH = "PATH";
 	private String MSG = "MSG";
	private int PLAY = 0;
	private int PAUSE = 1;
	private int RESUME = 2;
	private int PREVIOUS = 3;
	private int NEXT = 4;
	static boolean find = false;
	private static int index = 0;
	public static final String CURRENT = "com.hustunique.action.CURRENT";  
    public static final String MUSIC_PROCESS = "com.hustunique.action.MUSIC_PROCESS";    
	public static int current = 0;
	private MyReceiver myReceiver;
	private GestureDetector gestureDetector;
	public static ArrayList<HashMap<String, String>> musicList = new ArrayList<HashMap<String, String>>();
    public static HashMap<Long, String> lrcMap = new HashMap<Long, String>();
    public static ArrayList<Long> time = new ArrayList<Long>();
	public static Notification notification;
	public static PendingIntent contentIntent;
	public static NotificationManager notificationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_layout);
		Intent intent = null;
		getMusicList();
        intent = new Intent(this.getIntent());
        intent.setClass(getApplicationContext(), PlayMusic.class);  
        startService(intent);       //启动服务  
        init();
        myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();  
        filter.addAction(CURRENT);  
        filter.addAction(MUSIC_PROCESS);  
        registerReceiver(myReceiver, filter);  
		notificationManager = (NotificationManager)
				getSystemService(android.content.Context.NOTIFICATION_SERVICE);  
		notification = new Notification(R.drawable.ic_launcher, "音乐播放中", System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中  
		notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用  
		Intent notificationIntent = new Intent(getApplicationContext(), PlayActivity.class); // 点击该通知后要跳转的Activity 
		contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);  
	}

        
	public static void setLrc(long process){
		if((process >= time.get(index) && (process<time.get(index+1)))){
			lrcView.postInvalidate();
			lrcView.index++;
			index++;
		}
	}
	
	public static void getLrc(String titleString){
		lrcMap.clear();
		time.clear();
		ArrayList<Long> timeTemp = new ArrayList<Long>();
		ArrayList<String> contentTemp = new ArrayList<String>();
		try {  
	          FileInputStream stream = new FileInputStream(
	        		  Environment.getExternalStorageDirectory().getAbsolutePath() + "/音乐/"+titleString+".lrc");
	          find = true;
	          InputStreamReader inputStreamReader = new InputStreamReader(stream);
	          BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	          String temp = null;
	          Pattern pattern = Pattern.compile(
	        		  "\\[\\s*[0-9]{1,2}\\s*:\\s*[0-5][0-9]\\s*[\\.:]?\\s*[0-9]?[0-9]?\\s*\\]");
	          String content = null;
	          temp = bufferedReader.readLine();
	          while ((temp = bufferedReader.readLine()) != null) {
	        	  Matcher matcher = pattern.matcher(temp);
	        	  while (matcher.find()) {
	        		  String timeString = matcher.group();
	        		  timeString = timeString.substring(1,9);
	        		  content = temp.substring(10);
	        		  if(!content.equals("")){
		        		  timeTemp.add(toMillseconds(timeString));
		        		  time.add(toMillseconds(timeString));
		        		  contentTemp.add(content);
		        	  }
	        	  }
	          }
	          stream.close();
		}catch (FileNotFoundException e) {  
			find = false;
			return;
        }  
        catch (IOException e) {
        	find = false;
        	return;
        }
		Collections.sort(time);
		Collections.sort(timeTemp);
		for (int i = 0; i < contentTemp.size(); i++) {
			lrcMap.put(timeTemp.get(i), contentTemp.get(i));
		}
	}

	private static long toMillseconds(String string) {
		string = string.replace(".", ":");
		String timeData[] = string.split(":");
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);
    	return (minute * 60 + second) * 1000 + millisecond * 10;
	}
	
	public void getMusicList() {  
			/*Projection: 指定查询数据库表中的哪几列，返回的游标中将包括相应的信息。Null则返回所有信息。
	        selection: 指定查询条件
	        selectionArgs：参数selection里有 ？这个符号是，这里可以以实际值代替这个问号。如果selection这个没有？的话，那么这个String数组可以为null。
	        SortOrder：指定查询结果的排列顺序*/
		    Cursor cursor = getContentResolver().query(  
		        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,  
		        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);	  
		    
		    for (int i = 0; i < cursor.getCount(); i++) {  
		        cursor.moveToNext();  
		        String id = cursor.getString(cursor  
		            .getColumnIndex(MediaStore.Audio.Media._ID));   //音乐id  
		        String title = cursor.getString((cursor   
		            .getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题  
		        String artist = cursor.getString(cursor  
		            .getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家  
		        long duration = cursor.getLong(cursor  
		            .getColumnIndex(MediaStore.Audio.Media.DURATION));//时长  
		        String size = cursor.getString(cursor  
		            .getColumnIndex(MediaStore.Audio.Media.SIZE));  //文件大小  
		        String url = cursor.getString(cursor  
		            .getColumnIndex(MediaStore.Audio.Media.DATA));              //文件路径  
		        int isMusic = cursor.getInt(cursor  
		        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐  
		    if (isMusic != 0) {     //只把音乐添加到集合当中  
		    	HashMap<String, String> musicInfo = new HashMap<String, String>();
		    	musicInfo.put(ID, id);
		    	musicInfo.put(TITLE, title);
		    	musicInfo.put(ARTIST, artist);
		    	musicInfo.put(DURATION, formatTime(duration));
		    	musicInfo.put(SIZE, size);
		    	musicInfo.put(PATH, url);
		    	musicList.add(musicInfo);  
		        }  
		    }  
		}
	  
	  public static String formatTime(long time) {  
	        String min = time / (1000 * 60) + "";  
	        String sec = time % (1000 * 60) + "";  
	        if (min.length() < 2) {  
	            min = "0" + time / (1000 * 60) + "";  
	        } else {  
	            min = time / (1000 * 60) + "";  
	        }  
	        if (sec.length() == 4) {  
	            sec = "0" + (time % (1000 * 60)) + "";  
	        } else if (sec.length() == 3) {  
	            sec = "00" + (time % (1000 * 60)) + "";  
	        } else if (sec.length() == 2) {  
	            sec = "000" + (time % (1000 * 60)) + "";  
	        } else if (sec.length() == 1) {  
	            sec = "0000" + (time % (1000 * 60)) + "";  
	        }  
	        return min + ":" + sec.trim().substring(0, 2);  
	    }  
	  
	protected void init() {
		gestureDetector = new GestureDetector(getApplicationContext(),(OnGestureListener)this); 
		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.l);
		linearLayout.setOnTouchListener(this);
		linearLayout.setLongClickable(true); 
		title = (TextView)findViewById(R.id.titleshow);
		title.setText(musicList.get(current).get("TITLE"));
		artist = (TextView)findViewById(R.id.artistshow);
		artist.setText(musicList.get(current).get("ARTIST"));
		duration = (TextView)findViewById(R.id.durationshow);
		duration.setText(musicList.get(current).get("DURATION"));
		lrcView=(Lrc)findViewById(R.id.lrcView);
		currentTime = (TextView)findViewById(R.id.currenttime);
		processSeekBar = (SeekBar)findViewById(R.id.process);
		processSeekBar.setProgress(PlayMusic.mediaPlayer.getCurrentPosition());
		processSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				if(arg2){
					PlayMusic.mediaPlayer.seekTo(arg1);
				}
			}
		});

		 previous = (Button)findViewById(R.id.previous);
		 previous.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				previous();
				previous.setBackgroundResource(R.drawable.previous_glow);
				previous.setBackgroundResource(R.drawable.previous_music_selector);
			}
		});
		 playpause = (Button)findViewById(R.id.play);
		 if(PlayMusic.isPlay == true){
			 playpause.setBackgroundResource(R.drawable.pause);
		 }
		 playpause.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if((PlayMusic.isPlay == false) && (PlayMusic.isPause == false)){
					play();
				}
				else if(PlayMusic.isPause == true){
					resume();
				}
				else {
					pause();
				}
			}
		});
		 next = (Button)findViewById(R.id.next);
		 next.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					next();
					next.setBackgroundResource(R.drawable.next_glow);
					next.setBackgroundResource(R.drawable.next_music_selector);
				}
			});
	}
	
	 /** 
     * 下一首歌曲 
     */  
    public void next() {  
        if(current+1 < musicList.size()) {  
            Intent intent = new Intent(); 
            intent.putExtra(ID, current);
            intent.putExtra(MSG, NEXT);  
            playpause.setBackgroundResource(R.drawable.pause);
            List.playpause.setBackgroundResource(R.drawable.pause);
            intent.setClass(getApplicationContext(), PlayMusic.class);  
            startService(intent);       //启动服务  
        }else {  
            Toast.makeText(getApplicationContext(), "没有下一首了", Toast.LENGTH_SHORT).show();  
        }  
    }  
  
    /** 
     * 上一首歌曲 
     */  
    protected void previous() {  
        if(current-1 >= 0) {  
            Intent intent = new Intent();  
            intent.putExtra(ID, current);
            intent.putExtra(MSG, PREVIOUS);  
            playpause.setBackgroundResource(R.drawable.pause);
            List.playpause.setBackgroundResource(R.drawable.pause);
            intent.setClass(getApplicationContext(), PlayMusic.class);  
            startService(intent);       //启动服务  
        }else {  
            Toast.makeText(getApplicationContext(), "没有上一首了", Toast.LENGTH_SHORT).show();  
        }  
    }  
    
    /**
     * 播放
     * */ 
    protected void play() {  
    	Intent intent = new Intent();  
        intent.putExtra(ID, current);
        intent.putExtra(MSG, PLAY);  
        intent.setClass(getApplicationContext(), PlayMusic.class);  
        startService(intent);       //启动服务  
    } 
    
    /**
     * 暂停
     * */ 
    protected void pause() {  
    	Intent intent = new Intent();  
        intent.putExtra(MSG, PAUSE);  
        intent.setClass(getApplicationContext(), PlayMusic.class);
        startService(intent);       //启动服务  
    } 
    
    protected void resume() {  
    	Intent intent = new Intent();  
        intent.putExtra(MSG, RESUME);  
        intent.setClass(getApplicationContext(), PlayMusic.class);
        startService(intent);       //启动服务  
    } 
    
	private class MyBinder extends Binder{
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
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
									SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
									Editor editor =sp.edit();
									editor.putInt(ID, current);
									editor.commit();
									Intent intent = new Intent(
											getApplicationContext(), PlayMusic.class);
									stopService(intent); // 停止后台服务
									unregisterReceiver(myReceiver);
									notificationManager.cancelAll();
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
		else {
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return gestureDetector.onTouchEvent(arg1); 
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		if(arg1.getX() - arg0.getX() > 20 && Math.abs(arg2) > 0)  
		{  
			Intent intent = new Intent(getApplicationContext(),List.class);  
			startActivity(intent);  
			finish();
		}  
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
