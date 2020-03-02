package com.sip.voip.ui.call;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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

import static android.content.ContentValues.TAG;
import static android.widget.LinearLayout.VERTICAL;

public class CallFragment extends Fragment {

    private CallViewModel homeViewModel;

    private QuickAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(CallViewModel.class);

        View root = inflater.inflate(R.layout.fragment_call, container, false);

        EditText telNumber = (EditText)root.findViewById(R.id.tel_number);

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