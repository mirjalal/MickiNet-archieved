package com.talmir.transferfileoverwifidirect.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAction extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mimeTypes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String createTable = "CREATE VIRTUAL TABLE mimeTypes USING fts3(type VARCHAR(100) NOT NULL);";

    public DBAction(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS mimeTypes;");
        onCreate(db);
    }
}
