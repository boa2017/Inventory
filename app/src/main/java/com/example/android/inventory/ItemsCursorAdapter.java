/*
 *
 *  * PROJECT LICENSE
 *  *
 *  * This project was submitted by Beatriz Ovejero as part of the Android Developer
 *  * Nanodegree at Udacity.
 *  *
 *  * As part of Udacity Honor code, your submissions must be your own work, hence
 *  * submitting this project as yours will cause you to break the Udacity Honor Code
 *  * and the suspension of your account.
 *  *
 *  * As author of the project, I allow you to check it as a reference, but if you submit it
 *  * as your own project, it's your own responsibility if you get expelled.
 *  *
 *  * Copyright (c) 2018 Beatriz Ovejero
 *  *
 *  * Besides the above notice, the following license applies and this license notice must be
 *  * included in all works derived from this project.
 *  *
 *  * MIT License
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

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
