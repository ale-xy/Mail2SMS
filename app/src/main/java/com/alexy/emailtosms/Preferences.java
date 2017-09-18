package com.alexy.emailtosms;

import android.content.Context;
import android.content.SharedPreferences;

import com.alexy.emailtosms.data.MailSettings;

/**
 * Created by Alexey on 11/09/2017.
 */

class Preferences {
    private static final String PREFERENCE_ID = "com.alex.myapplication.PREFS";

    private static final String SERVICE_ENABLED = "SERVICE_ENABLED";
    private static final String MAIL_SERVER = "MAIL_SERVER";
    private static final String MAIL_LOGIN = "MAIL_LOGIN";
    private static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    private static final String MAIL_DOMAIN = "MAIL_DOMAIN";
    private static final String INBOX_FOLDER = "INBOX_FOLDER";
    private static final String NOTIFIED_FOLDER = "NOTIFIED_FOLDER";
    private static final String TIMEOUT_FOLDER = "TIMEOUT_FOLDER";
    private static final String NOT_NOTIFIED_FOLDER = "NOT_NOTIFIED_FOLDER";
    private static final String IGNORED_FOLDER = "IGNORED_FOLDER";
    private static final String CHECK_PERIOD = "CHECK_PERIOD";
    private static final String SMS_TIMEOUT = "SMS_TIMEOUT";
    private static final String DB_FILE = "DB_FILE";


    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCE_ID, Context.MODE_PRIVATE);
    }

    public static void setServiceEnabled(boolean enabled, Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().putBoolean(SERVICE_ENABLED, enabled).apply();
    }

    public static boolean isServiceEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(SERVICE_ENABLED, false);
    }

    public static void setDbFile(String path, Context context) {
        getSharedPreferences(context).edit().putString(DB_FILE, path).apply();
    }

    public static String getDbFile(Context context) {
        return getSharedPreferences(context).getString(DB_FILE, "");
    }

    public static MailSettings getMailSettings(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        MailSettings mailSettings = new MailSettings();

        mailSettings.mailServer = sharedPreferences.getString(MAIL_SERVER, "br30.hostgator.com.br");
        mailSettings.mailUser = sharedPreferences.getString(MAIL_LOGIN, null);
        mailSettings.mailPassword = sharedPreferences.getString(MAIL_PASSWORD, null);
        mailSettings.inboxFolder = sharedPreferences.getString(INBOX_FOLDER, "INBOX");
        mailSettings.notifiedFolder = sharedPreferences.getString(NOTIFIED_FOLDER, "INBOX.Checked and notified");
        mailSettings.timeoutFolder = sharedPreferences.getString(TIMEOUT_FOLDER, "INBOX.Checked with timeout");
        mailSettings.notNotifiedFolder = sharedPreferences.getString(NOT_NOTIFIED_FOLDER, "INBOX.Checked but no notification");
        mailSettings.ignoredFolder = sharedPreferences.getString(IGNORED_FOLDER, "INBOX.Checked and ignored");

        mailSettings.mailDomain = sharedPreferences.getString(MAIL_DOMAIN, "monitoracaoja.com.br");
        mailSettings.checkPeriod = sharedPreferences.getInt(CHECK_PERIOD, 60);
        mailSettings.smsTimeout = sharedPreferences.getInt(SMS_TIMEOUT, 10);

        return mailSettings;
    }

    public static void storeMailSettings(MailSettings settings, Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        sharedPreferences.edit().
                putString(MAIL_SERVER, settings.mailServer).
                putString(MAIL_LOGIN, settings.mailUser).
                putString(MAIL_PASSWORD, settings.mailPassword).
                putString(MAIL_DOMAIN, settings.mailDomain).
                putString(INBOX_FOLDER, settings.inboxFolder).
                putString(NOTIFIED_FOLDER, settings.notifiedFolder).
                putString(TIMEOUT_FOLDER, settings.timeoutFolder).
                putString(NOT_NOTIFIED_FOLDER, settings.notNotifiedFolder).
                putString(IGNORED_FOLDER, settings.ignoredFolder).
                putInt(CHECK_PERIOD, settings.checkPeriod).
                putInt(SMS_TIMEOUT, settings.smsTimeout).
                apply();
    }
}
