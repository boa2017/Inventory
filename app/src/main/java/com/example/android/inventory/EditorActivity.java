package com.example.android.inventory;

/**
 * Created by beita on 10/07/2017.
 */


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.inventory.data.ItemsContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;

    private static final int GET_PICTURE_REQUEST = 0;
    private static final int GIVE_PERMISSION = 1;
    ImageButton increase;
    ImageButton decrease;
    Uri uri;
    private Button getPictureButton;
    private Button orderItems;
    private Uri mCurrentItemUri;
    private EditText mItemEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private ImageView mImageView;
    private Spinner mSupplierSpinner;
    private int mSupplier = ItemsContract.ItemsEntry.SUPPLIER_UNKNOWN;
    private boolean mItemHasChanged = false;
    private String currentPhotoUri;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mItemEditText = (EditText) findViewById(R.id.item_name_edit);

        mQuantityEditText = (EditText) findViewById(R.id.quantity_edit);

        mPriceEditText = (EditText) findViewById(R.id.price_edit);

        mImageView = (ImageView) findViewById(R.id.image_view);

        getPictureButton = (Button) findViewById(R.id.get_picture);

        orderItems = (Button) findViewById(R.id.order_items);

        increase = (ImageButton) findViewById(R.id.increase);

        decrease = (ImageButton) findViewById(R.id.decrease);

        mSupplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);


        mItemEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.new_item));

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getPictureButton.setText(getString(R.string.change_picture));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        setupSpinner();

        getPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accessOpenImageSelector();
                mItemHasChanged = true;
            }
        });

        orderItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameItem = mItemEditText.getText().toString();
                String quantityOrder = mQuantityEditText.getText().toString();
                String priceItem = mPriceEditText.getText().toString();


                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, ("mailto:"));
                intent.putExtra(Intent.EXTRA_SUBJECT, (getString(R.string.goods_request)));
                intent.putExtra(Intent.EXTRA_TEXT, "Request for: " + nameItem + "\nPriced at: " + priceItem + " €" +
                        "\nQuantity to be ordered: " + quantityOrder + "\n\n Thank you!");

                startActivity(intent);

            }
        });

        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                substractOneToQuantity();
                mItemHasChanged = true;
            }
        });

        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOneToQuantity();
                mItemHasChanged = true;
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter supplierArrayAdapter = ArrayAdapter.createFromResource(this, R.array.array_supplier_options, android.R.layout.simple_spinner_item);
        supplierArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mSupplierSpinner.setAdapter(supplierArrayAdapter);

        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_Milan))) {
                        mSupplier = ItemsContract.ItemsEntry.SUPPLIER_MILAN;
                    } else if (selection.equals(getString(R.string.supplier_Bic))) {
                        mSupplier = ItemsContract.ItemsEntry.SUPPLIER_BIC;
                    } else if (selection.equals(getString(R.string.supplier_Staedtler))) {
                        mSupplier = ItemsContract.ItemsEntry.SUPPLIER_STAEDTLER;
                    } else if (selection.equals(getString(R.string.supplier_Staedtler))) {
                        mSupplier = ItemsContract.ItemsEntry.SUPPLIER_STAEDTLER;
                    } else {
                        mSupplier = ItemsContract.ItemsEntry.SUPPLIER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = ItemsContract.ItemsEntry.SUPPLIER_UNKNOWN;
            }
        });
    }

    public void accessOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    GIVE_PERMISSION);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GET_PICTURE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case GIVE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == GET_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                uri = resultData.getData();
                currentPhotoUri = uri.toString();

                Glide.with(this).load(currentPhotoUri)
                        .placeholder(R.mipmap.ic_launcher)
                        .crossFade()
                        .fitCenter()
                        .into(mImageView);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_item);
            menuItem.setVisible(false);

        }
        return true;
    }

    private void saveItem() {
        String itemName = mItemEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString();


        if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(quantity) || TextUtils.isEmpty(price)
                || mSupplier == ItemsContract.ItemsEntry.SUPPLIER_UNKNOWN ||currentPhotoUri == null) {
            Toast.makeText(this, getString(R.string.incorrect_entry), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_NAME, itemName);
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE, price);
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME, mSupplier);
        values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE, currentPhotoUri);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(ItemsContract.ItemsEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.failure_insert), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.successful_insert), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.failed_update), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.successful_update), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveItem();
                finish();
                return true;

            case R.id.action_delete_item:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all the attributes for an item, define a projection that contains
        // all columns from the items table
        String[] projection = {
                ItemsContract.ItemsEntry._ID,
                ItemsContract.ItemsEntry.COLUMN_ITEM_NAME,
                ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME,
                ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY,
                ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE,
                ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE};

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY);
            int pictureColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE);

            String name = cursor.getString(nameColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            mItemEditText.setText(name);
            mPriceEditText.setText(String.valueOf(price));
            mQuantityEditText.setText(String.valueOf(quantity));
            currentPhotoUri = cursor.getString(pictureColumnIndex);

            // Supplier is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Milan, 2 is Bic, 3 is Staedtler).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (supplier) {
                case ItemsContract.ItemsEntry.SUPPLIER_MILAN:
                    mSupplierSpinner.setSelection(1);
                    break;
                case ItemsContract.ItemsEntry.SUPPLIER_BIC:
                    mSupplierSpinner.setSelection(2);
                    break;
                case ItemsContract.ItemsEntry.SUPPLIER_STAEDTLER:
                    mSupplierSpinner.setSelection(3);
                    break;
                case ItemsContract.ItemsEntry.SUPPLIER_UNKNOWN:
                    mSupplierSpinner.setSelection(0);
                    break;
            }

            Glide.with(this).load(currentPhotoUri)
                    .placeholder(R.mipmap.ic_launcher)
                    .crossFade()
                    .fitCenter()
                    .into(mImageView);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemEditText.setText("");
        mSupplierSpinner.setSelection(0); // Select "Unknown" supplier
        mQuantityEditText.setText("");
        mPriceEditText.setText("");

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and the negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void substractOneToQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            mQuantityEditText.setText(String.valueOf(previousValue - 1));
        }
    }

    private void addOneToQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        mQuantityEditText.setText(String.valueOf(previousValue + 1));
    }

    /**
     * Perform the deletion of the item in the database.
     */

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted != 0) {
                Toast.makeText(this, getString(R.string.delete_item_successful), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditorActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.delete_item_failed), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}
