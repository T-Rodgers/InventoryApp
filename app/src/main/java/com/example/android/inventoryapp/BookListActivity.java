package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
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

import com.example.android.inventoryapp.data.BookContract.BookEntry;

public class BookListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the book data loader */
    private static final int BOOK_LOADER = 0;

    /**
     * Adapter for our book list
     */
    BookCursorAdapter mCursorAdapter;

    // Log tag for logging information
    private static final String LOG_TAG = BookListActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booklist_activity);

        FloatingActionButton insertFab = findViewById(R.id.insert_book_fab);
        insertFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailsIntent = new Intent(BookListActivity.this, BookEntryActivity.class);
                startActivity(detailsIntent);
            }
        });

        // Find ListView that will hold book data
        ListView bookListView = findViewById(R.id.booklist);

        // Sets an emptyview for when we don't have any books in our inventory
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Set an adapter to populate our listView from the cursor
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(BookListActivity.this, BookDetailsActivity.class);

                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                intent.setData(currentBookUri);

                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    /** Method to delete all books in database */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);

        Log.v(LOG_TAG, rowsDeleted + " rows have been deleted");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_booklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_delete_all_books:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_NAME_BOOK,
                BookEntry.COLUMN_NAME_QUANTITY,
                BookEntry.COLUMN_NAME_PRICE
        };

        return new CursorLoader(this,
                BookEntry.CONTENT_URI,
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

    // Will decrease quantity figure everytime the sale button is pressed
    public void decreaseCount(int id, int quantity){

        if (quantity > 0) {

            quantity--;

            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_NAME_QUANTITY, quantity);

            Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

            int rowsAffected = getContentResolver().update(updateUri, values, null, null);

            Log.v(LOG_TAG, rowsAffected + " rows have been updated");
        } else {
            Toast.makeText(this, "Nothing left to sell!", Toast.LENGTH_LONG).show();
        }
    }
}
