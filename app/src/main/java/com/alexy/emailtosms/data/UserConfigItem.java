package com.alexy.emailtosms.data;

import com.orm.dsl.Table;
import com.orm.dsl.Unique;

import java.util.Date;


/**
 * Created by Alexey on 10/09/2017.
 */

@Table(name="USER_CONFIG_ITEM")
public class UserConfigItem{
    public final static String[] HEADERS = new String[] {"userId", "userName", "contact1", "contact2", "contact3", "isValid", "messageSlot1", "messageSlot2", "messageSlot3"} ;

    @Unique
    String userId;

    String userName;

    String contact1;

    String contact2;

    String contact3;

    boolean isValid;

    String messageSlot1;

    String messageSlot2;

    String messageSlot3;

    Date lastMessageDate;

    public UserConfigItem() {
    }

    public UserConfigItem(String userId, String userName, String contact1, String contact2, String contact3, boolean isValid, String messageSlot1, String messageSlot2, String messageSlot3) {
        this.userId = userId;
        this.userName = userName;
        this.contact1 = contact1;
        this.contact2 = contact2;
        this.contact3 = contact3;
        this.isValid = isValid;
        this.messageSlot1 = messageSlot1;
        this.messageSlot2 = messageSlot2;
        this.messageSlot3 = messageSlot3;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContact1() {
        return contact1;
    }

    public void setContact1(String contact1) {
        this.contact1 = contact1;
    }

    public String getContact2() {
        return contact2;
    }

    public void setContact2(String contact2) {
        this.contact2 = contact2;
    }

    public String getContact3() {
        return contact3;
    }

    public void setContact3(String contact3) {
        this.contact3 = contact3;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean valid) {
        isValid = valid;
    }

    public String getMessageSlot1() {
        return messageSlot1;
    }

    public void setMessageSlot1(String messageSlot1) {
        this.messageSlot1 = messageSlot1;
    }

    public String getMessageSlot2() {
        return messageSlot2;
    }

    public void setMessageSlot2(String messageSlot2) {
        this.messageSlot2 = messageSlot2;
    }

    public String getMessageSlot3() {
        return messageSlot3;
    }

    public void setMessageSlot3(String messageSlot3) {
        this.messageSlot3 = messageSlot3;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    @Override
    public String toString() {
        return "UserConfigItem{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", contact1='" + contact1 + '\'' +
                ", contact2='" + contact2 + '\'' +
                ", contact3='" + contact3 + '\'' +
                ", isValid=" + isValid +
                ", messageSlot1='" + messageSlot1 + '\'' +
                ", messageSlot2='" + messageSlot2 + '\'' +
                ", messageSlot3='" + messageSlot3 + '\'' +
                ", lastMessageDate=" + lastMessageDate +
                '}';
    }
}
