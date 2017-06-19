package com.neladyn.domain;

public class AccessTokenData {
    private long app_id;
    private String application;
    private long expires_at;
    private boolean is_valid;
    private long issued_at;
    private long user_id;

    public AccessTokenData() {
    }

    public long getApp_id() {
        return app_id;
    }

    public void setApp_id(long app_id) {
        this.app_id = app_id;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public long getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(long expires_at) {
        this.expires_at = expires_at;
    }

    public boolean isIs_valid() {
        return is_valid;
    }

    public void setIs_valid(boolean is_valid) {
        this.is_valid = is_valid;
    }

    public long getIssued_at() {
        return issued_at;
    }

    public void setIssued_at(long issued_at) {
        this.issued_at = issued_at;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "AccessTokenData{" +
                "app_id=" + app_id +
                ", application='" + application + '\'' +
                ", expires_at=" + expires_at +
                ", is_valid=" + is_valid +
                ", issued_at=" + issued_at +
                ", user_id=" + user_id +
                '}';
    }
}
