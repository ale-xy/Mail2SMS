package com.alexy.emailtosms;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alexy.emailtosms.data.UserConfigItem;

import java.util.List;
import java.util.Map;

public class MailSmsService extends IntentService {

    public MailSmsService() {
        super("MailSmsService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("MailSmsService", "onHandleIntent");

        try {
//            Map<String, UserConfigItem> userCofigItems = DbLoader.readDb(Preferences.getDbFile(getApplicationContext()));
            MailProcessor mailProcessor = new MailProcessor(Preferences.getMailSettings(getApplicationContext()), getApplicationContext());
            mailProcessor.process();
        } catch (Exception e) {
            Log.e("MailSmsService", e.getMessage());
            e.printStackTrace();
        } finally {
            StartServiceReceiver.completeWakefulIntent(intent);
        }
    }
}
