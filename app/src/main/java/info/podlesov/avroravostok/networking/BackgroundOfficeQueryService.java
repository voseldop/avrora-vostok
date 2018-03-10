package info.podlesov.avroravostok.networking;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.podlesov.avroravostok.R;
import info.podlesov.avroravostok.ui.LoginActivity;
import info.podlesov.avroravostok.widget.StatusWidget;
import info.podlesov.avroravostok.data.AccountInfo;
import info.podlesov.avroravostok.data.CounterInfo;

import static com.google.android.gms.gcm.GcmNetworkManager.RESULT_FAILURE;
import static com.google.android.gms.gcm.GcmNetworkManager.RESULT_SUCCESS;

public class BackgroundOfficeQueryService extends GcmTaskService {
    private static final String ACTION_QUERY_ACCOUNT_INFO = "info.podlesov.avroravostok.networking.action.UPDATE_WIDGETS";
    private static final int DEFAULT_UPDATE_PERIOD = 3*60*60;
    private static final int DEFAULT_FLEX = 60*60;

    @Override
    public int onRunTask(TaskParams taskParams) {
        if (taskParams.getTag().equals(ACTION_QUERY_ACCOUNT_INFO)) {
            StatusWidget.handleWidgetsUpdate(getApplicationContext());
            return RESULT_SUCCESS;
        } else {
            return RESULT_FAILURE;
        }
    }

    @Override
    public void onInitializeTasks() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        if (appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext().getPackageName(), StatusWidget.class.getName())).length > 0) {
            startWidgetsUpdate(getApplicationContext());
        }
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startWidgetsUpdate(Context context) {

        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);

        Task task = new PeriodicTask.Builder()
                .setService(BackgroundOfficeQueryService.class)
                .setPeriod(DEFAULT_UPDATE_PERIOD)
                .setFlex(DEFAULT_FLEX)
                .setTag(BackgroundOfficeQueryService.ACTION_QUERY_ACCOUNT_INFO)
                .setUpdateCurrent(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .setPersisted(true)
                .build();

        gcmNetworkManager.schedule(task);
    }

    public static void stopWidgetsUpdate(Context context) {
        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);
        gcmNetworkManager.cancelAllTasks(BackgroundOfficeQueryService.class);
    }
}
