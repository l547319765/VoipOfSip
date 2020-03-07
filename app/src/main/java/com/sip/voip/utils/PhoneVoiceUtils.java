package com.sip.voip.utils;

import com.sip.voip.LinphoneManager;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;

public class PhoneVoiceUtils {

    private static volatile PhoneVoiceUtils sPhoneVoiceUtils;
    private Core mLinphoneCore = null;

    public static PhoneVoiceUtils getInstance() {
        if (sPhoneVoiceUtils == null) {
            synchronized (PhoneVoiceUtils.class) {
                if (sPhoneVoiceUtils == null) {
                    sPhoneVoiceUtils = new PhoneVoiceUtils();
                }
            }
        }
        return sPhoneVoiceUtils;
    }

    private PhoneVoiceUtils() {
        mLinphoneCore = LinphoneManager.getCore();
//        mLinphoneCore.enableEchoCancellation(true);
//        mLinphoneCore.enableEchoLimiter(true);
    }

    /**
     * 注册到服务器
     *
     * @param name     账号名
     * @param password 密码
     * @param host     IP地址：端口号
     */
    public void registerUserAuth(String name, String password, String host) {
        registerUserAuth(name, password, host, TransportType.Udp);
    }

    /**
     * 注册到服务器
     *
     * @param name     账号名
     * @param password 密码
     * @param host     IP地址：端口号
     * @param type     TransportType.Udp TransportType.Tcp TransportType.Tls
     */
    public void registerUserAuth(String name, String password, String host, TransportType type) {
        //    String identify = "sip:" + name + "@" + host;
        AccountCreator mAccountCreator = mLinphoneCore.createAccountCreator(null);

        mAccountCreator.setUsername(name);
        mAccountCreator.setDomain(host);
        mAccountCreator.setPassword(password);
        mAccountCreator.setTransport(type);

        ProxyConfig cfg = mAccountCreator.createProxyConfig();
        // Make sure the newly created one is the default
        mLinphoneCore.setDefaultProxyConfig(cfg);
    }

    //取消注册
    public void unRegisterUserAuth() {
        mLinphoneCore.clearAllAuthInfo();
    }

    /**
     * 是否已经注册了
     *
     * @return
     */
    public boolean isRegistered() {
        AuthInfo[] authInfos = mLinphoneCore.getAuthInfoList();
        if (authInfos.length > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 拨打电话
     *
     * @param phone 手机号
     * @return
     */
    public Call startSingleCallingTo(String phone) {
        Call call = null;
        try {
            Address addressToCall = mLinphoneCore.interpretUrl(phone);

            CallParams params = mLinphoneCore.createCallParams(null);

            params.enableVideo(false); //不可视频

            if (addressToCall != null) {
                call = mLinphoneCore.inviteAddressWithParams(addressToCall, params);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return call;
    }

    /**
     * 挂断电话
     */
    public void hangUp() {
        if (mLinphoneCore == null) {
            mLinphoneCore = LinphoneManager.getCore();
        }

        Call currentCall = mLinphoneCore.getCurrentCall();
        if (currentCall != null) {
            mLinphoneCore.terminateCall(currentCall);
        } else if (mLinphoneCore.isInConference()) {
            mLinphoneCore.terminateConference();
        } else {
            mLinphoneCore.terminateAllCalls();
        }

    }

    /**
     * 是否静音
     *
     * @param isMicMuted
     */
    public void toggleMicro(boolean isMicMuted) {
        if (mLinphoneCore == null) {
            mLinphoneCore = LinphoneManager.getCore();
        }
        mLinphoneCore.enableMic(isMicMuted);
    }

    /**
     * 接听来电
     *
     * @param
     */
    public void receiveCall(Call call) {
        if (mLinphoneCore == null) {
            mLinphoneCore = LinphoneManager.getCore();
        }
        CallParams params = mLinphoneCore.createCallParams(call);
        params.enableVideo(false);
        if (null != call) {
            call.acceptWithParams(params);
        }
    }

}