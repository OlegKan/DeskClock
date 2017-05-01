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

package com.android.deskclock2;

import android.app.Fragment;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.deskclock2.alarms.AlarmStateManager;
import com.android.deskclock2.data.DataModel;
import com.android.deskclock2.provider.Alarm;
import com.android.deskclock2.settings.SettingsActivity;

/**
 * DeskClock clock view for desk docks.
 */
public class DeskClock extends BaseActivity
        implements LabelDialogFragment.AlarmLabelDialogHandler {

    public static final int REQUEST_CHANGE_SETTINGS = 1;

    private static final String TAG = "DeskClock";

    private ImageView mFab;
    private ImageButton mLeftButton;
    private ImageButton mRightButton;

    /** {@code true} when a settings change necessitates recreating this activity. */
    private boolean mRecreateActivity;

    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        LogUtils.d(TAG, "onNewIntent with intent: %s", newIntent);

        // update our intent so that we can consult it to determine whether or
        // not the most recent launch was via a dock event
        setIntent(newIntent);
    }

    @VisibleForTesting
    DeskClockFragment getSelectedFragment() {
        return (DeskClockFragment) getFragmentManager().findFragmentById(R.id.content);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_ALARM);



        setContentView(R.layout.desk_clock);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (ImageView) findViewById(R.id.fab);
        mLeftButton = (ImageButton) findViewById(R.id.left_button);
        mRightButton = (ImageButton) findViewById(R.id.right_button);

        if (icicle == null) {
            // Set the background color to initially match the theme value so that we can
            // smoothly transition to the dynamic color.
            setBackgroundColor(getResources().getColor(R.color.default_background),
                    false /* animate */);

            setFragment();
        }

        mFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getSelectedFragment().onFabClick(view);
            }
        });
        mLeftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getSelectedFragment().onLeftButtonClick(view);
            }
        });
        mRightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getSelectedFragment().onRightButtonClick(view);
            }
        });

        // Inflate the menu during creation to avoid a double layout pass. Otherwise, the menu
        // inflation occurs *after* the initial draw and a second layout pass adds in the menu.
        onCreateOptionsMenu(toolbar.getMenu());

        // We need to update the system next alarm time on app startup because the
        // user might have clear our data.
        AlarmStateManager.updateNextAlarm(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataModel.getDataModel().setApplicationInForeground(true);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (mRecreateActivity) {
            mRecreateActivity = false;

            setFragment();
        }
    }

    @Override
    public void onPause() {
        DataModel.getDataModel().setApplicationInForeground(false);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.desk_clock_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                this.startActivityForResult(settingIntent, REQUEST_CHANGE_SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Recreate the activity if any settings have been changed
        if (requestCode == REQUEST_CHANGE_SETTINGS
                && resultCode == RESULT_OK) {
            mRecreateActivity = true;
        }
    }

    /**
     * Called by the LabelDialogFormat class after the dialog is finished.
     */
    @Override
    public void onDialogLabelSet(Alarm alarm, String label, String tag) {
        Fragment frag = getFragmentManager().findFragmentByTag(tag);
        if (frag instanceof AlarmClockFragment) {
            ((AlarmClockFragment) frag).setLabel(alarm, label);
        }
    }

    public int getSelectedTab() {
        return 1;
    }

    public ImageView getFab() {
        return mFab;
    }

    public ImageButton getLeftButton() {
        return mLeftButton;
    }

    public ImageButton getRightButton() {
        return mRightButton;
    }

    private void setFragment() {

        AlarmClockFragment fragment = new AlarmClockFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();

        fragment.setFabAppearance();
    }
}
