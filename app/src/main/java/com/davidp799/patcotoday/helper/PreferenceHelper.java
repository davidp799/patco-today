/*
 * Copyright (C) 2011-2012 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This file is part of AdAway.
 *
 * AdAway is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AdAway is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AdAway.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.davidp799.patcotoday.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.SyncStateContract;

import androidx.appcompat.app.AppCompatDelegate;

import com.davidp799.patcotoday.R;

import java.util.Collections;
import java.util.Set;

public class PreferenceHelper {
    public static int getDarkThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "deviceTheme", Context.MODE_PRIVATE );
        String pref = prefs.getString(
                "deviceTheme", "" );
        switch (pref) {
            case "MODE_NIGHT_NO":
                return AppCompatDelegate.MODE_NIGHT_NO;
            case "MODE_NIGHT_YES":
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
