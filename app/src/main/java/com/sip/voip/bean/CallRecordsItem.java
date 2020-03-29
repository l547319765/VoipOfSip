package com.sip.voip.bean;

public class CallRecordsItem {

    private String resId;
    private String callSip;
    private String connectSituation;
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

    public String getConnectSituation() {
        return connectSituation;
    }

    public void setConnectSituation(String connectSituation) {
        this.connectSituation = connectSituation;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }



    public CallRecordsItem(String callSip, String connectSituation, String startTime, String inOrOut) {
        this.callSip = callSip;
        this.connectSituation = connectSituation;
        this.startTime = startTime;
        this.inOrOut = inOrOut;
    }
    public CallRecordsItem() {
    }

}