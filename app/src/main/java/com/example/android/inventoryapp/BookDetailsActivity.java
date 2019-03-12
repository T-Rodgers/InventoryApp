package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.BookContract.BookEntry;

import java.text.NumberFormat;

public class BookDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;

    private TextView mBookTitle;

    private TextView mBookType;

    private TextView mBookSupplierName;

    private TextView mBookSupplierPhone;

    private TextView mBookQuantity;

    private int quantity;

    private TextView mPrice;

    /**
     * Keeps track of book has changed
     **/
    private boolean mBookHasChanged = false;

    /**
     * lets us know if a user has modified the view, then we can change mBookHasChanged boolean to
     * be true
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        setTitle(R.string.book_details_title);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);

        mBookTitle = findViewById(R.id.book_title_details);
        mBookType = findViewById(R.id.booktype_details);
        mBookSupplierName = findViewById(R.id.supplier_name_details);
        mBookSupplierPhone = findViewById(R.id.supplier_phone_details);
        mBookQuantity = findViewById(R.id.book_quantity_details);
        mPrice = findViewById(R.id.price_details);


        TextView addToQuantityView = findViewById(R.id.addTo_quantity_button);
        addToQuantityView.setOnTouchListener(mTouchListener);
        TextView minusQuantityView = findViewById(R.id.subtract_quantity_button);
        minusQuantityView.setOnTouchListener(mTouchListener);

        addToQuantityView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ContentValues cValues = new ContentValues();
                cValues.put(BookEntry.COLUMN_NAME_QUANTITY, Integer.valueOf(
                        mBookQuantity.getText().toString()) + 1);
                getContentResolver().update(mCurrentBookUri, cValues, null, null);
            }
        });

        minusQuantityView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                quantity = Integer.parseInt(mBookQuantity.getText().toString().trim());
                if (quantity > 0) {
                    quantity--;
                    ContentValues cValues = new ContentValues();
                    cValues.put(BookEntry.COLUMN_NAME_QUANTITY, quantity);
                    getContentResolver().update(mCurrentBookUri, cValues, null, null);
                }
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_book_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option
        switch (item.getItemId()) {
            case R.id.edit_book_details:

                // Return to BookEntry activity with values from details
                Intent intent = new Intent(BookDetailsActivity.this, BookEntryActivity.class);

                intent.setData(mCurrentBookUri);

                startActivity(intent);

                return true;

            case R.id.delete_item_details:
                // Dialog Box to confirm deletion
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                // If book hasn't changed go back to parent activity
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(BookDetailsActivity.this);
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_book_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_NAME_BOOK,
                BookEntry.COLUMN_BOOK_TYPE,
                BookEntry.COLUMN_NAME_QUANTITY,
                BookEntry.COLUMN_NAME_PRICE,
                BookEntry.COLUMN_NAME_SUPPLIER,
                BookEntry.COLUMN_NAME_SUPPLIER_PHONE
        };

        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Stop loading if the cursor is empty or there is less than 1 row in cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // find columns of book attributes
            int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME_BOOK);
            int typeColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_TYPE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME_SUPPLIER);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(bookNameColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = cursor.getString(supplierPhoneColumnIndex);

            String formattedNumber = PhoneNumberUtils.formatNumber(supplierPhoneNumber);

            mBookTitle.setText(name);
            mBookSupplierName.setText(supplierName);
            mBookSupplierPhone.setText(formattedNumber);
            mBookQuantity.setText(Integer.toString(quantity));
            mPrice.setText(String.valueOf(NumberFormat.getCurrencyInstance().format(price)));

            switch (type) {
                case BookEntry.BOOK_TYPE_HARDCOVER:
                    mBookType.setText(R.string.hardcover_type);
                    break;
                case BookEntry.BOOK_TYPE_PAPERBACK:
                    mBookType.setText(R.string.paperback_type);
                    break;
                default:
                    mBookType.setText(R.string.unknown_type);
                    break;
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mBookTitle.setText(R.string.unable_to_find_details);
        mBookSupplierName.setText(R.string.unable_to_find_details);
        mBookSupplierPhone.setText(R.string.unable_to_find_details);
        mBookType.setText(R.string.unable_to_find_details);
        mPrice.setText(R.string.unable_to_find_details);
        mBookQuantity.setText("--");

    }
}
