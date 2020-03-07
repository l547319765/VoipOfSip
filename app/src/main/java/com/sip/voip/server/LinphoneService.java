package com.sip.voip.server;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.sip.voip.CallActivity;
import com.sip.voip.LinphoneManager;
import com.sip.voip.R;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.tools.Log;
import org.linphone.mediastream.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class LinphoneService extends Service {
    private static final String START_LINPHONE_LOGS = " ==== Device information dump ====";
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
        return sInstance.mCore;
    }
    private Handler mHandler;
    private Timer mTimer;
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
/*方式原
                Toast.makeText(LinphoneService.this, "Incoming call received, answering it automatically", Toast.LENGTH_LONG).show();
                CallParams params = getCore().createCallParams(call);
                params.enableVideo(true);
                call.acceptWithParams(params);
 */
            } else if (cstate == Call.State.OutgoingProgress) { //正在呼叫

            } else if (cstate == Call.State.Connected) { //接通或者拒绝
//                if (null != sPhoneServiceCallback) {
//                    sPhoneServiceCallback.callConnected();
//                }
//方式原
//                    Intent intent = new Intent(LinphoneService.this, CallActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
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
    public static LinphoneService getInstance() {
        return sInstance;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
//        LinphoneManager.createAndStart(LinphoneService.this, mCoreListnerStub);
        String basePath = getFilesDir().getAbsolutePath();
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, getString(R.string.app_name));
        Log.i(START_LINPHONE_LOGS);
        dumpDeviceInformation();
        dumpInstalledLinphoneInformation();
        mHandler = new Handler();
        try {
            copyIfNotExist(R.raw.linphonerc_default, basePath + "/.linphonerc");
            copyFromPackage(R.raw.linphonerc_factory, "linphonerc");
        } catch (IOException ioe) {
            Log.e(ioe);
        }
        // Create the Core and add our listener
        mCore = Factory.instance()
                .createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);
        mCore.addListener(mCoreListener);
        // Core is ready to be configured
        configureCore();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (sInstance != null) {
            return START_STICKY;
        }
        sInstance = this;

        // Core must be started after being created and configured
        mCore.start();
        // We also MUST call the iterate() method of the Core on a regular basis
        TimerTask lTask = new TimerTask() {
                    @Override
                    public void run() { mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCore != null) {
                                            mCore.iterate();
                                        }
                                    }
                                });
                    }
                };
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        mCore.removeListener(mCoreListener);
        mTimer.cancel();
        mCore.stop();
        // A stopped Core can be started again
        // To ensure resources are freed, we must ensure it will be garbage collected
        mCore = null;
        // Don't forget to free the singleton as well
        sInstance = null;

        super.onDestroy();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // For this sample we will kill the Service at the same time we kill the app
        stopSelf();

        super.onTaskRemoved(rootIntent);
    }

    private void configureCore() {
        // We will create a directory for user signed certificates if needed
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.e(userCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(userCerts);
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Supported ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        sb.append("\n");
        Log.i(sb.toString());
    }
    private void dumpInstalledLinphoneInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.e(nnfe);
        }

        if (info != null) {
            Log.i(
                    "[Service] Linphone version is ",
                    info.versionName + " (" + info.versionCode + ")");
        } else {
            Log.i("[Service] Linphone version is unknown");
        }
    }
    private void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }
    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }
}
