package com.example.doctorbabu.SqliteDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.doctorbabu.DatabaseModels.AlarmListModel;

import java.util.ArrayList;

public class SqliteDatabase extends SQLiteOpenHelper {
    Context  context;
    private static final String DATABASE_NAME = "AlarmList.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME ="Alarm_list";

    private static final String COLUMN_ID ="id";
    private static final String COLUMN_MEDICINE_NAME ="medicine_Name";
    private static final String COLUMN_HOUR ="hour";
    private static final String COLUMN_MINUTE ="minute";
    private static final String COLUMN_BROADCAST_CODE ="broadcast_code";
    private static final String COLUMN_ALARM_TYPE ="alarm_type";
    private static final String COLUMN_ALARM_STATUS ="alarm_status";
    private static final String APPOINTMENT_TABLE ="Alarm_list";
    private static final String USER_ID ="user_id";
    private static final String APPOINTMENT_ID ="appointment_id";



    AlarmListModel alarmListModel;
    ArrayList<AlarmListModel> modelsArrayList;
    public SqliteDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String query = "create table if not exists "+TABLE_NAME+" ("+COLUMN_ID+" STRING PRIMARY KEY, "
                        +COLUMN_MEDICINE_NAME+" TEXT, "+COLUMN_HOUR+" INTEGER, "+COLUMN_MINUTE+" INTEGER, "+COLUMN_BROADCAST_CODE+" INTEGER, "
                        +COLUMN_ALARM_TYPE+" TEXT, "+COLUMN_ALARM_STATUS+" INTEGER);";
        database.execSQL(query);
        String appointmentQuery = "create table if not exists "+APPOINTMENT_TABLE+" ("+USER_ID+" STRING PRIMARY KEY, "
                +APPOINTMENT_ID+" TEXT);";
        database.execSQL(appointmentQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }



    public void insertAppointmentInfo(String userID,String appointmentID){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_ID,userID);
        contentValues.put(APPOINTMENT_ID,appointmentID);
        database.insert(APPOINTMENT_TABLE,null,contentValues);

    }

    public boolean searchAppointmentID(String appointmentID){
        String query = "Select *from "+APPOINTMENT_TABLE+ " WHERE appointment_id = "+appointmentID+";";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = null;
        alarmListModel = new AlarmListModel();
        if(database != null) {
            cursor = database.rawQuery(query, null);
        }
        return cursor != null;
    }
    public long addAlarmInfo(String id, String medicineName,int hour, int minute, int broadcastCode, String alarmType, int alarmStatus){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID,id);
        contentValues.put(COLUMN_MEDICINE_NAME,medicineName);
        contentValues.put(COLUMN_HOUR,hour);
        contentValues.put(COLUMN_MINUTE,minute);
        contentValues.put(COLUMN_BROADCAST_CODE,broadcastCode);
        contentValues.put(COLUMN_ALARM_TYPE,alarmType);
        contentValues.put(COLUMN_ALARM_STATUS,alarmStatus);
        return database.insert(TABLE_NAME,null,contentValues);
    }

    public void deactivateAlarm(String id){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ALARM_STATUS,0);
        database.update(TABLE_NAME,contentValues,"id=?",new String[]{id});
    }

    public ArrayList<AlarmListModel> readAllData(){
        String query = "Select *from "+TABLE_NAME;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = null;
        modelsArrayList = new ArrayList<>();
        if(database != null){
            cursor = database.rawQuery(query,null);
            while(cursor.moveToNext()){
                alarmListModel = new AlarmListModel();
                alarmListModel.setId(cursor.getString(0));
                alarmListModel.setMedicineName(cursor.getString(1));
                alarmListModel.setHour(cursor.getInt(2));
                alarmListModel.setMinute(cursor.getInt(3));
                alarmListModel.setBroadcastCode(cursor.getInt(4));
                alarmListModel.setAlarmType(cursor.getString(5));
                alarmListModel.setAlarmStatus(cursor.getInt(6));
                modelsArrayList.add(alarmListModel);
            }
            cursor.close();
        }
        return modelsArrayList;
    }
    public AlarmListModel readSpecificData(String id){
        String query = "Select *from "+TABLE_NAME+ " WHERE id = "+id+";";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = null;
        alarmListModel = new AlarmListModel();
        if(database != null){
            cursor = database.rawQuery(query,null);
            while (cursor.moveToNext()){
                alarmListModel.setId(cursor.getString(0));
                alarmListModel.setMedicineName(cursor.getString(1));
                alarmListModel.setHour(cursor.getInt(2));
                alarmListModel.setMinute(cursor.getInt(3));
                alarmListModel.setBroadcastCode(cursor.getInt(4));
                alarmListModel.setAlarmType(cursor.getString(5));
                alarmListModel.setAlarmStatus(cursor.getInt(6));
            }
            cursor.close();
            return alarmListModel;
        }
        return alarmListModel;
    }
    public void updateAlarm(String id, String medicineName,int hour, int minute,  int broadcastCode, String alarmType, int alarmStatus){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MEDICINE_NAME,medicineName);
        contentValues.put(COLUMN_HOUR,hour);
        contentValues.put(COLUMN_MINUTE,minute);
        contentValues.put(COLUMN_BROADCAST_CODE,broadcastCode);
        contentValues.put(COLUMN_ALARM_TYPE,alarmType);
        contentValues.put(COLUMN_ALARM_STATUS,alarmStatus);
        Log.w("Alarm hour :",String.valueOf(hour));
        database.update(TABLE_NAME,contentValues,"id=?",new String[]{id});
    }
    public void updateAlarmStatus(String id,int alarmStatus){
        SQLiteDatabase database= this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ALARM_STATUS,alarmStatus);
        database.update(TABLE_NAME,contentValues,"id=?",new String[]{id});
    }
    public void deleteAlarm(String id){
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_NAME,"id=?",new String[]{id});
    }
}
