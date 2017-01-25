package app.com.dawn2dusk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import app.com.dawn2dusk.HomeActivity;
import app.com.dawn2dusk.R;
import app.com.dawn2dusk.SunDataContract;

/**
 * Created by Akshay on 25-01-2017.
 */

public class TodayWidgetIntentService extends IntentService {
    private static final String[] WIDGET_COLUMNS = {
            SunDataContract.SunDataEntry.COLUMN_SUNRISE,
            SunDataContract.SunDataEntry.COLUMN_SUNSET,
            SunDataContract.SunDataEntry.COLUMN_DAY_LENGTH
    };
    // these indices must match the projection
    private static final int INDEX_SUNRISE= 0;
    private static final int INDEX_SUNSET = 1;
    private static final int INDEX_DAY_LENGTH = 2;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }
    int riseid = R.mipmap.ic_rice;
    int setid = R.mipmap.ic_set;
    int durid = R.mipmap.ic_dur;
    String description = "Sunrise";
    String maxTemp = "5:30am";

    // Perform this loop procedure for each Today widget


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("WidgetIntnetService","-->");
        AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(this);
        int[] appWidgetIds=appWidgetManager.getAppWidgetIds(new ComponentName(this,TodayWidgetProvider.class));
        Cursor data=getContentResolver().query(SunDataContract.SunDataEntry.CONTENT_URI, WIDGET_COLUMNS,null,null,null);
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }
        for (int appWidgetId : appWidgetIds) {

             int layoutId = R.layout.widget_today_large;

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_sunrise_icon, riseid);
            views.setImageViewResource(R.id.widget_sunset_icon, setid);
            views.setImageViewResource(R.id.widget_dur_icon, durid);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            views.setTextViewText(R.id.widget_sunrisetv, data.getString(0));
            views.setTextViewText(R.id.widget_sunsettv, data.getString(1));
            views.setTextViewText(R.id.widget_durtv, data.getString(2));

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            views.setOnClickPendingIntent(R.id.widget2, pendingIntent);
            views.setOnClickPendingIntent(R.id.widget3, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_sunrise_icon, description);
    }

}
