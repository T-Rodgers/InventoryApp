package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    private BookContract() {
    }

    /** Name for content provider */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URIs that apps will use to contact content
     * provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Appended path to base content URI for looking at book data */
    public static final String PATH_BOOKS = "books";

    /** Defines the contents in database table */
    public static class BookEntry implements BaseColumns{

        /**
         * content URI to access the book data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /** MIME type of the CONTENT_URI for a list of books */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /** MIME type of the CONTENT_URI for a single book. */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // Name of table
        public static final String TABLE_NAME = "books";
        // ID number for each book
        public static final String _ID = BaseColumns._ID;
        // Name of each Book
        public static final String COLUMN_NAME_BOOK = "book_name";
        // Price of each book
        public static final String COLUMN_NAME_PRICE = "price";
        // Quantity of each book
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        // Type of each book
        public static final String COLUMN_BOOK_TYPE = "book_type";
        // Supplier of said book
        public static final String COLUMN_NAME_SUPPLIER = "supplier_name";
        // Supplier phone number
        public static final String COLUMN_NAME_SUPPLIER_PHONE = "supplier_phone_number";

        // Values for type of book
        public static final int BOOK_TYPE_UNKNOWN = 0;
        public static final int BOOK_TYPE_HARDCOVER = 1;
        public static final int BOOK_TYPE_PAPERBACK = 2;

        public static boolean isValidBookType(int bookType){
            if (bookType == BOOK_TYPE_UNKNOWN || bookType == BOOK_TYPE_HARDCOVER ||
                    bookType == BOOK_TYPE_PAPERBACK) {
                return true;
            }
            return false;
        }
    }
}
