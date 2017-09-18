package com.alexy.emailtosms;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexy.emailtosms.data.MailSettings;
import com.alexy.emailtosms.data.UserConfigItem;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_BROWSE = 5555;

    @BindView(R.id.start_stop_button) Button startButton;
    @BindView(R.id.db_path) TextView dbPath;
    @BindView(R.id.load_db) Button loadDbButton;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.settings_button) Button settingsButton;

    @BindView(R.id.settings_layout) ViewGroup settingsLayout;

    @BindView(R.id.mail_server) EditText mailServer;
    @BindView(R.id.user_name) EditText userName;
    @BindView(R.id.password) EditText password;
    @BindView(R.id.mail_domain) EditText mailDomain;
    @BindView(R.id.inbox_folder) EditText inboxFolder;
    @BindView(R.id.notified_folder) EditText notifiedFolder;
    @BindView(R.id.timeout_folder) EditText timeoutFolder;
    @BindView(R.id.not_notified_folder) EditText notNotifiedFolder;
    @BindView(R.id.ignored_folder) EditText ignoredFolder;
    @BindView(R.id.check_period) EditText checkPeriod;
    @BindView(R.id.sms_timeout) EditText smsTimeout;
    @BindView(R.id.save_settings_button) Button saveSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        updateStartStopButton();

        dbPath.setText(Preferences.getDbFile(this));
        settingsLayout.setVisibility(View.GONE);
        fillMailSettings(Preferences.getMailSettings(this));

        scheduleAlarm(getApplicationContext());
    }

    private void updateStartStopButton() {
        boolean started = Preferences.isServiceEnabled(this);

        if (started) {
            startButton.setText(R.string.stop);
        } else {
            startButton.setText(R.string.start);
        }
    }

    @OnClick(R.id.start_stop_button)
    public void startStop() {
        boolean started = Preferences.isServiceEnabled(this);

        if (!started) {
            Preferences.setServiceEnabled(true, this);
            scheduleAlarm(getApplicationContext());
        } else {
            Preferences.setServiceEnabled(false, this);
            cancelAlarm(getApplicationContext());
        }

        updateStartStopButton();
    }


    @OnClick(R.id.load_db)
    public void loadDb() {
        String currentPath = Preferences.getDbFile(this);

        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(REQUEST_BROWSE)
                .withPath(new File(currentPath).getParent())
                .withFilter(Pattern.compile(".*\\.csv$"))
                .start();

//        FilePickerBuilder.getInstance().
//                setMaxCount(1).
//                addFileSupport("CSV file", new String[]{"csv"}).
//                enableDocSupport(false).showFolderView(true).
//                pickFile(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BROWSE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

                try {
                    validateDbFile(path);
                } catch (Throwable e) {
                    showError(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void validateDbFile(String path) throws Exception {
        AsyncTask<String, Void, Exception> task = new AsyncTask<String, Void, Exception>() {
            private String path;

            @Override
            protected void onPreExecute() {
                dbPath.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                loadDbButton.setEnabled(false);
            }

            @Override
            protected Exception doInBackground(String... strings) {
                path = strings[0];
                try {
                    Map<String, UserConfigItem> list = DbLoader.readDb(path);
                    Log.d("MainActivity", "read list: " + list);
                } catch (Exception e) {
                    return e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Exception e) {
                if (e == null) {
                    Preferences.setDbFile(path, MainActivity.this);
                    dbPath.setText(path);
                    restartService();
                } else {
                    showError(e.getMessage());
                    e.printStackTrace();
                }

                dbPath.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                loadDbButton.setEnabled(true);
            }
        };

        task.execute(path);

    }

    private void showError(String message) {
        final Snackbar snackbar = Snackbar.make(settingsLayout, message, Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        snackbar.show();
    }

    @OnClick(R.id.settings_button)
    public void showSettings() {
        if (settingsLayout.getVisibility() == View.VISIBLE) {
            settingsLayout.setVisibility(View.GONE);
            settingsButton.setText(R.string.open_settings);
        } else {
            settingsLayout.setVisibility(View.VISIBLE);
            settingsButton.setText(R.string.close_settings);
            saveSettingsButton.setEnabled(false);
        }
    }

    @OnClick(R.id.save_settings_button)
    public void saveSettings() {
        Preferences.storeMailSettings(collectMailSettings(), this);
        saveSettingsButton.setEnabled(false);
        restartService();
    }

    private void restartService() {
        if (Preferences.isServiceEnabled(this)) {
            cancelAlarm(this);
            scheduleAlarm(this);
            Snackbar.make(settingsLayout, R.string.service_restarted, Snackbar.LENGTH_LONG).show();
        }
    }

    @OnTextChanged({R.id.mail_server,
                    R.id.user_name,
                    R.id.password,
                    R.id.mail_domain,
                    R.id.inbox_folder,
                    R.id.notified_folder,
                    R.id.timeout_folder,
                    R.id.not_notified_folder,
                    R.id.ignored_folder,
                    R.id.check_period,
                    R.id.sms_timeout })
    public void onTextChanged(CharSequence text) {
        if (isSettingsChanged()) {
            saveSettingsButton.setEnabled(true);
        }
    }

    private void fillMailSettings(MailSettings mailSettings) {
        mailServer.setText(mailSettings.mailServer);
        userName.setText(mailSettings.mailUser);
        password.setText(mailSettings.mailPassword);
        mailDomain.setText(mailSettings.mailDomain);
        inboxFolder.setText(mailSettings.inboxFolder);
        notifiedFolder.setText(mailSettings.notifiedFolder);
        timeoutFolder.setText(mailSettings.timeoutFolder);
        notNotifiedFolder.setText(mailSettings.notNotifiedFolder);
        ignoredFolder.setText(mailSettings.ignoredFolder);
        checkPeriod.setText(String.valueOf(mailSettings.checkPeriod));
        smsTimeout.setText(String.valueOf(mailSettings.smsTimeout));
    }

    private MailSettings collectMailSettings() {
        //todo validate
        MailSettings mailSettings = new MailSettings();

        mailSettings.mailServer = mailServer.getText().toString();
        mailSettings.mailUser = userName.getText().toString();
        mailSettings.mailPassword = password.getText().toString();
        mailSettings.mailDomain = mailDomain.getText().toString();
        mailSettings.inboxFolder = inboxFolder.getText().toString();
        mailSettings.notifiedFolder = notifiedFolder.getText().toString();
        mailSettings.timeoutFolder = timeoutFolder.getText().toString();
        mailSettings.notNotifiedFolder = notNotifiedFolder.getText().toString();
        mailSettings.ignoredFolder = ignoredFolder.getText().toString();
        mailSettings.checkPeriod = Integer.parseInt(checkPeriod.getText().toString());
        mailSettings.smsTimeout = Integer.parseInt(smsTimeout.getText().toString());

        return mailSettings;
    }

    private boolean isSettingsChanged() {
        MailSettings mailSettings = Preferences.getMailSettings(this);

        boolean equals =
                TextUtils.equals(mailSettings.mailServer, mailServer.getText().toString()) &&
                TextUtils.equals(mailSettings.mailUser, userName.getText().toString()) &&
                TextUtils.equals(mailSettings.mailPassword, password.getText().toString()) &&
                TextUtils.equals(mailSettings.mailDomain, mailDomain.getText().toString()) &&
                TextUtils.equals(mailSettings.inboxFolder, inboxFolder.getText().toString()) &&
                TextUtils.equals(mailSettings.notifiedFolder, notifiedFolder.getText().toString()) &&
                TextUtils.equals(mailSettings.timeoutFolder, timeoutFolder.getText().toString()) &&
                TextUtils.equals(mailSettings.notNotifiedFolder, notNotifiedFolder.getText().toString()) &&
                TextUtils.equals(mailSettings.ignoredFolder, ignoredFolder.getText().toString()) &&
                TextUtils.equals(String.valueOf(mailSettings.checkPeriod), checkPeriod.getText().toString());
                TextUtils.equals(String.valueOf(mailSettings.smsTimeout), smsTimeout.getText().toString());

        return !equals;
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, StartServiceReceiver.class);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleAlarm(Context context) {
        Log.d("MainActivity", "scheduleAlarm");

        if (!Preferences.isServiceEnabled(context)) {
            Log.d("MainActivity", "service disabled");
            return;
        }

        final PendingIntent pIntent = getPendingIntent(context);

        long firstMillis = System.currentTimeMillis();

        long period = Preferences.getMailSettings(context).checkPeriod * 1000L;

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis, period, pIntent);
    }

    public static void cancelAlarm(Context context) {
        Log.d("MainActivity", "cancelAlarm");
        final PendingIntent pIntent = getPendingIntent(context);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
}
