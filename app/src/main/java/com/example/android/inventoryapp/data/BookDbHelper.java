package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    // If schema changes you must increment database version
    public static final int DATABASE_VERSION = 1;


    public static final String DATABASE_NAME = "inventory.db";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOK_ENTRIES = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                        + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + BookEntry.COLUMN_NAME_BOOK + " TEXT NOT NULL, "
                        + BookEntry.COLUMN_NAME_PRICE + " INTEGER NOT NULL, "
                        + BookEntry.COLUMN_BOOK_TYPE + " INTEGER NOT NULL DEFAULT 0, "
                        + BookEntry.COLUMN_NAME_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                        + BookEntry.COLUMN_NAME_SUPPLIER + " TEXT NOT NULL, "
                        + BookEntry.COLUMN_NAME_SUPPLIER_PHONE + " TEXT NOT NULL" + ");";

        db.execSQL(SQL_CREATE_BOOK_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
