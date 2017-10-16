package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by beita on 10/07/2017.
 */

public class ItemsProvider extends ContentProvider {

    public static final String LOG_TAG = ItemsProvider.class.getSimpleName();

    //Code URI matcher for the content URI for the items table */
    public static final int ITEMS = 100;

    //Code URI matcher for the content URI for a single item in the items table */

    public static final int ITEMS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. It runs the first time anything is called from this class.
    static {
        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY, ItemsContract.PATH_INVENTORY, ITEMS);
        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY, ItemsContract.PATH_INVENTORY + "/#", ITEMS_ID);
    }

    /**
     * Database helper object
     */
    private ItemsDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ItemsDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = db.query(ItemsContract.ItemsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEMS_ID:
                selection = ItemsContract.ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(ItemsContract.ItemsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("No query with unknown URI" + uri);
        }

        // Set notification URI on the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not allowed for" + uri);
        }
    }

    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    public Uri insertItem(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ItemsContract.ItemsEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("The item requires a name");
        }

        // Check that the supplier is valid
        Integer supplier = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME);
        if (supplier == null || ItemsContract.ItemsEntry.isValidSupplier(supplier) == false) {
            throw new IllegalArgumentException("The item requires a valid supplier");
        }

        // If the quantity is provided, check that it's greater than or equal to 0 items
        Integer quantity = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Item requires a valid quantity");
        }

        // If the price is provided, check that it's greater than or equal to 0 €
        Integer price = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Item requires a valid price");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new item with the given values
        long id = db.insert(ItemsContract.ItemsEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for" + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[]
            selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEMS_ID:
                // For the ITEMS_ID code, extract out the ID from the URI.
                selection = ItemsContract.ItemsEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for" + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        if (values.containsKey(ItemsContract.ItemsEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(ItemsContract.ItemsEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("The item requires a name");
            }
        }

        if (values.containsKey(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME)) {
            Integer supplier = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME);
            if (supplier == null) {
                throw new IllegalArgumentException("The item requires a valid supplier");
            }
        }

        if (values.containsKey(ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY)) {
            Integer quantity = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Item requires a positive quantity");
            }
        }

        if (values.containsKey(ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE)) {
            String picture = values.getAsString(ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE);
            if (picture == null) {
                throw new IllegalArgumentException("The item requires a picture");
            }
        }

        if (values.containsKey(ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE)) {
            Integer price = values.getAsInteger(ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Item requires a valid price");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(ItemsContract.ItemsEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                rowsDeleted = database.delete(ItemsContract.ItemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEMS_ID:
                selection = ItemsContract.ItemsEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ItemsContract.ItemsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete is not supported for" + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemsContract.ItemsEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return ItemsContract.ItemsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri" + uri + "with match" + match);
        }
    }
}

