package com.example.appmodel.ridematcher;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import java.security.cert.CRLException;
import java.util.ArrayList;



public class DBHandler extends SQLiteOpenHelper {

    public final static String DATABASE = "RideDatabase";
    public final static String CURRENT_RIDES_TABLE = "currentRides";
    public final static String PAST_RIDES_TABLE = "pastRides";
    public final static String CREDIT_CARD_TABLE = "creditTable";
    public final static String PAYPAL_TABLE = "payPalTable";

    public final static String DRIVER_NAME = "driverName";
    public final static String DRIVER_PHONE = "driverPhone";
    public final static String DRIVER_FARE = "driverFare";
    public final static String DRIVER_PAYMENT = "driverPayment";
    public final static String DRIVER_PHOTO = "driverPhoto";

    public final static String CREDIT_CARD_NUMBER = "creditNumber";
    public final static String CREDIT_CARD_MONTH = "creditMonth";
    public final static String CREDIT_CARD_YEAR = "creditYear";
    public final static String CREDIT_CARD_CVC = "creditCVC";

    public final static String PAYPAL_USERNAME = "paypalUsername";

    public DBHandler(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + CURRENT_RIDES_TABLE + "(id integer primary key, " + DRIVER_NAME + " text," +
                DRIVER_PHONE + " text," + DRIVER_PHOTO + " text," + DRIVER_FARE + " text)";
        String sql2 = "create table " + PAST_RIDES_TABLE + "(id integer primary key, " + DRIVER_NAME + " text," +
                DRIVER_PHOTO + " text," + DRIVER_PHOTO + " text," + DRIVER_FARE + " text)";
        db.execSQL(sql);
        db.execSQL(sql2);
    }

    public void clearTable(String table){
        String sql = "DELETE FROM " + table + " WHERE 1";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }
    public ArrayList<ArrayList<String>> retrieveList(String tablename){
        ArrayList<ArrayList<String>> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + tablename + " WHERE 1";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            ArrayList<String> item = new ArrayList<>();
            item.add(cursor.getString(cursor.getColumnIndex(DRIVER_NAME)));
            item.add(cursor.getString(cursor.getColumnIndex(DRIVER_PHONE)));
            item.add(cursor.getString((cursor.getColumnIndex(DRIVER_FARE))));
            item.add(cursor.getString(cursor.getColumnIndex(DRIVER_PAYMENT)));
            item.add(cursor.getString(cursor.getColumnIndex(DRIVER_PHOTO)));
            cursor.moveToNext();
            items.add(item);
        }

        cursor.close();
        return items;
    }


    public void insertIntoRides(String table, ArrayList<String> item){
        SQLiteDatabase sq = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DRIVER_NAME, item.get(0));
        cv.put(DRIVER_PHONE, item.get(1));
        cv.put(DRIVER_FARE, item.get(2));
        cv.put(DRIVER_PAYMENT, item.get(3));
        cv.put(DRIVER_PHOTO, item.get(4));

        sq.insert(table, null, cv);
    }

    public void insertIntoTablePayment(String tablename, ArrayList<String> params){
        SQLiteDatabase sq = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CREDIT_CARD_NUMBER, params.get(0));
        cv.put(CREDIT_CARD_MONTH, params.get(1));
        cv.put(CREDIT_CARD_YEAR, params.get(2));
        cv.put(CREDIT_CARD_CVC, params.get(3));
        sq.insert(tablename, null, cv);
    }

    public String getItemFromTable(String tablename, String columnname, int index){
        SQLiteDatabase db = getReadableDatabase();
        String item = "";
        String sql = "SELECT * FROM " + tablename + " WHERE id=" + index;
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            item = cursor.getString(cursor.getColumnIndex(columnname));
            cursor.moveToNext();
        }
        cursor.close();
        return item;
    }
    public ArrayList<String> getCardDetails(int index){
        ArrayList<String> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + CREDIT_CARD_TABLE + " WHERE id=" + index;
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            items.add(cursor.getString(cursor.getColumnIndex(CREDIT_CARD_NUMBER)));
            items.add(cursor.getString(cursor.getColumnIndex(CREDIT_CARD_MONTH)));
            items.add(cursor.getString(cursor.getColumnIndex(CREDIT_CARD_YEAR)));
            items.add(cursor.getString(cursor.getColumnIndex(CREDIT_CARD_CVC)));
            cursor.moveToNext();
        }

        cursor.close();
        return items;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CURRENT_RIDES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PAST_RIDES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PAYPAL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CREDIT_CARD_TABLE);
        onCreate(db);
    }

    public ArrayList<Pair<String, String>> getAllPayments(String table, String column, String method){
        ArrayList<Pair<String, String>> items = new ArrayList<>();
        SQLiteDatabase sq = getReadableDatabase();
        String sql = "SELECT * FROM " + table + " WHERE 1";
        Cursor cursor = sq.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            Pair<String, String> item = new Pair<>(method, cursor.getString(cursor.getColumnIndex(column)));
            items.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return items;
    }
}
