package com.morlunk.mumbleclient.app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.morlunk.mumbleclient.R;

public class Preferences extends SherlockPreferenceActivity {
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(android.os.Build.VERSION.SDK_INT >= 11) {
			getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferencesFragment()).commit();
		} else {
			addPreferencesFromResource(R.xml.preferences);
		}
	}
	
	public static class PreferencesFragment extends PreferenceFragment {
		
		/* (non-Javadoc)
		 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
		 */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			addPreferencesFromResource(R.xml.preferences);
		}
	}
}
