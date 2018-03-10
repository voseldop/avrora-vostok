package info.podlesov.avroravostok.networking;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.podlesov.avroravostok.R;
import info.podlesov.avroravostok.data.AccountInfo;
import info.podlesov.avroravostok.data.CounterInfo;
import info.podlesov.avroravostok.ui.LoginActivity;
import info.podlesov.avroravostok.widget.StatusWidget;

import static com.google.android.gms.gcm.GcmNetworkManager.RESULT_SUCCESS;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class InteractiveOfficeQueryService extends IntentService {
    private static final String ACTION_QUERY_ACCOUNT_INFO = "info.podlesov.avroravostok.networking.action.UPDATE_WIDGETS";

    public InteractiveOfficeQueryService() {
        super("InteractiveOfficeQueryService");
    }


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startWidgetsUpdate(Context context) {
        Intent intent = new Intent(context, InteractiveOfficeQueryService.class);
        intent.setAction(ACTION_QUERY_ACCOUNT_INFO);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent!=null && ACTION_QUERY_ACCOUNT_INFO.equals(intent.getAction())) {
            StatusWidget.handleWidgetsUpdate(getApplicationContext());
        }
    }
}
