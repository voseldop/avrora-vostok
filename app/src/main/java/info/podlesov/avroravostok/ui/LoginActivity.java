package info.podlesov.avroravostok.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;

import info.podlesov.avroravostok.R;
import info.podlesov.avroravostok.networking.OfficeHelper;
import info.podlesov.avroravostok.ui.AccountInfoActivity;

/**
 * A login screen that offers login via userId/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Spinner mTsgSpinner;

    private SharedPreferences config;

    private final String[] data = {"Kollontay", "Belysheva 5/6"};

    public enum TSG_CODE {
        TSG_CODE_KOLLONTAI(0),
        TSG_CODE_BELYSHEVA(1);

        private final int value;

        TSG_CODE(int val) {
            this.value = val;
        }

        @Override
        public String toString() {
            switch(this) {
                case TSG_CODE_KOLLONTAI:
                    return "s701";
                case TSG_CODE_BELYSHEVA:
                    return "s702";
                default:
                    return "Unknown";
            }
        }

        public int toInt() {
            return value;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserView = (EditText) findViewById(R.id.userId);
        mTsgSpinner = (Spinner) findViewById(R.id.tsg_select);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mLoginButton = (Button) findViewById(R.id.email_sign_in_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        config = getSharedPreferences(LoginActivity.class.getName(), MODE_PRIVATE);
        String username = config.getString("Username", "");
        String password = config.getString("Password", "");
        mUserView.setText(username);
        mPasswordView.setText(password);
        ArrayAdapter<String> tsgList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, data);
        mTsgSpinner.setAdapter(tsgList);
        int position;
        try {
            position = Integer.valueOf(config.getString("Tsg", "1"));
        }
        catch (NumberFormatException e) {
            position = 1;
        }

        mTsgSpinner.setSelection(tsgList.getPosition(data[position]));

        if (StringUtils.isNoneBlank(username, password)) {
            attemptLogin();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this, email, password, TSG_CODE.TSG_CODE_BELYSHEVA);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;
        private final TSG_CODE mTsg;
        private final Activity mActivity;

        private String errMessage;

        UserLoginTask(Activity activity, String user, String password, TSG_CODE tsg) {
            mUser = user;
            mPassword = password;
            mTsg = tsg;
            mActivity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                OfficeHelper helper = OfficeHelper.getInstance();
                helper.init();
                helper.setUsername(mUser).setPassword(mPassword).setTsgCode(mTsg.toString()).login();
                return true;
            } catch (IOException|URISyntaxException e) {
                errMessage = e.getMessage();
                Log.e(TAG, "Login failed", e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                startActivity(new Intent(mActivity, AccountInfoActivity.class));
                SharedPreferences.Editor editor = config.edit();
                editor.putString("Username", mUser);
                editor.putString("Password", mPassword);
                editor.putString("Tsg", Integer.toString(mTsg.toInt()));
                editor.apply();
                finish();
            } else {
                mPasswordView.setError(errMessage);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

