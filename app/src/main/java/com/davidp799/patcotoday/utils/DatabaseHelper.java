package com.davidp799.patcotoday.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "regular_schedule_database";
    public static final String ARRIVALS_TABLE_NAME = "regular_arrival_table";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + ARRIVALS_TABLE_NAME + "(id integer primary key, name text, salary text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ARRIVALS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insert(String s, String s1) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", s);
        contentValues.put("salary,", s1);
        db.insert(ARRIVALS_TABLE_NAME, null, contentValues);
        return true;
    }
}
