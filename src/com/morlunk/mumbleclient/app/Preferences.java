package com.morlunk.mumbleclient.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.morlunk.mumbleclient.R;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
