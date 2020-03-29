package com.sip.voip.utils;

import com.sip.voip.bean.CallRecordsItem;

public class CallRecordFactory {

    private static volatile CallRecordsItem callRecordsItem;

    private CallRecordFactory(){}

    public static CallRecordsItem newInstance() {
        if(callRecordsItem == null){
            synchronized(CallRecordFactory.class) {
                if (callRecordsItem == null) {
                    callRecordsItem = new CallRecordsItem();
                    return callRecordsItem;
                }
            }
        }
        return callRecordsItem;
    }

    public static boolean isInstance(){
        return (callRecordsItem ==null)?true:false;
    }

    public static void doDestroy(){
        callRecordsItem = null;
    }
}
