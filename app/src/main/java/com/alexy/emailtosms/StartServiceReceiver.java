package com.alexy.emailtosms;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class StartServiceReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("StartServiceReceiver", "onReceive");
        Intent serviceIntent = new Intent(context, MailSmsService.class);
        startWakefulService(context, serviceIntent);
    }
}
