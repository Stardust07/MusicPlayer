package com.hustunique.musicplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */

public class NewAppWidget extends AppWidgetProvider {
	
    private static String ACTION_START = "com.hustunique.action.START";
	private static String ACTION_PAUSE = "com.hustunique.action.PAUSE";
	private static String ACTION_NEXT = "com.hustunique.action.NEXT";
	private static String ACTION_PREVIOUS = "com.hustunique.action.PREVIOUS"; 
	private static String ACTION_RESUME = "com.hustunique.action.RESUME"; 
	public static int current;
	public static int titleID;
	public static RemoteViews views;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	@Override
	public void onEnabled(Context context) {
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		// Enter relevant functionality for when the last widget is disabled
	}

	static void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId) {

		// Construct the RemoteViews object
		views = new RemoteViews(context.getPackageName(),
				R.layout.new_app_widget);
		Intent startIntent = new Intent(ACTION_START);
		Intent pauseIntent = new Intent(ACTION_PAUSE);
		Intent resumeIntent = new Intent(ACTION_RESUME);
		PendingIntent startpendingIntent = PendingIntent.getBroadcast(context, 0, startIntent, 0);
		PendingIntent resumependingIntent = PendingIntent.getBroadcast(context, 0, resumeIntent, 0);
		PendingIntent pausependingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, 0);
		if(PlayMusic.isPlay == true){
			views.setImageViewResource(R.id.widget_play, R.drawable.pause);
			views.setOnClickPendingIntent(R.id.widget_play,pausependingIntent);
		}
		else if(PlayMusic.isPause == true){
			views.setImageViewResource(R.id.widget_play, R.drawable.play);
			views.setOnClickPendingIntent(R.id.widget_play, resumependingIntent);
		}
		else {
			views.setImageViewResource(R.id.widget_play, R.drawable.play);
			views.setOnClickPendingIntent(R.id.widget_play, startpendingIntent);
		}
		
		Intent previousIntent = new Intent(ACTION_PREVIOUS);
		PendingIntent previouspendingIntent = PendingIntent.getBroadcast(context, 0, previousIntent, 0);
		views.setOnClickPendingIntent(R.id.widget_previous, previouspendingIntent);
		
		Intent nextIntent = new Intent(ACTION_NEXT);
		PendingIntent nextpendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, 0);
		views.setOnClickPendingIntent(R.id.widget_next, nextpendingIntent);
		views.setTextViewText(R.id.title,List.musicList.get(current).get("TITLE"));

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
	
}
