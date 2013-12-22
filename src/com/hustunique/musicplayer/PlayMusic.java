package com.hustunique.musicplayer;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;

@SuppressLint("HandlerLeak")
public class PlayMusic extends Service {
	public static MediaPlayer mediaPlayer =  new MediaPlayer();       //媒体播放器对象  
	public static String path;                        //音乐文件路径  
    public static String title;                       //歌曲名  
    public static String artist;                      //艺术家 
    public static String duration;                    //播放长度  
    public static boolean isPause;                    //暂停状态  
    public static boolean isPlay;                     //暂停状态  
    public static Context context = null;
    public static LocalBroadcastManager localBroadcastManager = null;
    public static int msg;  
    public static int current;        // 记录当前正在播放的音乐  
    public static int mode = 3;         //播放状态，默认为顺序播放  
    public static int currentTime;        //当前播放进度  
    private String ID = "ID";
	private String TITLE = "TITLE";
	private String ARTIST = "ARTIST";
	private String DURATION = "DURATION";
	private String PATH = "PATH";
	private String MSG = "MSG";
	private int PLAY = 0;
	private int PAUSE = 1;
	private int RESUME = 2;
	private int PREVIOUS = 3;
	private int NEXT = 4;
	private MyReceiver myReceiver;
	private WidgetReceiver widgetReceiver;
	private ArrayList<HashMap<String, String>> musicList = new ArrayList<HashMap<String,String>>();
	private int size;
	
	public static final String CURRENT = "com.hustunique.action.CURRENT";   
    public static final String MUSIC_PROCESS = "com.hustunique.action.MUSIC_PROCESS";  
    public static final String MODE = "com.hustunique.action.MODE";  
    private String ACTION_START = "com.hustunique.action.START";
	private String ACTION_PAUSE = "com.hustunique.action.PAUSE";
	private String ACTION_NEXT = "com.hustunique.action.NEXT";
	private String ACTION_PREVIOUS = "com.hustunique.action.PREVIOUS"; 
	private static String ACTION_RESUME = "com.hustunique.action.RESUME"; 
    
	public PlayMusic() {
	}
	
	private Handler handler = new Handler() {  
		public void handleMessage(android.os.Message msg) {  
                if(mediaPlayer != null) {  
                    currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置  
                    Intent intent = new Intent(MUSIC_PROCESS);  
                    intent.putExtra("currentTime", currentTime);  
                    sendBroadcast(intent); // 给PlayerActivity发送广播  
                    handler.sendEmptyMessageDelayed(1, 1000);  
                }  
            }  
        };  

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	 @Override  
	    public void onCreate() {  
	        super.onCreate();
	        getMusicList();
	        size = musicList.size();
	        myReceiver = new MyReceiver();  
	        widgetReceiver = new WidgetReceiver();
	        IntentFilter filter = new IntentFilter();  
	        filter.addAction(MODE); 
	        filter.addAction(CURRENT);  
	        filter.addAction(MUSIC_PROCESS); 
	        registerReceiver(myReceiver, filter);  
	        IntentFilter widgetfilter = new IntentFilter(); 
	        widgetfilter.addAction(ACTION_START);
	        widgetfilter.addAction(ACTION_PAUSE);
	        widgetfilter.addAction(ACTION_NEXT);
	        widgetfilter.addAction(ACTION_PREVIOUS);
	        widgetfilter.addAction(ACTION_RESUME);
	        registerReceiver(widgetReceiver, widgetfilter);  
			
	        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					switch (mode) {
					case 1:
						mediaPlayer.setLooping(true);
						play(0);
						break;
					case 2:
						current++;  
	                    if(current > size - 1) {  //变为第一首的位置继续播放  
	                        current = 0;  
	                    }  
	                    Intent sendIntent = new Intent(CURRENT);  
	                    sendIntent.putExtra("current", current);  
	                    // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
	                    sendBroadcast(sendIntent);  
	                    Intent changeIntent = new Intent("CHANGE");
	         			sendBroadcast(changeIntent);  
	                    path = musicList.get(current).get(PATH);  
	                    play(0);   
						break;
					case 3:
						current++;  //下一首位置  
	                    if (current <= size - 1) {  
	                    Intent sendIntent1 = new Intent(CURRENT);  
	                    sendIntent1.putExtra("current", current);  
	                    // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
	                    sendBroadcast(sendIntent1);  
	                    Intent changeIntent2 = new Intent("CHANGE");
	         			sendBroadcast(changeIntent2);  
	                    path = musicList.get(current).get(PATH);   
	                    play(0);
	                    }
	                    else {  
	                        mediaPlayer.seekTo(0);  
	                        current = 0;  
	                        Intent sendIntent1 = new Intent(CURRENT);  
	                        sendIntent1.putExtra("current", current);  
	                        // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
	                        Intent changeIntent3 = new Intent("CHANGE");
		         			sendBroadcast(changeIntent3);  
	                        path = musicList.get(current).get(PATH);
	                        sendBroadcast(sendIntent1);  
	                    }  
						break;
					case 4:
						current = getRandomIndex(size - 1);  
	                    Intent sendIntent2 = new Intent(CURRENT);  
	                    sendIntent2.putExtra("current", current);  
	                    // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
	                    sendBroadcast(sendIntent2);  
	                    path = musicList.get(current).get(PATH); 
	                    play(0);
						break;
					default:
						break;
					}
				}
	        });
	    }  
	      
	    @Override  
	    public void onStart(Intent intent,int startId) {  
	    	current = intent.getIntExtra(ID, -1);      //当前播放歌曲的位置  
	        msg = intent.getIntExtra(MSG, -1);         //播放信息  
	        if (msg == PLAY) {    //直接播放音乐  
	        	PlayActivity.playpause.setBackgroundResource(R.drawable.pause);
	            List.playpause.setBackgroundResource(R.drawable.pause);
	            play(0);  
	        } else if (msg == PAUSE) {    //暂停  
	        	pause();    
	            PlayActivity.playpause.setBackgroundResource(R.drawable.play);
	            List.playpause.setBackgroundResource(R.drawable.play);
	        } else if (msg == RESUME) {    //继续播放  
	            resume();     
	            PlayActivity.playpause.setBackgroundResource(R.drawable.pause);
	            List.playpause.setBackgroundResource(R.drawable.pause);
	        } else if (msg == PREVIOUS) {    //上一首  
	            previous();  
	        } else if (msg == NEXT) {    //下一首  
	            next();  
	        } else if (msg == -1) {  
	            handler.sendEmptyMessage(1);  
	        }  
	        
	    }  
	    
	    private void play(int currentTime) {  
	        try {  
	        	Intent sendIntent = new Intent(CURRENT);  
	        	sendIntent.putExtra("current", current);  
	        	Intent changeIntent = new Intent("CHANGE");
	        	sendBroadcast(changeIntent);
	        	// 发送广播，将被Activity组件中的BroadcastReceiver接收到  
	        	sendBroadcast(sendIntent);  
	        	path = musicList.get(current).get(PATH);
	            mediaPlayer.reset();// 把各项参数恢复到初始状态  
	            mediaPlayer.setDataSource(path);  
	            mediaPlayer.prepare(); // 进行缓冲  
	            isPlay = true;
	            isPause = false;
	            mediaPlayer.start();
	            handler.sendEmptyMessage(1);  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	    }
	    
	    /** 
	     * 暂停音乐 
	     */  
	    private void pause() {  
	        if (mediaPlayer != null && mediaPlayer.isPlaying()) {  
	        	currentTime = mediaPlayer.getCurrentPosition();
	            mediaPlayer.pause();  
	            isPause = true; 
	            isPlay = false;
	        }  
	    }   
	    
	    private void resume() {  
	        if (isPause) {  
	            mediaPlayer.seekTo(currentTime);  
	            mediaPlayer.start();
	            isPause = false;  
	            isPlay = true;
	        }  
	    }  
	      
	    /** 
	     * 上一首 
	     */  
	    private void previous() {  
	    	current--;
	        Intent sendIntent = new Intent(CURRENT);  
	        sendIntent.putExtra("current", current);  
	        // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
	        sendBroadcast(sendIntent);  
	        Intent changeIntent = new Intent("CHANGE");
 			sendBroadcast(changeIntent);  
	        play(0);
	    }  
	  
	    /** 
	     * 下一首 
	     */  
	    private void next() {  
	    	current++;
	        Intent sendIntent = new Intent(CURRENT);  
	        sendIntent.putExtra("current", current);  
	        // 发送广播，将被Activity组件中的BroadcastReceiver接收到  
	        sendBroadcast(sendIntent); 
	        Intent changeIntent = new Intent("CHANGE");
 			sendBroadcast(changeIntent);  
	        play(0);
	    }  
	    
	    @Override  
	    public void onDestroy() {  
	        super.onDestroy();
	        if(mediaPlayer != null){  
	            mediaPlayer.stop();  
	            mediaPlayer.release();  
	            mediaPlayer = null;
	        }  
	        unregisterReceiver(myReceiver);  
	    }  
	      
	    @Override  
	    public boolean onUnbind(Intent intent) {  
	        return super.onUnbind(intent);  
	    }  
	      
	    public class MyBinder extends Binder{  
	        PlayMusic getService()  
	        {  
	            return PlayMusic.this;  
	        }  
	    }  
	    /** 
	     * 获取随机位置 
	     * @param end 
	     * @return 
	     */  
	    protected int getRandomIndex(int MAX) {  
	        int index = (int) (Math.random() * MAX);  
	        return index;  
	    }  
	    
	    protected void getMusicList() {  
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
	    
	    protected class WidgetReceiver extends BroadcastReceiver{

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				String action = arg1.getAction();
				if(action.equals(ACTION_START)){
					play(0);
				}
				else if(action.equals(ACTION_PAUSE)){
					pause();
				}
				else if(action.equals(ACTION_NEXT)){
					next();
				}
				else if(action.equals(ACTION_PREVIOUS)){
					previous();
				}
				else if(action.equals(ACTION_RESUME)){
					resume();
				}
			}
	    }
}
