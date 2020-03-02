package com.sip.voip.bean;


public class LoginRecordsItem {
    private String resId ;
    private String sipLogin;

    public LoginRecordsItem(String resId, String sipLogin, String sipDomain) {
        this.resId = resId;
        this.sipLogin = sipLogin;
        this.sipDomain = sipDomain;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public String getSipLogin() {
        return sipLogin;
    }

    public void setSipLogin(String sipLogin) {
        this.sipLogin = sipLogin;
    }

    public String getSipDomain() {
        return sipDomain;
    }

    public void setSipDomain(String sipDomain) {
        this.sipDomain = sipDomain;
    }

    private String sipDomain;

}
