package com.example.android.pets.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

public final class TaskContract {

    private TaskContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.pets";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TASKS = "pets";

    public static final class TaskEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TASKS);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TASKS;

        public final static String TABLE_NAME = "tasks";
        public final static String _ID = BaseColumns._ID;
        public final static String TASK_TEXT ="task";
        public final static String IMPORTANCE = "importance";

        public static final int IMPORTANCE_UNKNOWN = 0;
        public static final int IMPORTANCE_HIGH = 1;

        public static boolean isValidImportance(int gender) {
            if (gender == IMPORTANCE_UNKNOWN || gender == IMPORTANCE_HIGH)
                return true;
            return false;
        }
    }
}

