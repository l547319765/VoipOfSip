package com.sip.voip.ui.call;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sip.voip.LoginToSipActivity;
import com.sip.voip.R;
import com.sip.voip.common.RecyclerView.QuickAdapter;
import com.sip.voip.server.LinphoneManager;
import com.sip.voip.server.LinphoneService;
import com.sip.voip.utils.PhoneVoiceUtils;

import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.tools.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallFragment extends Fragment {
//    private CallViewModel homeViewModel;
    private QuickAdapter mAdapter;
    private ImageView mLed;
    private CoreListenerStub mCoreListener;
    private EditText mSipAddressToCall;
    private TextView mLoginUser;
    private AuthInfo[] authInfos;
    private Button logOut;
    private Button call;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        homeViewModel = new ViewModelProvider(this).get(CallViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_call, container, false);
        mLoginUser = root.findViewById(R.id.my_login_user);
        call = root.findViewById(R.id.call);
        logOut = root.findViewById(R.id.log_out);
        mSipAddressToCall = root.findViewById(R.id.tel_number);
        mLed  = root.findViewById(R.id.static_led);
        populateSliderContent();
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(final Core core, final ProxyConfig cfg, final RegistrationState state, String message) {
                if (core.getProxyConfigList() == null || core.getAuthInfoList().length == 0) {
                    showNoAccountConfigured();
                    return;
                }
                if ((core.getDefaultProxyConfig() != null && core.getDefaultProxyConfig().equals(cfg)) || core.getDefaultProxyConfig() == null) {
                            updateLed(state,core);
                }
                try {
                    mLoginUser.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Core core = LinphoneManager.getCore();
                                    if (core != null) {
                                        core.refreshRegisters();
                                    }
                                }
                            });
                } catch (IllegalStateException ise) {
                    Log.e(ise);
                }
            }
        };


        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogOut(root.getContext());
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Core core = LinphoneService.getCore();
                Address addressToCall = core.interpretUrl(mSipAddressToCall.getText().toString());
                CallParams params = core.createCallParams(null);
                if (addressToCall != null) {
                    core.inviteAddressWithParams(addressToCall, params); }
            }
        });
        RecyclerView callRecords = (RecyclerView)root.findViewById(R.id.call_records);
        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());
        //设置布局管理器
        callRecords.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        List<Map<String,String>> ls = initData();
        mAdapter = new QuickAdapter<Map<String,String>>(ls) {
            @Override
            public int getLayoutId(int viewType) {
                return R.layout.call_record_item;
            }
            @Override
            public void convert(VH holder, Map<String,String> data, int position) {
                holder.setText(R.id.sip_domain, data.get("sip_domain"));
                holder.setText(R.id.in_or_out, data.get("in_or_out"));
                holder.setText(R.id.start_time, data.get("start_time"));
                holder.setText(R.id.connect_situation, data.get("connect_situation"));
//                holder.itemView.setOnClickListener(); 此处还可以添加点击事件
            }
        };
        //设置Adapter
        callRecords.setAdapter(mAdapter);
        //设置分隔线
//        callRecords.addItemDecoration( new DividerGridItemDecoration(this ));
        //设置增加或删除条目的动画
        callRecords.setItemAnimator( new DefaultItemAnimator());

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Core core = LinphoneManager.getCore();

        // The best way to use Core listeners in Activities is to add them in onResume
        // and to remove them in onPause
        core.addListener(mCoreListener);

        // Manually update the LED registration state, in case it has been registered before
        // we add a chance to register the above listener
        ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
        if (proxyConfig != null) {
            mCoreListener.onRegistrationStateChanged(core, proxyConfig, proxyConfig.getState(), null);
        } else {
            showNoAccountConfigured();
            // No account configured, we display the configuration activity
//            startActivity(new Intent(this, ConfigureAccountActivity.class));
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        // Like I said above, remove unused Core listeners in onPause
        LinphoneService.getCore().removeListener(mCoreListener);
    }
    //更新信号灯函数
    private void updateLed(RegistrationState state,Core core) {
        authInfos = core.getAuthInfoList();
        if(authInfos.length>0){
            mLoginUser.setText(authInfos[0].getUsername());
        }else{
            mLoginUser.setText(R.string.no_login_account);
        }
        switch (state) {
            case Ok: // This state means you are connected, to can make and receive calls & messages
                mLed.setImageResource(R.drawable.led_connected);
                break;
            case None: // This state is the default state
            case Cleared: // This state is when you disconnected
                mLed.setImageResource(R.drawable.led_disconnected);
                break;
            case Failed: // This one means an error happened, for example a bad password
                mLed.setImageResource(R.drawable.led_error);
                break;
            case Progress: // Connection is in progress, next state will be either Ok or Failed
                mLed.setImageResource(R.drawable.led_inprogress);
                break;
        }

    }

    public List<Map<String,String>> initData(){
        List<Map<String,String>> ls = new ArrayList<Map<String,String>>();
        HashMap<String,String> itemData= new HashMap<>();
        itemData.put("sip_domain","120.25.237.138:111");
        itemData.put("in_or_out","已接通");
        itemData.put("start_time","18：11PM");
        itemData.put("connect_situation","拨入");
        ls.add(itemData);
        ls.add(itemData);
        for (int i = 0; i < 15; i++) {
            ls.add(itemData);
        }
        return  ls;
    }

    private void checkLogOut(Context context){
        AlertDialog.Builder bb = new AlertDialog.Builder(context);
        bb.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PhoneVoiceUtils.getInstance().unRegisterUserAuth();
                startActivity(
                        new Intent()
                                .setClass(
                                        getActivity(),
                                        LoginToSipActivity.class));
//                getActivity().finish();
                }
            });
        bb.setNegativeButton("取消",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        bb.setMessage("是否确认注销");
        bb.setTitle("提示");
        bb.show();
    }

    private void populateSliderContent() {
        if (LinphoneManager.getCore().getProxyConfigList().length == 0) {
            showNoAccountConfigured();
        }
    }

    private void showNoAccountConfigured() {
        mLed.setImageResource(R.drawable.led_disconnected);
        mLoginUser.setText(getString(R.string.no_login_account));
    }

}