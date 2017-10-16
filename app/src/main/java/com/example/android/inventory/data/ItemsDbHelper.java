package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by beita on 10/07/2017.
 */

public class ItemsDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ItemsDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    public final static String DATABASE_NAME = "items.db";

    public final static int DATABASE_VERSION = 1;

    public ItemsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Called when database is created for the first time.

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemsContract.ItemsEntry.TABLE_NAME + "("
                + ItemsContract.ItemsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemsContract.ItemsEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL, "
                + ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY + " INTEGER DEFAULT 0, "
                + ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME + " INTEGER NOT NULL, "
                + ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE + " TEXT NOT NULL DEFAULT 'no picture', "
                + ItemsContract.ItemsEntry.COLUMN_ITEM_SOLD + " INTEGER NOT NULL DEFAULT 0); ";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    //When the database needs to be updated.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ItemsContract.ItemsEntry.TABLE_NAME);
        onCreate(db);
    }
}




