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
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import static com.android.deskclock2.Utils.enforceMainLooper;

/**
 * All application-wide data is accessible through this singleton.
 */
public final class DataModel {

    /** Indicates the display style of clocks. */
    public enum ClockStyle {ANALOG, DIGITAL}

    /** Indicates the preferred sort order of cities. */
    public enum CitySort {NAME, UTC_OFFSET}

    /** The single instance of this data model that exists for the life of the application. */
    private static final DataModel sDataModel = new DataModel();

    private Handler mHandler;

    private Context mContext;

    /** The model from which settings are fetched. */
    private SettingsModel mSettingsModel;

    /** The model from which alarm data are fetched. */
    private AlarmModel mAlarmModel;

    /** The model from which notification data are fetched. */
    private NotificationModel mNotificationModel;

    public static DataModel getDataModel() {
        return sDataModel;
    }

    private DataModel() {}

    /**
     * The context may be set precisely once during the application life.
     */
    public void setContext(Context context) {
        if (mContext != null) {
            throw new IllegalStateException("context has already been set");
        }
        mContext = context.getApplicationContext();

        mSettingsModel = new SettingsModel(mContext);
        mNotificationModel = new NotificationModel();
        mAlarmModel = new AlarmModel(mContext, mSettingsModel);
    }

    /**
     * @return a handler associated with the main thread
     */
    private synchronized Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    //
    // Application
    //

    /**
     * @param inForeground {@code true} to indicate the application is open in the foreground
     */
    public void setApplicationInForeground(boolean inForeground) {
        enforceMainLooper();

        if (mNotificationModel.isApplicationInForeground() != inForeground) {
            mNotificationModel.setApplicationInForeground(inForeground);
        }
    }

    /**
     * @return {@code true} when the application is open in the foreground; {@code false} otherwise
     */
    public boolean isApplicationInForeground() {
        return mNotificationModel.isApplicationInForeground();
    }

    //
    // Alarms
    //

    /**
     * @return the uri of the ringtone to which all new alarms default
     */
    public Uri getDefaultAlarmRingtoneUri() {
        enforceMainLooper();
        return mAlarmModel.getDefaultAlarmRingtoneUri();
    }

    /**
     * @param uri the uri of the ringtone to which future new alarms will default
     */
    public void setDefaultAlarmRingtoneUri(Uri uri) {
        enforceMainLooper();
        mAlarmModel.setDefaultAlarmRingtoneUri(uri);
    }

    /**
     * @param uri the uri of a ringtone
     * @return the title of the ringtone with the {@code uri}; {@code null} if it cannot be fetched
     */
    public String getAlarmRingtoneTitle(Uri uri) {
        enforceMainLooper();
        return mAlarmModel.getAlarmRingtoneTitle(uri);
    }
}