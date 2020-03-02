package com.sip.voip.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sip.voip.R;
import com.sip.voip.common.RecyclerView.QuickAdapter;

import org.linphone.core.RegistrationState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;

    private QuickAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        View root = inflater.inflate(R.layout.fragment_login, container, false);


        RecyclerView loginRecords = (RecyclerView)root.findViewById(R.id.login_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());

        //设置布局管理器
        loginRecords.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        List<Map<String,String>> ls = initData();

        mAdapter = new QuickAdapter<Map<String,String>>(ls) {
            @Override
            public int getLayoutId(int viewType) {
                return R.layout.login_record_item;
            }

            @Override
            public void convert(VH holder, Map<String,String> data, int position) {
//                holder.setText(R.id.led, data.get("sip_state"));
                holder.setText(R.id.login_user, data.get("login_user"));
                holder.setText(R.id.login_sip, data.get("login_sip"));
                updateLed((ImageView)holder.getView(R.id.led),RegistrationState.fromInt(Integer.valueOf(data.get("sip_state"))));
//                holder.itemView.setOnClickListener(); 此处还可以添加点击事件
            }
        };
        //设置Adapter
        loginRecords.setAdapter(mAdapter);
        //设置分隔线
//        callRecords.addItemDecoration( new DividerGridItemDecoration(this ));
        //设置增加或删除条目的动画
        loginRecords.setItemAnimator( new DefaultItemAnimator());


        return root;
    }
    //更新信号灯函数
    private void updateLed(ImageView mLed, RegistrationState state) {
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
        itemData.put("sip_state","1");
        itemData.put("login_sip","111.111.11.11");
        itemData.put("login_user","111111111");
        ls.add(itemData);
        ls.add(itemData);
        for (int i = 0; i < 15; i++) {
            ls.add(itemData);
        }
        return  ls;
    }
}