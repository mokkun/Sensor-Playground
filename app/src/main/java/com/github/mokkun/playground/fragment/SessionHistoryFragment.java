package com.github.mokkun.playground.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.github.mokkun.playground.R;
import com.github.mokkun.playground.database.PlaygroundContentProvider;
import com.github.mokkun.playground.database.loader.SimpleCursorLoader;
import com.github.mokkun.playground.utils.DateFormatUtils;

import java.util.Date;
import java.util.Locale;

import static com.github.mokkun.playground.database.PlaygroundContract.SessionEntry;

public class SessionHistoryFragment extends Fragment {
    private static final int SESSIONS_LOADER_ID = 0;
    private SessionHistoryAdapter mAdapter;

    public static Fragment createFragment() {
        return new SessionHistoryFragment();
    }

    public static CharSequence getTitle(@NonNull Context context) {
        return context.getString(R.string.title_session_history);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_history, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new SessionHistoryAdapter(view.getContext());
        final ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(mAdapter);

        final LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(SESSIONS_LOADER_ID, null, createSessionCallbacks());
    }

    private LoaderManager.LoaderCallbacks<Cursor> createSessionCallbacks() {
        return new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                final Context context = getContext();
                if (context != null) {
                    return new SessionHistoryCursorLoader(context);
                }
                return null;
            }

            @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapCursor(data);
            }

            @Override public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapCursor(null);
            }
        };
    }

    public void refresh() {
        final LoaderManager loaderManager = getLoaderManager();
        final Loader<Object> sessionsLoader = loaderManager.getLoader(SESSIONS_LOADER_ID);
        sessionsLoader.cancelLoad();
        if (mAdapter != null) {
            loaderManager.restartLoader(SESSIONS_LOADER_ID, null, createSessionCallbacks());
        }
    }

    private static class SessionHistoryAdapter extends SimpleCursorAdapter {
        public SessionHistoryAdapter(@NonNull Context context) {
            super(context, R.layout.listitem_session_history, null,
                    new String[] {
                            SessionEntry.COLUMN_NAME_START_TIME,
                            SessionEntry.COLUMN_NAME_DURATION,
                            SessionEntry.COLUMN_NAME_STEPS,
                            SessionEntry.COLUMN_NAME_DISTANCE
                    },
                    new int[] {
                            R.id.session_date,
                            R.id.session_time,
                            R.id.session_steps,
                            R.id.session_distance
                    }, 0);

            final String totalTimeFormat = context.getString(R.string.total_time);
            final String stepsFormat = context.getString(R.string.steps_count);
            final String distanceFormat = context.getString(R.string.distance);
            setViewBinder(new ViewBinder() {
                @Override public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    final String columnName = cursor.getColumnName(columnIndex);
                    switch (columnName) {
                        case SessionEntry.COLUMN_NAME_START_TIME:
                            ((TextView) view).setText(DateFormatUtils
                                    .formatDateTimeNoYear(new Date(cursor.getLong(columnIndex))));
                            return true;
                        case SessionEntry.COLUMN_NAME_DURATION:
                            ((TextView) view).setText(String.format(Locale.ENGLISH, totalTimeFormat,
                                    DateFormatUtils.formatTimeNoZone(
                                            new Date(cursor.getLong(columnIndex)))));
                            return true;
                        case SessionEntry.COLUMN_NAME_STEPS:
                            ((TextView) view).setText(String.format(Locale.ENGLISH, stepsFormat,
                                    cursor.getLong(columnIndex)));
                            return true;
                        case SessionEntry.COLUMN_NAME_DISTANCE:
                            ((TextView) view).setText(String.format(Locale.ENGLISH, distanceFormat,
                                    cursor.getFloat(columnIndex)));
                            return true;
                        default:
                            return false;
                    }
                }
            });
        }
    }

    private static class SessionHistoryCursorLoader extends SimpleCursorLoader {
        public SessionHistoryCursorLoader(@NonNull Context context) {
            super(context.getApplicationContext());
        }

        @Override public Cursor loadInBackground() {
            return getContext().getContentResolver()
                    .query(PlaygroundContentProvider.Service.SESSIONS.getUri(), null, null, null,
                            SessionEntry.SQL_ORDER_BY);
        }
    }
}
