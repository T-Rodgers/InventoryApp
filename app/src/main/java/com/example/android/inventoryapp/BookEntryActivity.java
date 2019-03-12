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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryapp.data.BookContract;
import com.example.android.inventoryapp.data.BookContract.BookEntry;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class BookEntryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifies book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * Content URI for existing book. it will be null if it's a new book
     */
    private Uri mCurrentBookUri;

    /**
     * EditText for book name
     */
    private EditText mNameEditText;

    /**
     * EditText for Price
     **/
    private EditText mPriceEditText;

    /**
     * Quantity TextView
     */
    private EditText mQuantityEditText;

    /**
     * Spinner for book type
     */
    private Spinner mBookTypeSpinner;

    /**
     * EditText for Supplier
     */
    private EditText mSupplierNameEditText;

    /**
     * EditText for SupplierPhone
     */
    private EditText mSupplierPhoneEditText;

    /**
     * Keeps track of book has changed
     **/
    private boolean mBookHasChanged = false;

    private int mBookType = BookContract.BookEntry.BOOK_TYPE_UNKNOWN;

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
        setContentView(R.layout.activity_book_entry);

        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        mQuantityEditText = findViewById(R.id.quantity_edit);

        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.add_a_book_title));
            // Invalidate the options menu. Since we are adding a new book. There is no need to
            // delete entry
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_book_title));

            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mNameEditText = findViewById(R.id.book_title);
        mPriceEditText = findViewById(R.id.price_edit);
        mSupplierNameEditText = findViewById(R.id.supplier_name_edit);
        mSupplierPhoneEditText = findViewById(R.id.supplier_phone_edit);
        mBookTypeSpinner = findViewById(R.id.book_type_spinner);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mBookTypeSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

    }

    /**
     * Setting up the spinner that will hold the data for book type
     */
    private void setupSpinner() {
        // Adapter for spinner that uses our array values
        ArrayAdapter bookTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_book_type_options, android.R.layout.simple_spinner_item);

        bookTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mBookTypeSpinner.setAdapter(bookTypeSpinnerAdapter);

        mBookTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.hardcover_type))) {
                        mBookType = BookEntry.BOOK_TYPE_HARDCOVER;
                    } else if (selection.equals(getString(R.string.paperback_type))) {
                        mBookType = BookEntry.BOOK_TYPE_PAPERBACK;
                    } else {
                        mBookType = BookEntry.BOOK_TYPE_UNKNOWN;
                    }
                }
            }

            // Must define onNothingSelected
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mBookType = BookEntry.BOOK_TYPE_UNKNOWN;
            }
        });
    }


    // Check so if any of the fields are empty we will let user know what they need to do before
    // proceeding.
    public boolean isValidBook() {

        String nameString = mNameEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(quantityString)) {
            mQuantityEditText.setText(String.valueOf(0));
        }

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.no_title_toast), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.no_price_toast), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.no_supplier_name_toast), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(supplierPhoneString)) {
            Toast.makeText(this, getString(R.string.no_supplier_phone_toast), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get user input from book details activity to save to database
     */
    private void saveBook() {
        // Retrieve data from editText and textView fields
        String titleString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantity = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        // Check if this is a new entry and if fields are blank or not
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantity) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneString) && mBookType == BookEntry.BOOK_TYPE_UNKNOWN) {

            return;
        }

        // Create content values object where column names are keys and attributes from
        // book details are the values
        ContentValues values = new ContentValues();

        values.put(BookEntry.COLUMN_NAME_BOOK, titleString);
        values.put(BookEntry.COLUMN_NAME_PRICE, priceString);
        values.put(BookEntry.COLUMN_NAME_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_BOOK_TYPE, mBookType);
        values.put(BookEntry.COLUMN_NAME_SUPPLIER, supplierNameString);
        values.put(BookEntry.COLUMN_NAME_SUPPLIER_PHONE, supplierPhoneString);

        // Find if this is a new or existing book by checking if Book URI is null or not
        if (mCurrentBookUri == null) {
            // This is a new book so insert into the provider, and return the content URI
            // for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            if (newUri == null) {
                // If content is null then there was an error saving book
                Toast.makeText(this, getString(R.string.error_saving_book),
                        Toast.LENGTH_SHORT).show();
            } else {
                // If not null then book was successfully saved
                Toast.makeText(this, getString(R.string.success_saving_book),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // The other path would be that this is an existing book so update book with mCurrentBookUri
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.error_updating_book),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.success_updating_book),
                        Toast.LENGTH_SHORT).show();
            }
        }
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

            mNameEditText.setText(name);
            mPriceEditText.setText(String.valueOf(price));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhoneNumber);
            mQuantityEditText.setText(String.valueOf(quantity));

            switch (type) {
                case BookEntry.BOOK_TYPE_HARDCOVER:
                    mBookTypeSpinner.setSelection(1);
                    break;
                case BookEntry.BOOK_TYPE_PAPERBACK:
                    mBookTypeSpinner.setSelection(2);
                    break;
                default:
                    mBookTypeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mBookTypeSpinner.setSelection(0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_book_entry, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option
        switch (item.getItemId()) {
            case R.id.save_item:
                if (isValidBook()) {

                    saveBook();

                    finish();
                    return true;
                } else {
                    return false;
                }

            case android.R.id.home:
                // If book hasn't changed go back to parent activity
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(BookEntryActivity.this);
                    return true;
                }

                // If there are no unsaved changes, setup a dialog to let user know.
                // Create a click listener to handle the user confirming that
                // changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked back button, we will go to parent activity
                                NavUtils.navigateUpFromSameTask(BookEntryActivity.this);
                            }
                        };

                // Show dialog that lets the user know they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_msg);
        builder.setPositiveButton(R.string.discard_book, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing_book, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
}
