package com.sip.voip.server;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import com.sip.voip.R;
import com.sip.voip.utils.DatabaseHelper;
import com.sip.voip.utils.LinphoneUtils;
import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;
import org.linphone.core.PresenceBasicStatus;
import org.linphone.core.PresenceModel;
import org.linphone.core.Transports;
import org.linphone.core.tools.Log;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 初始化 linphone
 */
public class LinphoneManager {
    private Context mServiceContext;
    private static LinphoneManager instance;
    private static boolean sExited;
    private String mLinphoneFactoryConfigFile = null;
    public String mLinphoneConfigFile = null;
    private AccountCreator mAccountCreator;
    private static final String DEFAULT_ASSISTANT_RC = "/default_assistant_create.rc";
    private static final String LINPHONE_ASSISTANT_RC = "/linphone_assistant_create.rc";
    private String mUserCerts = null;
    private Resources mResources;
    private Core mCore;
    private CoreListener mCoreListener;
    private Timer mTimer;
    private Handler mHandler;
    private DatabaseHelper dbHelper;
    public LinphoneManager(Context serviceContext) {
        mServiceContext = serviceContext;
        String basePath = mServiceContext.getFilesDir().getAbsolutePath();
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled); //日志开关
        Factory.instance().setDebugMode(true, "Linphone");
        sExited = false;
        mLinphoneFactoryConfigFile = basePath + "/linphonerc";
        mLinphoneConfigFile = basePath + "/.linphonerc";
        mUserCerts = basePath + "/user-certs";
        mResources = serviceContext.getResources();
        mHandler = new Handler();
        dbHelper = new DatabaseHelper(serviceContext,"BookStore.db",null,1);

    }
    public static synchronized final DatabaseHelper getDatabaseHelper() {
        return getInstance().dbHelper;
    }
    public synchronized static final LinphoneManager createAndStart(Context context, CoreListener coreListener) {
        if (instance != null) {
            throw new RuntimeException("Linphone Manager is already initialized");
        }
        instance = new LinphoneManager(context);
        instance.startLibLinphone(context, coreListener);
        return instance;
    }
    private synchronized void startLibLinphone(Context context, CoreListener coreListener) {
        try {
            mCoreListener = coreListener;
            copyAssetsFromPackage();
            mCore = Factory.instance().createCore(mLinphoneConfigFile, mLinphoneFactoryConfigFile, context);
            mCore.addListener(coreListener);
            initLibLinphone();
            LinphoneUtils.dumpDeviceInformation();
            LinphoneUtils.dumpInstalledLinphoneInformation(context);
            LinphoneManager.setSipPort();
            mCore.start();
            TimerTask lTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyAssetsFromPackage() throws IOException {
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.linphonerc_default, mLinphoneConfigFile);
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.linphonerc_factory, new File(mLinphoneFactoryConfigFile).getName());
    }
    private void initLibLinphone() {
        File f = new File(mUserCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.e(mUserCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(mUserCerts);

    }
    public static synchronized Core getCoreIfManagerNotDestroyOrNull() {
        if (sExited || instance == null) {
            Log.e("Trying to get linphone core while LinphoneManager already destroyed or not created");
            return null;
        }
        return getCore();
    }
    public static synchronized final Core getCore() {
        return getInstance().mCore;
    }
    public static final boolean isInstanceiated() {
        return instance != null;
    }
    public static synchronized final LinphoneManager getInstance() {
        if (instance != null) {
            return instance;
        }
        if (sExited) {
            throw new RuntimeException("Linphone Manager was already destroyed. " + "Better use getLcIfManagerNotDestroyed and check returned value");
        }
        throw new RuntimeException("Linphone Manager should be created before accessed");
    }
    public static synchronized void destroy() {
        if (instance == null) {
            return;
        }
        sExited = true;
        instance.doDestroy();
    }
    private void doDestroy() {
        Log.w("[Manager] Destroying Manager");
        changeStatusToOffline();
        try {
            mCore.removeListener(mCoreListener);
            mTimer.cancel();
            mCore.stop();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mCore = null;
            instance = null;
        }
    }
    public void changeStatusToOnline() {
        if (mCore == null) return;
        PresenceModel model = mCore.createPresenceModel();
        model.setBasicStatus(PresenceBasicStatus.Open);
        mCore.setPresenceModel(model);
    }
    private void changeStatusToOffline() {
        if (mCore != null) {
            PresenceModel model = mCore.getPresenceModel();
            model.setBasicStatus(PresenceBasicStatus.Closed);
            mCore.setPresenceModel(model);
        }
    }
    private static Core getLc(){
        return LinphoneManager.getCore();
    }
    public static void setSipPort() {
        if (getLc() == null) return;
        Transports transports = getLc().getTransports();
        transports.setUdpPort(0);
        transports.setTcpPort(10003);
        getLc().setTransports(transports);
    }
}