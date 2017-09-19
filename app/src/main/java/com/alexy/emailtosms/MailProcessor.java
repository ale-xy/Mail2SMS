package com.alexy.emailtosms;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.alexy.emailtosms.data.MailSettings;
import com.alexy.emailtosms.data.ServiceStatus;
import com.alexy.emailtosms.data.UserConfigItem;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

/**
 * Created by Alexey on 10/09/2017.
 */

public class MailProcessor {

    static class SmsMessage {
        final String phoneNumber;
        final String text;
        final String user;

        public SmsMessage(String phoneNumber, String text, String user) {
            this.phoneNumber = phoneNumber;
            this.text = text;
            this.user = user;
        }
    }

    private enum EmailAction {
        SEND_SMS,
        TIMEOUT,
        USER_DISABLED,
        IGNORED
    }

    private final MailSettings mailSettings;
    private final Context context;

    public MailProcessor(MailSettings mailSettings, Context context) {
        this.mailSettings = mailSettings;
        this.context = context;
    }

    public void process() {
        Exception exception = null;

        try {
            Store store = connectToMailServer();

            Folder[] folders = store.getDefaultFolder().list();

            for (Folder folder:folders) {
                Log.d("MailProcessor", "folder " + folder.getName());
            }

            Folder inbox = store.getFolder(mailSettings.inboxFolder);
            inbox.open(Folder.READ_WRITE);

            Message[] emails = inbox.getMessages();

            if (emails.length > 0) {

                for (Message email:emails) {
                    String user = getUser(email);

                    EmailAction action = getEmailAction(user);

                    String folderName;

                    switch (action) {
                        case IGNORED:
                            folderName = mailSettings.ignoredFolder;
                            break;
                        case TIMEOUT:
                            folderName = mailSettings.timeoutFolder;
                            break;
                        case USER_DISABLED:
                            folderName = mailSettings.notNotifiedFolder;
                            break;
                        case SEND_SMS:
                            List<SmsMessage> sms = getSmsMessages(user);
                            if (sms != null && !sms.isEmpty()) {
                                folderName = mailSettings.notifiedFolder;

                                for (SmsMessage smsMessage: sms) {
                                    sendSms(smsMessage);
                                }

                                UserConfigItem userConfigItem = getUserConfig(user);
                                userConfigItem.setLastMessageDate(Calendar.getInstance().getTime());
                                SugarRecord.update(userConfigItem);
                            } else {
                                folderName = mailSettings.notNotifiedFolder;
                            }

                            break;
                        default:
                            folderName = null;
                    }

                    Folder destination = store.getFolder(folderName);

                    if (!destination.exists()) {
                        boolean created = destination.create(Folder.HOLDS_MESSAGES);
                        Log.d("MailProcessor", "folder " + destination.getName() +" created: " + created);
                    }

                    moveMessages(inbox, email, destination);
                }
            } else {
                Log.d("MailProcessor", "No messages found");
            }

            inbox.close(true);
            store.close();


        } catch (Exception e) {
            exception = e;
            Log.e("MailProcessor", e.getMessage());
            e.printStackTrace();
        }

        ServiceStatus serviceStatus = Select.from(ServiceStatus.class).first();

        if (serviceStatus == null) {
            serviceStatus = new ServiceStatus();
        }

        serviceStatus.setSuccess(exception == null);
        serviceStatus.setErrorText(exception == null ? null : exception.getMessage());
        serviceStatus.setLastCheck(Calendar.getInstance().getTime());
        SugarRecord.update(serviceStatus);
        Log.d("MailProcessor", "Service status: " + serviceStatus);

        Intent statusIntent = new Intent(MainActivity.STATUS_ACTION);
        context.sendBroadcast(statusIntent);
    }

    private void moveMessages(Folder inbox, Message email, Folder destination) throws MessagingException {
        destination.open(Folder.READ_WRITE);

        inbox.copyMessages(new Message[]{email}, destination);
        Flags deleted = new Flags(Flags.Flag.DELETED);
        inbox.setFlags(new Message[]{email}, deleted, true);

        destination.close(true);
    }

    @NonNull
    private Store connectToMailServer() throws MessagingException {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        Session emailSession = Session.getDefaultInstance(properties);
        Store store = emailSession.getStore("imaps");
        store.connect(mailSettings.mailServer, mailSettings.mailUser, mailSettings.mailPassword);
        return store;
    }

    private String getUser(Message email) throws MessagingException{
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
            Log.d("MailProcessor", "Invalid address "+from[0]);
            return null;
        }

        String user = parts[0];
        String domain = parts[1];

        if(!TextUtils.equals(domain.toLowerCase(), mailSettings.mailDomain.toLowerCase())) {
            Log.d("MailProcessor", address + " is not from domain");
            return null;
        }

        return user;
    }

    private EmailAction getEmailAction(String user) {
        if (TextUtils.isEmpty(user)) {
            return EmailAction.IGNORED;
        }

        UserConfigItem userConfigItem = getUserConfig(user);

        if (userConfigItem == null) {
            return EmailAction.IGNORED;
        }

        if (!userConfigItem.isValid()) {
            Log.d("MailProcessor", user + " is disabled");
            return EmailAction.USER_DISABLED;
        }

        Date now = Calendar.getInstance().getTime();

        long timeout = mailSettings.smsTimeout * 1000L * 60;

        Log.d("MailProcessor", "user "+ user + " last message " + userConfigItem.getLastMessageDate());

        if (userConfigItem.getLastMessageDate() != null &&
                now.getTime() - userConfigItem.getLastMessageDate().getTime() < timeout) {
            Log.d("MailProcessor", "user "+ user + " timeout not expired");
            return EmailAction.TIMEOUT;
        }

        return EmailAction.SEND_SMS;
    }

    private UserConfigItem getUserConfig(String user) {
        List<UserConfigItem> configItems = Select.from(UserConfigItem.class).where(Condition.prop("USER_ID").eq(user)).list();

        if (configItems.isEmpty()) {
            Log.d("MailProcessor", user + " not found");
            return null;
        }

        return configItems.get(0);
    }

    private List<SmsMessage> getSmsMessages(String user) throws MessagingException{

        UserConfigItem userConfigItem = getUserConfig(user);

        Log.d("MailProcessor", "Creating sms for user " + user);

        StringBuilder builder = new StringBuilder();
        builder.append(userConfigItem.getMessageSlot1()).append(" ").append(userConfigItem.getMessageSlot2());

        Date now = Calendar.getInstance().getTime();
        String dateTime = DateFormat.format("dd/MM/yyyy - HH:mm", now).toString();

        builder.append(" - ").append(dateTime);

        String messageText = builder.toString();

        List<SmsMessage> smsMessages = new ArrayList<>();

        if (!TextUtils.isEmpty(userConfigItem.getContact1())) {
            smsMessages.add(new SmsMessage(userConfigItem.getContact1(), messageText, user));
        }

        if (!TextUtils.isEmpty(userConfigItem.getContact2())) {
            smsMessages.add(new SmsMessage(userConfigItem.getContact2(), messageText, user));
        }

        if (!TextUtils.isEmpty(userConfigItem.getContact3())) {
            smsMessages.add(new SmsMessage(userConfigItem.getContact3(), messageText, user));
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
