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
	public static MediaPlayer mediaPlayer =  new MediaPlayer();       //ý�岥��������  
	public static String path;                        //�����ļ�·��  
    public static String title;                       //������  
    public static String artist;                      //������ 
    public static String duration;                    //���ų���  
    public static boolean isPause;                    //��ͣ״̬  
    public static boolean isPlay;                     //��ͣ״̬  
    public static Context context = null;
    public static LocalBroadcastManager localBroadcastManager = null;
    public static int msg;  
    public static int current;        // ��¼��ǰ���ڲ��ŵ�����  
    public static int mode = 3;         //����״̬��Ĭ��Ϊ˳�򲥷�  
    public static int currentTime;        //��ǰ���Ž���  
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
                    currentTime = mediaPlayer.getCurrentPosition(); // ��ȡ��ǰ���ֲ��ŵ�λ��  
                    Intent intent = new Intent(MUSIC_PROCESS);  
                    intent.putExtra("currentTime", currentTime);  
                    sendBroadcast(intent); // ��PlayerActivity���͹㲥  
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
	                    if(current > size - 1) {  //��Ϊ��һ�׵�λ�ü�������  
	                        current = 0;  
	                    }  
	                    Intent sendIntent = new Intent(CURRENT);  
	                    sendIntent.putExtra("current", current);  
	                    // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
	                    sendBroadcast(sendIntent);  
	                    Intent changeIntent = new Intent("CHANGE");
	         			sendBroadcast(changeIntent);  
	                    path = musicList.get(current).get(PATH);  
	                    play(0);   
						break;
					case 3:
						current++;  //��һ��λ��  
	                    if (current <= size - 1) {  
	                    Intent sendIntent1 = new Intent(CURRENT);  
	                    sendIntent1.putExtra("current", current);  
	                    // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
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
	                        // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
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
	                    // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
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
	    	current = intent.getIntExtra(ID, -1);      //��ǰ���Ÿ�����λ��  
	        msg = intent.getIntExtra(MSG, -1);         //������Ϣ  
	        if (msg == PLAY) {    //ֱ�Ӳ�������  
	        	PlayActivity.playpause.setBackgroundResource(R.drawable.pause);
	            List.playpause.setBackgroundResource(R.drawable.pause);
	            play(0);  
	        } else if (msg == PAUSE) {    //��ͣ  
	        	pause();    
	            PlayActivity.playpause.setBackgroundResource(R.drawable.play);
	            List.playpause.setBackgroundResource(R.drawable.play);
	        } else if (msg == RESUME) {    //��������  
	            resume();     
	            PlayActivity.playpause.setBackgroundResource(R.drawable.pause);
	            List.playpause.setBackgroundResource(R.drawable.pause);
	        } else if (msg == PREVIOUS) {    //��һ��  
	            previous();  
	        } else if (msg == NEXT) {    //��һ��  
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
	        	// ���͹㲥������Activity����е�BroadcastReceiver���յ�  
	        	sendBroadcast(sendIntent);  
	        	path = musicList.get(current).get(PATH);
	            mediaPlayer.reset();// �Ѹ�������ָ�����ʼ״̬  
	            mediaPlayer.setDataSource(path);  
	            mediaPlayer.prepare(); // ���л���  
	            isPlay = true;
	            isPause = false;
	            mediaPlayer.start();
	            handler.sendEmptyMessage(1);  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	    }
	    
	    /** 
	     * ��ͣ���� 
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
	     * ��һ�� 
	     */  
	    private void previous() {  
	    	current--;
	        Intent sendIntent = new Intent(CURRENT);  
	        sendIntent.putExtra("current", current);  
	        // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
	        sendBroadcast(sendIntent);  
	        Intent changeIntent = new Intent("CHANGE");
 			sendBroadcast(changeIntent);  
	        play(0);
	    }  
	  
	    /** 
	     * ��һ�� 
	     */  
	    private void next() {  
	    	current++;
	        Intent sendIntent = new Intent(CURRENT);  
	        sendIntent.putExtra("current", current);  
	        // ���͹㲥������Activity����е�BroadcastReceiver���յ�  
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
	     * ��ȡ���λ�� 
	     * @param end 
	     * @return 
	     */  
	    protected int getRandomIndex(int MAX) {  
	        int index = (int) (Math.random() * MAX);  
	        return index;  
	    }  
	    
	    protected void getMusicList() {  
			/*Projection: ָ����ѯ���ݿ���е��ļ��У����ص��α��н�������Ӧ����Ϣ��Null�򷵻�������Ϣ��
	        selection: ָ����ѯ����
	        selectionArgs������selection���� ����������ǣ����������ʵ��ֵ��������ʺš����selection���û�У��Ļ�����ô���String�������Ϊnull��
	        SortOrder��ָ����ѯ���������˳��*/
		    Cursor cursor = getContentResolver().query(  
		        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,  
		        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);	  
		    
		    for (int i = 0; i < cursor.getCount(); i++) {  
		        cursor.moveToNext();  
		        String id = cursor.getString(cursor  
		            .getColumnIndex(MediaStore.Audio.Media._ID));   //����id  
		        String title = cursor.getString((cursor   
		            .getColumnIndex(MediaStore.Audio.Media.TITLE)));//���ֱ���  
		        String artist = cursor.getString(cursor  
		            .getColumnIndex(MediaStore.Audio.Media.ARTIST));//������  
		        long duration = cursor.getLong(cursor  
		            .getColumnIndex(MediaStore.Audio.Media.DURATION));//ʱ��  
		        String url = cursor.getString(cursor  
		            .getColumnIndex(MediaStore.Audio.Media.DATA));              //�ļ�·��  
		        int isMusic = cursor.getInt(cursor  
		        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//�Ƿ�Ϊ����  
		    if (isMusic != 0) {     //ֻ��������ӵ����ϵ���  
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
