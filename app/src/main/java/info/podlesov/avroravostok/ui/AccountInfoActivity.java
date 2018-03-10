package info.podlesov.avroravostok.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.podlesov.avroravostok.R;
import info.podlesov.avroravostok.data.AccountInfo;
import info.podlesov.avroravostok.data.ChargesInfo;
import info.podlesov.avroravostok.data.CounterInfo;
import info.podlesov.avroravostok.networking.OfficeHelper;


public class AccountInfoActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private TextView mAccountBalance;
    private LinearLayout mAccountSummary;
    private TableLayout mChargesSummary;
    private AccountInfoTask mTask;
    private AccountInfo accountInfo;
    private CounterInfo counterInfo;
    private ChargesInfo chargesInfo;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayout mCardsLayout;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mAccountSummary.setVisibility(View.VISIBLE);
                    mChargesSummary.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_bills:
                    mChargesSummary.setVisibility(View.VISIBLE);
                    mAccountSummary.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        mAccountBalance = (TextView) findViewById(R.id.AccountBalance);
        mAccountSummary = (LinearLayout) findViewById(R.id.account_summary);
        mChargesSummary = (TableLayout) findViewById(R.id.charges_summary);
        mCardsLayout = (LinearLayout) findViewById(R.id.counter_cards);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mRefreshLayout.setOnRefreshListener(this);

        mTask = new AccountInfoTask();
        mTask.execute();
    }

    @Override
    public void onRefresh() {
        mTask = new AccountInfoTask();
        mTask.execute();
    }

    public class AccountInfoTask extends AsyncTask<Void, Void, Boolean> {

        public final TableRow.LayoutParams NO_SPAN_PARAMS;
        public final TableRow.LayoutParams SPAN_PARAMS;
        public final LinearLayout.LayoutParams ICON_LAYOUT_PARAMS;

        public AccountInfoTask() {
            super();
            NO_SPAN_PARAMS = new TableRow.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            NO_SPAN_PARAMS.gravity=Gravity.CENTER;

            SPAN_PARAMS = new TableRow.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            SPAN_PARAMS.gravity = Gravity.CENTER;
            SPAN_PARAMS.span = 2;

            ICON_LAYOUT_PARAMS = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            ICON_LAYOUT_PARAMS.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            ICON_LAYOUT_PARAMS.setMargins(50, 0, 50, 0);
            ICON_LAYOUT_PARAMS.weight = 0;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OfficeHelper helper = OfficeHelper.getInstance();

            try {
                while (!helper.isLoggedIn()) {
                    helper.init();
                    helper.login();
                }
                accountInfo = AccountInfo.fromString(helper.status());
                counterInfo = CounterInfo.fromString(helper.getCounterData());
                chargesInfo = ChargesInfo.fromString(helper.getChargesData());
            } catch (IOException|JSONException|URISyntaxException e) {
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mTask = null;
            if (accountInfo != null) {
                mAccountBalance.setText(accountInfo.getBalanceStatus());

                ActionBar ab = getSupportActionBar();
                ab.setTitle(accountInfo.getOwnerName());
                ab.setSubtitle(accountInfo.getAccountNumber());

            }

            mCardsLayout.removeAllViews();
            mChargesSummary.removeAllViews();

            if (counterInfo != null) {
                for (CounterInfo.CounterEntry counter: counterInfo.getCounters()) {
                    createCounterView(counter, mCardsLayout);
                }
            }

            if (chargesInfo != null) {
                for (ChargesInfo.ChargesData data: chargesInfo.getCharges()) {
                    createChargesRow(data, mChargesSummary);
                }
            }

            if (!success) {
                Toast.makeText(getApplicationContext(),
                               R.string.error_data_update,
                               Toast.LENGTH_LONG).show();
            }
            mRefreshLayout.setRefreshing(false);

        }

        private void createChargesRow(ChargesInfo.ChargesData data, ViewGroup parent) {
            TableRow row = (TableRow)getLayoutInflater().inflate(R.layout.charges_row, parent, false);
            for (int i=0; i<8; i++) {
                TextView text = (TextView)row.getVirtualChildAt(i);
                text.setText(data.getValue(i));
            }
            mChargesSummary.addView(row);
        }

        private View createCounterView(CounterInfo.CounterEntry counter, ViewGroup parent) {
            View result = getLayoutInflater().inflate(R.layout.counter_card_template, parent, false);

            CardView card = result.findViewById(R.id.card_template);
            TextView heading = result.findViewById(R.id.card_heading);
            TextView subtitle = result.findViewById(R.id.card_old_value);
            TextView value = result.findViewById(R.id.card_new_value);
            TextView date = result.findViewById(R.id.card_date);
            ImageView icon = result.findViewById(R.id.card_icon);

            heading.setText(counter.getName());
            subtitle.setText(counter.getValueOld());
            value.setText(StringUtils.stripToEmpty(counter.getValueCurrent()));
            date.setText(StringUtils.stripToEmpty(counter.getDate()));

            int iconId = R.drawable.icon_disabled;
            if (counter.getEnabled()) {
                iconId = StringUtils.isNotEmpty(counter.getValueCurrent()) ? R.drawable.icon_complete
                        : R.drawable.icon_attention;
            }
            icon.setImageResource(iconId);
            card.setCardBackgroundColor(counter.getEnabled() ? Color.WHITE : Color.LTGRAY);

            parent.addView(result);

            result.setOnClickListener(new CounterClickListener(counter));

            return result;
        }
    }

    public class SubmitCounterTask extends AsyncTask<Void, Void, String> {
        private final int value;
        private final CounterInfo.CounterEntry entry;
        private String status;

        SubmitCounterTask(CounterInfo.CounterEntry entry, int value) {
            super();
            this.entry = entry;
            this.value = value;
        }

        @Override
        protected String doInBackground(Void... voids) {
            OfficeHelper helper = OfficeHelper.getInstance();

            try {
                while (!helper.isLoggedIn()) {
                    helper.init();
                    helper.login();
                }
                OfficeHelper.getInstance().submitCounter(entry, value);
                return getApplicationContext().getString(R.string.success);

            } catch (IOException|URISyntaxException e) {
                return e.getLocalizedMessage();
            }
        }
        @Override
        protected void onPostExecute(final String status) {
            Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
            new AccountInfoTask().execute();
        }
    }

    final static Pattern valuePattern = Pattern.compile("\\d+");

    private class CounterClickListener implements View.OnClickListener {
        private CounterInfo.CounterEntry entry;
        public CounterClickListener(CounterInfo.CounterEntry entry) {
            this.entry = entry;
        }

        @Override
        public void onClick(View view) {
            View dialogContent = getLayoutInflater().inflate(R.layout.activity_counter_entry, null);
            final NumberPicker picker = dialogContent.findViewById(R.id.counter_value_picker);
            String minValue = entry.getValueOld();
            String value = StringUtils.isBlank(entry.getValueCurrent()) ? entry.getValueOld() : entry.getValueCurrent();

            Matcher matcher = valuePattern.matcher(value);
            matcher.find();
            value = matcher.group();

            matcher = valuePattern.matcher(minValue);
            matcher.find();
            minValue = matcher.group();


            picker.setMinValue(Integer.valueOf(minValue));
            picker.setMaxValue(Integer.MAX_VALUE);
            picker.setValue(Integer.valueOf(value));
            picker.setWrapSelectorWheel(false);
            AlertDialog dialog = new AlertDialog.Builder(AccountInfoActivity.this)
                    .setTitle(entry.getName())
                    .setMessage(R.string.counter_entry_dialog_label)
                    .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SubmitCounterTask task = new SubmitCounterTask(entry, picker.getValue());
                    task.execute();
                }
            })
                    .setView(dialogContent)
                    .create();
            dialog.show();
        }
    }
}
