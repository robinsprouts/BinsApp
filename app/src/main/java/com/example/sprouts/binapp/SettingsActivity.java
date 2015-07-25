package com.example.sprouts.binapp;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by sprouts on 19/07/15.
 */

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}