package com.sshattered.sltusagemeter;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class SLTWidget extends AppWidgetProvider {

    public static String REFRESH_ACTION = "android.appwidget.action.APPWIDGET_UPDATE";
    private RemoteViews views;
    private String total = "0";
    private String day = "0";
    private String night = "0";

    static private PendingIntent getPenIntent(Context context) {
        Intent intent = new Intent(context, SLTWidget.class);
        intent.setAction(REFRESH_ACTION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            views = new RemoteViews(context.getPackageName(), R.layout.s_l_t_widget);
            views.setOnClickPendingIntent(R.id.button,
                    getPenIntent(context));
            views.setTextViewText(R.id.txtTotal, String.format(Locale.ENGLISH, "Total - %.2fGB", Float.parseFloat(total)));
            views.setTextViewText(R.id.txtDay, String.format(Locale.ENGLISH, "Day - %.2fGB", Float.valueOf(day)));
            views.setTextViewText(R.id.txtNight, String.format(Locale.ENGLISH, "Night - %.2fGB", Float.valueOf(night)));

            appWidgetManager.updateAppWidget(appWidgetId, views);
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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (REFRESH_ACTION.equals(intent.getAction())) {
            views = new RemoteViews(context.getPackageName(), R.layout.s_l_t_widget);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, SLTWidget.class));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SLTHandler.Instance(context).SetLogin(SLTHandler.Instance(context).GetDetails()[0],
                            SLTHandler.Instance(context).GetDetails()[1]);
                    boolean state = SLTHandler.Instance(context).GetLoginTelephone();
                    if(state) {
                        total = String.valueOf(SLTHandler.Instance(context)._packUsage.total);
                        day = String.valueOf(SLTHandler.Instance(context)._packUsage.day);
                        night = String.valueOf(SLTHandler.Instance(context)._packUsage.night);
                        views.setTextViewText(R.id.txtTotal, total);
                        onUpdate(context, appWidgetManager, appWidgetIds);
                    }
                }
            }).start();
        }
    }
}