package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by beita on 10/07/2017.
 */

public class ItemsContract {

    /**
     * The "Content authority" is a name for the entire content provider. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory2017";

    // Private constructor to prevent classes to be created.

    private ItemsContract() {
    }

    public static final class ItemsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public final static String TABLE_NAME = "items";

        public final static String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_ITEM_PICTURE = "picture";
        public static final String COLUMN_ITEM_SOLD = "sales";

        public static final int SUPPLIER_UNKNOWN = 0;
        public static final int SUPPLIER_MILAN = 1;
        public static final int SUPPLIER_BIC = 2;
        public static final int SUPPLIER_STAEDTLER = 3;

        public static boolean isValidSupplier(int supplier) {
            if (supplier == SUPPLIER_UNKNOWN || supplier == SUPPLIER_MILAN || supplier == SUPPLIER_BIC || supplier == SUPPLIER_STAEDTLER) {
                return true;
            }
            return false;
        }
    }
}