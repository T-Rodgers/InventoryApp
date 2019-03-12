package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    private static final String LOG_TAG = BookProvider.class.getSimpleName();

    /**
     * URI matcher code for the conetn URI for the books list/table
     */
    private static final int BOOKS = 100;

    /**
     * URI matcher code for the content URI for a single book in the books list/table
     */
    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        // URI matcher fines our code BOOKS = 100, so we know that we are trying to access multiple
        // rows of the book table
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);

        // URI matcher finds our mapped code BOOK_ID = 101 so we know that we are trying to access
        // a single row of the books table. the # is used as a wildcard so we know it can be
        // switched out for an integer. the there is no integer at the end then it does not match.
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    /**
     * Database helper object
     */
    private BookDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // For the books code the cursor can contain multiple rows of the books table
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                // For BOOK_ID code, extract the ID from the URI. this is for the selection
                // and selection arguments cases for each ? in the selections there will be an
                // element that wills in the ?.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }

        // this the data in the URI changes we know to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not support for ");
        }
    }

    /**
     * Insert book into database with ContentValues. A new content URI will be returned
     * for that row
     */
    private Uri insertBook(Uri uri, ContentValues values) {
        // Check that the book name isn't null
        String name = values.getAsString(BookEntry.COLUMN_NAME_BOOK);
        if (name == null) {
            throw new IllegalArgumentException("Book needs to have a name");
        }

        // Check that book type is valid
        Integer bookType = values.getAsInteger(BookEntry.COLUMN_BOOK_TYPE);
        if (bookType == null || !BookEntry.isValidBookType(bookType)) {
            throw new IllegalArgumentException("Book needs valid book type");
        }

        // Check to make sure the book has a price
        Integer price = values.getAsInteger(BookEntry.COLUMN_NAME_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Book requires a price");
        }

        // Check to make sure book has a supplier name
        String supplierName = values.getAsString(BookEntry.COLUMN_NAME_SUPPLIER);
        if (supplierName == null) {
            throw new IllegalArgumentException("Book needs to have supplier name");
        }

        // Check to make sure book has supplier phone number
        String supplierPhone = values.getAsString(BookEntry.COLUMN_NAME_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw new IllegalArgumentException("Book needs to have supplier phone number");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Insert new book with given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        // If ID is -1 then the insertion was unsuccessful. In this case we log the error and return
        // null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the book content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID od the new row appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writable dtabase
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Deleted all rows that match selection and selection args
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row with the given ID
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // Extract the ID from the URI so we know which row to update. selection will be
                // _id=? and selection args will be a String array that contains the actual ID.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update books in table wih content values. Changes will be applied to rows with specified
     * selection and selection args elements. Return number of rows that were updated successfully
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        // check if the COLUMN_BOOK_NAME KEY IS PRESENT, see to it that name is not null
        if (values.containsKey(BookEntry.COLUMN_NAME_BOOK)) {
            String name = values.getAsString(BookEntry.COLUMN_NAME_BOOK);
            if (name == null) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }

        // check if quantity column key is present and see that it is not null
        if (values.containsKey(BookEntry.COLUMN_NAME_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_NAME_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Book requires valid quantity");
            }
        }

        // check if price column key is present and see that it is not null or less than "0"
        if (values.containsKey(BookEntry.COLUMN_NAME_PRICE)) {
            Integer price = values.getAsInteger(BookEntry.COLUMN_NAME_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Book requires valid price");
            }
        }

        // check if type column key is present
        if (values.containsKey(BookEntry.COLUMN_BOOK_TYPE)) {
            Integer type = values.getAsInteger(BookEntry.COLUMN_BOOK_TYPE);
            if (type == null || !BookEntry.isValidBookType(type)) {
                throw new IllegalArgumentException("Requires valid book type");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_NAME_SUPPLIER)) {
            String supplierName = values.getAsString(BookEntry.COLUMN_NAME_SUPPLIER);
            if (supplierName == null) {
                throw new IllegalArgumentException("Book requires supplier name");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_NAME_SUPPLIER_PHONE)) {
            String supplierPhone = values.getAsString(BookEntry.COLUMN_NAME_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Book requires supplier phone number");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        // get writable database to update data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Update database and get the number of rows that were updated.
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection,
                selectionArgs);

        // If one or more rows were updated then all listeners will be told that the data has
        // changed in the given URI
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;

    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI" + uri + " with match " + match);
        }
    }
}


