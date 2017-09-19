package com.alexy.emailtosms;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class MailSmsService extends IntentService {
    private static AtomicBoolean busy = new AtomicBoolean(false);

    public MailSmsService() {
        super("MailSmsService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("MailSmsService", "onHandleIntent");


        if (!busy.compareAndSet(false, true)) {
            Log.d("MailProcessor", "Already processing");
            return;
        }

        try {
            MailProcessor mailProcessor = new MailProcessor(Preferences.getMailSettings(getApplicationContext()), getApplicationContext());
            mailProcessor.process();
        } catch (Exception e) {
            Log.e("MailSmsService", e.getMessage());
            e.printStackTrace();
        } finally {
            StartServiceReceiver.completeWakefulIntent(intent);
            busy.set(false);
        }
    }
}
