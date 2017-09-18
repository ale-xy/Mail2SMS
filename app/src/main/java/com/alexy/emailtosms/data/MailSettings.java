package com.alexy.emailtosms.data;

/**
 * Created by Alexey on 10/09/2017.
 */

public class MailSettings {
    public String mailDomain;
    public String mailServer;
    public String mailUser;
    public String mailPassword;
    public String inboxFolder;

    public String notifiedFolder;
    public String timeoutFolder;
    public String notNotifiedFolder;
    public String ignoredFolder;

    public int checkPeriod;
    public int smsTimeout;
}
