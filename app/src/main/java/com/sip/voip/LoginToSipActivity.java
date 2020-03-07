package com.sip.voip;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sip.voip.server.LinphoneService;
import com.sip.voip.utils.PhoneVoiceUtils;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

public class LoginToSipActivity extends Activity {
    private EditText mUsername,mPassword,mDomain;
    private AccountCreator mAccountCreator;
    private RadioGroup mTransport;
    private Button mConnect;
    private CoreListenerStub mCoreListener;
    private PhoneVoiceUtils phoneVoiceUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_window);
        mAccountCreator = LinphoneService.getCore().createAccountCreator(null);
        mUsername  = (EditText)findViewById(R.id.assistant_username) ;
        mPassword  = (EditText)findViewById(R.id.assistant_password) ;
        mDomain    = (EditText)findViewById(R.id.assistant_domain) ;
        mTransport = findViewById(R.id.assistant_transports);
        mConnect = findViewById(R.id.assistant_login);
        phoneVoiceUtils = PhoneVoiceUtils.getInstance();
        mConnect.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        configureAccount();
                    }
                });
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                if (state == RegistrationState.Ok) {
                    Toast.makeText(LoginToSipActivity.this, "Success: " + message, Toast.LENGTH_LONG).show();

                    finish();
                } else if (state == RegistrationState.Failed) {
                    Toast.makeText(LoginToSipActivity.this, "Failure: " + message, Toast.LENGTH_LONG).show();
                }
            }
        };
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LinphoneService.getCore().addListener(mCoreListener);
    }

    @Override
    protected void onPause() {
        LinphoneService.getCore().removeListener(mCoreListener);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void configureAccount() {
        TransportType tst = TransportType.Udp;
        switch (mTransport.getCheckedRadioButtonId()) {
            case R.id.transport_udp:
                tst = TransportType.Udp;
                break;
            case R.id.transport_tcp:
                tst = TransportType.Tcp;
                break;
            case R.id.transport_tls:
                tst = TransportType.Tls;
                break;
        }
        phoneVoiceUtils.registerUserAuth(mUsername.getText().toString(),mPassword.getText().toString()
                ,mDomain.getText().toString(),tst);
    }
}
