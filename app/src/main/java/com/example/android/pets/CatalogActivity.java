package com.example.android.pets;

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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.TaskContract.TaskEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER = 0;

    private Uri currentTaskUri; //!

    TaskCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView taskListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        taskListView.setEmptyView(emptyView);

        cursorAdapter = new TaskCursorAdapter(this, null);
        taskListView.setAdapter(cursorAdapter);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, id);
                intent.setData(currentTaskUri);
                startActivity(intent);
            }
        });

        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, l);
                getContentResolver().delete(currentTaskUri, null, null);
                return true;
            }
        });

        getLoaderManager().initLoader(TASK_LOADER, null, this);
    }

    private void insertPet() {
        ContentValues values = new ContentValues();
        values.put(TaskEntry.TASK_TEXT, "Написать приложение");
        values.put(TaskEntry.IMPORTANCE, TaskEntry.IMPORTANCE_UNKNOWN);

        Uri newUri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);
    }

    private void deleteAllTasks() {
        int rowsDeleted = getContentResolver().delete(TaskEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from task database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                deleteAllTasks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                TaskEntry._ID,
                TaskEntry.TASK_TEXT,
                TaskEntry.IMPORTANCE };

        return new CursorLoader(this,
                TaskEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
