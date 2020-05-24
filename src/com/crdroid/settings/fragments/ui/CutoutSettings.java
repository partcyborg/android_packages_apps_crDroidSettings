/*
 * Copyright (C) 2019-2020 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crdroid.settings.fragments.ui;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;

import com.crdroid.settings.R;

public class CutoutSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener  {

    private static final String TAG = "CutoutSettings";

    private static final String DISPLAY_CUTOUT_MODE = "display_cutout_mode";
    private static final String SYSUI_DISPLAY_CUTOUT = "sysui_display_cutout";
    private static final String STOCK_STATUSBAR_IN_HIDE = "stock_statusbar_in_hide";

    private ListPreference mDisplayCutoutMode;
    private SwitchPreference mDisplayCutout;
    private SwitchPreference mStockStatusbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.cutout);

        Context mContext = getActivity().getApplicationContext();
        ContentResolver resolver = mContext.getContentResolver();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        boolean hasDisplayCutout = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_fillMainBuiltInDisplayCutout);
        boolean mSysUIDispCutout = Settings.System.getIntForUser(resolver,
                Settings.System.SYSUI_DISPLAY_CUTOUT, hasDisplayCutout ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        int mCutoutMode = Settings.System.getIntForUser(resolver,
                Settings.System.DISPLAY_CUTOUT_MODE, 0, UserHandle.USER_CURRENT);

        mDisplayCutout = (SwitchPreference) prefScreen.findPreference(SYSUI_DISPLAY_CUTOUT);
        mDisplayCutout.setChecked(mSysUIDispCutout);
        mDisplayCutout.setEnabled(mCutoutMode == 0);

        mStockStatusbar = (SwitchPreference) prefScreen.findPreference(STOCK_STATUSBAR_IN_HIDE);
        mStockStatusbar.setEnabled(mCutoutMode == 2);

        mDisplayCutoutMode = (ListPreference) prefScreen.findPreference(DISPLAY_CUTOUT_MODE);
        mDisplayCutoutMode.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDisplayCutoutMode) {
            int mode = Integer.valueOf((String) newValue);
            mDisplayCutout.setEnabled(mode == 0);
            mStockStatusbar.setEnabled(mode == 2);
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        boolean hasDisplayCutout;
        Settings.System.putIntForUser(resolver,
                Settings.System.DISPLAY_CUTOUT_MODE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STOCK_STATUSBAR_IN_HIDE, 1, UserHandle.USER_CURRENT);
        // Assign this after resetting DISPLAY_CUTOUT_MODE
        hasDisplayCutout = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_fillMainBuiltInDisplayCutout);
        Settings.System.putIntForUser(resolver,
                Settings.System.SYSUI_DISPLAY_CUTOUT, hasDisplayCutout ? 1 : 0, UserHandle.USER_CURRENT);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CRDROID_SETTINGS;
    }
}
