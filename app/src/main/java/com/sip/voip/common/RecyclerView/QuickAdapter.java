package com.sip.voip.common.RecyclerView;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class QuickAdapter<T> extends RecyclerView.Adapter<QuickAdapter.VH>{
    //数据
    private List<T> mDatas;
    public QuickAdapter(List<T> datas){
        this.mDatas = datas;
    }

    //通过传入的类型判断这个数据放在哪里。我的数据是4个
    public abstract int getLayoutId(int viewType);

    //获取当前item的布局
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return VH.get(parent,getLayoutId(viewType));
    }

    //数据渲染进view
    @Override
    public void onBindViewHolder(VH holder, int position) {
        convert(holder, mDatas.get(position), position);
    }
    //获得总数
    @Override
    public int getItemCount() {
        return mDatas.size();
    }


    public abstract void convert(VH holder, T data, int position);

    //holder里四个view，分别是4个数据
    public static class VH extends RecyclerView.ViewHolder{
        //id和视图之间开启对应
        private SparseArray<View> mViews;
        //item
        private View mConvertView;

        private VH(View v){
            super(v);
            mConvertView = v;
            mViews = new SparseArray<>();
        }

        public static VH get(ViewGroup parent, int layoutId){
            View convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new VH(convertView);
        }


        //获取view
        public <T extends View> T getView(int id){
            View v = mViews.get(id);
            if(v == null){
                v = mConvertView.findViewById(id);
                mViews.put(id, v);
            }
            return (T)v;
        }
        //设置数据
        public void setText(int id, String value){
            TextView view = getView(id);
            view.setText(value);
        }
    }
}