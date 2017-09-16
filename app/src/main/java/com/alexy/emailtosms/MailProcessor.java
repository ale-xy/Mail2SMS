package com.alexy.emailtosms;

import android.content.Context;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.alexy.emailtosms.data.MailSettings;
import com.alexy.emailtosms.data.UserConfigItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

/**
 * Created by Alexey on 10/09/2017.
 */

public class MailProcessor {

    static class SmsMessage {
        public SmsMessage(String phoneNumber, String text) {
            this.phoneNumber = phoneNumber;
            this.text = text;
        }

        String phoneNumber;
        String text;
    }

    private final MailSettings mailSettings;
    private final Map<String, UserConfigItem> userConfigItems;
    private final Context context;

    public MailProcessor(MailSettings mailSettings, Map<String, UserConfigItem> userConfigItems, Context context) {
        this.mailSettings = mailSettings;
        this.userConfigItems = userConfigItems;
        this.context = context;
    }

    public synchronized void process() {

        try {
            Properties properties = new Properties();

            properties.setProperty("mail.store.protocol", "imaps");
            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore("imaps");
            store.connect(mailSettings.mailServer, mailSettings.mailUser, mailSettings.mailPassword);

//            Folder[] folders = store.getDefaultFolder().list();
//
//            for (Folder folder:folders) {
//                Log.d("MailProcessor", "folder " + folder.getName());
//            }

            Folder inbox = store.getFolder(mailSettings.inboxFolder);
            inbox.open(Folder.READ_WRITE);

            Message[] emails = inbox.getMessages();

            if (emails.length > 0) {
                Folder checked = store.getFolder(mailSettings.checkedFolder);

                if (!checked.exists()) {
                    boolean created = checked.create(Folder.HOLDS_MESSAGES);
                    Log.d("MailProcessor", "folder "+mailSettings.checkedFolder+" created: " + created);
                }

                checked.open(Folder.READ_WRITE);

                for (Message email:emails) {
                    List<SmsMessage> sms = getSmsMessages(email);
                    if (sms != null && !sms.isEmpty()) {
                        for (SmsMessage smsMessage: sms) {
                            sendSms(smsMessage);
                        }
                    }

                    inbox.copyMessages(new Message[]{email}, checked);
                    Flags deleted = new Flags(Flags.Flag.DELETED);
                    inbox.setFlags(new Message[]{email}, deleted, true);
                }

                checked.close(true);
            } else {
                Log.d("MailProcessor", "No messages found");
            }

            inbox.close(true);
            store.close();


        } catch (Exception e) {
            Log.e("MailProcessor", e.getMessage());
            e.printStackTrace();
        }
    }


    private List<SmsMessage> getSmsMessages(Message email) throws MessagingException{
        Address[] from = email.getFrom();
        if (from == null || from.length == 0) {
            Log.d("MailProcessor", "No from address found");
            return null;
        }

        if (!(from[0] instanceof InternetAddress)) {
            Log.d("MailProcessor", "Invalid address "+from[0]);
            return null;
        }

        String address = ((InternetAddress)(from[0])).getAddress();
        String[] parts = address.split("@");

        if (parts.length != 2) {

        }

        String user = parts[0];
        String domain = parts[1];

        if(!TextUtils.equals(domain.toLowerCase(), mailSettings.mailDomain.toLowerCase())) {
            Log.d("MailProcessor", address + " is not from domain");
            return null;
        }

        if (!userConfigItems.containsKey(user)) {
            Log.d("MailProcessor", user + " not found");
            return null;
        }

        UserConfigItem userConfigItem = userConfigItems.get(user);

        if (!userConfigItem.isValid()) {
            Log.d("MailProcessor", user + " is disabled");
            return null;
        }

        Log.d("MailProcessor", "Creating sms for user " + user);

        StringBuilder builder = new StringBuilder(userConfigItem.getMessageSlot1());
        builder.append(" ").append(userConfigItem.getMessageSlot2());

        Date now = Calendar.getInstance().getTime();
        String dateTime = DateFormat.format("dd/MM/yyyy - HH:mm", now).toString();

        builder.append(" - ").append(dateTime);

        String messageText = builder.toString();

        List<SmsMessage> smsMessages = new ArrayList<>();

        if (!TextUtils.isEmpty(userConfigItem.getContact1())) {
            smsMessages.add(new SmsMessage(userConfigItem.getContact1(), messageText));
        }

        if (!TextUtils.isEmpty(userConfigItem.getContact2())) {
            smsMessages.add(new SmsMessage(userConfigItem.getContact2(), messageText));
        }

        if (!TextUtils.isEmpty(userConfigItem.getContact3())) {
            smsMessages.add(new SmsMessage(userConfigItem.getContact3(), messageText));
        }

        if (!smsMessages.isEmpty()) {
            return smsMessages;
        }

        return null;
    }

    private void sendSms(SmsMessage smsMessage) {
        try {
            Log.d("MailProcessor", "Sending sms "+ smsMessage.text + " to " + smsMessage.phoneNumber);
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> text = smsManager.divideMessage(smsMessage.text);
            Log.d("MailProcessor", "Message split into " + text);
            smsManager.sendMultipartTextMessage(smsMessage.phoneNumber, null, text, null, null);
        } catch (Exception e) {
            Log.e("MailProcessor", e.getMessage());
            e.printStackTrace();
        }
    }
}
