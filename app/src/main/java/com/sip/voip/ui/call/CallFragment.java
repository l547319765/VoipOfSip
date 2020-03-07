package com.sip.voip.ui.call;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sip.voip.R;
import com.sip.voip.common.RecyclerView.QuickAdapter;
import com.sip.voip.server.LinphoneService;

import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.widget.LinearLayout.VERTICAL;

public class CallFragment extends Fragment {
    private CallViewModel homeViewModel;
    private QuickAdapter mAdapter;
    private ImageView mLed;
    private CoreListenerStub mCoreListener;
    private EditText mSipAddressToCall;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(CallViewModel.class);
        View root = inflater.inflate(R.layout.fragment_call, container, false);

        mSipAddressToCall = (EditText)root.findViewById(R.id.tel_number);


        mLed  = (ImageView)root.findViewById(R.id.static_led);
        // Monitors the registration state of our account(s) and update the LED accordingly
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                updateLed(state);
            }
        };

        Button call = (Button)root.findViewById(R.id.call);

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Core core = LinphoneService.getCore();
                Address addressToCall = core.interpretUrl(mSipAddressToCall.getText().toString());
                CallParams params = core.createCallParams(null);

//                Switch videoEnabled = findViewById(R.id.call_with_video);
//                params.enableVideo(videoEnabled.isChecked());

                if (addressToCall != null) {
                    core.inviteAddressWithParams(addressToCall, params);
                }
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

        // The best way to use Core listeners in Activities is to add them in onResume
        // and to remove them in onPause
        LinphoneService.getCore().addListener(mCoreListener);

        // Manually update the LED registration state, in case it has been registered before
        // we add a chance to register the above listener
        ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
        if (proxyConfig != null) {
            updateLed(proxyConfig.getState());
        } else {
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
    private void updateLed(RegistrationState state) {
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




}