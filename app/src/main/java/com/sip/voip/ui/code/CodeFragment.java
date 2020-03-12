package com.sip.voip.ui.code;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sip.voip.R;
import com.sip.voip.bean.CodeCs;
import com.sip.voip.common.RecyclerView.QuickAdapter;
import com.sip.voip.server.LinphoneManager;

import org.linphone.core.Core;
import org.linphone.core.PayloadType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CodeFragment extends Fragment {

    private CodeViewModel codeViewModel;
    private QuickAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_code, container, false);
        RecyclerView callRecords = (RecyclerView)root.findViewById(R.id.call_records);
        LinearLayoutManager layoutManager = new LinearLayoutManager(root.getContext());
        //设置布局管理器
        callRecords.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);




















        mAdapter = new QuickAdapter<CodeCs>(initCodeCs()) {
            @Override
            public int getLayoutId(int viewType) {
                return R.layout.code_item;
            }
            @Override
            public void convert(VH holder, final CodeCs data, int position) {
                holder.setText(R.id.code_name,data.getTitle());
                holder.setText(R.id.code_rate,data.getSubTitle());
                Switch codeEnable = holder.getView(R.id.code_enable);
                codeEnable.setChecked(data.isEnabled());
                // 添加监听
                codeEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        data.getPayloadType().enable(isChecked);
                    }
                });
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
    public List<CodeCs> initCodeCs() {
        Core core = LinphoneManager.getCore();
        List ls = new LinkedList<CodeCs>();
        if (core != null) {
            for (final PayloadType pt : core.getAudioPayloadTypes()) {
                String title = pt.getMimeType();
                if (pt.getMimeType().equals("mpeg4-generic")) {
                    title="AAC-ELD";
                }
                String subTitle = pt.getClockRate()+"Hz";
                boolean bt = pt.enabled();
                CodeCs cc = new CodeCs(pt,title,subTitle,bt);
                ls.add(cc);
            }
        }
        return ls;
    }
    public void changeCodeCs(){

    }
}