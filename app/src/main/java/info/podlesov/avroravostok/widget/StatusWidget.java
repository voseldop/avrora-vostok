package info.podlesov.avroravostok.widget;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.podlesov.avroravostok.R;
import info.podlesov.avroravostok.data.AccountInfo;
import info.podlesov.avroravostok.data.CounterInfo;
import info.podlesov.avroravostok.networking.BackgroundOfficeQueryService;
import info.podlesov.avroravostok.networking.InteractiveOfficeQueryService;
import info.podlesov.avroravostok.networking.OfficeHelper;
import info.podlesov.avroravostok.ui.LoginActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StatusWidget extends AppWidgetProvider {
    private static final String ACTION_REFRESH = "info.podlesov.avroravostok.action.REFRESH";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");

    // Setup empty widget content and schedule widget update
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);
        setupActions(context, views);
        InteractiveOfficeQueryService.startWidgetsUpdate(context);
    }

    @Override
    public void onEnabled(Context context) {
        BackgroundOfficeQueryService.startWidgetsUpdate(context);
        InteractiveOfficeQueryService.startWidgetsUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        BackgroundOfficeQueryService.stopWidgetsUpdate(context);
    }

    public static PendingIntent getPendingRefreshIntent(Context context) {
        Intent intent = new Intent(context, StatusWidget.class);
        intent.setAction(ACTION_REFRESH);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getPendingLoginIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            InteractiveOfficeQueryService.startWidgetsUpdate(context);
        }
    }

    private static void setupActions(Context context, RemoteViews views) {
        views.setOnClickPendingIntent(R.id.widget_icon_refresh, getPendingRefreshIntent(context));
        views.setOnClickPendingIntent(R.id.widget_icon_balance, getPendingRefreshIntent(context));
        views.setOnClickPendingIntent(R.id.widget_text_status, getPendingRefreshIntent(context));
        views.setOnClickPendingIntent(R.id.widget_update_time, getPendingRefreshIntent(context));
        views.setOnClickPendingIntent(R.id.widget_icon_status, getPendingLoginIntent(context));

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context.getPackageName(), StatusWidget.class.getName()),
                views);
    }

    /**
     * Handle widget update in the provided background thread
     */
    public static void handleWidgetsUpdate(Context context) {

        AccountInfo accountInfo;
        CounterInfo counterInfo;
        int status = R.drawable.icon_attention;
        SharedPreferences config = context.getSharedPreferences(LoginActivity.class.getCanonicalName(), context.MODE_PRIVATE);
        String widgetText = context.getString(R.string.error_data_update);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_widget);
        OfficeHelper helper = OfficeHelper.getInstance();

        views.setTextViewText(R.id.widget_text_status, context.getString(R.string.status_updating));
        views.setViewVisibility(R.id.widget_icon_refresh, View.GONE);
        views.setViewVisibility(R.id.widget_progress_bar, View.VISIBLE);

        setupActions(context, views);

        try {
            while (!helper.isLoggedIn()) {
                helper.init();

                String username = config.getString("Username", null);
                String password = config.getString("Password", null);
                LoginActivity.TSG_CODE tsg  = LoginActivity.TSG_CODE.values()[Integer.valueOf(config.getString("Tsg", "1"))];

                helper.setUsername(username).setPassword(password).setTsgCode(tsg.toString()).login();
            }
            accountInfo = AccountInfo.get(helper);
            counterInfo = CounterInfo.get(helper);

            if (counterInfo.isDisabled()) {
               status = R.drawable.icon_disabled;
            } else if (counterInfo.isComplete()) {
                status = R.drawable.icon_complete;
            } else {
                buildCounterNotification(context);
            }

            widgetText = accountInfo.getBalanceStatus();
        } catch (IOException |JSONException | URISyntaxException e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT);
        }
        finally {
            views.setImageViewResource(R.id.widget_icon_status, status);
            views.setViewVisibility(R.id.widget_icon_refresh, View.VISIBLE);
            views.setViewVisibility(R.id.widget_progress_bar, View.GONE);
            views.setTextViewText(R.id.widget_text_status, widgetText);
            views.setTextViewText(R.id.widget_update_time, sdf.format(new Date()));
            setupActions(context, views);
        }
    }

    private static void buildCounterNotification(Context context) {

        Intent resultIntent = new Intent(context, LoginActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        String notifyText = context.getString(R.string.counter_entry_dialog_label);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon_attention)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(notifyText)
                .setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}

