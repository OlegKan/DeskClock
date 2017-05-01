/*
 *  Copyright (C) 2015 The Android Open Source Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.android.deskclock2.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.deskclock2.LogUtils;

/**
 * Helper class for opening the database from multiple providers.  Also provides
 * some common functionality.
 */
class ClockDatabaseHelper extends SQLiteOpenHelper {

    // Database and table names
    static final String DATABASE_NAME = "alarms.db";
    static final int DATABASE_VERSION = 1;
    static final String ALARMS_TABLE_NAME = "alarm_templates";
    static final String INSTANCES_TABLE_NAME = "alarm_instances";

    private static void createAlarmsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ALARMS_TABLE_NAME + " (" +
                ClockContract.AlarmsColumns._ID + " INTEGER PRIMARY KEY," +
                ClockContract.AlarmsColumns.HOUR + " INTEGER NOT NULL, " +
                ClockContract.AlarmsColumns.MINUTES + " INTEGER NOT NULL, " +
                ClockContract.AlarmsColumns.DAYS_OF_WEEK + " INTEGER NOT NULL, " +
                ClockContract.AlarmsColumns.ENABLED + " INTEGER NOT NULL, " +
                ClockContract.AlarmsColumns.VIBRATE + " INTEGER NOT NULL, " +
                ClockContract.AlarmsColumns.LABEL + " TEXT NOT NULL, " +
                ClockContract.AlarmsColumns.RINGTONE + " TEXT, " +
                ClockContract.AlarmsColumns.DELETE_AFTER_USE + " INTEGER NOT NULL DEFAULT 0);");
        LogUtils.i("Alarms Table created");
    }

    private static void createInstanceTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + INSTANCES_TABLE_NAME + " (" +
                ClockContract.InstancesColumns._ID + " INTEGER PRIMARY KEY," +
                ClockContract.InstancesColumns.YEAR + " INTEGER NOT NULL, " +
                ClockContract.InstancesColumns.MONTH + " INTEGER NOT NULL, " +
                ClockContract.InstancesColumns.DAY + " INTEGER NOT NULL, " +
                ClockContract.InstancesColumns.HOUR + " INTEGER NOT NULL, " +
                ClockContract.InstancesColumns.MINUTES + " INTEGER NOT NULL, " +
                ClockContract.InstancesColumns.VIBRATE + " INTEGER NOT NULL, " +
                ClockContract.InstancesColumns.LABEL + " TEXT NOT NULL, " +
                ClockContract.InstancesColumns.RINGTONE + " TEXT, " +
                ClockContract.InstancesColumns.ALARM_STATE + " INTEGER NOT NULL, " +
                ClockContract.InstancesColumns.ALARM_ID + " INTEGER REFERENCES " +
                    ALARMS_TABLE_NAME + "(" + ClockContract.AlarmsColumns._ID + ") " +
                    "ON UPDATE CASCADE ON DELETE CASCADE" +
                ");");
        LogUtils.i("Instance table created");
    }

    public ClockDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAlarmsTable(db);
        createInstanceTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        LogUtils.v("Upgrading alarms database from version %d to %d", oldVersion, currentVersion);
    }

    long fixAlarmInsert(ContentValues values) {
        // Why are we doing this? Is this not a programming bug if we try to
        // insert an already used id?
        final SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long rowId = -1;
        try {
            // Check if we are trying to re-use an existing id.
            final Object value = values.get(ClockContract.AlarmsColumns._ID);
            if (value != null) {
                long id = (Long) value;
                if (id > -1) {
                    final String[] columns = {ClockContract.AlarmsColumns._ID};
                    final String selection = ClockContract.AlarmsColumns._ID + " = ?";
                    final String[] selectionArgs = {String.valueOf(id)};
                    try (Cursor cursor = db.query(ALARMS_TABLE_NAME, columns, selection,
                            selectionArgs, null, null, null)) {
                        if (cursor.moveToFirst()) {
                            // Record exists. Remove the id so sqlite can generate a new one.
                            values.putNull(ClockContract.AlarmsColumns._ID);
                        }
                    }
                }
            }

            rowId = db.insert(ALARMS_TABLE_NAME, ClockContract.AlarmsColumns.RINGTONE, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (rowId < 0) {
            throw new SQLException("Failed to insert row");
        }
        LogUtils.v("Added alarm rowId = " + rowId);

        return rowId;
    }
}
