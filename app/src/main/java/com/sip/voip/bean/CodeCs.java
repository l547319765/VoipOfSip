package com.sip.voip.bean;

import org.linphone.core.PayloadType;

public class CodeCs {
    private PayloadType payloadType;

    public CodeCs(PayloadType payloadType, String title, String subTitle, boolean enabled) {
        this.payloadType = payloadType;
        this.title = title;
        SubTitle = subTitle;
        this.enabled = enabled;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return SubTitle;
    }

    public void setSubTitle(String subTitle) {
        SubTitle = subTitle;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private String title;
    private String SubTitle;
    private boolean enabled;

}
