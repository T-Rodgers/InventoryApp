package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.BookContract.BookEntry;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /** Creates new blank book list item view */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate book list item layout
        return LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);
    }

    /** Method binds book data to book list item layout */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView bookNameTextView = view.findViewById(R.id.book_name);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        TextView priceTextView = view.findViewById(R.id.price);
        Button saleButton = view.findViewById(R.id.sale_button);



        // Find columns of book attributes that we want
        final int columnIdIndex = cursor.getColumnIndex(BookEntry._ID);
        int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME_BOOK);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME_PRICE);

        // Read attributes that we want
        final int productId = cursor.getInt(columnIdIndex);
        String bookName = cursor.getString(bookNameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        int price = cursor.getInt(priceColumnIndex);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookListActivity bookListActivity = (BookListActivity) context;
                bookListActivity.decreaseCount(productId, quantity);
            }
        });
        bookNameTextView.setText(bookName);
        quantityTextView.setText(NumberFormat.getNumberInstance(
                Locale.US).format(quantity));

        priceTextView.setText(String.valueOf(price));
    }

}
