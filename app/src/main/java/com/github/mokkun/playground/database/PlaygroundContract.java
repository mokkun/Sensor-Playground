package com.github.mokkun.playground.database;

import android.provider.BaseColumns;

public final class PlaygroundContract {
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "mokkun.db";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ", ";

    private PlaygroundContract() {
        throw new AssertionError("Instantiation is not supported.");
    }

    public static final class SessionEntry implements BaseColumns {
        public static final String TABLE_NAME = "`Sessions`";
        public static final String COLUMN_NAME_START_TIME = "startTime";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_STEPS = "steps";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String SQL_ORDER_BY = "_id DESC";

        static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + SessionEntry.TABLE_NAME + " ("
                + SessionEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP
                + SessionEntry.COLUMN_NAME_START_TIME + INTEGER_TYPE + COMMA_SEP
                + SessionEntry.COLUMN_NAME_DURATION + INTEGER_TYPE + COMMA_SEP
                + SessionEntry.COLUMN_NAME_STEPS + INTEGER_TYPE + COMMA_SEP
                + SessionEntry.COLUMN_NAME_DISTANCE + INTEGER_TYPE
                + ")";
        static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + SessionEntry.TABLE_NAME;

        private SessionEntry() {
            throw new AssertionError("Instantiation is not supported.");
        }
    }
}
