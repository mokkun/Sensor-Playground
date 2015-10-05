package com.github.mokkun.playground.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.mokkun.playground.database.PlaygroundContract.*;

public class PlaygroundContentProvider extends ContentProvider {
    private static final String SCHEME = "content://";
    private static final String AUTHORITY = "com.github.mokkun.playground.contentprovider";
    private static final String BASE_URL = SCHEME + AUTHORITY;
    private static final Map<Integer, String> TABLES = new HashMap<>();
    static {
        TABLES.put(Content.SESSIONS, SessionEntry.TABLE_NAME);
    }

    private static final AtomicBoolean sInitialised = new AtomicBoolean(false);
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static void initialise() {
        if (!sInitialised.getAndSet(true)) {
            sUriMatcher.addURI(AUTHORITY, "sessions", Content.SESSIONS);
        }
    }

    public static int matchUri(Uri uri) {
        return sUriMatcher.match(uri);
    }

    @Override public boolean onCreate() {
        return true;
    }

    @Nullable @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        final DatabaseHelper databaseHelper = getDatabaseHelper();
        if (databaseHelper == null) {
            return null;
        }
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        final Cursor cursor = database.query(TABLES.get(sUriMatcher.match(uri)), projection,
                selection, selectionArgs, null, null, sortOrder);
        final ContentResolver contentResolver = getContentResolver();
        if (contentResolver != null) {
            cursor.setNotificationUri(contentResolver, uri);
        }
        return cursor;
    }

    @Nullable @Override public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable @Override public Uri insert(@NonNull Uri uri, ContentValues values) {
        final DatabaseHelper databaseHelper = getDatabaseHelper();
        if (databaseHelper == null) {
            return null;
        }
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.insert(TABLES.get(sUriMatcher.match(uri)), null, values);
        notifyChange(uri);
        return uri;
    }

    @Override public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

    @Nullable private ContentResolver getContentResolver() {
        final Context context = getContext();
        if (context == null) {
            return null;
        }
        return context.getContentResolver();
    }

    @Nullable private DatabaseHelper getDatabaseHelper() {
        final Context context = getContext();
        if (context == null) {
            return null;
        }
        return DatabaseHelper.getSharedInstance(context);
    }

    private void notifyChange(Uri uri) {
        final ContentResolver contentResolver = getContentResolver();
        if (contentResolver != null) {
            contentResolver.notifyChange(uri, null, false);
        }
    }

    public enum Service {
        SESSIONS("/sessions");

        private final Uri mUri;

        Service(String serviceName) {
            mUri = Uri.parse(BASE_URL + serviceName);
        }

        public Uri getUri() {
            return mUri;
        }
    }

    public static class Content {
        public static final int SESSIONS = 1;
    }
}
