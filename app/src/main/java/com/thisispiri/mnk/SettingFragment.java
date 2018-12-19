package com.thisispiri.mnk;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

/**Shows the preferences for the user to edit.*/
public class SettingFragment extends PreferenceFragmentCompat {
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		findPreference("graphicsPreset").setOnPreferenceChangeListener((Preference pref, Object newVal) -> {
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
			//TODO: Simplify
			switch((Integer) newVal) {
			case 1:
			edit.putString("symbols", "0");
			edit.putString("lineType", "0");
			edit.putInt("backgroundColor", 0xFFFFFFFF);
			edit.putInt("xColor", 0xFF0000FF);
			edit.putInt("oColor", 0xFFFF0000);
			break;
			case 2:
			edit.putString("symbols", "1");
			edit.putString("lineType", "1");
			edit.putInt("backgroundColor", 0xFFB69B4C);
			edit.putInt("xColor", 0xFF000000);
			edit.putInt("oColor", 0xFFFFFFFF);
			break;
			}
			edit.apply();
		});
	}
}