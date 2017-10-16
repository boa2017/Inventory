package com.example.android.inventory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.inventory.data.ItemsContract;

/**
 * Created by beita on 10/07/2017.
 */
public class ItemsCursorAdapter extends CursorAdapter {


    public ItemsCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /*flags*/);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView nameSupplierTextView = (TextView) view.findViewById(R.id.supplier);
        TextView itemsSold = (TextView) view.findViewById(R.id.sold);
        ImageView sellButton = (ImageView) view.findViewById(R.id.button_sell);
        ImageView imageViewPhotoItem = (ImageView) view.findViewById(R.id.image_view_list);

        // Find the columns of item attributes that we are interested in
        int nameColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_NAME);
        int priceIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_PRICE);
        int quantityIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY);
        int itemIdColumnIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry._ID);
        int supplierIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_SUPPLIER_NAME);
        int photoIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_PICTURE);
        int soldIndex = cursor.getColumnIndex(ItemsContract.ItemsEntry.COLUMN_ITEM_SOLD);

        String itemName = cursor.getString(nameColumnIndex);
        final String itemQuantity = cursor.getString(quantityIndex);
        final long itemId = cursor.getLong(itemIdColumnIndex);
        final String itemPrice = cursor.getString(priceIndex);
        final int supplierName = cursor.getInt(supplierIndex);
        Uri thumbUri = Uri.parse(cursor.getString(photoIndex));
        final String sold = cursor.getString(soldIndex);
        final String nameSupplier;
        switch (supplierName) {
            case 1:
                nameSupplier = "Milan";
                break;
            case 2:
                nameSupplier = "Bic";
                break;
            case 3:
                nameSupplier = "Staedtler";
                break;
            default:
                nameSupplier = "Unknown";
                break;
        }

        final Uri currentProductUri = ContentUris.withAppendedId(ItemsContract.ItemsEntry.CONTENT_URI, itemId);

        nameTextView.setText(itemName);
        quantityTextView.setText("Quantity: " + itemQuantity);
        priceTextView.setText("Price: " + itemPrice + " $");
        nameSupplierTextView.setText("Supplier: " + nameSupplier);
        itemsSold.setText("Sold: " + sold);

        final int currentQuantity = Integer.parseInt(itemQuantity);
        final int itemSold = Integer.parseInt(sold);

        Glide.with(context).load(thumbUri)
                .placeholder(R.mipmap.ic_launcher)
                .crossFade()
                .centerCrop()
                .into(imageViewPhotoItem);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (currentQuantity > 0) {
                    int quantityValue = currentQuantity;
                    int sold1 = itemSold;

                    values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_QUANTITY, --quantityValue);
                    values.put(ItemsContract.ItemsEntry.COLUMN_ITEM_SOLD, ++sold1);
                    resolver.update(
                            currentProductUri,
                            values,
                            null,
                            null);
                    context.getContentResolver().notifyChange(currentProductUri, null);
                }
            }
        });
    }
}
