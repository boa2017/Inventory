package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventory.data.ItemsContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ITEM_LOADER_ID = 0;

    ItemsCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);

            }
        });

        ListView itemListView = (ListView) findViewById(R.id.list_view);

        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new ItemsCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(ItemsContract.ItemsEntry.CONTENT_URI, id);

                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER_ID, null, this);
    }

    private void insertItem() {
        ContentValues values = new ContentValues();
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_NAME, "Scissors");
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE, "5");
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY, "10");
        values.put(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME, ItemsContract.ItemsEntry.SUPPLIER_BIC);
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE, R.drawable.scissors);
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_SOLD, 0);

        Uri newUri = getContentResolver().insert(ItemsContract.ItemsEntry.CONTENT_URI, values);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_set_message);
        builder.setPositiveButton(R.string.confirm_set_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllItems();
                Toast.makeText(MainActivity.this, R.string.toast_onclick, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Helper method to delete all items in the database.
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemsContract.ItemsEntry.CONTENT_URI, null, null);
        Log.v("mainActivity", rowsDeleted + "rows deleted from Inventory2017 database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;

            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ItemsContract.ItemsEntry._ID,
                ItemsContract.ItemsEntry.COLUMN_ITEM_NAME,
                ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY,
                ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE,
                ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME,
                ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE,
                ItemsContract.ItemsEntry.COLUMN_ITEM_SOLD};

        return new CursorLoader(this,
                ItemsContract.ItemsEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}