package com.sip.voip.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.sip.voip.bean.CallRecordsItem;

import java.util.LinkedList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;

    public static final String  CREATE_BOOK  ="create table CallRecord(" +
            "res_id integer primary key autoincrement," +
            "call_sip text," +
            "connect_situation text," +
            "start_time text," +
            "in_or_out text)";


    public DatabaseHelper (Context context,String name,SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_BOOK);
        Toast.makeText(mContext,"Create success",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db ,int oldVersion ,int newVersion){

    }

    public static void insertCallRecord(SQLiteDatabase db , CallRecordsItem callRecordsItem){
        ContentValues values = new ContentValues();
        values.put("call_sip",callRecordsItem.getCallSip());
        values.put("connect_situation",callRecordsItem.getConnectSituation());
        values.put("start_time",callRecordsItem.getStartTime());
        values.put("in_or_out",callRecordsItem.getInOrOut());
        db.insert("CallRecord",null,values);
        values.clear();
    }

    public static void updateCallRecord(SQLiteDatabase db , CallRecordsItem callRecordsItem){
        ContentValues values = new ContentValues();
        values.put("call_sip",callRecordsItem.getCallSip());
        values.put("connect_situation",callRecordsItem.getConnectSituation());
        values.put("start_time",callRecordsItem.getStartTime());
        values.put("in_or_out",callRecordsItem.getInOrOut());
        db.update("CallRecord",values,"res_id = ?",new String[]{callRecordsItem.getResId()});
        values.clear();
    }

    public static void deleteCallRecord(SQLiteDatabase db ,CallRecordsItem callRecordsItem){
        db.delete("CallRecord","res_id = ?",new String[]{callRecordsItem.getResId()});
    }

    public static List selectCallRecord(SQLiteDatabase db){
        Cursor cursor = db.query("CallRecord",null,null,null,null,null,null);
        LinkedList CallRecords = new LinkedList<CallRecordsItem>();
        if(cursor.getCount()>0){
            do{
                CallRecordsItem callRecordsItem = new CallRecordsItem();
                callRecordsItem.setResId(cursor.getString(cursor.getColumnIndex("res_id")));
                callRecordsItem.setResId(cursor.getString(cursor.getColumnIndex("call_sip")));
                callRecordsItem.setResId(cursor.getString(cursor.getColumnIndex("connect_situation")));
                callRecordsItem.setResId(cursor.getString(cursor.getColumnIndex("start_time")));
                callRecordsItem.setResId(cursor.getString(cursor.getColumnIndex("in_or_out")));
                CallRecords.add(callRecordsItem);
            }while(cursor.moveToNext());
        }
        return CallRecords;
    }
}
