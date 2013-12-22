package com.hustunique.musicplayer;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class List extends Activity implements OnGestureListener,OnTouchListener{
	private String ID = "ID";
	private String TITLE = "TITLE";
	private String ARTIST = "ARTIST";
	private String DURATION = "DURATION";
	private String SIZE = "SIZE";
	private String PATH = "PATH";
	private String MSG = "MSG";
	private GestureDetector gestureDetector;
	private Button previous;
	public static Button playpause;
	private Button next;
	private int PLAY = 0;
	private int PAUSE = 1;
	private int RESUME = 2;
	private int PREVIOUS = 3;
	private int NEXT = 4;
	public static int current = -1;
	public static ArrayList<HashMap<String, String>> musicList = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.musiclist);
		getMusicList();
		loadList();
		init();
	}
	
	protected void loadList(){
		ListView listView = (ListView)findViewById(R.id.musiclist);
		SimpleAdapter sim = new SimpleAdapter(this, musicList,
				R.layout.list_item, new String[] {
						TITLE, ARTIST,DURATION}, new int[] { R.id.title,
						R.id.artist, R.id.duration });
		listView.setAdapter(sim);
		listView.setOnItemClickListener(new MusicListItemClickListener());  
	}

	public void getMusicList() {  
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
	        String size = cursor.getString(cursor  
	            .getColumnIndex(MediaStore.Audio.Media.SIZE));  //�ļ���С  
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
	    	musicInfo.put(SIZE, size);
	    	musicInfo.put(PATH, url);
	    	musicList.add(musicInfo);  
	        }  
	    }  
	}
	
	 private class MusicListItemClickListener implements OnItemClickListener {  

	        @Override  
	        public void onItemClick(AdapterView<?> parent, View view, int position,  
	                long id) {  
	        	    current = position;
	        	    findViewById(R.id.play).setBackgroundResource(R.drawable.pause);
	                Intent intent = new Intent();  
	                intent.putExtra(PATH, musicList.get(position).get(PATH));   
	                intent.putExtra(ID, current);
	                intent.putExtra(DURATION, musicList.get(position).get(DURATION)); 
	                intent.putExtra(TITLE, musicList.get(position).get(TITLE)); 
	                intent.putExtra(ARTIST, musicList.get(position).get(ARTIST)); 
	                intent.putExtra(MSG, PLAY);  
	                intent.setClass(getApplicationContext(), PlayActivity.class);
	                startActivity(intent);
	                finish();
	            }  
	    }
	 
	 protected void init() {
		 gestureDetector = new GestureDetector(getApplicationContext(),(OnGestureListener)this); 
		 LinearLayout linearLayout = (LinearLayout)findViewById(R.id.listlayout);
     	 linearLayout.setOnTouchListener(this);
     	 linearLayout.setLongClickable(true); 
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
	     * ��һ�׸��� 
	     */  
	    public void next() {  
	    	 
	        if(current+1 < musicList.size()) {  
	            Intent intent = new Intent(); 
	            intent.putExtra(ID, current);
                intent.putExtra(MSG, NEXT);  
                intent.setClass(getApplicationContext(), PlayMusic.class);  
                startService(intent);       //��������  
	        }else {  
	            Toast.makeText(getApplicationContext(), "û����һ����", Toast.LENGTH_SHORT).show();  
	        }  
	    }  
	  
	    /** 
	     * ��һ�׸��� 
	     */  
	    protected void previous() {  
	        if(current-1 >= 0) {  
	            Intent intent = new Intent();  
	            intent.putExtra(ID, current);
                intent.putExtra(MSG, PREVIOUS);  
                intent.setClass(getApplicationContext(), PlayMusic.class);  
                startService(intent);       //��������  
	        }else {  
	            Toast.makeText(getApplicationContext(), "û����һ����", Toast.LENGTH_SHORT).show();  
	        }  
	    }  
	    
	    /**
	     * ����
	     * */ 
	    protected void play() {  
	    	Intent intent = new Intent();  
            intent.putExtra(ID, current);
            intent.putExtra(MSG, PLAY);  
            intent.setClass(getApplicationContext(), PlayMusic.class);  
            startService(intent);       //��������  
	    } 
	    
	    /**
	     * ��ͣ
	     * */ 
	    protected void pause() {  
	    	Intent intent = new Intent();  
            intent.putExtra(MSG, PAUSE);  
            intent.setClass(getApplicationContext(), PlayMusic.class);
            startService(intent);       //��������  
	    } 
	    
	    protected void resume() {  
	    	Intent intent = new Intent();  
            intent.putExtra(MSG, RESUME);  
            intent.setClass(getApplicationContext(), PlayMusic.class);
            startService(intent);       //��������  
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
					.setTitle("�˳�")
					.setMessage("��ȷ��Ҫ�˳���")
					.setNegativeButton("ȡ��", null)
					.setPositiveButton("ȷ��",
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

									stopService(intent); // ֹͣ��̨����
									PlayActivity.notificationManager.cancelAll();
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
	public boolean onTouch(View v, MotionEvent event){ 
		return gestureDetector.onTouchEvent(event); 
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
		if(arg0.getX() - arg1.getX() > 10 && Math.abs(arg2) > 0)  
		{  
			Intent intent = new Intent(getApplicationContext(),PlayActivity.class);  
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