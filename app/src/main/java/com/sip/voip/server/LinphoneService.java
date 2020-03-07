package com.sip.voip.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.sip.voip.CallActivity;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.tools.Log;

public class LinphoneService extends Service {
    private static LinphoneService sInstance;
    private static PhoneServiceCallback sPhoneServiceCallback;
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
/*  方式一
                Log.i("zss","----- getRemoteAddress().getUsername: " + call.getRemoteAddress().getUsername() + "  getRemoteAddress().getDomain: " +  call.getRemoteAddress().getDomain() + "  getRemoteAddress().getDisplayName:" + call.getRemoteAddress().getDisplayName() + "  getRemoteAddress().getPort:" + call.getRemoteAddress().getPort() + "  getUsername: " + call.getRemoteAddress().getPassword() );
                Log.i("zss", "----getTlsCert: " + authInfo[i].getTlsCert() + "   getTlsCertPath:" + authInfo[i].getTlsCertPath());
                Intent intent = new Intent(LinphoneService.this, CustomReceiveActivity.class);
                CustomReceiveActivity.getReceivedCallFromService(call);
                ReceiveDataModel receiveDataModel = new ReceiveDataModel();
                receiveDataModel.setActiveCall(false);
                receiveDataModel.setNum(call.getRemoteAddress().getUsername());
                intent.putExtra("ReceiveDataModel", receiveDataModel);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                if (null != sPhoneServiceCallback) {
                    Log.i("zss", "---- sPhoneServiceCallback ");
                    sPhoneServiceCallback.incomingCall(call);
                }
*/
                Toast.makeText(LinphoneService.this, "Incoming call received, answering it automatically", Toast.LENGTH_LONG).show();
                CallParams params = getCore().createCallParams(call);
                params.enableVideo(true);
                call.acceptWithParams(params);
            } else if (cstate == Call.State.OutgoingProgress) { //正在呼叫

            } else if (cstate == Call.State.Connected) { //接通或者拒绝
//                if (null != sPhoneServiceCallback) {
//                    sPhoneServiceCallback.callConnected();
//                }
                    Intent intent = new Intent(LinphoneService.this, CallActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
            } else if (cstate == Call.State.End || (cstate == Call.State.Released)) { //挂断，未接
                if (null != sPhoneServiceCallback) {
                    sPhoneServiceCallback.callReleased();
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