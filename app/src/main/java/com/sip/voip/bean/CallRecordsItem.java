package com.sip.voip.bean;

public class CallRecordsItem {

    private String resId;
    private String callSip;
    private String sipRecord;
    private String startTime;
    private String inOrOut;

    public String getInOrOut() {
        return inOrOut;
    }

    public void setInOrOut(String inOrOut) {
        this.inOrOut = inOrOut;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public String getCallSip() {
        return callSip;
    }

    public void setCallSip(String callSip) {
        this.callSip = callSip;
    }

    public String getSipRecord() {
        return sipRecord;
    }

    public void setSipRecord(String sipRecord) {
        this.sipRecord = sipRecord;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }



    public CallRecordsItem(String resId, String callSip, String sipRecord, String startTime, String inOrOut) {
        this.resId = resId;
        this.callSip = callSip;
        this.sipRecord = sipRecord;
        this.startTime = startTime;
        this.inOrOut = inOrOut;
    }

}