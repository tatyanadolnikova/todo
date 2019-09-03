package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.TaskContract.TaskEntry;

public class TaskCursorAdapter extends CursorAdapter {

    public TaskCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView taskTextView = (TextView) view.findViewById(R.id.name);
        int textColumnIndex = cursor.getColumnIndex(TaskEntry.TASK_TEXT);
        String petName = cursor.getString(textColumnIndex);
        taskTextView.setText(petName);
    }
}