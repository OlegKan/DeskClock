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
import android.provider.Settings;

/**
 * All settings data is accessed via this model.
 */
final class SettingsModel {

    private final Context mContext;

    SettingsModel(Context context) {
        mContext = context;
    }

    Uri getDefaultAlarmRingtoneUri() {
        return SettingsDAO.getDefaultAlarmRingtoneUri(mContext,
                Settings.System.DEFAULT_ALARM_ALERT_URI);
    }

    void setDefaultAlarmRingtoneUri(Uri uri) {
        SettingsDAO.setDefaultAlarmRingtoneUri(mContext, uri);
    }
}