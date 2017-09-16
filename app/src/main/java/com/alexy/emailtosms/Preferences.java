package com.alexy.emailtosms;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

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
    private static final String CHECKED_FOLDER = "CHECKED_FOLDER";
//    private static final String SMS_SENDER = "SMS_SENDER";
    private static final String CHECK_PERIOD = "CHECK_PERIOD";
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

        mailSettings.mailServer = sharedPreferences.getString(MAIL_SERVER, null);
        mailSettings.mailUser = sharedPreferences.getString(MAIL_LOGIN, null);
        mailSettings.mailPassword = sharedPreferences.getString(MAIL_PASSWORD, null);
        mailSettings.inboxFolder = sharedPreferences.getString(INBOX_FOLDER, "INBOX");
        mailSettings.checkedFolder = sharedPreferences.getString(CHECKED_FOLDER, "Checked");
        mailSettings.mailDomain = sharedPreferences.getString(MAIL_DOMAIN, null);
//        mailSettings.smsSender = sharedPreferences.getString(SMS_SENDER, null);
        mailSettings.checkPeriod = sharedPreferences.getInt(CHECK_PERIOD, 60);

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
                putString(CHECKED_FOLDER, settings.checkedFolder).
//                putString(SMS_SENDER, settings.smsSender).
                putInt(CHECK_PERIOD, settings.checkPeriod).
                apply();
    }
}
