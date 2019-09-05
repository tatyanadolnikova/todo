package com.example.android.pets;

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

import com.example.android.pets.data.TaskContract.TaskEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_TASK_LOADER = 0;
    private Uri currentTaskUri;
    private EditText taskText;
    private Spinner spinner;
    private int importance = TaskEntry.IMPORTANCE_UNKNOWN;
    private boolean taskHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            taskHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentTaskUri = intent.getData();

        if (currentTaskUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_task));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_task));
            getLoaderManager().initLoader(EXISTING_TASK_LOADER, null, this);
        }

        taskText = (EditText) findViewById(R.id.edit_task_text);
        spinner = (Spinner) findViewById(R.id.spinner_gender);

        taskText.setOnTouchListener(touchListener);
        spinner.setOnTouchListener(touchListener);

        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter importanceSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_importance_options, android.R.layout.simple_spinner_item);
        importanceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(importanceSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.importance_high)))
                        importance = TaskEntry.IMPORTANCE_HIGH;
                    else
                        importance = TaskEntry.IMPORTANCE_UNKNOWN;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                importance = TaskEntry.IMPORTANCE_UNKNOWN;
            }
        });
    }

    private void saveTask() {
        String taskString = taskText.getText().toString().trim();

        if (currentTaskUri == null
                && TextUtils.isEmpty(taskString)
                && importance == TaskEntry.IMPORTANCE_UNKNOWN)
            return;

        ContentValues values = new ContentValues();
        values.put(TaskEntry.TASK_TEXT, taskString);
        values.put(TaskEntry.IMPORTANCE, importance);

        if (currentTaskUri == null) {
            Uri newUri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentTaskUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_task_successful),
                        Toast.LENGTH_SHORT).show();
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
        if (currentTaskUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveTask();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!taskHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!taskHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                TaskEntry._ID,
                TaskEntry.TASK_TEXT,
                TaskEntry.IMPORTANCE};

        return new CursorLoader(this,
                currentTaskUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1)
            return;

        if (cursor.moveToFirst()) {
            int taskColumnIndex = cursor.getColumnIndex(TaskEntry.TASK_TEXT);
            int importanceColumnIndex = cursor.getColumnIndex(TaskEntry.IMPORTANCE);

            String text = cursor.getString(taskColumnIndex);
            int importance = cursor.getInt(importanceColumnIndex);

            taskText.setText(text);
            switch (importance) {
                case TaskEntry.IMPORTANCE_HIGH:
                    spinner.setSelection(1);
                    break;
                default:
                    spinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        taskText.setText("");
        spinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteTask();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteTask() {
        if (currentTaskUri != null) {
            int rowsDeleted = getContentResolver().delete(currentTaskUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}