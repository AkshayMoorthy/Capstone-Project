package app.com.dawn2dusk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import app.com.dawn2dusk.HomeActivity;
import app.com.dawn2dusk.R;

/**
 * Created by Akshay on 25-01-2017.
 */

public class TodayWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context,TodayWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context,TodayWidgetIntentService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
       if(HomeActivity.ACTION_DATA_UPDATED.equals(intent.getAction()))
        {
            Log.d("TodayWidgetProvider","Broadcast Received");
            context.startService(new Intent(context,TodayWidgetIntentService.class));
        }
    }
}