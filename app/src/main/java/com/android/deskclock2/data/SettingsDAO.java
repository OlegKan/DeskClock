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

package com.android.deskclock2.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.android.deskclock2.R;
import com.android.deskclock2.Utils;
import com.android.deskclock2.data.DataModel.CitySort;
import com.android.deskclock2.data.DataModel.ClockStyle;

import java.util.Locale;

/**
 * This class encapsulates the storage of application preferences in {@link SharedPreferences}.
 */
final class SettingsDAO {

    private static final String KEY_SORT_PREFERENCE = "sort_preference";
    private static final String KEY_DEFAULT_ALARM_RINGTONE_URI = "default_alarm_ringtone_uri";

    // Lazily instantiated and cached for the life of the application.
    private static SharedPreferences sPrefs;

    private SettingsDAO() {}

    /**
     * @return an enumerated value indicating the order in which cities are ordered
     */
    static CitySort getCitySort(Context context) {
        final int defaultSortOrdinal = CitySort.NAME.ordinal();
        final SharedPreferences prefs = getSharedPreferences(context);
        final int citySortOrdinal = prefs.getInt(KEY_SORT_PREFERENCE, defaultSortOrdinal);
        return CitySort.values()[citySortOrdinal];
    }

    /**
     * @return the uri of the selected ringtone or the {@code defaultUri} if no explicit selection
     *      has yet been made
     */
    static Uri getDefaultAlarmRingtoneUri(Context context, Uri defaultUri) {
        final SharedPreferences prefs = getSharedPreferences(context);
        final String uriString = prefs.getString(KEY_DEFAULT_ALARM_RINGTONE_URI, null);
        return uriString == null ? defaultUri : Uri.parse(uriString);
    }

    /**
     * @param uri identifies the default ringtone to play for new alarms
     */
    static void setDefaultAlarmRingtoneUri(Context context, Uri uri) {
        final SharedPreferences prefs = getSharedPreferences(context);
        prefs.edit().putString(KEY_DEFAULT_ALARM_RINGTONE_URI, uri.toString()).apply();
    }

    private static ClockStyle getClockStyle(Context context, String prefKey) {
        final String defaultStyle = context.getString(R.string.default_clock_style);
        final SharedPreferences prefs = getSharedPreferences(context);
        final String clockStyle = prefs.getString(prefKey, defaultStyle);
        // Use hardcoded locale to perform toUpperCase, because in some languages toUpperCase adds
        // accent to character, which breaks the enum conversion.
        return ClockStyle.valueOf(clockStyle.toUpperCase(Locale.US));
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if (sPrefs == null) {
            sPrefs = Utils.getDefaultSharedPreferences(context.getApplicationContext());
        }

        return sPrefs;
    }
}
