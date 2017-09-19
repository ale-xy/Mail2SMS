package com.alexy.emailtosms.data;

import com.orm.dsl.Table;

import java.util.Date;

/**
 * Created by alexeykrichun on 18/09/2017.
 */

@Table(name="SERVICE_STATUS")
public class ServiceStatus{
    Date lastCheck;
    boolean success;
    String errorText;

    public ServiceStatus() {
    }

    public ServiceStatus(Date lastCheck, boolean success, String errorText) {
        this.lastCheck = lastCheck;
        this.success = success;
        this.errorText = errorText;
    }

    public Date getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(Date lastCheck) {
        this.lastCheck = lastCheck;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    @Override
    public String toString() {
        return "ServiceStatus{" +
                "lastCheck=" + lastCheck +
                ", success=" + success +
                ", errorText='" + errorText + '\'' +
                '}';
    }

}
