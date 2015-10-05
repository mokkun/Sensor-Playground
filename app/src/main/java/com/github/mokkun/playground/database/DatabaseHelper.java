package com.github.mokkun.playground.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.github.mokkun.playground.utils.Logger;

import static com.github.mokkun.playground.database.PlaygroundContract.DATABASE_NAME;
import static com.github.mokkun.playground.database.PlaygroundContract.DATABASE_VERSION;
import static com.github.mokkun.playground.database.PlaygroundContract.SessionEntry;

public final class DatabaseHelper extends SQLiteOpenHelper {
    private static volatile DatabaseHelper sSharedInstance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getSharedInstance(@NonNull Context context) {
        if (sSharedInstance == null) {
            sSharedInstance = new DatabaseHelper(context);
        }
        return sSharedInstance;
    }

    @Override public void onCreate(SQLiteDatabase db) {
        Logger.d(this, "onCreate");
        db.execSQL(SessionEntry.SQL_CREATE_ENTRIES);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d(this, "onUpgrade");
        // We won't handle data migration for this sample app.
        db.execSQL(SessionEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
