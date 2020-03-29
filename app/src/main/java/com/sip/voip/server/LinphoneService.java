package com.sip.voip.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.sip.voip.CallActivity;
import com.sip.voip.bean.CallRecordsItem;
import com.sip.voip.utils.CallRecordFactory;
import com.sip.voip.utils.DatabaseHelper;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.tools.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LinphoneService extends Service {
    private static LinphoneService sInstance;
    private static PhoneServiceCallback sPhoneServiceCallback;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss
    private Core mCore;
    public static void addCallback(PhoneServiceCallback phoneServiceCallback) {
        sPhoneServiceCallback = phoneServiceCallback;
    }
    public static boolean isReady() {
        return sInstance != null;
    }
    public static Core getCore() {
        if(sInstance.mCore==null){
            sInstance.mCore = LinphoneManager.getCore();
        }
        return sInstance.mCore;
    }
    //监听
    private CoreListenerStub mCoreListener = new CoreListenerStub() {
        /**
         *  通话状态
         * @param lc
         * @param call
         * @param cstate
         * @param message
         */
        @Override
        public void onCallStateChanged(Core lc, Call call, Call.State cstate, String message) {
            Log.i("zss", "---- 通话状态  [ 状态：" + cstate + "  ；消息：  " + message + " ]");
            if (cstate == Call.State.IncomingReceived) { //来电
                Toast.makeText(LinphoneService.this, "Incoming call received, answering it automatically", Toast.LENGTH_LONG).show();
                CallParams params = getCore().createCallParams(call);
                params.enableVideo(false);
                call.acceptWithParams(params);
                if (null != sPhoneServiceCallback) {
                    sPhoneServiceCallback.incomingCall(call);
                }
            } else if (cstate == Call.State.OutgoingProgress) { //正在呼叫
                if (null != sPhoneServiceCallback) {
                    sPhoneServiceCallback.OutgoingProgress(call);
                }
            } else if (cstate == Call.State.Connected) { //接通或者拒绝
                Intent intent = new Intent(LinphoneService.this, CallActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (null != sPhoneServiceCallback) {
                    sPhoneServiceCallback.callConnected(call);
                }
            } else if (cstate == Call.State.End || (cstate == Call.State.Released)) { //挂断，未接
                if (null != sPhoneServiceCallback) {
                    sPhoneServiceCallback.callReleased(call);
                }
            }
        }

        /**
         * 注册状态
         * @param lc
         * @param cfg
         * @param cstate
         * @param message
         */
        @Override
        public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState cstate, String message) {
            if (null != sPhoneServiceCallback) {
                sPhoneServiceCallback.onRegistrationStateChanged(lc, cfg, cstate, message);
            }
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.i("zss", "---- Service_onCreate ");
        LinphoneManager.createAndStart(LinphoneService.this, mCoreListener);
        LinphoneService.addCallback(new PhoneServiceCallback() {
            @Override
            public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState cstate, String message) {
                super.onRegistrationStateChanged(lc, cfg, cstate, message);
            }

            @Override
            public void incomingCall(Call linphoneCall) {
                super.incomingCall(linphoneCall);
            }
            @Override
            public void OutgoingProgress(Call linphoneCall) {
                super.OutgoingProgress(linphoneCall);
            }

            @Override
            public void callConnected(Call linphoneCall) {
                super.callConnected(linphoneCall);
            }

            @Override
            public void callReleased(Call linphoneCall) {
                super.callReleased(linphoneCall);
                linphoneCall.getRemoteAddress();
            }
        });
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("zss", "---- Service_onStartCommand ");
        // If our Service is already running, no need to continue
        if (sInstance != null) {
            return START_STICKY;
        }
        sInstance = this;
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        Log.i("zss", "---- Service_onDestroy ");
        sInstance = null;
        LinphoneManager.destroy();
        super.onDestroy();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("zss", "---- Service_onTaskRemoved ");
        sInstance = null;
        LinphoneManager.destroy();
        // For this sample we will kill the Service at the same time we kill the app
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}